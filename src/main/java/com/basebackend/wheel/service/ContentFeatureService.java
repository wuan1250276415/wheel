package com.basebackend.wheel.service;

import com.basebackend.wheel.dto.ContentFeatureDTO;
import com.basebackend.wheel.entity.WheelContent;
import com.basebackend.wheel.enums.DifficultyLevel;

import java.util.List;

/**
 * 内容特征服务接口
 * 负责提取和维护内容特征数据，支持推荐系统
 *
 * @author wheel-api
 * @since 2025-01-31
 */
public interface ContentFeatureService {

    /**
     * 提取内容特征
     * 当新内容被审核通过时调用，提取标签并计算初始特征分数
     *
     * @param content 转盘内容
     * @return 内容特征DTO
     */
    ContentFeatureDTO extractFeatures(WheelContent content);

    /**
     * 计算内容热度分数
     * 基于转盘次数和时间衰减计算热度
     *
     * @param contentId 内容ID
     * @return 热度分数（0.0-1.0）
     */
    double calculatePopularityScore(Long contentId);

    /**
     * 分类内容难度
     * 基于内容文本分析确定难度等级
     *
     * @param content 转盘内容
     * @return 难度等级
     */
    DifficultyLevel categorizeContent(WheelContent content);

    /**
     * 批量更新热度分数
     * 用于定时任务，批量更新所有活跃内容的热度分数
     */
    void batchUpdatePopularityScores();

    /**
     * 获取内容特征
     * 获取指定内容的特征信息
     *
     * @param contentId 内容ID
     * @return 内容特征DTO，如果不存在返回null
     */
    ContentFeatureDTO getContentFeature(Long contentId);

    /**
     * 批量获取内容特征
     *
     * @param contentIds 内容ID列表
     * @return 内容特征列表
     */
    List<ContentFeatureDTO> getContentFeatures(List<Long> contentIds);

    /**
     * 更新内容特征
     * 当内容被修改时更新特征
     *
     * @param content 转盘内容
     */
    void updateContentFeature(WheelContent content);
}
