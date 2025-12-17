package com.basebackend.wheel.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 情侣邀请DTO
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Data
public class CoupleInviteDTO {

    /**
     * 对方手机号
     */
    @NotBlank(message = "对方手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String partnerPhoneNumber;

    /**
     * 邀请消息（可选）
     */
    private String inviteMessage;
}
