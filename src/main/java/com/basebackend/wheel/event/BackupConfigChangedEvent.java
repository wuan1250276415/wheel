package com.basebackend.wheel.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 备份配置变更事件
 * 当备份配置更新时发布此事件，调度器监听并刷新调度
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Getter
public class BackupConfigChangedEvent extends ApplicationEvent {

    /**
     * 变更的配置键（可选，null 表示批量更新）
     */
    private final String configKey;

    /**
     * 新的配置值（可选）
     */
    private final String newValue;

    /**
     * 构造函数 - 单个配置变更
     *
     * @param source    事件源
     * @param configKey 配置键
     * @param newValue  新值
     */
    public BackupConfigChangedEvent(Object source, String configKey, String newValue) {
        super(source);
        this.configKey = configKey;
        this.newValue = newValue;
    }

    /**
     * 构造函数 - 批量配置变更
     *
     * @param source 事件源
     */
    public BackupConfigChangedEvent(Object source) {
        super(source);
        this.configKey = null;
        this.newValue = null;
    }

    /**
     * 是否为批量更新
     *
     * @return 是否批量更新
     */
    public boolean isBatchUpdate() {
        return configKey == null;
    }

    /**
     * 是否为 cron 表达式相关的配置变更
     *
     * @return 是否 cron 相关
     */
    public boolean isCronRelated() {
        if (configKey == null) {
            return true; // 批量更新可能包含 cron 变更
        }
        return configKey.endsWith("_cron");
    }
}
