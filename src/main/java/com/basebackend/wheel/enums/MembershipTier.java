package com.basebackend.wheel.enums;

import lombok.Getter;

@Getter
public enum MembershipTier {

    VIP(1, "VIP"),
    SVIP(2, "SVIP");

    private final int code;
    private final String description;

    MembershipTier(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static MembershipTier fromCode(int code) {
        for (MembershipTier tier : values()) {
            if (tier.code == code) {
                return tier;
            }
        }
        throw new IllegalArgumentException("Unknown membership tier code: " + code);
    }
}
