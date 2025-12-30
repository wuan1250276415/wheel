package com.basebackend.wheel.dto;

import lombok.Data;

/**
 * 主题VO
 */
@Data
public class ThemeVO {
    /**
     * 主题ID
     */
    private Long id;

    /**
     * 主题名称
     */
    private String themeName;

    /**
     * 主题标识key
     */
    private String themeKey;

    /**
     * 主题描述
     */
    private String description;

    /**
     * 主题配置JSON
     */
    private String themeConfig;

    /**
     * 预览图URL
     */
    private String previewUrl;

    /**
     * 积分价格
     */
    private Integer price;

    /**
     * 是否默认主题
     */
    private Boolean isDefault;

    /**
     * 是否已拥有
     */
    private Boolean owned;

    /**
     * 是否使用中
     */
    private Boolean active;
}
