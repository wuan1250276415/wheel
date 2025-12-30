package com.basebackend.wheel.enums;

import lombok.Getter;

@Getter
public enum TaskType {

    WHEEL_CHALLENGE(1, "转盘挑战"),
    COUPLE_INTERACTION(2, "情侣互动"),
    EXPLORATION_CHALLENGE(3, "探索挑战"),
    SOCIAL_CHALLENGE(4, "社交挑战");

    private final int code;
    private final String description;

    TaskType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TaskType fromCode(int code) {
        for (TaskType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown task type code: " + code);
    }
}
