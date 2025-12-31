package com.basebackend.wheel.dto;

import lombok.Data;

/**
 * 推荐结果VO
 * 
 * @author wheel-api
 * @since 2025-01-31
 */
@Data
public class RecommendationVO {

    /**
     * 内容ID
     */
    private Long contentId;

    /**
     * 内容文本
     */
    private String contentText;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 推荐分数
     */
    private Double score;

    /**
     * 推荐原因
     */
    private String reason;
}
