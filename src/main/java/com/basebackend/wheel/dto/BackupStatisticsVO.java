package com.basebackend.wheel.dto;

import com.basebackend.wheel.enums.BackupHealthStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 备份统计VO
 * 用于展示备份系统的统计信息和健康状态
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupStatisticsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 最后一次备份时间
     */
    private LocalDateTime lastBackupTime;

    /**
     * 最后一次备份大小（字节）
     */
    private Long lastBackupSize;

    /**
     * 最后一次备份大小（格式化显示）
     */
    private String lastBackupSizeFormatted;

    /**
     * 总备份数量
     */
    private Integer totalBackupCount;

    /**
     * 成功备份数量
     */
    private Integer successCount;

    /**
     * 失败备份数量
     */
    private Integer failureCount;

    /**
     * 成功率（0-100）
     */
    private Double successRate;

    /**
     * 成功率（格式化显示）
     */
    private String successRateFormatted;

    /**
     * 总存储使用量（字节）
     */
    private Long totalStorageUsed;

    /**
     * 总存储使用量（格式化显示）
     */
    private String totalStorageUsedFormatted;

    /**
     * 可用存储空间（字节）
     */
    private Long availableStorage;

    /**
     * 可用存储空间（格式化显示）
     */
    private String availableStorageFormatted;

    /**
     * 存储使用率（0-100）
     */
    private Double storageUsagePercent;

    /**
     * 健康状态
     */
    private String healthStatus;

    /**
     * 健康状态代码
     */
    private Integer healthStatusCode;

    /**
     * 健康状态消息
     */
    private String healthMessage;

    /**
     * 距离上次成功备份的小时数
     */
    private Integer hoursSinceLastBackup;

    /**
     * 是否有备份正在进行
     */
    private Boolean backupInProgress;

    /**
     * 是否有恢复正在进行
     */
    private Boolean restoreInProgress;

    /**
     * 今日备份数量
     */
    private Integer todayBackupCount;

    /**
     * 本周备份数量
     */
    private Integer weekBackupCount;

    /**
     * 本月备份数量
     */
    private Integer monthBackupCount;

    /**
     * 格式化文件大小
     *
     * @param bytes 字节数
     * @return 格式化字符串
     */
    public static String formatFileSize(long bytes) {
        if (bytes <= 0) {
            return "0 B";
        }
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes;
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return String.format("%.2f %s", size, units[unitIndex]);
    }

    /**
     * 计算成功率
     *
     * @param successCount 成功数量
     * @param totalCount   总数量
     * @return 成功率（0-100）
     */
    public static double calculateSuccessRate(int successCount, int totalCount) {
        if (totalCount <= 0) {
            return 0.0;
        }
        return (double) successCount / totalCount * 100;
    }

    /**
     * 创建空统计
     *
     * @return 空统计VO
     */
    public static BackupStatisticsVO empty() {
        return BackupStatisticsVO.builder()
                .totalBackupCount(0)
                .successCount(0)
                .failureCount(0)
                .successRate(0.0)
                .successRateFormatted("0.00%")
                .totalStorageUsed(0L)
                .totalStorageUsedFormatted("0 B")
                .healthStatus(BackupHealthStatus.WARNING.getName())
                .healthStatusCode(BackupHealthStatus.WARNING.getCode())
                .healthMessage("暂无备份记录")
                .backupInProgress(false)
                .restoreInProgress(false)
                .todayBackupCount(0)
                .weekBackupCount(0)
                .monthBackupCount(0)
                .build();
    }
}
