package com.basebackend.wheel.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 广告展示类型枚举
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Getter
public enum ImpressionType {
    /**
     * 展示
     */
    VIEW(1, "展示"),

    /**
     * 点击
     */
    CLICK(2, "点击");

    @EnumValue
    @JsonValue
    private final Integer value;
    private final String description;

    ImpressionType(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
}
