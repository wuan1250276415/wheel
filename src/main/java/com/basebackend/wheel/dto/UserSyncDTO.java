package com.basebackend.wheel.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户同步DTO
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Data
public class UserSyncDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 用户类型
     */
    private Integer userType;

    /**
     * 用户状态
     */
    private Integer status;
}
