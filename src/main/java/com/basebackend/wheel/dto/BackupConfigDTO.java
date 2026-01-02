package com.basebackend.wheel.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 备份配置更新DTO
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Data
public class BackupConfigDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 每日备份 cron 表达式
     */
    @Pattern(regexp = "^[0-9*/?\\-,\\sA-Z]+$", message = "无效的cron表达式格式")
    private String dailyCron;

    /**
     * 每周备份 cron 表达式
     */
    @Pattern(regexp = "^[0-9*/?\\-,\\sA-Z]+$", message = "无效的cron表达式格式")
    private String weeklyCron;

    /**
     * 清理任务 cron 表达式
     */
    @Pattern(regexp = "^[0-9*/?\\-,\\sA-Z]+$", message = "无效的cron表达式格式")
    private String cleanupCron;

    /**
     * 健康检查 cron 表达式
     */
    @Pattern(regexp = "^[0-9*/?\\-,\\sA-Z]+$", message = "无效的cron表达式格式")
    private String healthCheckCron;

    /**
     * 备份保留天数
     */
    @Min(value = 1, message = "保留天数最少为1天")
    @Max(value = 365, message = "保留天数最多为365天")
    private Integer retentionDays;

    /**
     * 最少保留备份数
     */
    @Min(value = 1, message = "最少保留备份数最少为1")
    @Max(value = 100, message = "最少保留备份数最多为100")
    private Integer minBackupCount;

    /**
     * 备份存储路径
     */
    @NotBlank(message = "存储路径不能为空")
    private String storagePath;

    /**
     * 是否启用加密
     */
    private Boolean encryptionEnabled;

    /**
     * 无备份告警阈值（小时）
     */
    @Min(value = 1, message = "告警阈值最少为1小时")
    @Max(value = 168, message = "告警阈值最多为168小时（7天）")
    private Integer alertThresholdHours;

    /**
     * 存储告警阈值（GB）
     */
    @Min(value = 1, message = "存储告警阈值最少为1GB")
    @Max(value = 1000, message = "存储告警阈值最多为1000GB")
    private Integer storageWarningThresholdGb;
}
