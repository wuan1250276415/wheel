package com.basebackend.wheel.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 内容举报查询参数DTO
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
public class ContentReportQueryDTO {

    /**
     * 处理状态：0-待处理，1-已处理，2-已忽略
     */
    private Integer handleStatus;

    /**
     * 举报类型：0-内容 1-用户 2-评论
     */
    private Integer reportType;

    /**
     * 举报原因
     */
    private Integer reportReason;

    /**
     * 优先级：1-普通 2-高 3-紧急
     */
    private Integer priority;

    /**
     * 内容ID
     */
    private Long contentId;

    /**
     * 举报人ID
     */
    private Long reporterId;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 页码，默认1
     */
    private Integer pageNum = 1;

    /**
     * 每页大小，默认10
     */
    private Integer pageSize = 10;
}
