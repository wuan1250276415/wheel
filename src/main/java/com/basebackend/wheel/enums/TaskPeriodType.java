package com.basebackend.wheel.enums;

import lombok.Getter;

@Getter
public enum TaskPeriodType {

    DAILY(1, "每日"),
    WEEKLY(2, "每周");

    private final int code;
    private final String description;

    TaskPeriodType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TaskPeriodType fromCode(int code) {
        for (TaskPeriodType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown task period type code: " + code);
    }
}
