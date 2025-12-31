package com.basebackend.wheel.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 黑名单申诉处理DTO
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
public class BlacklistAppealHandleDTO {

    /**
     * 黑名单记录ID
     */
    @NotNull(message = "黑名单ID不能为空")
    private Long blacklistId;

    /**
     * 是否通过申诉
     */
    @NotNull(message = "处理结果不能为空")
    private Boolean approved;

    /**
     * 处理结果说明
     */
    private String result;

    /**
     * 处理人ID
     */
    private Long handlerId;
}
