package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.config.BackupProperties;
import com.basebackend.wheel.dto.BackupConfigDTO;
import com.basebackend.wheel.dto.BackupConfigVO;
import com.basebackend.wheel.entity.BackupConfig;
import com.basebackend.wheel.event.BackupConfigChangedEvent;
import com.basebackend.wheel.mapper.BackupConfigMapper;
import com.basebackend.wheel.service.BackupConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 备份配置服务实现
 * 支持从 application.yml 和数据库加载配置，数据库配置优先
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BackupConfigServiceImpl implements BackupConfigService {

    private final BackupConfigMapper backupConfigMapper;
    private final BackupProperties backupProperties;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 本地配置缓存
     */
    private final Map<String, String> configCache = new ConcurrentHashMap<>();

    /**
     * 缓存名称
     */
    private static final String CACHE_NAME = "backupConfig";

    @PostConstruct
    public void init() {
        // 初始化时加载所有配置到本地缓存
        refreshCache();
        log.info("备份配置服务初始化完成，已加载 {} 条配置", configCache.size());
    }

    // ==================== 配置获取方法 ====================

    @Override
    public int getIntConfig(String configKey, int defaultValue) {
        String value = getConfigValue(configKey);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("配置值解析失败，使用默认值: key={}, value={}, default={}", configKey, value, defaultValue);
            return defaultValue;
        }
    }

    @Override
    public String getStringConfig(String configKey, String defaultValue) {
        String value = getConfigValue(configKey);
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    @Override
    public boolean getBooleanConfig(String configKey, boolean defaultValue) {
        String value = getConfigValue(configKey);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    // ==================== 便捷获取方法 ====================

    @Override
    public String getDailyCron() {
        return getStringConfig(BackupConfig.KEY_DAILY_CRON, backupProperties.getDailyCron());
    }

    @Override
    public String getWeeklyCron() {
        return getStringConfig(BackupConfig.KEY_WEEKLY_CRON, backupProperties.getWeeklyCron());
    }

    @Override
    public String getCleanupCron() {
        return getStringConfig(BackupConfig.KEY_CLEANUP_CRON, backupProperties.getCleanupCron());
    }

    @Override
    public String getHealthCheckCron() {
        return getStringConfig(BackupConfig.KEY_HEALTH_CHECK_CRON, backupProperties.getHealthCheckCron());
    }

    @Override
    public int getRetentionDays() {
        return getIntConfig(BackupConfig.KEY_RETENTION_DAYS, backupProperties.getRetentionDays());
    }

    @Override
    public int getMinBackupCount() {
        return getIntConfig(BackupConfig.KEY_MIN_BACKUP_COUNT, backupProperties.getMinBackupCount());
    }

    @Override
    public String getStoragePath() {
        return getStringConfig(BackupConfig.KEY_STORAGE_PATH, backupProperties.getStoragePath());
    }

    @Override
    public boolean isEncryptionEnabled() {
        return getBooleanConfig(BackupConfig.KEY_ENCRYPTION_ENABLED, backupProperties.isEncryptionEnabled());
    }

    @Override
    public int getAlertThresholdHours() {
        return getIntConfig(BackupConfig.KEY_ALERT_THRESHOLD_HOURS, backupProperties.getAlertThresholdHours());
    }

    @Override
    public int getStorageWarningThresholdGb() {
        return getIntConfig(BackupConfig.KEY_STORAGE_WARNING_THRESHOLD_GB, backupProperties.getStorageWarningThresholdGb());
    }

    // ==================== 配置管理方法 ====================

    @Override
    @Cacheable(value = CACHE_NAME, key = "'all'")
    public List<BackupConfig> getAllConfigs() {
        return backupConfigMapper.selectAllConfigs();
    }

    @Override
    public Map<String, String> getAllConfigsAsMap() {
        return new HashMap<>(configCache);
    }

    @Override
    public BackupConfigVO getConfigVO() {
        BackupConfigVO vo = new BackupConfigVO();
        
        vo.setDailyCron(getDailyCron());
        vo.setDailyCronDescription(describeCron(vo.getDailyCron()));
        
        vo.setWeeklyCron(getWeeklyCron());
        vo.setWeeklyCronDescription(describeCron(vo.getWeeklyCron()));
        
        vo.setCleanupCron(getCleanupCron());
        vo.setCleanupCronDescription(describeCron(vo.getCleanupCron()));
        
        vo.setHealthCheckCron(getHealthCheckCron());
        vo.setHealthCheckCronDescription(describeCron(vo.getHealthCheckCron()));
        
        vo.setRetentionDays(getRetentionDays());
        vo.setMinBackupCount(getMinBackupCount());
        vo.setStoragePath(getStoragePath());
        vo.setEncryptionEnabled(isEncryptionEnabled());
        vo.setAlertThresholdHours(getAlertThresholdHours());
        vo.setStorageWarningThresholdGb(getStorageWarningThresholdGb());
        
        // 获取最后更新时间
        List<BackupConfig> configs = getAllConfigs();
        if (!configs.isEmpty()) {
            LocalDateTime lastUpdate = configs.stream()
                    .map(BackupConfig::getUpdateTime)
                    .filter(t -> t != null)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            vo.setLastUpdatedTime(lastUpdate);
        }
        
        return vo;
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void updateConfig(String configKey, String configValue) {
        int updated = backupConfigMapper.updateValueByKey(configKey, configValue, null);
        if (updated > 0) {
            // 更新本地缓存
            configCache.put(configKey, configValue);
            log.info("备份配置已更新: key={}, value={}", configKey, configValue);
            
            // 发布配置变更事件
            eventPublisher.publishEvent(new BackupConfigChangedEvent(this, configKey, configValue));
        } else {
            log.warn("备份配置更新失败，配置不存在: key={}", configKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void updateConfigs(BackupConfigDTO configDTO) {
        // 先验证配置
        String validationError = validateConfig(configDTO);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        List<BackupConfig> updates = new ArrayList<>();

        if (StringUtils.hasText(configDTO.getDailyCron())) {
            addConfigUpdate(updates, BackupConfig.KEY_DAILY_CRON, configDTO.getDailyCron());
        }
        if (StringUtils.hasText(configDTO.getWeeklyCron())) {
            addConfigUpdate(updates, BackupConfig.KEY_WEEKLY_CRON, configDTO.getWeeklyCron());
        }
        if (StringUtils.hasText(configDTO.getCleanupCron())) {
            addConfigUpdate(updates, BackupConfig.KEY_CLEANUP_CRON, configDTO.getCleanupCron());
        }
        if (StringUtils.hasText(configDTO.getHealthCheckCron())) {
            addConfigUpdate(updates, BackupConfig.KEY_HEALTH_CHECK_CRON, configDTO.getHealthCheckCron());
        }
        if (configDTO.getRetentionDays() != null) {
            addConfigUpdate(updates, BackupConfig.KEY_RETENTION_DAYS, String.valueOf(configDTO.getRetentionDays()));
        }
        if (configDTO.getMinBackupCount() != null) {
            addConfigUpdate(updates, BackupConfig.KEY_MIN_BACKUP_COUNT, String.valueOf(configDTO.getMinBackupCount()));
        }
        if (StringUtils.hasText(configDTO.getStoragePath())) {
            addConfigUpdate(updates, BackupConfig.KEY_STORAGE_PATH, configDTO.getStoragePath());
        }
        if (configDTO.getEncryptionEnabled() != null) {
            addConfigUpdate(updates, BackupConfig.KEY_ENCRYPTION_ENABLED, String.valueOf(configDTO.getEncryptionEnabled()));
        }
        if (configDTO.getAlertThresholdHours() != null) {
            addConfigUpdate(updates, BackupConfig.KEY_ALERT_THRESHOLD_HOURS, String.valueOf(configDTO.getAlertThresholdHours()));
        }
        if (configDTO.getStorageWarningThresholdGb() != null) {
            addConfigUpdate(updates, BackupConfig.KEY_STORAGE_WARNING_THRESHOLD_GB, String.valueOf(configDTO.getStorageWarningThresholdGb()));
        }

        // 批量更新
        for (BackupConfig config : updates) {
            backupConfigMapper.updateValueByKey(config.getConfigKey(), config.getConfigValue(), config.getUpdateBy());
            configCache.put(config.getConfigKey(), config.getConfigValue());
        }

        log.info("备份配置批量更新完成，共更新 {} 条配置", updates.size());
        
        // 发布配置变更事件
        if (!updates.isEmpty()) {
            eventPublisher.publishEvent(new BackupConfigChangedEvent(this));
        }
    }

    @Override
    public String validateConfig(BackupConfigDTO configDTO) {
        List<String> errors = new ArrayList<>();

        // 验证 cron 表达式
        if (StringUtils.hasText(configDTO.getDailyCron()) && !isValidCronExpression(configDTO.getDailyCron())) {
            errors.add("每日备份cron表达式无效: " + configDTO.getDailyCron());
        }
        if (StringUtils.hasText(configDTO.getWeeklyCron()) && !isValidCronExpression(configDTO.getWeeklyCron())) {
            errors.add("每周备份cron表达式无效: " + configDTO.getWeeklyCron());
        }
        if (StringUtils.hasText(configDTO.getCleanupCron()) && !isValidCronExpression(configDTO.getCleanupCron())) {
            errors.add("清理任务cron表达式无效: " + configDTO.getCleanupCron());
        }
        if (StringUtils.hasText(configDTO.getHealthCheckCron()) && !isValidCronExpression(configDTO.getHealthCheckCron())) {
            errors.add("健康检查cron表达式无效: " + configDTO.getHealthCheckCron());
        }

        // 验证保留天数
        if (configDTO.getRetentionDays() != null) {
            if (configDTO.getRetentionDays() < 1) {
                errors.add("保留天数不能小于1");
            }
            if (configDTO.getRetentionDays() > 365) {
                errors.add("保留天数不能大于365");
            }
        }

        // 验证最少保留备份数
        if (configDTO.getMinBackupCount() != null) {
            if (configDTO.getMinBackupCount() < 1) {
                errors.add("最少保留备份数不能小于1");
            }
            if (configDTO.getMinBackupCount() > 100) {
                errors.add("最少保留备份数不能大于100");
            }
        }

        // 验证存储路径
        if (StringUtils.hasText(configDTO.getStoragePath())) {
            File storageDir = new File(configDTO.getStoragePath());
            if (storageDir.exists() && !storageDir.isDirectory()) {
                errors.add("存储路径不是有效的目录: " + configDTO.getStoragePath());
            }
            // 注意：不检查目录是否存在，因为可能需要创建
        }

        // 验证告警阈值
        if (configDTO.getAlertThresholdHours() != null) {
            if (configDTO.getAlertThresholdHours() < 1) {
                errors.add("告警阈值不能小于1小时");
            }
            if (configDTO.getAlertThresholdHours() > 168) {
                errors.add("告警阈值不能大于168小时（7天）");
            }
        }

        // 验证存储告警阈值
        if (configDTO.getStorageWarningThresholdGb() != null) {
            if (configDTO.getStorageWarningThresholdGb() < 1) {
                errors.add("存储告警阈值不能小于1GB");
            }
            if (configDTO.getStorageWarningThresholdGb() > 1000) {
                errors.add("存储告警阈值不能大于1000GB");
            }
        }

        return errors.isEmpty() ? null : String.join("; ", errors);
    }

    @Override
    public boolean isValidCronExpression(String cronExpression) {
        if (!StringUtils.hasText(cronExpression)) {
            return false;
        }
        try {
            CronExpression.parse(cronExpression);
            return true;
        } catch (IllegalArgumentException e) {
            log.debug("无效的cron表达式: {}, 错误: {}", cronExpression, e.getMessage());
            return false;
        }
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void refreshCache() {
        configCache.clear();
        List<BackupConfig> configs = backupConfigMapper.selectAllConfigs();
        for (BackupConfig config : configs) {
            if (config.getConfigValue() != null) {
                configCache.put(config.getConfigKey(), config.getConfigValue());
            }
        }
        log.info("备份配置缓存已刷新，共 {} 条配置", configCache.size());
    }

    // ==================== 私有方法 ====================

    /**
     * 获取配置值（优先从本地缓存获取，然后从数据库，最后从 properties）
     */
    private String getConfigValue(String configKey) {
        // 优先从本地缓存获取
        String value = configCache.get(configKey);
        if (value != null) {
            return value;
        }

        // 缓存未命中，从数据库查询
        BackupConfig config = backupConfigMapper.selectByKey(configKey);
        if (config != null && config.getConfigValue() != null) {
            // 更新本地缓存
            configCache.put(configKey, config.getConfigValue());
            return config.getConfigValue();
        }

        // 数据库也没有，返回 null（让调用方使用默认值）
        return null;
    }

    /**
     * 添加配置更新项
     */
    private void addConfigUpdate(List<BackupConfig> updates, String key, String value) {
        BackupConfig config = new BackupConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        updates.add(config);
    }

    /**
     * 描述 cron 表达式（简单的人类可读描述）
     */
    private String describeCron(String cronExpression) {
        if (!StringUtils.hasText(cronExpression)) {
            return "未配置";
        }
        
        // 简单的 cron 描述，实际项目中可以使用更完善的库
        try {
            String[] parts = cronExpression.split("\\s+");
            if (parts.length < 6) {
                return cronExpression;
            }
            
            String second = parts[0];
            String minute = parts[1];
            String hour = parts[2];
            String dayOfMonth = parts[3];
            String month = parts[4];
            String dayOfWeek = parts[5];
            
            StringBuilder desc = new StringBuilder();
            
            // 处理星期
            if (!"?".equals(dayOfWeek) && !"*".equals(dayOfWeek)) {
                desc.append("每周");
                switch (dayOfWeek) {
                    case "SUN": case "1": desc.append("日"); break;
                    case "MON": case "2": desc.append("一"); break;
                    case "TUE": case "3": desc.append("二"); break;
                    case "WED": case "4": desc.append("三"); break;
                    case "THU": case "5": desc.append("四"); break;
                    case "FRI": case "6": desc.append("五"); break;
                    case "SAT": case "7": desc.append("六"); break;
                    default: desc.append(dayOfWeek);
                }
                desc.append(" ");
            } else if ("*".equals(dayOfMonth)) {
                desc.append("每天 ");
            } else if (!"?".equals(dayOfMonth)) {
                desc.append("每月").append(dayOfMonth).append("日 ");
            }
            
            // 处理时间
            if ("*".equals(hour)) {
                desc.append("每小时");
            } else {
                desc.append(hour).append(":");
                desc.append("0".equals(minute) ? "00" : minute);
            }
            
            return desc.toString();
        } catch (Exception e) {
            return cronExpression;
        }
    }
}
