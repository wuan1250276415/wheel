package com.basebackend.wheel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basebackend.wheel.dto.SensitiveWordResult;
import com.basebackend.wheel.entity.SensitiveWord;
import com.basebackend.wheel.mapper.SensitiveWordMapper;
import com.basebackend.wheel.service.SensitiveWordFilter;
import com.basebackend.wheel.service.SensitiveWordService;
import com.basebackend.wheel.util.SensitiveWordVariantGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 敏感词服务实现
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveWordServiceImpl implements SensitiveWordService {

    private final SensitiveWordMapper sensitiveWordMapper;
    
    @Autowired
    @Lazy
    private SensitiveWordFilter sensitiveWordFilter;

    /**
     * 是否正在重新加载
     */
    private final AtomicBoolean reloading = new AtomicBoolean(false);

    /**
     * 上次加载时间
     */
    private volatile long lastLoadTime = 0;

    /**
     * 最小重新加载间隔（毫秒）
     */
    private static final long MIN_RELOAD_INTERVAL = 5000;

    /**
     * 应用启动时初始化敏感词库
     */
    @PostConstruct
    public void init() {
        initializeSensitiveWords();
    }

    @Override
    public void initializeSensitiveWords() {
        log.info("开始初始化敏感词库...");
        try {
            List<SensitiveWord> words = sensitiveWordMapper.selectAllEnabled();
            
            // 为每个敏感词生成变体
            for (SensitiveWord word : words) {
                if (word.getVariants() == null || word.getVariants().isEmpty()) {
                    List<String> variants = SensitiveWordVariantGenerator.generateVariants(word.getWord());
                    word.setVariants(variants);
                }
            }
            
            sensitiveWordFilter.initialize(words);
            lastLoadTime = System.currentTimeMillis();
            log.info("敏感词库初始化完成，加载敏感词数量: {}", words.size());
        } catch (Exception e) {
            log.error("敏感词库初始化失败", e);
        }
    }

    @Override
    public void reloadSensitiveWords() {
        // 防止频繁重新加载
        long now = System.currentTimeMillis();
        if (now - lastLoadTime < MIN_RELOAD_INTERVAL) {
            log.warn("敏感词库重新加载过于频繁，跳过本次加载");
            return;
        }

        // 防止并发重新加载
        if (!reloading.compareAndSet(false, true)) {
            log.warn("敏感词库正在重新加载中，跳过本次请求");
            return;
        }

        try {
            log.info("开始热更新敏感词库...");
            List<SensitiveWord> words = sensitiveWordMapper.selectAllEnabled();
            
            // 为每个敏感词生成变体
            for (SensitiveWord word : words) {
                if (word.getVariants() == null || word.getVariants().isEmpty()) {
                    List<String> variants = SensitiveWordVariantGenerator.generateVariants(word.getWord());
                    word.setVariants(variants);
                }
            }
            
            sensitiveWordFilter.reload(words);
            lastLoadTime = System.currentTimeMillis();
            log.info("敏感词库热更新完成，当前敏感词数量: {}", words.size());
        } catch (Exception e) {
            log.error("敏感词库热更新失败", e);
        } finally {
            reloading.set(false);
        }
    }

    /**
     * 定时检查敏感词库更新（每5分钟）
     */
    @Scheduled(fixedRate = 300000)
    public void scheduledReload() {
        log.debug("定时检查敏感词库更新...");
        // 这里可以添加检查数据库更新时间的逻辑
        // 如果有更新则调用 reloadSensitiveWords()
    }

    @Override
    public SensitiveWordResult detectSensitiveWords(String text) {
        if (!sensitiveWordFilter.isInitialized()) {
            log.warn("敏感词过滤器未初始化，尝试初始化...");
            initializeSensitiveWords();
        }
        return sensitiveWordFilter.detect(text);
    }

    @Override
    public String replaceSensitiveWords(String text, char replacement) {
        if (!sensitiveWordFilter.isInitialized()) {
            log.warn("敏感词过滤器未初始化，尝试初始化...");
            initializeSensitiveWords();
        }
        return sensitiveWordFilter.replace(text, replacement);
    }

    @Override
    public boolean containsSensitiveWord(String text) {
        if (!sensitiveWordFilter.isInitialized()) {
            log.warn("敏感词过滤器未初始化，尝试初始化...");
            initializeSensitiveWords();
        }
        return sensitiveWordFilter.containsSensitiveWord(text);
    }

    @Override
    @Transactional
    public boolean addSensitiveWord(SensitiveWord sensitiveWord) {
        // 检查是否已存在
        if (sensitiveWordMapper.existsByWord(sensitiveWord.getWord())) {
            log.warn("敏感词已存在: {}", sensitiveWord.getWord());
            return false;
        }

        // 自动生成变体
        if (sensitiveWord.getVariants() == null || sensitiveWord.getVariants().isEmpty()) {
            List<String> variants = SensitiveWordVariantGenerator.generateVariants(sensitiveWord.getWord());
            sensitiveWord.setVariants(variants);
        }

        // 设置默认值
        if (sensitiveWord.getStatus() == null) {
            sensitiveWord.setStatus(SensitiveWord.STATUS_ENABLED);
        }
        if (sensitiveWord.getLevel() == null) {
            sensitiveWord.setLevel(SensitiveWord.LEVEL_MEDIUM);
        }
        if (sensitiveWord.getCategory() == null) {
            sensitiveWord.setCategory(SensitiveWord.CATEGORY_OTHER);
        }

        int result = sensitiveWordMapper.insert(sensitiveWord);
        
        if (result > 0) {
            // 触发热更新
            reloadSensitiveWords();
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public int batchAddSensitiveWords(List<SensitiveWord> words) {
        if (words == null || words.isEmpty()) {
            return 0;
        }

        // 过滤已存在的敏感词
        List<SensitiveWord> newWords = new ArrayList<>();
        for (SensitiveWord word : words) {
            if (!sensitiveWordMapper.existsByWord(word.getWord())) {
                // 自动生成变体
                if (word.getVariants() == null || word.getVariants().isEmpty()) {
                    List<String> variants = SensitiveWordVariantGenerator.generateVariants(word.getWord());
                    word.setVariants(variants);
                }
                // 设置默认值
                if (word.getStatus() == null) {
                    word.setStatus(SensitiveWord.STATUS_ENABLED);
                }
                if (word.getLevel() == null) {
                    word.setLevel(SensitiveWord.LEVEL_MEDIUM);
                }
                if (word.getCategory() == null) {
                    word.setCategory(SensitiveWord.CATEGORY_OTHER);
                }
                newWords.add(word);
            }
        }

        if (newWords.isEmpty()) {
            return 0;
        }

        int result = sensitiveWordMapper.batchInsert(newWords);
        
        if (result > 0) {
            // 触发热更新
            reloadSensitiveWords();
        }
        
        return result;
    }

    @Override
    @Transactional
    public boolean updateSensitiveWord(SensitiveWord sensitiveWord) {
        // 如果变体为空，自动生成
        if (sensitiveWord.getVariants() == null || sensitiveWord.getVariants().isEmpty()) {
            List<String> variants = SensitiveWordVariantGenerator.generateVariants(sensitiveWord.getWord());
            sensitiveWord.setVariants(variants);
        }

        int result = sensitiveWordMapper.updateById(sensitiveWord);
        
        if (result > 0) {
            // 触发热更新
            reloadSensitiveWords();
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteSensitiveWord(Long id) {
        int result = sensitiveWordMapper.deleteById(id);
        
        if (result > 0) {
            // 触发热更新
            reloadSensitiveWords();
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean updateStatus(Long id, Integer status) {
        SensitiveWord word = new SensitiveWord();
        word.setId(id);
        word.setStatus(status);
        
        int result = sensitiveWordMapper.updateById(word);
        
        if (result > 0) {
            // 触发热更新
            reloadSensitiveWords();
            return true;
        }
        return false;
    }

    @Override
    public Page<SensitiveWord> querySensitiveWords(int page, int size, Integer category, String keyword) {
        Page<SensitiveWord> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        
        if (category != null) {
            wrapper.eq(SensitiveWord::getCategory, category);
        }
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SensitiveWord::getWord, keyword);
        }
        
        wrapper.orderByDesc(SensitiveWord::getCreateTime);
        
        return sensitiveWordMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public SensitiveWord getSensitiveWordById(Long id) {
        return sensitiveWordMapper.selectById(id);
    }

    @Override
    public Map<String, Object> getSensitiveWordStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总数
        statistics.put("total", sensitiveWordMapper.selectCount(null));
        
        // 启用数量
        LambdaQueryWrapper<SensitiveWord> enabledWrapper = new LambdaQueryWrapper<>();
        enabledWrapper.eq(SensitiveWord::getStatus, SensitiveWord.STATUS_ENABLED);
        statistics.put("enabled", sensitiveWordMapper.selectCount(enabledWrapper));
        
        // 各分类数量
        List<Map<String, Object>> categoryStats = sensitiveWordMapper.countByCategory();
        statistics.put("categoryStats", categoryStats);
        
        // 当前DFA树中的敏感词数量
        statistics.put("loadedCount", sensitiveWordFilter.getWordCount());
        
        return statistics;
    }

    @Override
    public int getSensitiveWordCount() {
        return sensitiveWordFilter.getWordCount();
    }

    @Override
    public boolean existsSensitiveWord(String word) {
        return sensitiveWordMapper.existsByWord(word);
    }
}
