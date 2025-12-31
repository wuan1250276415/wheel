package com.basebackend.wheel.enums;

import lombok.Getter;

/**
 * 敏感词分类枚举
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Getter
public enum SensitiveWordCategory {

    PORN(1, "色情", "涉及色情、低俗内容"),
    VIOLENCE(2, "暴力", "涉及暴力、血腥内容"),
    POLITICS(3, "政治", "涉及政治敏感内容"),
    AD(4, "广告", "涉及广告、推广内容"),
    OTHER(5, "其他", "其他违规内容");

    private final int code;
    private final String name;
    private final String description;

    SensitiveWordCategory(int code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static SensitiveWordCategory fromCode(int code) {
        for (SensitiveWordCategory category : values()) {
            if (category.code == code) {
                return category;
            }
        }
        return OTHER;
    }

    /**
     * 根据名称获取枚举
     */
    public static SensitiveWordCategory fromName(String name) {
        for (SensitiveWordCategory category : values()) {
            if (category.name.equals(name)) {
                return category;
            }
        }
        return OTHER;
    }
}
