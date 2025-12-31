package com.basebackend.wheel.dto;

import lombok.Data;

/**
 * 分类排名VO
 * 
 * @author wheel-api
 * @since 2025-01-31
 */
@Data
public class CategoryRankVO {

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类图标URL
     */
    private String iconUrl;

    /**
     * 排名位置
     */
    private Integer rank;

    /**
     * 偏好权重
     */
    private Double weight;
}
