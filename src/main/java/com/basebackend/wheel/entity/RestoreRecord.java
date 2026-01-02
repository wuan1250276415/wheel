package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 恢复记录表
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("restore_record")
public class RestoreRecord extends BaseEntity {

    /**
     * 源备份ID
     */
    @TableField("backup_id")
    private Long backupId;

    /**
     * 恢复前备份ID
     */
    @TableField("pre_restore_backup_id")
    private Long preRestoreBackupId;

    /**
     * 来源类型: 1-本地备份 2-上传文件
     */
    @TableField("source_type")
    private Integer sourceType;

    /**
     * 源文件名
     */
    @TableField("source_filename")
    private String sourceFilename;

    /**
     * 状态: 0-进行中 1-成功 2-失败 3-已回滚
     */
    @TableField("status")
    private Integer status;

    /**
     * 恢复耗时(毫秒)
     */
    @TableField("duration_ms")
    private Long durationMs;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 来源类型常量
     */
    public static final int SOURCE_TYPE_LOCAL = 1;
    public static final int SOURCE_TYPE_UPLOAD = 2;

    /**
     * 状态常量
     */
    public static final int STATUS_IN_PROGRESS = 0;
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_FAILED = 2;
    public static final int STATUS_ROLLED_BACK = 3;

    /**
     * 获取来源类型名称
     */
    public String getSourceTypeName() {
        if (sourceType == null) {
            return "未知";
        }
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
     * 获取状态名称
     */
    public String getStatusName() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case STATUS_IN_PROGRESS:
                return "进行中";
            case STATUS_SUCCESS:
                return "成功";
            case STATUS_FAILED:
                return "失败";
            case STATUS_ROLLED_BACK:
                return "已回滚";
            default:
                return "未知";
        }
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return status != null && status == STATUS_SUCCESS;
    }

    /**
     * 判断是否失败
     */
    public boolean isFailed() {
        return status != null && status == STATUS_FAILED;
    }

    /**
     * 判断是否进行中
     */
    public boolean isInProgress() {
        return status != null && status == STATUS_IN_PROGRESS;
    }

    /**
     * 判断是否已回滚
     */
    public boolean isRolledBack() {
        return status != null && status == STATUS_ROLLED_BACK;
    }

    /**
     * 判断是否来自本地备份
     */
    public boolean isFromLocalBackup() {
        return sourceType != null && sourceType == SOURCE_TYPE_LOCAL;
    }

    /**
     * 判断是否来自上传文件
     */
    public boolean isFromUpload() {
        return sourceType != null && sourceType == SOURCE_TYPE_UPLOAD;
    }

    /**
     * 格式化耗时
     */
    public String getDurationFormatted() {
        if (durationMs == null || durationMs <= 0) {
            return "0秒";
        }
        long seconds = durationMs / 1000;
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
