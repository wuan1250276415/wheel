package com.basebackend.wheel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 恢复结果 VO
 * 用于返回恢复操作的结果信息
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestoreResultVO {

    /**
     * 恢复记录ID
     */
    private Long restoreId;

    /**
     * 源备份ID（从本地备份恢复时）
     */
    private Long backupId;

    /**
     * 恢复前备份ID
     */
    private Long preRestoreBackupId;

    /**
     * 来源类型: 1-本地备份 2-上传文件
     */
    private Integer sourceType;

    /**
     * 来源类型名称
     */
    private String sourceTypeName;

    /**
     * 源文件名
     */
    private String sourceFilename;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 状态: 0-进行中 1-成功 2-失败 3-已回滚
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 恢复耗时（毫秒）
     */
    private Long durationMs;

    /**
     * 恢复耗时（格式化显示）
     */
    private String durationFormatted;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 恢复开始时间
     */
    private LocalDateTime startTime;

    /**
     * 恢复完成时间
     */
    private LocalDateTime endTime;

    /**
     * 提示消息
     */
    private String message;

    // ==================== 来源类型常量 ====================

    public static final int SOURCE_TYPE_LOCAL = 1;
    public static final int SOURCE_TYPE_UPLOAD = 2;

    // ==================== 状态常量 ====================

    public static final int STATUS_IN_PROGRESS = 0;
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_FAILED = 2;
    public static final int STATUS_ROLLED_BACK = 3;

    /**
     * 创建成功结果
     */
    public static RestoreResultVO success(Long restoreId, Long backupId, Long preRestoreBackupId,
                                          int sourceType, String sourceFilename, long durationMs) {
        return RestoreResultVO.builder()
                .restoreId(restoreId)
                .backupId(backupId)
                .preRestoreBackupId(preRestoreBackupId)
                .sourceType(sourceType)
                .sourceTypeName(getSourceTypeName(sourceType))
                .sourceFilename(sourceFilename)
                .success(true)
                .status(STATUS_SUCCESS)
                .statusName("成功")
                .durationMs(durationMs)
                .durationFormatted(formatDuration(durationMs))
                .message("数据库恢复成功")
                .build();
    }

    /**
     * 创建失败结果
     */
    public static RestoreResultVO failed(Long restoreId, String errorMessage) {
        return RestoreResultVO.builder()
                .restoreId(restoreId)
                .success(false)
                .status(STATUS_FAILED)
                .statusName("失败")
                .errorMessage(errorMessage)
                .message("数据库恢复失败: " + errorMessage)
                .build();
    }

    /**
     * 创建进行中结果
     */
    public static RestoreResultVO inProgress(Long restoreId, Long backupId, int sourceType, String sourceFilename) {
        return RestoreResultVO.builder()
                .restoreId(restoreId)
                .backupId(backupId)
                .sourceType(sourceType)
                .sourceTypeName(getSourceTypeName(sourceType))
                .sourceFilename(sourceFilename)
                .success(false)
                .status(STATUS_IN_PROGRESS)
                .statusName("进行中")
                .message("数据库恢复进行中")
                .build();
    }

    /**
     * 创建已回滚结果
     */
    public static RestoreResultVO rolledBack(Long restoreId, Long preRestoreBackupId, String errorMessage) {
        return RestoreResultVO.builder()
                .restoreId(restoreId)
                .preRestoreBackupId(preRestoreBackupId)
                .success(false)
                .status(STATUS_ROLLED_BACK)
                .statusName("已回滚")
                .errorMessage(errorMessage)
                .message("恢复失败，已回滚到恢复前状态")
                .build();
    }

    /**
     * 获取来源类型名称
     */
    public static String getSourceTypeName(int sourceType) {
        switch (sourceType) {
            case SOURCE_TYPE_LOCAL:
                return "本地备份";
            case SOURCE_TYPE_UPLOAD:
                return "上传文件";
            default:
                return "未知";
        }
    }

    /**
     * 格式化时长
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
}
