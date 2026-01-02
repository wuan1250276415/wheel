package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 备份记录表
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("backup_record")
public class BackupRecord extends BaseEntity {

    /**
     * 备份类型: 1-每日 2-每周 3-手动
     */
    @TableField("backup_type")
    private Integer backupType;

    /**
     * 备份文件名
     */
    @TableField("filename")
    private String filename;

    /**
     * 文件存储路径
     */
    @TableField("file_path")
    private String filePath;

    /**
     * 文件大小(字节)
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 是否加密: 0-否 1-是
     */
    @TableField("is_encrypted")
    private Integer isEncrypted;

    /**
     * 状态: 0-进行中 1-成功 2-失败
     */
    @TableField("status")
    private Integer status;

    /**
     * 备份描述
     */
    @TableField("description")
    private String description;

    /**
     * 备份耗时(毫秒)
     */
    @TableField("duration_ms")
    private Long durationMs;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 备份类型常量
     */
    public static final int TYPE_DAILY = 1;
    public static final int TYPE_WEEKLY = 2;
    public static final int TYPE_MANUAL = 3;

    /**
     * 状态常量
     */
    public static final int STATUS_IN_PROGRESS = 0;
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_FAILED = 2;

    /**
     * 加密状态常量
     */
    public static final int ENCRYPTED_NO = 0;
    public static final int ENCRYPTED_YES = 1;

    /**
     * 获取备份类型名称
     */
    public String getBackupTypeName() {
        if (backupType == null) {
            return "未知";
        }
        switch (backupType) {
            case TYPE_DAILY:
                return "每日备份";
            case TYPE_WEEKLY:
                return "每周备份";
            case TYPE_MANUAL:
                return "手动备份";
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
            default:
                return "未知";
        }
    }

    /**
     * 判断是否加密
     */
    public boolean isEncryptedFile() {
        return isEncrypted != null && isEncrypted == ENCRYPTED_YES;
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
     * 格式化文件大小
     */
    public String getFileSizeFormatted() {
        if (fileSize == null || fileSize <= 0) {
            return "0 B";
        }
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = fileSize;
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return String.format("%.2f %s", size, units[unitIndex]);
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
