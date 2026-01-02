package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 备份配置VO（用于前端展示）
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Data
public class BackupConfigVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 每日备份 cron 表达式
     */
    private String dailyCron;

    /**
     * 每日备份描述（人类可读）
     */
    private String dailyCronDescription;

    /**
     * 每周备份 cron 表达式
     */
    private String weeklyCron;

    /**
     * 每周备份描述（人类可读）
     */
    private String weeklyCronDescription;

    /**
     * 清理任务 cron 表达式
     */
    private String cleanupCron;

    /**
     * 清理任务描述（人类可读）
     */
    private String cleanupCronDescription;

    /**
     * 健康检查 cron 表达式
     */
    private String healthCheckCron;

    /**
     * 健康检查描述（人类可读）
     */
    private String healthCheckCronDescription;

    /**
     * 备份保留天数
     */
    private Integer retentionDays;

    /**
     * 最少保留备份数
     */
    private Integer minBackupCount;

    /**
     * 备份存储路径
     */
    private String storagePath;

    /**
     * 是否启用加密
     */
    private Boolean encryptionEnabled;

    /**
     * 无备份告警阈值（小时）
     */
    private Integer alertThresholdHours;

    /**
     * 存储告警阈值（GB）
     */
    private Integer storageWarningThresholdGb;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdatedTime;

    /**
     * 最后更新人
     */
    private String lastUpdatedBy;
}
