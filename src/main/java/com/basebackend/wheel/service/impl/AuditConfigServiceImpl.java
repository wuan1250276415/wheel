package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.entity.AuditConfig;
import com.basebackend.wheel.mapper.AuditConfigMapper;
import com.basebackend.wheel.service.AuditConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 审核配置服务实现
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditConfigServiceImpl implements AuditConfigService {

    private final AuditConfigMapper auditConfigMapper;

    /**
     * 本地配置缓存
     */
    private final Map<String, String> configCache = new ConcurrentHashMap<>();

    /**
     * 缓存名称
     */
    private static final String CACHE_NAME = "auditConfig";

    @PostConstruct
    public void init() {
        // 初始化时加载所有配置到本地缓存
        refreshCache();
        log.info("审核配置服务初始化完成，已加载 {} 条配置", configCache.size());
    }

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
        return value != null ? value : defaultValue;
    }

    @Override
    public boolean getBooleanConfig(String configKey, boolean defaultValue) {
        String value = getConfigValue(configKey);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    @Override
    public int getAiPassThreshold() {
        return getIntConfig(AuditConfig.KEY_AI_PASS_THRESHOLD, AuditConfig.DEFAULT_AI_PASS_THRESHOLD);
    }

    @Override
    public int getAiRejectThreshold() {
        return getIntConfig(AuditConfig.KEY_AI_REJECT_THRESHOLD, AuditConfig.DEFAULT_AI_REJECT_THRESHOLD);
    }

    @Override
    public int getReportPriorityThreshold() {
        return getIntConfig(AuditConfig.KEY_REPORT_PRIORITY_THRESHOLD, AuditConfig.DEFAULT_REPORT_PRIORITY_THRESHOLD);
    }

    @Override
    public int getVipAuditPriority() {
        return getIntConfig(AuditConfig.KEY_VIP_AUDIT_PRIORITY, AuditConfig.DEFAULT_VIP_AUDIT_PRIORITY);
    }

    @Override
    public int getSvipAuditPriority() {
        return getIntConfig(AuditConfig.KEY_SVIP_AUDIT_PRIORITY, AuditConfig.DEFAULT_SVIP_AUDIT_PRIORITY);
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'all'")
    public List<AuditConfig> getAllConfigs() {
        return auditConfigMapper.selectAllConfigs();
    }

    @Override
    public Map<String, String> getAllConfigsAsMap() {
        return new HashMap<>(configCache);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void updateConfig(String configKey, String configValue) {
        int updated = auditConfigMapper.updateValueByKey(configKey, configValue);
        if (updated > 0) {
            // 更新本地缓存
            configCache.put(configKey, configValue);
            log.info("审核配置已更新: key={}, value={}", configKey, configValue);
        } else {
            log.warn("审核配置更新失败，配置不存在: key={}", configKey);
        }
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void refreshCache() {
        configCache.clear();
        List<AuditConfig> configs = auditConfigMapper.selectAllConfigs();
        for (AuditConfig config : configs) {
            configCache.put(config.getConfigKey(), config.getConfigValue());
        }
        log.info("审核配置缓存已刷新，共 {} 条配置", configCache.size());
    }

    /**
     * 获取配置值（优先从本地缓存获取）
     */
    private String getConfigValue(String configKey) {
        // 优先从本地缓存获取
        String value = configCache.get(configKey);
        if (value != null) {
            return value;
        }
        
        // 缓存未命中，从数据库查询
        AuditConfig config = auditConfigMapper.selectByKey(configKey);
        if (config != null && config.getConfigValue() != null) {
            // 更新本地缓存
            configCache.put(configKey, config.getConfigValue());
            return config.getConfigValue();
        }
        
        return null;
    }
}
