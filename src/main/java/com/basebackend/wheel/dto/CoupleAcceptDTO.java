package com.basebackend.wheel.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 接受情侣邀请DTO
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Data
public class CoupleAcceptDTO {

    /**
     * 邀请码
     */
    @NotBlank(message = "邀请码不能为空")
    private String inviteCode;
}
