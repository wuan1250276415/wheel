package com.basebackend.wheel.service;

import com.basebackend.wheel.entity.AuditConfig;

import java.util.List;
import java.util.Map;

/**
 * 审核配置服务接口
 *
 * @author wheel-api
 * @since 2025-02-01
 */
public interface AuditConfigService {

    /**
     * 获取配置值
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    int getIntConfig(String configKey, int defaultValue);

    /**
     * 获取配置值
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    String getStringConfig(String configKey, String defaultValue);

    /**
     * 获取配置值
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    boolean getBooleanConfig(String configKey, boolean defaultValue);

    /**
     * 获取AI通过阈值
     *
     * @return 通过阈值（风险分低于此值自动通过）
     */
    int getAiPassThreshold();

    /**
     * 获取AI拒绝阈值
     *
     * @return 拒绝阈值（风险分高于此值自动拒绝）
     */
    int getAiRejectThreshold();

    /**
     * 获取举报优先级提升阈值
     *
     * @return 举报数达到此值提升优先级
     */
    int getReportPriorityThreshold();

    /**
     * 获取VIP用户审核优先级
     *
     * @return VIP用户审核优先级
     */
    int getVipAuditPriority();

    /**
     * 获取SVIP用户审核优先级
     *
     * @return SVIP用户审核优先级
     */
    int getSvipAuditPriority();

    /**
     * 获取所有配置
     *
     * @return 配置列表
     */
    List<AuditConfig> getAllConfigs();

    /**
     * 获取所有配置（Map形式）
     *
     * @return 配置Map
     */
    Map<String, String> getAllConfigsAsMap();

    /**
     * 更新配置
     *
     * @param configKey   配置键
     * @param configValue 配置值
     */
    void updateConfig(String configKey, String configValue);

    /**
     * 刷新配置缓存
     */
    void refreshCache();
}
