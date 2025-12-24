package com.basebackend.wheel.enums;

import lombok.Getter;

@Getter
public enum RelationshipStatus {

    PENDING(0, "待确认"),
    CONFIRMED(1, "已确认"),
    UNBOUND(2, "已解绑");

    private final int code;
    private final String description;

    RelationshipStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static RelationshipStatus fromCode(int code) {
        for (RelationshipStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown relationship status code: " + code);
    }
}
