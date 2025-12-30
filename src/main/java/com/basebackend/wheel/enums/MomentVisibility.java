package com.basebackend.wheel.enums;

import lombok.Getter;

/**
 * 动态可见性枚举
 */
@Getter
public enum MomentVisibility {
    /**
     * 仅情侣可见
     */
    COUPLE_ONLY(0, "仅情侣可见"),

    /**
     * 公开
     */
    PUBLIC(1, "公开");

    private final Integer code;
    private final String description;

    MomentVisibility(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static MomentVisibility fromCode(Integer code) {
        if (code == null) {
            return COUPLE_ONLY;
        }
        for (MomentVisibility visibility : values()) {
            if (visibility.code.equals(code)) {
                return visibility;
            }
        }
        return COUPLE_ONLY;
    }
}
