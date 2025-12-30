package com.basebackend.wheel.enums;

import lombok.Getter;

@Getter
public enum RewardType {

    BADGE(1, "虚拟勋章"),
    SPIN_CHANCE(2, "转盘次数");

    private final int code;
    private final String description;

    RewardType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static RewardType fromCode(int code) {
        for (RewardType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown reward type code: " + code);
    }
}
