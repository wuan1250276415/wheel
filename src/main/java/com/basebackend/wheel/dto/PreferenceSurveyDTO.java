package com.basebackend.wheel.dto;

import lombok.Data;

import java.util.List;

/**
 * 偏好调查DTO
 * 
 * @author wheel-api
 * @since 2025-01-31
 */
@Data
public class PreferenceSurveyDTO {

    /**
     * 选择的分类ID列表
     */
    private List<Long> selectedCategoryIds;

    /**
     * 是否跳过调查
     */
    private boolean skipped;
}
