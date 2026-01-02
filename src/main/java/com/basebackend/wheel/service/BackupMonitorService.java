package com.basebackend.wheel.service;

import com.basebackend.wheel.dto.BackupProgressVO;
import com.basebackend.wheel.enums.AlertType;
import com.basebackend.wheel.enums.BackupHealthStatus;

import java.util.List;

/**
 * 备份监控服务接口
 * 负责备份进度跟踪、健康检查和告警通知
 *
 * @author wheel-api
 * @since 2025-02-02
 */
public interface BackupMonitorService {

    // ==================== 备份进度管理 ====================

    /**
     * 记录备份开始
     *
     * @param backupId 备份记录ID
     */
    void recordBackupStart(Long backupId);

    /**
     * 更新备份进度
     *
     * @param backupId  备份记录ID
     * @param progress  进度百分比 (0-100)
     * @param stageCode 阶段代码
     */
    void updateBackupProgress(Long backupId, int progress, int stageCode);

    /**
     * 更新备份进度（带已处理数据量）
     *
     * @param backupId       备份记录ID
     * @param progress       进度百分比 (0-100)
     * @param stageCode      阶段代码
     * @param processedBytes 已处理数据量（字节）
     */
    void updateBackupProgress(Long backupId, int progress, int stageCode, long processedBytes);

    /**
     * 记录备份完成
     *
     * @param backupId 备份记录ID
     * @param success  是否成功
     */
    void recordBackupComplete(Long backupId, boolean success);

    /**
     * 记录备份完成（带错误信息）
     *
     * @param backupId     备份记录ID
     * @param success      是否成功
     * @param errorMessage 错误信息（失败时）
     */
    void recordBackupComplete(Long backupId, boolean success, String errorMessage);

    /**
     * 获取备份进度
     *
     * @param backupId 备份记录ID
     * @return 备份进度VO，如果不存在返回null
     */
    BackupProgressVO getBackupProgress(Long backupId);

    /**
     * 检查是否有备份正在进行
     *
     * @return 是否有备份进行中
     */
    boolean isBackupInProgress();

    /**
     * 获取当前进行中的备份ID
     *
     * @return 备份ID，如果没有进行中的备份返回null
     */
    Long getCurrentBackupId();

    // ==================== 恢复进度管理 ====================

    /**
     * 记录恢复开始
     *
     * @param restoreId 恢复记录ID
     */
    void recordRestoreStart(Long restoreId);

    /**
     * 更新恢复进度
     *
     * @param restoreId 恢复记录ID
     * @param progress  进度百分比 (0-100)
     * @param stage     阶段描述
     */
    void updateRestoreProgress(Long restoreId, int progress, String stage);

    /**
     * 记录恢复完成
     *
     * @param restoreId    恢复记录ID
     * @param success      是否成功
     * @param errorMessage 错误信息（失败时）
     */
    void recordRestoreComplete(Long restoreId, boolean success, String errorMessage);

    /**
     * 检查是否有恢复正在进行
     *
     * @return 是否有恢复进行中
     */
    boolean isRestoreInProgress();

    /**
     * 获取当前进行中的恢复ID
     *
     * @return 恢复ID，如果没有进行中的恢复返回null
     */
    Long getCurrentRestoreId();

    // ==================== 健康检查 ====================

    /**
     * 检查备份健康状态
     * 综合评估备份系统的整体健康状况
     *
     * @return 健康状态
     */
    BackupHealthStatus checkHealth();

    /**
     * 获取健康状态详细信息
     *
     * @return 健康状态详情
     */
    HealthCheckResult getHealthCheckResult();

    /**
     * 执行健康检查并发送告警（如果需要）
     * 通常由定时任务调用
     */
    void performHealthCheck();

    // ==================== 告警管理 ====================

    /**
     * 发送告警通知
     *
     * @param alertType 告警类型
     * @param message   告警消息
     */
    void sendAlert(AlertType alertType, String message);

    /**
     * 发送告警通知（带详细信息）
     *
     * @param alertType 告警类型
     * @param message   告警消息
     * @param details   详细信息
     */
    void sendAlert(AlertType alertType, String message, String details);

    /**
     * 检查是否需要发送无备份告警
     *
     * @return 是否需要告警
     */
    boolean shouldAlertNoRecentBackup();

    /**
     * 检查是否需要发送存储空间告警
     *
     * @return 是否需要告警
     */
    boolean shouldAlertStorageWarning();

    /**
     * 检查是否需要发送存储空间危险告警
     *
     * @return 是否需要告警
     */
    boolean shouldAlertStorageCritical();

    /**
     * 获取最近的告警列表
     *
     * @param limit 数量限制
     * @return 告警列表
     */
    List<AlertRecord> getRecentAlerts(int limit);

    /**
     * 清除告警冷却期（用于测试或紧急情况）
     *
     * @param alertType 告警类型
     */
    void clearAlertCooldown(AlertType alertType);

    // ==================== 统计信息 ====================

    /**
     * 获取最后一次成功备份的时间戳
     *
     * @return 时间戳（毫秒），如果没有成功备份返回null
     */
    Long getLastSuccessfulBackupTimestamp();

    /**
     * 获取距离上次成功备份的小时数
     *
     * @return 小时数，如果没有成功备份返回-1
     */
    int getHoursSinceLastBackup();

    /**
     * 健康检查结果
     */
    interface HealthCheckResult {
        /**
         * 获取健康状态
         */
        BackupHealthStatus getStatus();

        /**
         * 获取状态消息
         */
        String getMessage();

        /**
         * 获取最后备份时间
         */
        Long getLastBackupTimestamp();

        /**
         * 获取距离上次备份的小时数
         */
        int getHoursSinceLastBackup();

        /**
         * 获取存储使用率
         */
        double getStorageUsagePercent();

        /**
         * 获取存储状态
         */
        String getStorageStatus();

        /**
         * 是否有进行中的备份
         */
        boolean hasBackupInProgress();

        /**
         * 是否有进行中的恢复
         */
        boolean hasRestoreInProgress();

        /**
         * 获取告警列表
         */
        java.util.List<String> getAlerts();
    }

    /**
     * 告警记录
     */
    interface AlertRecord {
        /**
         * 获取告警类型
         */
        AlertType getType();

        /**
         * 获取告警消息
         */
        String getMessage();

        /**
         * 获取详细信息
         */
        String getDetails();

        /**
         * 获取告警时间戳
         */
        long getTimestamp();
    }
}
