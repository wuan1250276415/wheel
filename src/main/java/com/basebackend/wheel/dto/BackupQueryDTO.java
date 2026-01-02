package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 备份查询DTO
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Data
public class BackupQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 备份类型: 1-每日 2-每周 3-手动
     */
    private Integer backupType;

    /**
     * 状态: 0-进行中 1-成功 2-失败
     */
    private Integer status;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 关键词搜索（文件名或描述）
     */
    private String keyword;
}
