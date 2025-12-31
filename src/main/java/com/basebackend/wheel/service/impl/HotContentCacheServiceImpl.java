package com.basebackend.wheel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.basebackend.wheel.entity.WheelCategory;
import com.basebackend.wheel.entity.WheelContent;
import com.basebackend.wheel.mapper.WheelCategoryMapper;
import com.basebackend.wheel.mapper.WheelContentMapper;
import com.basebackend.wheel.service.HotContentCacheService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 热门内容缓存服务实现类
 * 负责管理热门内容的Redis缓存，支持推荐系统的快速访问
 *
 * @author wheel-api
 * @since 2025-01-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HotContentCacheServiceImpl implements HotContentCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final WheelContentMapper wheelContentMapper;
    private final WheelCategoryMapper wheelCategoryMapper;
    private final ObjectMapper objectMapper;

    // 缓存键前缀
    private static final String HOT_CONTENT_CACHE_PREFIX = "recommendation:hot_content:category:";
    private static final String CACHE_STATS_KEY = "recommendation:hot_content:stats";
    
    // 缓存配置
    private static final int MAX_CACHE_SIZE_PER_CATEGORY = 100;
    private static final long CACHE_TTL_SECONDS = 3600; // 1小时
    
    // 缓存统计
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);


    @Override
    public List<WheelContent> getHotContentByCategory(Long categoryId, int limit) {
        if (categoryId == null) {
            log.warn("获取热门内容失败：分类ID为空");
            return Collections.emptyList();
        }

        // 限制返回数量
        int actualLimit = Math.min(limit, MAX_CACHE_SIZE_PER_CATEGORY);

        try {
            // 1. 尝试从Redis缓存获取
            List<WheelContent> cachedContent = getFromCache(categoryId);
            if (cachedContent != null && !cachedContent.isEmpty()) {
                cacheHits.incrementAndGet();
                log.debug("缓存命中: categoryId={}, size={}", categoryId, cachedContent.size());
                return cachedContent.stream()
                        .limit(actualLimit)
                        .collect(Collectors.toList());
            }

            cacheMisses.incrementAndGet();
            log.debug("缓存未命中: categoryId={}", categoryId);

            // 2. 从数据库加载
            List<WheelContent> contents = loadHotContentFromDb(categoryId, MAX_CACHE_SIZE_PER_CATEGORY);
            
            // 3. 存入缓存
            if (!contents.isEmpty()) {
                saveToCache(categoryId, contents);
            }

            return contents.stream()
                    .limit(actualLimit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取热门内容失败: categoryId={}, error={}", categoryId, e.getMessage(), e);
            // Redis不可用时降级到数据库查询
            return loadHotContentFromDb(categoryId, actualLimit);
        }
    }

    @Override
    public void refreshCache() {
        log.info("开始刷新热门内容缓存");
        
        try {
            // 1. 获取所有启用的分类
            List<WheelCategory> categories = wheelCategoryMapper.selectEnabledCategoriesOrderBySort();
            if (categories == null || categories.isEmpty()) {
                log.info("没有启用的分类，跳过缓存刷新");
                return;
            }

            int successCount = 0;
            int failCount = 0;

            // 2. 为每个分类刷新缓存
            for (WheelCategory category : categories) {
                try {
                    List<WheelContent> hotContents = loadHotContentFromDb(category.getId(), MAX_CACHE_SIZE_PER_CATEGORY);
                    
                    if (!hotContents.isEmpty()) {
                        saveToCache(category.getId(), hotContents);
                        successCount++;
                        log.debug("刷新分类缓存成功: categoryId={}, size={}", category.getId(), hotContents.size());
                    } else {
                        // 清除空分类的缓存
                        clearCategoryCache(category.getId());
                        successCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("刷新分类缓存失败: categoryId={}, error={}", category.getId(), e.getMessage());
                }
            }

            log.info("热门内容缓存刷新完成: 总分类={}, 成功={}, 失败={}", 
                    categories.size(), successCount, failCount);

            // 3. 检查缓存命中率
            checkCacheHitRate();

        } catch (Exception e) {
            log.error("刷新热门内容缓存失败: error={}", e.getMessage(), e);
        }
    }

    @Override
    public void invalidateContent(Long contentId) {
        if (contentId == null) {
            return;
        }

        log.info("失效内容缓存: contentId={}", contentId);

        try {
            // 1. 获取内容的分类ID
            WheelContent content = wheelContentMapper.selectById(contentId);
            if (content == null) {
                log.warn("内容不存在: contentId={}", contentId);
                return;
            }

            Long categoryId = content.getCategoryId();
            
            // 2. 从缓存中移除该内容
            List<WheelContent> cachedContents = getFromCache(categoryId);
            if (cachedContents != null && !cachedContents.isEmpty()) {
                List<WheelContent> updatedContents = cachedContents.stream()
                        .filter(c -> !c.getId().equals(contentId))
                        .collect(Collectors.toList());
                
                if (updatedContents.size() < cachedContents.size()) {
                    saveToCache(categoryId, updatedContents);
                    log.info("从缓存中移除内容: contentId={}, categoryId={}", contentId, categoryId);
                }
            }

        } catch (Exception e) {
            log.error("失效内容缓存失败: contentId={}, error={}", contentId, e.getMessage(), e);
        }
    }

    @Override
    public double getCacheHitRate() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;
        
        if (total == 0) {
            return 1.0; // 没有请求时返回100%
        }
        
        return (double) hits / total;
    }

    @Override
    public List<WheelContent> getHotContentByCategoryWithStats(Long categoryId) {
        // 与getHotContentByCategory相同，但会更新统计
        return getHotContentByCategory(categoryId, MAX_CACHE_SIZE_PER_CATEGORY);
    }

    @Override
    public void resetCacheStats() {
        cacheHits.set(0);
        cacheMisses.set(0);
        log.info("缓存统计已重置");
    }

    @Override
    public List<WheelContent> getAllHotContent(int limit) {
        List<WheelContent> allHotContent = new ArrayList<>();
        
        try {
            // 获取所有启用的分类
            List<WheelCategory> categories = wheelCategoryMapper.selectEnabledCategoriesOrderBySort();
            if (categories == null || categories.isEmpty()) {
                return Collections.emptyList();
            }

            // 从每个分类获取热门内容
            for (WheelCategory category : categories) {
                List<WheelContent> categoryContent = getHotContentByCategory(category.getId(), limit);
                allHotContent.addAll(categoryContent);
            }

            // 按热度分数排序
            allHotContent.sort((c1, c2) -> {
                Double score1 = c1.getPopularityScore() != null ? c1.getPopularityScore() : 0.0;
                Double score2 = c2.getPopularityScore() != null ? c2.getPopularityScore() : 0.0;
                return score2.compareTo(score1);
            });

            return allHotContent;

        } catch (Exception e) {
            log.error("获取所有热门内容失败: error={}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public void warmUpCache() {
        log.info("开始预热热门内容缓存");
        refreshCache();
        log.info("热门内容缓存预热完成");
    }


    // ==================== 私有辅助方法 ====================

    /**
     * 从Redis缓存获取热门内容
     */
    private List<WheelContent> getFromCache(Long categoryId) {
        try {
            String cacheKey = HOT_CONTENT_CACHE_PREFIX + categoryId;
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            
            if (cached != null) {
                return objectMapper.convertValue(cached, new TypeReference<List<WheelContent>>() {});
            }
        } catch (Exception e) {
            log.warn("从缓存获取热门内容失败: categoryId={}, error={}", categoryId, e.getMessage());
        }
        return null;
    }

    /**
     * 保存热门内容到Redis缓存
     */
    private void saveToCache(Long categoryId, List<WheelContent> contents) {
        try {
            String cacheKey = HOT_CONTENT_CACHE_PREFIX + categoryId;
            redisTemplate.opsForValue().set(cacheKey, contents, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
            log.debug("保存热门内容到缓存: categoryId={}, size={}", categoryId, contents.size());
        } catch (Exception e) {
            log.warn("保存热门内容到缓存失败: categoryId={}, error={}", categoryId, e.getMessage());
        }
    }

    /**
     * 清除分类缓存
     */
    private void clearCategoryCache(Long categoryId) {
        try {
            String cacheKey = HOT_CONTENT_CACHE_PREFIX + categoryId;
            redisTemplate.delete(cacheKey);
            log.debug("清除分类缓存: categoryId={}", categoryId);
        } catch (Exception e) {
            log.warn("清除分类缓存失败: categoryId={}, error={}", categoryId, e.getMessage());
        }
    }

    /**
     * 从数据库加载热门内容
     * 按热度分数降序排列，只返回已审核通过且启用的内容
     */
    private List<WheelContent> loadHotContentFromDb(Long categoryId, int limit) {
        try {
            LambdaQueryWrapper<WheelContent> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(WheelContent::getCategoryId, categoryId)
                    .eq(WheelContent::getAuditStatus, 1) // 已审核通过
                    .eq(WheelContent::getStatus, 1) // 已启用
                    .eq(WheelContent::getDeleted, false) // 未删除
                    .orderByDesc(WheelContent::getPopularityScore) // 按热度分数降序
                    .orderByDesc(WheelContent::getCreateTime) // 次要排序：创建时间降序
                    .last("LIMIT " + limit);

            List<WheelContent> contents = wheelContentMapper.selectList(queryWrapper);
            log.debug("从数据库加载热门内容: categoryId={}, size={}", categoryId, contents.size());
            return contents;

        } catch (Exception e) {
            log.error("从数据库加载热门内容失败: categoryId={}, error={}", categoryId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 检查缓存命中率并记录警告
     */
    private void checkCacheHitRate() {
        double hitRate = getCacheHitRate();
        
        if (hitRate < 0.8) {
            log.warn("缓存命中率低于80%: hitRate={}, hits={}, misses={}", 
                    String.format("%.2f%%", hitRate * 100), 
                    cacheHits.get(), 
                    cacheMisses.get());
        } else {
            log.info("缓存命中率: hitRate={}, hits={}, misses={}", 
                    String.format("%.2f%%", hitRate * 100), 
                    cacheHits.get(), 
                    cacheMisses.get());
        }
    }
}
