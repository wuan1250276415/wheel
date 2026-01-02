package com.basebackend.wheel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 备份进度 VO
 * 用于展示备份操作的实时进度
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupProgressVO {

    /**
     * 备份记录ID
     */
    private Long backupId;

    /**
     * 进度百分比 (0-100)
     */
    private Integer progress;

    /**
     * 当前阶段
     * 可能的值: 准备中/导出中/压缩中/加密中/完成/失败
     */
    private String stage;

    /**
     * 阶段代码
     * 0-准备中 1-导出中 2-压缩中 3-加密中 4-完成 5-失败
     */
    private Integer stageCode;

    /**
     * 预计剩余时间（毫秒）
     */
    private Long estimatedRemainingMs;

    /**
     * 预计剩余时间（格式化显示）
     */
    private String estimatedRemainingFormatted;

    /**
     * 已处理数据量（字节）
     */
    private Long processedBytes;

    /**
     * 已处理数据量（格式化显示）
     */
    private String processedBytesFormatted;

    /**
     * 开始时间戳
     */
    private Long startTimestamp;

    /**
     * 已耗时（毫秒）
     */
    private Long elapsedMs;

    /**
     * 已耗时（格式化显示）
     */
    private String elapsedFormatted;

    /**
     * 错误信息（如果失败）
     */
    private String errorMessage;

    /**
     * 是否已完成
     */
    private Boolean completed;

    /**
     * 是否成功
     */
    private Boolean success;

    // ==================== 阶段常量 ====================

    public static final int STAGE_PREPARING = 0;
    public static final int STAGE_EXPORTING = 1;
    public static final int STAGE_COMPRESSING = 2;
    public static final int STAGE_ENCRYPTING = 3;
    public static final int STAGE_COMPLETED = 4;
    public static final int STAGE_FAILED = 5;

    public static final String STAGE_NAME_PREPARING = "准备中";
    public static final String STAGE_NAME_EXPORTING = "导出中";
    public static final String STAGE_NAME_COMPRESSING = "压缩中";
    public static final String STAGE_NAME_ENCRYPTING = "加密中";
    public static final String STAGE_NAME_COMPLETED = "完成";
    public static final String STAGE_NAME_FAILED = "失败";

    /**
     * 获取阶段名称
     *
     * @param stageCode 阶段代码
     * @return 阶段名称
     */
    public static String getStageName(int stageCode) {
        switch (stageCode) {
            case STAGE_PREPARING:
                return STAGE_NAME_PREPARING;
            case STAGE_EXPORTING:
                return STAGE_NAME_EXPORTING;
            case STAGE_COMPRESSING:
                return STAGE_NAME_COMPRESSING;
            case STAGE_ENCRYPTING:
                return STAGE_NAME_ENCRYPTING;
            case STAGE_COMPLETED:
                return STAGE_NAME_COMPLETED;
            case STAGE_FAILED:
                return STAGE_NAME_FAILED;
            default:
                return "未知";
        }
    }

    /**
     * 创建初始进度
     *
     * @param backupId 备份ID
     * @return 初始进度VO
     */
    public static BackupProgressVO initial(Long backupId) {
        return BackupProgressVO.builder()
                .backupId(backupId)
                .progress(0)
                .stage(STAGE_NAME_PREPARING)
                .stageCode(STAGE_PREPARING)
                .startTimestamp(System.currentTimeMillis())
                .elapsedMs(0L)
                .elapsedFormatted("0秒")
                .completed(false)
                .success(false)
                .build();
    }

    /**
     * 创建完成进度
     *
     * @param backupId 备份ID
     * @param success  是否成功
     * @param elapsedMs 耗时
     * @param errorMessage 错误信息
     * @return 完成进度VO
     */
    public static BackupProgressVO completed(Long backupId, boolean success, long elapsedMs, String errorMessage) {
        return BackupProgressVO.builder()
                .backupId(backupId)
                .progress(100)
                .stage(success ? STAGE_NAME_COMPLETED : STAGE_NAME_FAILED)
                .stageCode(success ? STAGE_COMPLETED : STAGE_FAILED)
                .estimatedRemainingMs(0L)
                .estimatedRemainingFormatted("0秒")
                .elapsedMs(elapsedMs)
                .elapsedFormatted(formatDuration(elapsedMs))
                .errorMessage(errorMessage)
                .completed(true)
                .success(success)
                .build();
    }

    /**
     * 格式化时长
     *
     * @param ms 毫秒数
     * @return 格式化字符串
     */
    public static String formatDuration(long ms) {
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

    /**
     * 格式化字节数
     *
     * @param bytes 字节数
     * @return 格式化字符串
     */
    public static String formatBytes(long bytes) {
        if (bytes < 0) {
            return "未知";
        }

        final long KB = 1024;
        final long MB = KB * 1024;
        final long GB = MB * 1024;

        if (bytes >= GB) {
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
