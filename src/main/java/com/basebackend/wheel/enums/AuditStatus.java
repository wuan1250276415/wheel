package com.basebackend.wheel.enums;

import lombok.Getter;

@Getter
public enum AuditStatus {

    PENDING(0, "待审核"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已拒绝");

    private final int code;
    private final String description;

    AuditStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static AuditStatus fromCode(int code) {
        for (AuditStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown audit status code: " + code);
    }
}
