package com.basebackend.wheel.dto;

import lombok.Data;

import java.util.List;

/**
 * 情侣推荐VO
 * 
 * @author wheel-api
 * @since 2025-01-31
 */
@Data
public class CoupleRecommendationVO {

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 推荐内容列表
     */
    private List<RecommendationVO> contents;

    /**
     * 匹配原因
     */
    private String matchReason;

    /**
     * 匹配度分数
     */
    private Double compatibilityScore;
}
