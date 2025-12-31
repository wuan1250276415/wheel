package com.basebackend.wheel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * 内容申诉处理请求DTO
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentAppealHandleDTO {

    /**
     * 内容ID
     */
    @NotNull(message = "内容ID不能为空")
    private Long contentId;

    /**
     * 是否批准申诉
     */
    @NotNull(message = "审核结果不能为空")
    private Boolean approved;

    /**
     * 处理意见
     */
    private String auditComment;
}
