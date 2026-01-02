package com.basebackend.wheel.dto;

import com.basebackend.wheel.entity.BackupRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 备份记录VO（用于前端展示）
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupRecordVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 备份ID
     */
    private Long id;

    /**
     * 备份类型: 1-每日 2-每周 3-手动
     */
    private Integer backupType;

    /**
     * 备份类型名称
     */
    private String backupTypeName;

    /**
     * 备份文件名
     */
    private String filename;

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
     * 状态: 0-进行中 1-成功 2-失败
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 备份描述
     */
    private String description;

    /**
     * 备份耗时（毫秒）
     */
    private Long durationMs;

    /**
     * 备份耗时（格式化显示）
     */
    private String durationFormatted;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 创建人名称
     */
    private String createdByName;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 从实体转换为VO
     *
     * @param record 备份记录实体
     * @return 备份记录VO
     */
    public static BackupRecordVO fromEntity(BackupRecord record) {
        if (record == null) {
            return null;
        }
        return BackupRecordVO.builder()
                .id(record.getId())
                .backupType(record.getBackupType())
                .backupTypeName(record.getBackupTypeName())
                .filename(record.getFilename())
                .fileSize(record.getFileSize())
                .fileSizeFormatted(record.getFileSizeFormatted())
                .isEncrypted(record.isEncryptedFile())
                .status(record.getStatus())
                .statusName(record.getStatusName())
                .description(record.getDescription())
                .durationMs(record.getDurationMs())
                .durationFormatted(record.getDurationFormatted())
                .errorMessage(record.getErrorMessage())
                .createdBy(record.getCreateBy())
                .createdTime(record.getCreateTime())
                .updatedTime(record.getUpdateTime())
                .build();
    }
}
