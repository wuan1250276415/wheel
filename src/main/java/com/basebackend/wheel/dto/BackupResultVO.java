package com.basebackend.wheel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 备份结果VO
 * 用于返回备份操作的结果
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupResultVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 备份记录ID
     */
    private Long backupId;

    /**
     * 备份文件名
     */
    private String filename;

    /**
     * 备份类型: 1-每日 2-每周 3-手动
     */
    private Integer backupType;

    /**
     * 备份类型名称
     */
    private String backupTypeName;

    /**
     * 状态: 0-进行中 1-成功 2-失败
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件大小（格式化显示）
     */
    private String fileSizeFormatted;

    /**
     * 是否加密
     */
    private Boolean isEncrypted;

    /**
     * 备份耗时（毫秒）
     */
    private Long durationMs;

    /**
     * 备份耗时（格式化显示）
     */
    private String durationFormatted;

    /**
     * 错误信息（如果失败）
     */
    private String errorMessage;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    private LocalDateTime endTime;

    /**
     * 操作消息
     */
    private String message;

    /**
     * 创建成功结果
     *
     * @param backupId 备份ID
     * @param filename 文件名
     * @param backupType 备份类型
     * @return 成功结果
     */
    public static BackupResultVO started(Long backupId, String filename, int backupType) {
        return BackupResultVO.builder()
                .backupId(backupId)
                .filename(filename)
                .backupType(backupType)
                .backupTypeName(getBackupTypeName(backupType))
                .status(0)
                .statusName("进行中")
                .startTime(LocalDateTime.now())
                .message("备份任务已启动")
                .build();
    }

    /**
     * 创建成功完成结果
     *
     * @param backupId 备份ID
     * @param filename 文件名
     * @param backupType 备份类型
     * @param fileSize 文件大小
     * @param durationMs 耗时
     * @param isEncrypted 是否加密
     * @return 成功结果
     */
    public static BackupResultVO success(Long backupId, String filename, int backupType,
                                         long fileSize, long durationMs, boolean isEncrypted) {
        return BackupResultVO.builder()
                .backupId(backupId)
                .filename(filename)
                .backupType(backupType)
                .backupTypeName(getBackupTypeName(backupType))
                .status(1)
                .statusName("成功")
                .fileSize(fileSize)
                .fileSizeFormatted(formatFileSize(fileSize))
                .isEncrypted(isEncrypted)
                .durationMs(durationMs)
                .durationFormatted(formatDuration(durationMs))
                .endTime(LocalDateTime.now())
                .message("备份完成")
                .build();
    }

    /**
     * 创建失败结果
     *
     * @param backupId 备份ID
     * @param errorMessage 错误信息
     * @return 失败结果
     */
    public static BackupResultVO failed(Long backupId, String errorMessage) {
        return BackupResultVO.builder()
                .backupId(backupId)
                .status(2)
                .statusName("失败")
                .errorMessage(errorMessage)
                .endTime(LocalDateTime.now())
                .message("备份失败: " + errorMessage)
                .build();
    }

    /**
     * 获取备份类型名称
     */
    private static String getBackupTypeName(int backupType) {
        switch (backupType) {
            case 1:
                return "每日备份";
            case 2:
                return "每周备份";
            case 3:
                return "手动备份";
            default:
                return "未知";
        }
    }

    /**
     * 格式化文件大小
     */
    private static String formatFileSize(long bytes) {
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
     * 格式化耗时
     */
    private static String formatDuration(long ms) {
        if (ms <= 0) {
            return "0秒";
        }
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return String.format("%d小时%d分%d秒", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%d分%d秒", minutes, seconds % 60);
        } else {
            return String.format("%d秒", seconds);
        }
    }
}
