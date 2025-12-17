package com.basebackend.wheel.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 用户资料DTO
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Data
public class UserProfileDTO {

    /**
     * 用户昵称
     */
    @NotBlank(message = "昵称不能为空")
    @Size(max = 20, message = "昵称长度不能超过20字符")
    private String nickname;

    /**
     * 性别：0-未知，1-男，2-女
     */
    @NotNull(message = "性别不能为空")
    private Integer gender;

    /**
     * 年龄
     */
    @NotNull(message = "年龄不能为空")
    private Integer age;

    /**
     * 城市
     */
    @Size(max = 50, message = "城市名称长度不能超过50字符")
    private String city;

    /**
     * 头像URL
     */
    private String avatarUrl;
}
