package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.dto.MatchedWord;
import com.basebackend.wheel.dto.SensitiveWordResult;
import com.basebackend.wheel.entity.SensitiveWord;
import com.basebackend.wheel.service.SensitiveWordFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 基于DFA（确定有限自动机）算法的敏感词过滤器实现
 * 
 * DFA算法特点：
 * 1. 构建时间复杂度：O(n*m)，n为敏感词数量，m为平均敏感词长度
 * 2. 检测时间复杂度：O(n)，n为待检测文本长度
 * 3. 空间复杂度：O(n*m)，使用HashMap存储状态转移
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Slf4j
@Component
public class DfaSensitiveWordFilter implements SensitiveWordFilter {

    /**
     * DFA状态转移表的结束标记
     */
    private static final String IS_END_KEY = "isEnd";

    /**
     * 敏感词元数据键
     */
    private static final String WORD_META_KEY = "wordMeta";

    /**
     * DFA状态转移表（根节点）
     */
    private volatile Map<Character, Object> dfaMap = new HashMap<>();

    /**
     * 敏感词元数据映射（word -> SensitiveWord）
     */
    private volatile Map<String, SensitiveWord> wordMetaMap = new HashMap<>();

    /**
     * 读写锁，保证热更新时的线程安全
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 是否已初始化
     */
    private volatile boolean initialized = false;

    /**
     * 敏感词数量
     */
    private volatile int wordCount = 0;

    @Override
    public void initialize(List<SensitiveWord> words) {
        lock.writeLock().lock();
        try {
            buildDfaTree(words);
            initialized = true;
            log.info("DFA敏感词过滤器初始化完成，加载敏感词数量: {}", wordCount);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void reload(List<SensitiveWord> words) {
        lock.writeLock().lock();
        try {
            // 创建新的DFA树
            Map<Character, Object> newDfaMap = new HashMap<>();
            Map<String, SensitiveWord> newWordMetaMap = new HashMap<>();
            
            buildDfaTreeInternal(words, newDfaMap, newWordMetaMap);
            
            // 原子替换
            this.dfaMap = newDfaMap;
            this.wordMetaMap = newWordMetaMap;
            this.wordCount = newWordMetaMap.size();
            
            log.info("DFA敏感词库热更新完成，当前敏感词数量: {}", wordCount);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public SensitiveWordResult detect(String text) {
        long startTime = System.currentTimeMillis();
        
        if (text == null || text.isEmpty()) {
            return SensitiveWordResult.empty(0);
        }

        lock.readLock().lock();
        try {
            List<MatchedWord> matchedWords = new ArrayList<>();
            int textLength = text.length();
            
            for (int i = 0; i < textLength; i++) {
                int matchLength = checkSensitiveWord(text, i, matchedWords);
                if (matchLength > 0) {
                    // 跳过已匹配的字符，继续检测
                    i += matchLength - 1;
                }
            }

            long detectTime = System.currentTimeMillis() - startTime;
            
            SensitiveWordResult result = SensitiveWordResult.builder()
                    .hasSensitiveWord(!matchedWords.isEmpty())
                    .matchedWords(matchedWords)
                    .detectTimeMs(detectTime)
                    .build();
            
            result.calculateRiskScore();
            return result;
            
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public String replace(String text, char replacement) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        SensitiveWordResult result = detect(text);
        if (!result.isHasSensitiveWord()) {
            return text;
        }

        StringBuilder sb = new StringBuilder(text);
        // 按位置倒序排列，避免替换时索引变化
        List<MatchedWord> sortedWords = new ArrayList<>(result.getMatchedWords());
        sortedWords.sort((a, b) -> b.getStartIndex() - a.getStartIndex());

        for (MatchedWord word : sortedWords) {
            int start = word.getStartIndex();
            int end = word.getEndIndex();
            String replacementStr = String.valueOf(replacement).repeat(end - start);
            sb.replace(start, end, replacementStr);
        }

        return sb.toString();
    }

    @Override
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        lock.readLock().lock();
        try {
            int textLength = text.length();
            for (int i = 0; i < textLength; i++) {
                if (checkSensitiveWordExists(text, i)) {
                    return true;
                }
            }
            return false;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int getWordCount() {
        return wordCount;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * 构建DFA树
     */
    private void buildDfaTree(List<SensitiveWord> words) {
        this.dfaMap = new HashMap<>();
        this.wordMetaMap = new HashMap<>();
        buildDfaTreeInternal(words, this.dfaMap, this.wordMetaMap);
        this.wordCount = this.wordMetaMap.size();
    }

    /**
     * 内部构建DFA树方法
     */
    @SuppressWarnings("unchecked")
    private void buildDfaTreeInternal(List<SensitiveWord> words, 
                                       Map<Character, Object> targetDfaMap,
                                       Map<String, SensitiveWord> targetWordMetaMap) {
        if (words == null || words.isEmpty()) {
            return;
        }

        for (SensitiveWord sensitiveWord : words) {
            if (sensitiveWord.getWord() == null || sensitiveWord.getWord().isEmpty()) {
                continue;
            }
            
            // 添加原始敏感词
            addWordToDfa(sensitiveWord.getWord(), sensitiveWord, targetDfaMap, targetWordMetaMap);
            
            // 添加变体词
            if (sensitiveWord.getVariants() != null) {
                for (String variant : sensitiveWord.getVariants()) {
                    if (variant != null && !variant.isEmpty()) {
                        addWordToDfa(variant, sensitiveWord, targetDfaMap, targetWordMetaMap);
                    }
                }
            }
        }
    }

    /**
     * 将单个敏感词添加到DFA树
     */
    @SuppressWarnings("unchecked")
    private void addWordToDfa(String word, SensitiveWord meta,
                              Map<Character, Object> targetDfaMap,
                              Map<String, SensitiveWord> targetWordMetaMap) {
        // 转换为小写进行匹配（不区分大小写）
        String normalizedWord = normalizeWord(word);
        
        Map<Character, Object> currentMap = targetDfaMap;
        
        for (int i = 0; i < normalizedWord.length(); i++) {
            char c = normalizedWord.charAt(i);
            Object nextObj = currentMap.get(c);
            
            if (nextObj == null) {
                // 创建新节点
                Map<Character, Object> newMap = new HashMap<>();
                currentMap.put(c, newMap);
                currentMap = newMap;
            } else {
                currentMap = (Map<Character, Object>) nextObj;
            }
            
            // 最后一个字符，标记为结束
            if (i == normalizedWord.length() - 1) {
                currentMap.put(IS_END_KEY.charAt(0), true);
                currentMap.put(WORD_META_KEY.charAt(0), meta);
                targetWordMetaMap.put(normalizedWord, meta);
            }
        }
    }

    /**
     * 检测从指定位置开始的敏感词
     * 
     * @param text 文本
     * @param startIndex 起始位置
     * @param matchedWords 匹配结果列表
     * @return 匹配的敏感词长度，0表示未匹配
     */
    @SuppressWarnings("unchecked")
    private int checkSensitiveWord(String text, int startIndex, List<MatchedWord> matchedWords) {
        Map<Character, Object> currentMap = dfaMap;
        int matchLength = 0;
        int lastMatchLength = 0;
        SensitiveWord lastMatchMeta = null;
        StringBuilder matchedWordBuilder = new StringBuilder();
        
        for (int i = startIndex; i < text.length(); i++) {
            char c = normalizeChar(text.charAt(i));
            
            // 跳过特殊分隔符（支持符号分隔的变体词识别）
            if (isIgnorableChar(c)) {
                matchLength++;
                continue;
            }
            
            Object nextObj = currentMap.get(c);
            
            if (nextObj == null) {
                // 无法继续匹配
                break;
            }
            
            currentMap = (Map<Character, Object>) nextObj;
            matchLength++;
            matchedWordBuilder.append(text.charAt(i));
            
            // 检查是否是敏感词结尾
            if (currentMap.containsKey(IS_END_KEY.charAt(0))) {
                lastMatchLength = matchLength;
                lastMatchMeta = (SensitiveWord) currentMap.get(WORD_META_KEY.charAt(0));
            }
        }
        
        // 如果找到匹配的敏感词
        if (lastMatchLength > 0 && lastMatchMeta != null) {
            MatchedWord matched = MatchedWord.builder()
                    .word(text.substring(startIndex, startIndex + lastMatchLength))
                    .category(lastMatchMeta.getCategory())
                    .level(lastMatchMeta.getLevel())
                    .startIndex(startIndex)
                    .endIndex(startIndex + lastMatchLength)
                    .build();
            matchedWords.add(matched);
            return lastMatchLength;
        }
        
        return 0;
    }

    /**
     * 快速检测从指定位置开始是否存在敏感词（不记录详情）
     */
    @SuppressWarnings("unchecked")
    private boolean checkSensitiveWordExists(String text, int startIndex) {
        Map<Character, Object> currentMap = dfaMap;
        
        for (int i = startIndex; i < text.length(); i++) {
            char c = normalizeChar(text.charAt(i));
            
            if (isIgnorableChar(c)) {
                continue;
            }
            
            Object nextObj = currentMap.get(c);
            
            if (nextObj == null) {
                return false;
            }
            
            currentMap = (Map<Character, Object>) nextObj;
            
            if (currentMap.containsKey(IS_END_KEY.charAt(0))) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 标准化敏感词（转小写、去除特殊字符）
     */
    private String normalizeWord(String word) {
        if (word == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : word.toCharArray()) {
            char normalized = normalizeChar(c);
            if (!isIgnorableChar(normalized)) {
                sb.append(normalized);
            }
        }
        return sb.toString();
    }

    /**
     * 标准化字符（转小写）
     */
    private char normalizeChar(char c) {
        // 转换为小写
        if (c >= 'A' && c <= 'Z') {
            return (char) (c + 32);
        }
        // 全角转半角
        if (c >= '！' && c <= '～') {
            return (char) (c - 65248);
        }
        // 全角空格
        if (c == '　') {
            return ' ';
        }
        return c;
    }

    /**
     * 判断是否为可忽略的分隔字符
     * 用于识别如 "敏*感*词" 这样的变体
     */
    private boolean isIgnorableChar(char c) {
        // 常见的分隔符
        return c == '*' || c == '_' || c == '-' || c == '.' || 
               c == ' ' || c == '\t' || c == '\n' || c == '\r' ||
               c == '·' || c == '•' || c == '。' || c == '，' ||
               c == '@' || c == '#' || c == '$' || c == '%' ||
               c == '^' || c == '&' || c == '!' || c == '~';
    }
}
