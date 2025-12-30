package com.basebackend.wheel.enums;

import lombok.Getter;

@Getter
public enum TaskDifficulty {

    EASY(1, "简单"),
    MEDIUM(2, "中等"),
    HARD(3, "困难");

    private final int code;
    private final String description;

    TaskDifficulty(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TaskDifficulty fromCode(int code) {
        for (TaskDifficulty difficulty : values()) {
            if (difficulty.code == code) {
                return difficulty;
            }
        }
        throw new IllegalArgumentException("Unknown task difficulty code: " + code);
    }
}
