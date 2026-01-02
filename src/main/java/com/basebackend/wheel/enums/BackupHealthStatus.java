package com.basebackend.wheel.enums;

import lombok.Getter;

/**
 * 备份健康状态枚举
 * 用于表示备份系统的整体健康状况
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Getter
public enum BackupHealthStatus {

    HEALTHY(0, "健康", "备份系统运行正常"),
    WARNING(1, "警告", "备份系统存在潜在问题"),
    CRITICAL(2, "危险", "备份系统存在严重问题");

    private final int code;
    private final String name;
    private final String description;

    BackupHealthStatus(int code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    /**
     * 根据代码获取状态
     *
     * @param code 状态代码
     * @return 健康状态
     */
    public static BackupHealthStatus fromCode(int code) {
        for (BackupHealthStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown backup health status code: " + code);
    }

    /**
     * 判断是否健康
     *
     * @return 是否健康
     */
    public boolean isHealthy() {
        return this == HEALTHY;
    }

    /**
     * 判断是否需要关注
     *
     * @return 是否需要关注
     */
    public boolean needsAttention() {
        return this == WARNING || this == CRITICAL;
    }

    /**
     * 判断是否危险
     *
     * @return 是否危险
     */
    public boolean isCritical() {
        return this == CRITICAL;
    }
}
