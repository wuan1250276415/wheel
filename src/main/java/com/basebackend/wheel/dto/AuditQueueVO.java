package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 审核队列项VO
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
public class AuditQueueVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 内容ID
     */
    private Long id;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 内容文本
     */
    private String contentText;

    /**
     * 提交用户ID
     */
    private Long createUserId;

    /**
     * 提交用户名称
     */
    private String createUserName;

    /**
     * 审核优先级：1-普通，2-VIP，3-SVIP
     */
    private Integer auditPriority;

    /**
     * 审核优先级名称
     */
    private String auditPriorityName;

    /**
     * 风险评分（0-100）
     */
    private Double riskScore;

    /**
     * 风险等级：1-低，2-中，3-高
     */
    private Integer riskLevel;

    /**
     * 风险等级名称
     */
    private String riskLevelName;

    /**
     * AI预审结果
     */
    private Map<String, Object> aiAuditResult;

    /**
     * 敏感词检测结果
     */
    private Map<String, Object> sensitiveWordResult;

    /**
     * 被举报次数
     */
    private Integer reportCount;

    /**
     * 提交时间
     */
    private LocalDateTime createTime;

    /**
     * 等待时长（毫秒）
     */
    private Long waitingTime;
}
