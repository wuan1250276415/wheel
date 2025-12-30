package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.basebackend.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 内容审核日志表
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("content_audit_log")
public class ContentAuditLog extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 内容ID
     */
    @TableField("content_id")
    private Long contentId;

    /**
     * 提交用户ID
     */
    @TableField("submit_user_id")
    private Long submitUserId;

    /**
     * 审核状态：0-待审核，1-已通过，2-已拒绝
     */
    @TableField("audit_status")
    private Integer auditStatus;

    /**
     * 审核人ID
     */
    @TableField("auditor_id")
    private Long auditorId;

    /**
     * 审核时间
     */
    @TableField("audited_at")
    private LocalDateTime auditedAt;

    /**
     * 审核意见
     */
    @TableField("audit_comment")
    private String auditComment;

    /**
     * AI预审结果（JSON格式）
     */
    @TableField("ai_audit_result")
    private String aiAuditResult;

    /**
     * 审核类型：0-自动审核，1-人工审核
     */
    @TableField("audit_type")
    private Integer auditType;

    /**
     * 处理时长（毫秒）
     */
    @TableField("process_time")
    private Long processTime;

    /**
     * 审核来源：WEB, MOBILE, API
     */
    @TableField("audit_source")
    private String auditSource;
}
