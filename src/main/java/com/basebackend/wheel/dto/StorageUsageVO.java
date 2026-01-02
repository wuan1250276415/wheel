package com.basebackend.wheel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 存储使用情况 VO
 * 用于展示备份存储的使用状态
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageUsageVO {

    /**
     * 已使用空间（字节）
     */
    private Long usedBytes;

    /**
     * 已使用空间（格式化显示，如 "1.5 GB"）
     */
    private String usedFormatted;

    /**
     * 总空间（字节）
     */
    private Long totalBytes;

    /**
     * 总空间（格式化显示）
     */
    private String totalFormatted;

    /**
     * 可用空间（字节）
     */
    private Long availableBytes;

    /**
     * 可用空间（格式化显示）
     */
    private String availableFormatted;

    /**
     * 备份文件数量
     */
    private Integer fileCount;

    /**
     * 使用率（0-100）
     */
    private Double usagePercent;

    /**
     * 使用率（格式化显示，如 "45.5%"）
     */
    private String usagePercentFormatted;

    /**
     * 存储状态
     * NORMAL - 正常
     * WARNING - 警告（接近阈值）
     * CRITICAL - 危险（超过阈值）
     */
    private String status;

    /**
     * 告警阈值（GB）
     */
    private Integer warningThresholdGb;

    /**
     * 从 StorageUsage 接口创建 VO
     *
     * @param usage            存储使用情况
     * @param warningThreshold 告警阈值（GB）
     * @return StorageUsageVO
     */
    public static StorageUsageVO fromStorageUsage(
            com.basebackend.wheel.service.BackupStorageService.StorageUsage usage,
            int warningThreshold) {
        
        long usedBytes = usage.getUsedBytes();
        long totalBytes = usage.getTotalBytes();
        long availableBytes = usage.getAvailableBytes();
        double usagePercent = usage.getUsagePercent();
        
        // 计算状态
        long warningThresholdBytes = (long) warningThreshold * 1024 * 1024 * 1024;
        String status;
        if (usedBytes >= warningThresholdBytes) {
            status = "CRITICAL";
        } else if (usedBytes >= warningThresholdBytes * 0.8) {
            status = "WARNING";
        } else {
            status = "NORMAL";
        }

        return StorageUsageVO.builder()
                .usedBytes(usedBytes)
                .usedFormatted(formatBytes(usedBytes))
                .totalBytes(totalBytes)
                .totalFormatted(formatBytes(totalBytes))
                .availableBytes(availableBytes)
                .availableFormatted(formatBytes(availableBytes))
                .fileCount(usage.getFileCount())
                .usagePercent(usagePercent)
                .usagePercentFormatted(String.format("%.1f%%", usagePercent))
                .status(status)
                .warningThresholdGb(warningThreshold)
                .build();
    }

    /**
     * 格式化字节数为可读字符串
     *
     * @param bytes 字节数
     * @return 格式化字符串
     */
    private static String formatBytes(long bytes) {
        if (bytes < 0) {
            return "未知";
        }
        
        final long KB = 1024;
        final long MB = KB * 1024;
        final long GB = MB * 1024;
        final long TB = GB * 1024;

        if (bytes >= TB) {
            return String.format("%.2f TB", (double) bytes / TB);
        } else if (bytes >= GB) {
            return String.format("%.2f GB", (double) bytes / GB);
        } else if (bytes >= MB) {
            return String.format("%.2f MB", (double) bytes / MB);
        } else if (bytes >= KB) {
            return String.format("%.2f KB", (double) bytes / KB);
        } else {
            return bytes + " B";
        }
    }
}
