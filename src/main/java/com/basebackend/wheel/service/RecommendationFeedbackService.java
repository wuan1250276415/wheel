package com.basebackend.wheel.service;

import com.basebackend.wheel.dto.RecommendationFeedbackDTO;

/**
 * 推荐反馈服务接口
 * 负责收集用户对推荐的反馈，用于改进推荐质量
 *
 * @author wheel-api
 * @since 2025-01-31
 */
public interface RecommendationFeedbackService {

    /**
     * 记录点击事件
     * 当用户点击推荐内容时调用
     *
     * @param userId 用户ID
     * @param contentId 内容ID
     * @param sessionId 会话ID
     */
    void recordClick(Long userId, Long contentId, String sessionId);

    /**
     * 记录忽略事件（基于会话）
     * 当用户在会话中未点击推荐内容时调用
     *
     * @param userId 用户ID
     * @param contentId 内容ID
     * @param sessionId 会话ID
     */
    void recordIgnore(Long userId, Long contentId, String sessionId);

    /**
     * 记录推荐反馈
     * 统一处理各种类型的反馈
     *
     * @param userId 用户ID
     * @param feedbackDTO 反馈数据
     */
    void recordFeedback(Long userId, RecommendationFeedbackDTO feedbackDTO);

    /**
     * 计算内容的点击率（CTR）
     *
     * @param contentId 内容ID
     * @return 点击率（0-1之间），如果曝光数不足则返回null
     */
    Double calculateCTR(Long contentId);

    /**
     * 根据CTR调整内容推荐分数
     * 当CTR低于阈值时降低推荐分数
     *
     * @param contentId 内容ID
     */
    void adjustScoreBasedOnCTR(Long contentId);

    /**
     * 批量调整所有内容的推荐分数
     * 用于定时任务
     */
    void batchAdjustScores();

    /**
     * 记录推荐曝光
     * 当推荐内容展示给用户时调用
     *
     * @param userId 用户ID
     * @param contentId 内容ID
     * @param score 推荐分数
     * @param reason 推荐原因
     * @param sessionId 会话ID
     */
    void recordImpression(Long userId, Long contentId, Double score, String reason, String sessionId);

    /**
     * 获取内容的曝光次数
     *
     * @param contentId 内容ID
     * @return 曝光次数
     */
    Long getImpressionCount(Long contentId);

    /**
     * 获取内容的点击次数
     *
     * @param contentId 内容ID
     * @return 点击次数
     */
    Long getClickCount(Long contentId);

    /**
     * 清理过期的推荐日志
     * 保留最近90天的记录
     *
     * @return 删除的记录数
     */
    int cleanupOldLogs();
}
