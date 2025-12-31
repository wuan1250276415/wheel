package com.basebackend.wheel.dto;

import lombok.Data;

/**
 * 默契度计算结果
 */
@Data
public class CompatibilityResult {

    /**
     * 总评分(0-100)
     */
    private int totalScore;

    /**
     * 转盘频率相似度评分(0-100)
     */
    private double spinFrequencyScore;

    /**
     * 分类偏好重叠度评分(0-100)
     */
    private double categoryOverlapScore;

    /**
     * 活跃时间重叠度评分(0-100)
     */
    private double activeTimeOverlapScore;

    /**
     * 聊天响应速度评分(0-100)
     */
    private double chatResponseScore;

    /**
     * 评分描述
     */
    private String description;
}
