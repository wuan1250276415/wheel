package com.basebackend.wheel.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 推荐反馈DTO
 * 
 * @author wheel-api
 * @since 2025-01-31
 */
@Data
public class RecommendationFeedbackDTO {

    /**
     * 内容ID
     */
    @NotNull(message = "内容ID不能为空")
    private Long contentId;

    /**
     * 反馈类型：CLICK, IGNORE, LIKE, DISLIKE
     */
    @NotBlank(message = "反馈类型不能为空")
    private String feedbackType;

    /**
     * 会话ID
     */
    private String sessionId;
}
