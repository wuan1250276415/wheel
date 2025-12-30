package com.basebackend.wheel.enums;

import lombok.Getter;

@Getter
public enum MembershipStatus {

    PENDING(0, "待激活"),
    ACTIVE(1, "有效"),
    EXPIRED(2, "已过期"),
    CANCELLED(3, "已取消");

    private final int code;
    private final String description;

    MembershipStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static MembershipStatus fromCode(int code) {
        for (MembershipStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown membership status code: " + code);
    }
}
