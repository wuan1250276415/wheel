package com.basebackend.wheel.dto;

import lombok.Data;

import java.util.List;

/**
 * 内容特征DTO
 * 
 * @author wheel-api
 * @since 2025-01-31
 */
@Data
public class ContentFeatureDTO {

    /**
     * 内容ID
     */
    private Long contentId;

    /**
     * 内容标签列表
     */
    private List<String> tags;

    /**
     * 热度分数
     */
    private Double popularityScore;

    /**
     * 难度等级：1-简单，2-中等，3-困难
     */
    private Integer difficultyLevel;

    /**
     * 难度描述
     */
    private String difficultyDesc;

    /**
     * 特征向量
     */
    private double[] featureVector;
}
