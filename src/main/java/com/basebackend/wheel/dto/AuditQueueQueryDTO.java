package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审核队列查询参数DTO
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
public class AuditQueueQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 内容类型（分类ID）
     */
    private Long categoryId;

    /**
     * 审核优先级：1-普通，2-VIP，3-SVIP
     */
    private Integer auditPriority;

    /**
     * 风险等级：1-低，2-中，3-高
     */
    private Integer riskLevel;

    /**
     * 提交开始时间
     */
    private LocalDateTime startTime;

    /**
     * 提交结束时间
     */
    private LocalDateTime endTime;

    /**
     * 提交用户ID
     */
    private Long submitUserId;

    /**
     * 关键词搜索
     */
    private String keyword;

    /**
     * 页码，默认1
     */
    private Integer pageNum = 1;

    /**
     * 每页大小，默认20
     */
    private Integer pageSize = 20;
}
