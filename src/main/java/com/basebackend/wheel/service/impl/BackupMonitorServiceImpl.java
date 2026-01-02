package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.dto.BackupProgressVO;
import com.basebackend.wheel.entity.BackupRecord;
import com.basebackend.wheel.enums.AlertType;
import com.basebackend.wheel.enums.BackupHealthStatus;
import com.basebackend.wheel.mapper.BackupRecordMapper;
import com.basebackend.wheel.service.BackupConfigService;
import com.basebackend.wheel.service.BackupMonitorService;
import com.basebackend.wheel.service.BackupStorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 备份监控服务实现
 * 使用 Redis 存储备份进度，实现健康检查和告警逻辑
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BackupMonitorServiceImpl implements BackupMonitorService {

    private final StringRedisTemplate redisTemplate;
    private final BackupRecordMapper backupRecordMapper;
    private final BackupConfigService backupConfigService;
    private final BackupStorageService backupStorageService;
    private final ObjectMapper objectMapper;

    // ==================== Redis Key 常量 ====================

    private static final String KEY_PREFIX = "backup:monitor:";
    private static final String KEY_BACKUP_PROGRESS = KEY_PREFIX + "progress:backup:";
    private static final String KEY_RESTORE_PROGRESS = KEY_PREFIX + "progress:restore:";
    private static final String KEY_CURRENT_BACKUP = KEY_PREFIX + "current:backup";
    private static final String KEY_CURRENT_RESTORE = KEY_PREFIX + "current:restore";
    private static final String KEY_ALERT_COOLDOWN = KEY_PREFIX + "alert:cooldown:";
    private static final String KEY_ALERT_HISTORY = KEY_PREFIX + "alert:history:";

    // ==================== 超时配置 ====================

    /**
     * 进度信息过期时间（小时）
     */
    private static final int PROGRESS_EXPIRE_HOURS = 24;

    /**
     * 告警冷却时间（分钟）
     */
    private static final int ALERT_COOLDOWN_MINUTES = 30;

    // ==================== 备份进度管理 ====================

    @Override
    public void recordBackupStart(Long backupId) {
        if (backupId == null) {
            log.warn("备份ID为空，无法记录备份开始");
            return;
        }

        try {
            BackupProgressVO progress = BackupProgressVO.initial(backupId);
            String progressJson = objectMapper.writeValueAsString(progress);
            
            // 存储进度信息
            String progressKey = KEY_BACKUP_PROGRESS + backupId;
            redisTemplate.opsForValue().set(progressKey, progressJson, PROGRESS_EXPIRE_HOURS, TimeUnit.HOURS);
            
            // 标记当前进行中的备份
            redisTemplate.opsForValue().set(KEY_CURRENT_BACKUP, String.valueOf(backupId), PROGRESS_EXPIRE_HOURS, TimeUnit.HOURS);
            
            log.info("备份开始记录: backupId={}", backupId);
        } catch (JsonProcessingException e) {
            log.error("序列化备份进度失败: backupId={}", backupId, e);
        }
    }

    @Override
    public void updateBackupProgress(Long backupId, int progress, int stageCode) {
        updateBackupProgress(backupId, progress, stageCode, 0);
    }

    @Override
    public void updateBackupProgress(Long backupId, int progress, int stageCode, long processedBytes) {
        if (backupId == null) {
            return;
        }

        try {
            String progressKey = KEY_BACKUP_PROGRESS + backupId;
            String existingJson = redisTemplate.opsForValue().get(progressKey);
            
            BackupProgressVO progressVO;
            if (existingJson != null) {
                progressVO = objectMapper.readValue(existingJson, BackupProgressVO.class);
            } else {
                progressVO = BackupProgressVO.initial(backupId);
            }

            // 更新进度信息
            progressVO.setProgress(Math.min(100, Math.max(0, progress)));
            progressVO.setStageCode(stageCode);
            progressVO.setStage(BackupProgressVO.getStageName(stageCode));
            progressVO.setProcessedBytes(processedBytes);
            progressVO.setProcessedBytesFormatted(BackupProgressVO.formatBytes(processedBytes));

            // 计算已耗时
            if (progressVO.getStartTimestamp() != null) {
                long elapsedMs = System.currentTimeMillis() - progressVO.getStartTimestamp();
                progressVO.setElapsedMs(elapsedMs);
                progressVO.setElapsedFormatted(BackupProgressVO.formatDuration(elapsedMs));

                // 估算剩余时间
                if (progress > 0 && progress < 100) {
                    long estimatedTotalMs = elapsedMs * 100 / progress;
                    long estimatedRemainingMs = estimatedTotalMs - elapsedMs;
                    progressVO.setEstimatedRemainingMs(Math.max(0, estimatedRemainingMs));
                    progressVO.setEstimatedRemainingFormatted(BackupProgressVO.formatDuration(estimatedRemainingMs));
                }
            }

            String progressJson = objectMapper.writeValueAsString(progressVO);
            redisTemplate.opsForValue().set(progressKey, progressJson, PROGRESS_EXPIRE_HOURS, TimeUnit.HOURS);

            log.debug("备份进度更新: backupId={}, progress={}%, stage={}", backupId, progress, progressVO.getStage());
        } catch (Exception e) {
            log.error("更新备份进度失败: backupId={}", backupId, e);
        }
    }

    @Override
    public void recordBackupComplete(Long backupId, boolean success) {
        recordBackupComplete(backupId, success, null);
    }

    @Override
    public void recordBackupComplete(Long backupId, boolean success, String errorMessage) {
        if (backupId == null) {
            return;
        }

        try {
            String progressKey = KEY_BACKUP_PROGRESS + backupId;
            String existingJson = redisTemplate.opsForValue().get(progressKey);
            
            long elapsedMs = 0;
            if (existingJson != null) {
                BackupProgressVO existingProgress = objectMapper.readValue(existingJson, BackupProgressVO.class);
                if (existingProgress.getStartTimestamp() != null) {
                    elapsedMs = System.currentTimeMillis() - existingProgress.getStartTimestamp();
                }
            }

            BackupProgressVO completedProgress = BackupProgressVO.completed(backupId, success, elapsedMs, errorMessage);
            String progressJson = objectMapper.writeValueAsString(completedProgress);
            redisTemplate.opsForValue().set(progressKey, progressJson, PROGRESS_EXPIRE_HOURS, TimeUnit.HOURS);

            // 清除当前进行中的备份标记
            String currentBackupId = redisTemplate.opsForValue().get(KEY_CURRENT_BACKUP);
            if (currentBackupId != null && currentBackupId.equals(String.valueOf(backupId))) {
                redisTemplate.delete(KEY_CURRENT_BACKUP);
            }

            log.info("备份完成记录: backupId={}, success={}, elapsedMs={}", backupId, success, elapsedMs);

            // 如果失败，发送告警
            if (!success) {
                sendAlert(AlertType.BACKUP_FAILED, "备份执行失败: " + (errorMessage != null ? errorMessage : "未知错误"));
            }
        } catch (Exception e) {
            log.error("记录备份完成失败: backupId={}", backupId, e);
        }
    }

    @Override
    public BackupProgressVO getBackupProgress(Long backupId) {
        if (backupId == null) {
            return null;
        }

        try {
            String progressKey = KEY_BACKUP_PROGRESS + backupId;
            String progressJson = redisTemplate.opsForValue().get(progressKey);
            
            if (progressJson != null) {
                BackupProgressVO progress = objectMapper.readValue(progressJson, BackupProgressVO.class);
                
                // 更新已耗时（如果还在进行中）
                if (progress.getCompleted() == null || !progress.getCompleted()) {
                    if (progress.getStartTimestamp() != null) {
                        long elapsedMs = System.currentTimeMillis() - progress.getStartTimestamp();
                        progress.setElapsedMs(elapsedMs);
                        progress.setElapsedFormatted(BackupProgressVO.formatDuration(elapsedMs));
                    }
                }
                
                return progress;
            }
        } catch (Exception e) {
            log.error("获取备份进度失败: backupId={}", backupId, e);
        }
        
        return null;
    }

    @Override
    public boolean isBackupInProgress() {
        return getCurrentBackupId() != null;
    }

    @Override
    public Long getCurrentBackupId() {
        String currentBackupId = redisTemplate.opsForValue().get(KEY_CURRENT_BACKUP);
        if (currentBackupId != null) {
            try {
                return Long.parseLong(currentBackupId);
            } catch (NumberFormatException e) {
                log.warn("解析当前备份ID失败: {}", currentBackupId);
            }
        }
        return null;
    }

    // ==================== 恢复进度管理 ====================

    @Override
    public void recordRestoreStart(Long restoreId) {
        if (restoreId == null) {
            return;
        }

        try {
            BackupProgressVO progress = BackupProgressVO.builder()
                    .backupId(restoreId)
                    .progress(0)
                    .stage("验证中")
                    .stageCode(0)
                    .startTimestamp(System.currentTimeMillis())
                    .elapsedMs(0L)
                    .elapsedFormatted("0秒")
                    .completed(false)
                    .success(false)
                    .build();

            String progressJson = objectMapper.writeValueAsString(progress);
            String progressKey = KEY_RESTORE_PROGRESS + restoreId;
            redisTemplate.opsForValue().set(progressKey, progressJson, PROGRESS_EXPIRE_HOURS, TimeUnit.HOURS);
            redisTemplate.opsForValue().set(KEY_CURRENT_RESTORE, String.valueOf(restoreId), PROGRESS_EXPIRE_HOURS, TimeUnit.HOURS);

            log.info("恢复开始记录: restoreId={}", restoreId);
        } catch (JsonProcessingException e) {
            log.error("序列化恢复进度失败: restoreId={}", restoreId, e);
        }
    }

    @Override
    public void updateRestoreProgress(Long restoreId, int progress, String stage) {
        if (restoreId == null) {
            return;
        }

        try {
            String progressKey = KEY_RESTORE_PROGRESS + restoreId;
            String existingJson = redisTemplate.opsForValue().get(progressKey);

            BackupProgressVO progressVO;
            if (existingJson != null) {
                progressVO = objectMapper.readValue(existingJson, BackupProgressVO.class);
            } else {
                progressVO = BackupProgressVO.builder()
                        .backupId(restoreId)
                        .startTimestamp(System.currentTimeMillis())
                        .completed(false)
                        .success(false)
                        .build();
            }

            progressVO.setProgress(Math.min(100, Math.max(0, progress)));
            progressVO.setStage(stage);

            if (progressVO.getStartTimestamp() != null) {
                long elapsedMs = System.currentTimeMillis() - progressVO.getStartTimestamp();
                progressVO.setElapsedMs(elapsedMs);
                progressVO.setElapsedFormatted(BackupProgressVO.formatDuration(elapsedMs));
            }

            String progressJson = objectMapper.writeValueAsString(progressVO);
            redisTemplate.opsForValue().set(progressKey, progressJson, PROGRESS_EXPIRE_HOURS, TimeUnit.HOURS);

            log.debug("恢复进度更新: restoreId={}, progress={}%, stage={}", restoreId, progress, stage);
        } catch (Exception e) {
            log.error("更新恢复进度失败: restoreId={}", restoreId, e);
        }
    }

    @Override
    public void recordRestoreComplete(Long restoreId, boolean success, String errorMessage) {
        if (restoreId == null) {
            return;
        }

        try {
            String progressKey = KEY_RESTORE_PROGRESS + restoreId;
            String existingJson = redisTemplate.opsForValue().get(progressKey);

            long elapsedMs = 0;
            if (existingJson != null) {
                BackupProgressVO existingProgress = objectMapper.readValue(existingJson, BackupProgressVO.class);
                if (existingProgress.getStartTimestamp() != null) {
                    elapsedMs = System.currentTimeMillis() - existingProgress.getStartTimestamp();
                }
            }

            BackupProgressVO completedProgress = BackupProgressVO.builder()
                    .backupId(restoreId)
                    .progress(100)
                    .stage(success ? "完成" : "失败")
                    .elapsedMs(elapsedMs)
                    .elapsedFormatted(BackupProgressVO.formatDuration(elapsedMs))
                    .errorMessage(errorMessage)
                    .completed(true)
                    .success(success)
                    .build();

            String progressJson = objectMapper.writeValueAsString(completedProgress);
            redisTemplate.opsForValue().set(progressKey, progressJson, PROGRESS_EXPIRE_HOURS, TimeUnit.HOURS);

            String currentRestoreId = redisTemplate.opsForValue().get(KEY_CURRENT_RESTORE);
            if (currentRestoreId != null && currentRestoreId.equals(String.valueOf(restoreId))) {
                redisTemplate.delete(KEY_CURRENT_RESTORE);
            }

            log.info("恢复完成记录: restoreId={}, success={}, elapsedMs={}", restoreId, success, elapsedMs);

            if (!success) {
                sendAlert(AlertType.RESTORE_FAILED, "数据库恢复失败: " + (errorMessage != null ? errorMessage : "未知错误"));
            }
        } catch (Exception e) {
            log.error("记录恢复完成失败: restoreId={}", restoreId, e);
        }
    }

    @Override
    public boolean isRestoreInProgress() {
        return getCurrentRestoreId() != null;
    }

    @Override
    public Long getCurrentRestoreId() {
        String currentRestoreId = redisTemplate.opsForValue().get(KEY_CURRENT_RESTORE);
        if (currentRestoreId != null) {
            try {
                return Long.parseLong(currentRestoreId);
            } catch (NumberFormatException e) {
                log.warn("解析当前恢复ID失败: {}", currentRestoreId);
            }
        }
        return null;
    }

    // ==================== 健康检查 ====================

    @Override
    public BackupHealthStatus checkHealth() {
        return getHealthCheckResult().getStatus();
    }

    @Override
    public HealthCheckResult getHealthCheckResult() {
        List<String> alerts = new ArrayList<>();
        BackupHealthStatus status = BackupHealthStatus.HEALTHY;
        String message = "备份系统运行正常";

        // 检查最后备份时间
        int hoursSinceLastBackup = getHoursSinceLastBackup();
        int alertThresholdHours = backupConfigService.getAlertThresholdHours();

        if (hoursSinceLastBackup < 0) {
            status = BackupHealthStatus.CRITICAL;
            message = "没有成功的备份记录";
            alerts.add("没有成功的备份记录");
        } else if (hoursSinceLastBackup > alertThresholdHours) {
            status = BackupHealthStatus.CRITICAL;
            message = String.format("距离上次成功备份已超过%d小时", hoursSinceLastBackup);
            alerts.add(message);
        } else if (hoursSinceLastBackup > alertThresholdHours / 2) {
            if (status == BackupHealthStatus.HEALTHY) {
                status = BackupHealthStatus.WARNING;
                message = String.format("距离上次成功备份已%d小时，接近告警阈值", hoursSinceLastBackup);
            }
            alerts.add(String.format("距离上次成功备份已%d小时", hoursSinceLastBackup));
        }

        // 检查存储使用情况
        double storageUsagePercent = 0;
        String storageStatus = "NORMAL";
        try {
            BackupStorageService.StorageUsage storageUsage = backupStorageService.getStorageUsage();
            storageUsagePercent = storageUsage.getUsagePercent();
            long usedGb = storageUsage.getUsedBytes() / (1024 * 1024 * 1024);
            int warningThresholdGb = backupConfigService.getStorageWarningThresholdGb();

            if (usedGb >= warningThresholdGb) {
                storageStatus = "CRITICAL";
                if (status != BackupHealthStatus.CRITICAL) {
                    status = BackupHealthStatus.CRITICAL;
                    message = "备份存储空间已超过告警阈值";
                }
                alerts.add(String.format("存储使用量已达%dGB，超过阈值%dGB", usedGb, warningThresholdGb));
            } else if (usedGb >= warningThresholdGb * 0.8) {
                storageStatus = "WARNING";
                if (status == BackupHealthStatus.HEALTHY) {
                    status = BackupHealthStatus.WARNING;
                    message = "备份存储空间接近告警阈值";
                }
                alerts.add(String.format("存储使用量已达%dGB，接近阈值%dGB", usedGb, warningThresholdGb));
            }
        } catch (Exception e) {
            log.error("获取存储使用情况失败", e);
            alerts.add("无法获取存储使用情况");
        }

        // 检查是否有进行中的操作
        boolean hasBackupInProgress = isBackupInProgress();
        boolean hasRestoreInProgress = isRestoreInProgress();

        Long lastBackupTimestamp = getLastSuccessfulBackupTimestamp();
        final BackupHealthStatus finalStatus = status;
        final String finalMessage = message;
        final double finalStorageUsagePercent = storageUsagePercent;
        final String finalStorageStatus = storageStatus;

        return new HealthCheckResult() {
            @Override
            public BackupHealthStatus getStatus() {
                return finalStatus;
            }

            @Override
            public String getMessage() {
                return finalMessage;
            }

            @Override
            public Long getLastBackupTimestamp() {
                return lastBackupTimestamp;
            }

            @Override
            public int getHoursSinceLastBackup() {
                return hoursSinceLastBackup;
            }

            @Override
            public double getStorageUsagePercent() {
                return finalStorageUsagePercent;
            }

            @Override
            public String getStorageStatus() {
                return finalStorageStatus;
            }

            @Override
            public boolean hasBackupInProgress() {
                return hasBackupInProgress;
            }

            @Override
            public boolean hasRestoreInProgress() {
                return hasRestoreInProgress;
            }

            @Override
            public List<String> getAlerts() {
                return alerts;
            }
        };
    }

    @Override
    public void performHealthCheck() {
        log.debug("执行备份健康检查...");

        HealthCheckResult result = getHealthCheckResult();

        if (result.getStatus() == BackupHealthStatus.CRITICAL) {
            // 检查是否需要发送无备份告警
            if (shouldAlertNoRecentBackup()) {
                sendAlert(AlertType.NO_RECENT_BACKUP, result.getMessage());
            }

            // 检查是否需要发送存储告警
            if (shouldAlertStorageWarning()) {
                sendAlert(AlertType.STORAGE_CRITICAL, "备份存储空间已超过告警阈值");
            }
        } else if (result.getStatus() == BackupHealthStatus.WARNING) {
            if (shouldAlertStorageWarning()) {
                sendAlert(AlertType.STORAGE_WARNING, "备份存储空间接近告警阈值");
            }
        }

        log.info("备份健康检查完成: status={}, message={}", result.getStatus(), result.getMessage());
    }

    // ==================== 告警管理 ====================

    @Override
    public void sendAlert(AlertType alertType, String message) {
        sendAlert(alertType, message, null);
    }

    @Override
    public void sendAlert(AlertType alertType, String message, String details) {
        // 检查冷却期
        String cooldownKey = KEY_ALERT_COOLDOWN + alertType.name();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            log.debug("告警在冷却期内，跳过发送: type={}", alertType);
            return;
        }

        // 记录告警日志
        log.warn("【备份告警】类型: {}, 消息: {}, 详情: {}", alertType.getName(), message, details);

        // 存储告警历史
        try {
            String alertKey = KEY_ALERT_HISTORY + System.currentTimeMillis();
            String alertData = String.format(
                    "{\"type\":\"%s\",\"code\":%d,\"message\":\"%s\",\"details\":\"%s\",\"time\":%d}",
                    alertType.name(), alertType.getCode(), message, 
                    details != null ? details : "", System.currentTimeMillis());
            redisTemplate.opsForValue().set(alertKey, alertData, 7, TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("存储告警历史失败", e);
        }

        // 设置冷却期
        redisTemplate.opsForValue().set(cooldownKey, "1", ALERT_COOLDOWN_MINUTES, TimeUnit.MINUTES);

        // TODO: 集成实际的通知渠道（如：邮件、短信、企业微信等）
        // 这里可以调用 PushService 或其他推送服务发送通知给管理员
    }

    @Override
    public boolean shouldAlertNoRecentBackup() {
        String cooldownKey = KEY_ALERT_COOLDOWN + AlertType.NO_RECENT_BACKUP.name();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            return false;
        }

        int hoursSinceLastBackup = getHoursSinceLastBackup();
        int alertThresholdHours = backupConfigService.getAlertThresholdHours();

        return hoursSinceLastBackup < 0 || hoursSinceLastBackup > alertThresholdHours;
    }

    @Override
    public boolean shouldAlertStorageWarning() {
        String cooldownKey = KEY_ALERT_COOLDOWN + AlertType.STORAGE_WARNING.name();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            return false;
        }

        try {
            BackupStorageService.StorageUsage storageUsage = backupStorageService.getStorageUsage();
            long usedGb = storageUsage.getUsedBytes() / (1024 * 1024 * 1024);
            int warningThresholdGb = backupConfigService.getStorageWarningThresholdGb();

            return usedGb >= warningThresholdGb * 0.8;
        } catch (Exception e) {
            log.error("检查存储告警条件失败", e);
            return false;
        }
    }

    @Override
    public boolean shouldAlertStorageCritical() {
        String cooldownKey = KEY_ALERT_COOLDOWN + AlertType.STORAGE_CRITICAL.name();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            return false;
        }

        try {
            BackupStorageService.StorageUsage storageUsage = backupStorageService.getStorageUsage();
            long usedGb = storageUsage.getUsedBytes() / (1024 * 1024 * 1024);
            int warningThresholdGb = backupConfigService.getStorageWarningThresholdGb();

            // Critical when usage exceeds the threshold
            return usedGb >= warningThresholdGb;
        } catch (Exception e) {
            log.error("检查存储危险告警条件失败", e);
            return false;
        }
    }

    @Override
    public List<AlertRecord> getRecentAlerts(int limit) {
        List<AlertRecord> alerts = new ArrayList<>();
        try {
            // Get all alert history keys
            java.util.Set<String> keys = redisTemplate.keys(KEY_ALERT_HISTORY + "*");
            if (keys == null || keys.isEmpty()) {
                return alerts;
            }

            // Sort keys by timestamp (descending) and limit
            List<String> sortedKeys = keys.stream()
                    .sorted((k1, k2) -> {
                        // Extract timestamp from key
                        String ts1 = k1.substring(KEY_ALERT_HISTORY.length());
                        String ts2 = k2.substring(KEY_ALERT_HISTORY.length());
                        return Long.compare(Long.parseLong(ts2), Long.parseLong(ts1));
                    })
                    .limit(limit)
                    .collect(java.util.stream.Collectors.toList());

            for (String key : sortedKeys) {
                String alertJson = redisTemplate.opsForValue().get(key);
                if (alertJson != null) {
                    try {
                        // Parse the JSON manually since it's a simple format
                        com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(alertJson);
                        String typeName = node.get("type").asText();
                        String message = node.get("message").asText();
                        String details = node.has("details") ? node.get("details").asText() : null;
                        long timestamp = node.get("time").asLong();

                        AlertType alertType;
                        try {
                            alertType = AlertType.valueOf(typeName);
                        } catch (IllegalArgumentException e) {
                            continue; // Skip unknown alert types
                        }

                        final AlertType finalAlertType = alertType;
                        final String finalMessage = message;
                        final String finalDetails = details;
                        final long finalTimestamp = timestamp;

                        alerts.add(new AlertRecord() {
                            @Override
                            public AlertType getType() {
                                return finalAlertType;
                            }

                            @Override
                            public String getMessage() {
                                return finalMessage;
                            }

                            @Override
                            public String getDetails() {
                                return finalDetails;
                            }

                            @Override
                            public long getTimestamp() {
                                return finalTimestamp;
                            }
                        });
                    } catch (Exception e) {
                        log.warn("解析告警记录失败: {}", alertJson, e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取最近告警失败", e);
        }
        return alerts;
    }

    @Override
    public void clearAlertCooldown(AlertType alertType) {
        if (alertType == null) {
            return;
        }
        String cooldownKey = KEY_ALERT_COOLDOWN + alertType.name();
        redisTemplate.delete(cooldownKey);
        log.info("已清除告警冷却期: type={}", alertType);
    }

    // ==================== 统计信息 ====================

    @Override
    public Long getLastSuccessfulBackupTimestamp() {
        BackupRecord lastBackup = backupRecordMapper.selectLastSuccessBackup();
        if (lastBackup != null && lastBackup.getCreateTime() != null) {
            return lastBackup.getCreateTime()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
        }
        return null;
    }

    @Override
    public int getHoursSinceLastBackup() {
        Long lastBackupTimestamp = getLastSuccessfulBackupTimestamp();
        if (lastBackupTimestamp == null) {
            return -1;
        }

        long currentTimestamp = System.currentTimeMillis();
        long diffMs = currentTimestamp - lastBackupTimestamp;
        return (int) (diffMs / (1000 * 60 * 60));
    }
}
