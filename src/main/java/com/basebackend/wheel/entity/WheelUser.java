package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.basebackend.database.entity.BaseEntity;
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
public class WheelUser extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;


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

}
