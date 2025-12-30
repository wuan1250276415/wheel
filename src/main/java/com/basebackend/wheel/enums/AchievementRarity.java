package com.basebackend.wheel.enums;

import lombok.Getter;

@Getter
public enum AchievementRarity {

    COMMON(1, "普通"),
    RARE(2, "稀有"),
    EPIC(3, "史诗"),
    LEGENDARY(4, "传说");

    private final int code;
    private final String description;

    AchievementRarity(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static AchievementRarity fromCode(int code) {
        for (AchievementRarity rarity : values()) {
            if (rarity.code == code) {
                return rarity;
            }
        }
        throw new IllegalArgumentException("Unknown achievement rarity code: " + code);
    }
}
