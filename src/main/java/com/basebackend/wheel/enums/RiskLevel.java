package com.basebackend.wheel.enums;

import lombok.Getter;

/**
 * 风险等级枚举
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Getter
public enum RiskLevel {

    LOW(1, "低", 10),
    MEDIUM(2, "中", 25),
    HIGH(3, "高", 50);

    private final int code;
    private final String name;
    private final int score;

    RiskLevel(int code, String name, int score) {
        this.code = code;
        this.name = name;
        this.score = score;
    }

    /**
     * 根据code获取枚举
     */
    public static RiskLevel fromCode(int code) {
        for (RiskLevel level : values()) {
            if (level.code == code) {
                return level;
            }
        }
        return MEDIUM;
    }

    /**
     * 根据名称获取枚举
     */
    public static RiskLevel fromName(String name) {
        for (RiskLevel level : values()) {
            if (level.name.equals(name)) {
                return level;
            }
        }
        return MEDIUM;
    }
}
