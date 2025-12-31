package com.basebackend.wheel.service;

import com.basebackend.wheel.dto.SensitiveWordResult;
import com.basebackend.wheel.entity.SensitiveWord;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.Map;

/**
 * 敏感词服务接口
 *
 * @author wheel-api
 * @since 2025-02-01
 */
public interface SensitiveWordService {

    /**
     * 初始化敏感词库
     * 从数据库加载所有启用的敏感词并构建DFA树
     */
    void initializeSensitiveWords();

    /**
     * 热更新敏感词库
     * 重新从数据库加载敏感词并更新DFA树，不需要重启服务
     */
    void reloadSensitiveWords();

    /**
     * 检测文本中的敏感词
     *
     * @param text 待检测文本
     * @return 检测结果
     */
    SensitiveWordResult detectSensitiveWords(String text);

    /**
     * 替换文本中的敏感词
     *
     * @param text        原文本
     * @param replacement 替换字符
     * @return 替换后的文本
     */
    String replaceSensitiveWords(String text, char replacement);

    /**
     * 检查文本是否包含敏感词
     *
     * @param text 待检测文本
     * @return 是否包含敏感词
     */
    boolean containsSensitiveWord(String text);

    /**
     * 添加敏感词
     *
     * @param sensitiveWord 敏感词实体
     * @return 是否添加成功
     */
    boolean addSensitiveWord(SensitiveWord sensitiveWord);

    /**
     * 批量添加敏感词
     *
     * @param words 敏感词列表
     * @return 添加数量
     */
    int batchAddSensitiveWords(List<SensitiveWord> words);

    /**
     * 更新敏感词
     *
     * @param sensitiveWord 敏感词实体
     * @return 是否更新成功
     */
    boolean updateSensitiveWord(SensitiveWord sensitiveWord);

    /**
     * 删除敏感词
     *
     * @param id 敏感词ID
     * @return 是否删除成功
     */
    boolean deleteSensitiveWord(Long id);

    /**
     * 启用/禁用敏感词
     *
     * @param id     敏感词ID
     * @param status 状态：0-禁用 1-启用
     * @return 是否操作成功
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 分页查询敏感词
     *
     * @param page     页码
     * @param size     每页大小
     * @param category 分类（可选）
     * @param keyword  关键词（可选）
     * @return 分页结果
     */
    Page<SensitiveWord> querySensitiveWords(int page, int size, Integer category, String keyword);

    /**
     * 根据ID获取敏感词
     *
     * @param id 敏感词ID
     * @return 敏感词实体
     */
    SensitiveWord getSensitiveWordById(Long id);

    /**
     * 获取敏感词统计信息
     *
     * @return 统计信息（各分类数量等）
     */
    Map<String, Object> getSensitiveWordStatistics();

    /**
     * 获取当前敏感词库大小
     *
     * @return 敏感词数量
     */
    int getSensitiveWordCount();

    /**
     * 检查敏感词是否存在
     *
     * @param word 敏感词
     * @return 是否存在
     */
    boolean existsSensitiveWord(String word);
}
