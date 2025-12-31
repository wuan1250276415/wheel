package com.basebackend.wheel.service;

import com.basebackend.wheel.dto.SensitiveWordResult;
import com.basebackend.wheel.entity.SensitiveWord;

import java.util.List;

/**
 * 敏感词过滤服务接口
 *
 * @author wheel-api
 * @since 2025-02-01
 */
public interface SensitiveWordFilter {

    /**
     * 初始化DFA树
     *
     * @param words 敏感词列表
     */
    void initialize(List<SensitiveWord> words);

    /**
     * 热更新敏感词库
     *
     * @param words 新的敏感词列表
     */
    void reload(List<SensitiveWord> words);

    /**
     * 检测文本中的敏感词
     *
     * @param text 待检测文本
     * @return 检测结果
     */
    SensitiveWordResult detect(String text);

    /**
     * 替换文本中的敏感词
     *
     * @param text        原文本
     * @param replacement 替换字符
     * @return 替换后的文本
     */
    String replace(String text, char replacement);

    /**
     * 检查文本是否包含敏感词
     *
     * @param text 待检测文本
     * @return 是否包含敏感词
     */
    boolean containsSensitiveWord(String text);

    /**
     * 获取当前敏感词库大小
     *
     * @return 敏感词数量
     */
    int getWordCount();

    /**
     * 检查是否已初始化
     *
     * @return 是否已初始化
     */
    boolean isInitialized();
}
