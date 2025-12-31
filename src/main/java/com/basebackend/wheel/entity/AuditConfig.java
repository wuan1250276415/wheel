package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审核配置表
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("audit_config")
public class AuditConfig extends BaseEntity {

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
     * 配置说明
     */
    @TableField("description")
    private String description;

    /**
     * 配置键常量
     */
    public static final String KEY_AI_PASS_THRESHOLD = "ai_pass_threshold";
    public static final String KEY_AI_REJECT_THRESHOLD = "ai_reject_threshold";
    public static final String KEY_REPORT_PRIORITY_THRESHOLD = "report_priority_threshold";
    public static final String KEY_VIP_AUDIT_PRIORITY = "vip_audit_priority";
    public static final String KEY_SVIP_AUDIT_PRIORITY = "svip_audit_priority";
    public static final String KEY_SENSITIVE_WORD_HOT_RELOAD_INTERVAL = "sensitive_word_hot_reload_interval";
    public static final String KEY_BLACKLIST_CACHE_TTL = "blacklist_cache_ttl";
    public static final String KEY_MAX_REPORT_PER_CONTENT_PER_USER = "max_report_per_content_per_user";
    public static final String KEY_LOW_CREDIBILITY_THRESHOLD = "low_credibility_threshold";
    public static final String KEY_CREDIBILITY_DECREASE_ON_INVALID = "credibility_decrease_on_invalid";
    public static final String KEY_CREDIBILITY_INCREASE_ON_VALID = "credibility_increase_on_valid";

    /**
     * 默认配置值常量
     */
    public static final int DEFAULT_AI_PASS_THRESHOLD = 30;
    public static final int DEFAULT_AI_REJECT_THRESHOLD = 80;
    public static final int DEFAULT_REPORT_PRIORITY_THRESHOLD = 3;
    public static final int DEFAULT_VIP_AUDIT_PRIORITY = 2;
    public static final int DEFAULT_SVIP_AUDIT_PRIORITY = 3;
    public static final int DEFAULT_SENSITIVE_WORD_HOT_RELOAD_INTERVAL = 300;
    public static final int DEFAULT_BLACKLIST_CACHE_TTL = 3600;
    public static final int DEFAULT_MAX_REPORT_PER_CONTENT_PER_USER = 1;
    public static final int DEFAULT_LOW_CREDIBILITY_THRESHOLD = 30;
    public static final int DEFAULT_CREDIBILITY_DECREASE_ON_INVALID = 10;
    public static final int DEFAULT_CREDIBILITY_INCREASE_ON_VALID = 5;

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
}
