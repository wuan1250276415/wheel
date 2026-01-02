package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 备份配置表
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("backup_config")
public class BackupConfig extends BaseEntity {

    /**
     * 配置键
     */
    @TableField("config_key")
    private String configKey;

    /**
     * 配置值
     */
    @TableField("config_value")
    private String configValue;

    /**
     * 配置描述
     */
    @TableField("description")
    private String description;

    /**
     * 配置键常量
     */
    public static final String KEY_DAILY_CRON = "daily_cron";
    public static final String KEY_WEEKLY_CRON = "weekly_cron";
    public static final String KEY_CLEANUP_CRON = "cleanup_cron";
    public static final String KEY_HEALTH_CHECK_CRON = "health_check_cron";
    public static final String KEY_RETENTION_DAYS = "retention_days";
    public static final String KEY_MIN_BACKUP_COUNT = "min_backup_count";
    public static final String KEY_STORAGE_PATH = "storage_path";
    public static final String KEY_ENCRYPTION_ENABLED = "encryption_enabled";
    public static final String KEY_ALERT_THRESHOLD_HOURS = "alert_threshold_hours";
    public static final String KEY_STORAGE_WARNING_THRESHOLD_GB = "storage_warning_threshold_gb";

    /**
     * 默认配置值常量
     */
    public static final String DEFAULT_DAILY_CRON = "0 0 2 * * ?";
    public static final String DEFAULT_WEEKLY_CRON = "0 0 3 ? * SUN";
    public static final String DEFAULT_CLEANUP_CRON = "0 0 4 * * ?";
    public static final String DEFAULT_HEALTH_CHECK_CRON = "0 0 * * * ?";
    public static final int DEFAULT_RETENTION_DAYS = 30;
    public static final int DEFAULT_MIN_BACKUP_COUNT = 7;
    public static final String DEFAULT_STORAGE_PATH = "/data/backups";
    public static final boolean DEFAULT_ENCRYPTION_ENABLED = true;
    public static final int DEFAULT_ALERT_THRESHOLD_HOURS = 48;
    public static final int DEFAULT_STORAGE_WARNING_THRESHOLD_GB = 50;

    /**
     * 获取整数配置值
     */
    public Integer getIntValue() {
        if (this.configValue == null || this.configValue.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(this.configValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取整数配置值，带默认值
     */
    public int getIntValue(int defaultValue) {
        Integer value = getIntValue();
        return value != null ? value : defaultValue;
    }

    /**
     * 获取长整数配置值
     */
    public Long getLongValue() {
        if (this.configValue == null || this.configValue.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(this.configValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取布尔配置值
     */
    public Boolean getBooleanValue() {
        if (this.configValue == null || this.configValue.isEmpty()) {
            return null;
        }
        return "true".equalsIgnoreCase(this.configValue) || "1".equals(this.configValue);
    }

    /**
     * 获取布尔配置值，带默认值
     */
    public boolean getBooleanValue(boolean defaultValue) {
        Boolean value = getBooleanValue();
        return value != null ? value : defaultValue;
    }

    /**
     * 获取双精度配置值
     */
    public Double getDoubleValue() {
        if (this.configValue == null || this.configValue.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(this.configValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取字符串配置值
     */
    public String getStringValue() {
        return this.configValue;
    }

    /**
     * 获取字符串配置值，带默认值
     */
    public String getStringValue(String defaultValue) {
        return this.configValue != null && !this.configValue.isEmpty() ? this.configValue : defaultValue;
    }
}
