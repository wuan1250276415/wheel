package com.basebackend.wheel.enums;

import lombok.Getter;

/**
 * 内容难度等级枚举
 * 
 * @author wheel-api
 * @since 2025-01-31
 */
@Getter
public enum DifficultyLevel {

    EASY(1, "简单"),
    MEDIUM(2, "中等"),
    HARD(3, "困难");

    private final int code;
    private final String description;

    DifficultyLevel(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static DifficultyLevel fromCode(int code) {
        for (DifficultyLevel level : values()) {
            if (level.code == code) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown difficulty level code: " + code);
    }
}
