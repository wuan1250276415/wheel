package com.basebackend.wheel.enums;

import lombok.Getter;

@Getter
public enum AchievementCategory {

    WHEEL(1, "转盘类"),
    COUPLE(2, "情侣类"),
    EXPLORATION(3, "探索类"),
    SOCIAL(4, "社交类");

    private final int code;
    private final String description;

    AchievementCategory(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static AchievementCategory fromCode(int code) {
        for (AchievementCategory category : values()) {
            if (category.code == code) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown achievement category code: " + code);
    }
}
