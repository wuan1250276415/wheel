package com.basebackend.wheel.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 黑名单申诉DTO
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
public class BlacklistAppealDTO {

    /**
     * 黑名单记录ID
     */
    @NotNull(message = "黑名单ID不能为空")
    private Long blacklistId;

    /**
     * 申诉理由
     */
    @NotBlank(message = "申诉理由不能为空")
    private String appealReason;

    /**
     * 申诉人ID（用户ID）
     */
    private Long appealerId;

    /**
     * 用户ID（兼容字段，与appealerId相同）
     */
    private Long userId;

    /**
     * 设置用户ID（同时设置appealerId）
     */
    public void setUserId(Long userId) {
        this.userId = userId;
        this.appealerId = userId;
    }
}
