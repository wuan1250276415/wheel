package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.wheel_user")
public class WheelUser extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 手机号（加密）
     */
    @TableField(value = "phone_number")
    @Size(max = 255, message = "手机号（加密）最大长度要小于 255")
    @NotBlank(message = "手机号（加密）不能为空")
    private String phoneNumber;

    /**
     * 用户昵称
     */
    @TableField(value = "nickname")
    @Size(max = 100, message = "用户昵称最大长度要小于 100")
    private String nickname;

    /**
     * 头像URL
     */
    @TableField(value = "avatar_url")
    @Size(max = 500, message = "头像URL最大长度要小于 500")
    private String avatarUrl;

    /**
     * 性别：0-未知，1-男，2-女
     */
    @TableField(value = "gender")
    private Integer gender;

    /**
     * 年龄
     */
    @TableField(value = "age")
    private Integer age;

    /**
     * 城市
     */
    @TableField(value = "city")
    @Size(max = 100, message = "城市最大长度要小于 100")
    private String city;

    /**
     * 用户状态：0-禁用，1-正常，2-待审核
     */
    @TableField(value = "`status`")
    private Integer status;

    /**
     * 最后登录时间
     */
    @TableField(value = "last_login_time")
    private LocalDateTime lastLoginTime;
}