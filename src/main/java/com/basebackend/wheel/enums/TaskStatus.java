package com.basebackend.wheel.enums;

import lombok.Getter;

@Getter
public enum TaskStatus {

    IN_PROGRESS(0, "进行中"),
    COMPLETED(1, "已完成"),
    CLAIMED(2, "已领奖"),
    EXPIRED(3, "已过期");

    private final int code;
    private final String description;

    TaskStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TaskStatus fromCode(int code) {
        for (TaskStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown task status code: " + code);
    }
}
