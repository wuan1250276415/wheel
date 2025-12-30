package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 转盘主题模板表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.wheel_theme")
public class WheelTheme extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主题名称
     */
    @TableField(value = "theme_name")
    private String themeName;

    /**
     * 主题标识key（如：romantic, tech, retro）
     */
    @TableField(value = "theme_key")
    private String themeKey;

    /**
     * 主题描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 主题配置（CSS变量JSON）
     */
    @TableField(value = "theme_config")
    private String themeConfig;

    /**
     * 预览图URL
     */
    @TableField(value = "preview_url")
    private String previewUrl;

    /**
     * 积分价格（0表示免费）
     */
    @TableField(value = "price")
    private Integer price;

    /**
     * 是否默认主题：0-否，1-是
     */
    @TableField(value = "is_default")
    private Boolean isDefault;

    /**
     * 主题状态：0-禁用，1-启用
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 排序权重
     */
    @TableField(value = "sort_order")
    private Integer sortOrder;

    /**
     * 会员等级要求：0-无要求，1-VIP，2-SVIP
     */
    @TableField(value = "required_tier")
    private Integer requiredTier;
}
