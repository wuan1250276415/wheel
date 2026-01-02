package com.basebackend.wheel.enums;

import lombok.Getter;

/**
 * 告警类型枚举
 * 用于备份监控系统的告警分类
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Getter
public enum AlertType {

    BACKUP_FAILED(1, "备份失败", "数据库备份执行失败"),
    BACKUP_TIMEOUT(2, "备份超时", "数据库备份执行超时"),
    NO_RECENT_BACKUP(3, "无近期备份", "超过阈值时间没有成功的备份"),
    STORAGE_WARNING(4, "存储空间警告", "备份存储空间使用率过高"),
    STORAGE_CRITICAL(5, "存储空间危险", "备份存储空间即将耗尽"),
    RESTORE_FAILED(6, "恢复失败", "数据库恢复执行失败"),
    ENCRYPTION_ERROR(7, "加密错误", "备份文件加密或解密失败"),
    INTEGRITY_ERROR(8, "完整性错误", "备份文件完整性验证失败"),
    CLEANUP_FAILED(9, "清理失败", "过期备份清理失败"),
    CONFIG_ERROR(10, "配置错误", "备份配置存在问题");

    private final int code;
    private final String name;
    private final String description;

    AlertType(int code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    /**
     * 根据代码获取告警类型
     *
     * @param code 告警代码
     * @return 告警类型
     */
    public static AlertType fromCode(int code) {
        for (AlertType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown alert type code: " + code);
    }

    /**
     * 判断是否为严重告警
     *
     * @return 是否严重
     */
    public boolean isCritical() {
        return this == BACKUP_FAILED 
            || this == STORAGE_CRITICAL 
            || this == RESTORE_FAILED 
            || this == INTEGRITY_ERROR;
    }

    /**
     * 判断是否为存储相关告警
     *
     * @return 是否存储相关
     */
    public boolean isStorageRelated() {
        return this == STORAGE_WARNING || this == STORAGE_CRITICAL;
    }

    /**
     * 判断是否为备份相关告警
     *
     * @return 是否备份相关
     */
    public boolean isBackupRelated() {
        return this == BACKUP_FAILED 
            || this == BACKUP_TIMEOUT 
            || this == NO_RECENT_BACKUP;
    }
}
