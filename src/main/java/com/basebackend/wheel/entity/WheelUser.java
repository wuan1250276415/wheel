package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户表
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("wheel_user")
public class WheelUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID - 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 手机号 - 加密存储
     */
    @TableField("phone_number")
    private String phoneNumber;

    /**
     * 用户昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 头像URL
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 性别：0-未知，1-男，2-女
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 年龄
     */
    @TableField("age")
    private Integer age;

    /**
     * 城市
     */
    @TableField("city")
    private String city;

    /**
     * 用户状态：0-禁用，1-正常，2-待审核
     */
    @TableField("status")
    private Integer status;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 创建人
     */
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 更新人
     */
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;
}
