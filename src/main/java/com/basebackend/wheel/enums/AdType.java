package com.basebackend.wheel.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 广告类型枚举
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Getter
public enum AdType {
    /**
     * 图片广告
     */
    IMAGE(1, "图片"),

    /**
     * 视频广告
     */
    VIDEO(2, "视频"),

    /**
     * 文字广告
     */
    TEXT(3, "文字");

    @EnumValue
    @JsonValue
    private final Integer value;
    private final String description;

    AdType(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
}
