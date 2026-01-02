package com.basebackend.wheel.service;

import com.basebackend.wheel.dto.BackupConfigDTO;
import com.basebackend.wheel.dto.BackupConfigVO;
import com.basebackend.wheel.entity.BackupConfig;

import java.util.List;
import java.util.Map;

/**
 * 备份配置服务接口
 *
 * @author wheel-api
 * @since 2025-02-02
 */
public interface BackupConfigService {

    // ==================== 配置获取方法 ====================

    /**
     * 获取整数配置值
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    int getIntConfig(String configKey, int defaultValue);

    /**
     * 获取字符串配置值
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    String getStringConfig(String configKey, String defaultValue);

    /**
     * 获取布尔配置值
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    boolean getBooleanConfig(String configKey, boolean defaultValue);

    // ==================== 便捷获取方法 ====================

    /**
     * 获取每日备份 cron 表达式
     *
     * @return cron 表达式
     */
    String getDailyCron();

    /**
     * 获取每周备份 cron 表达式
     *
     * @return cron 表达式
     */
    String getWeeklyCron();

    /**
     * 获取清理任务 cron 表达式
     *
     * @return cron 表达式
     */
    String getCleanupCron();

    /**
     * 获取健康检查 cron 表达式
     *
     * @return cron 表达式
     */
    String getHealthCheckCron();

    /**
     * 获取备份保留天数
     *
     * @return 保留天数
     */
    int getRetentionDays();

    /**
     * 获取最少保留备份数
     *
     * @return 最少保留数
     */
    int getMinBackupCount();

    /**
     * 获取备份存储路径
     *
     * @return 存储路径
     */
    String getStoragePath();

    /**
     * 是否启用加密
     *
     * @return 是否启用
     */
    boolean isEncryptionEnabled();

    /**
     * 获取无备份告警阈值（小时）
     *
     * @return 告警阈值
     */
    int getAlertThresholdHours();

    /**
     * 获取存储告警阈值（GB）
     *
     * @return 告警阈值
     */
    int getStorageWarningThresholdGb();

    // ==================== 配置管理方法 ====================

    /**
     * 获取所有配置
     *
     * @return 配置列表
     */
    List<BackupConfig> getAllConfigs();

    /**
     * 获取所有配置（Map形式）
     *
     * @return 配置Map
     */
    Map<String, String> getAllConfigsAsMap();

    /**
     * 获取配置VO（用于前端展示）
     *
     * @return 配置VO
     */
    BackupConfigVO getConfigVO();

    /**
     * 更新单个配置
     *
     * @param configKey   配置键
     * @param configValue 配置值
     */
    void updateConfig(String configKey, String configValue);

    /**
     * 批量更新配置
     *
     * @param configDTO 配置DTO
     */
    void updateConfigs(BackupConfigDTO configDTO);

    /**
     * 验证配置值
     *
     * @param configDTO 配置DTO
     * @return 验证结果，null表示验证通过，否则返回错误信息
     */
    String validateConfig(BackupConfigDTO configDTO);

    /**
     * 验证 cron 表达式
     *
     * @param cronExpression cron 表达式
     * @return 是否有效
     */
    boolean isValidCronExpression(String cronExpression);

    /**
     * 刷新配置缓存
     */
    void refreshCache();
}
