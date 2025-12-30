package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户主题拥有表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.user_theme")
public class UserTheme extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 主题ID
     */
    @TableField(value = "theme_id")
    private Long themeId;

    /**
     * 是否使用中：0-否，1-是
     */
    @TableField(value = "is_active")
    private Boolean isActive;

    /**
     * 购买时间
     */
    @TableField(value = "purchased_at")
    private Date purchasedAt;

    /**
     * 获取方式：0-免费，1-购买，2-赠送，3-活动
     */
    @TableField(value = "purchase_type")
    private Integer purchaseType;
}
