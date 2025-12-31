package com.basebackend.wheel.enums;

import lombok.Getter;

/**
 * 纪念日类型枚举
 */
@Getter
public enum AnniversaryType {

    RELATIONSHIP(1, "恋爱纪念日"),
    BIRTHDAY(2, "生日"),
    CUSTOM(3, "自定义");

    private final int code;
    private final String name;

    AnniversaryType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public static AnniversaryType fromCode(int code) {
        for (AnniversaryType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown anniversary type code: " + code);
    }
}
