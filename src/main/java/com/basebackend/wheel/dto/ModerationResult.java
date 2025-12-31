package com.basebackend.wheel.dto;

import com.basebackend.wheel.enums.AuditDecision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 内容审核结果
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationResult {

    /**
     * 内容ID
     */
    private Long contentId;

    /**
     * 审核决策
     */
    private AuditDecision decision;

    /**
     * 综合风险评分 (0-100)
     */
    private double riskScore;

    /**
     * 风险原因列表
     */
    @Builder.Default
    private List<String> riskReasons = new ArrayList<>();

    /**
     * AI审核结果
     */
    private AiAuditResult aiResult;

    /**
     * 敏感词检测结果
     */
    private SensitiveWordResult sensitiveResult;

    /**
     * 审核优先级：1-普通 2-高 3-紧急
     */
    private Integer priority;

    /**
     * 审核阶段：AI_AUDIT, SENSITIVE_FILTER, MANUAL_REVIEW
     */
    private String auditStage;

    /**
     * 总处理时长（毫秒）
     */
    private long totalProcessTimeMs;

    /**
     * 是否被黑名单拦截
     */
    private boolean blockedByBlacklist;

    /**
     * 黑名单拦截原因
     */
    private String blacklistReason;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核日志ID
     */
    private Long auditLogId;

    /**
     * 审核阶段常量
     */
    public static final String STAGE_BLACKLIST_CHECK = "BLACKLIST_CHECK";
    public static final String STAGE_AI_AUDIT = "AI_AUDIT";
    public static final String STAGE_SENSITIVE_FILTER = "SENSITIVE_FILTER";
    public static final String STAGE_MANUAL_REVIEW = "MANUAL_REVIEW";

    /**
     * 优先级常量
     */
    public static final int PRIORITY_NORMAL = 1;
    public static final int PRIORITY_HIGH = 2;
    public static final int PRIORITY_URGENT = 3;

    /**
     * 创建自动通过结果
     */
    public static ModerationResult autoPass(Long contentId, double riskScore, 
                                             AiAuditResult aiResult, 
                                             SensitiveWordResult sensitiveResult,
                                             long processTimeMs) {
        return ModerationResult.builder()
                .contentId(contentId)
                .decision(AuditDecision.PASS)
                .riskScore(riskScore)
                .riskReasons(new ArrayList<>())
                .aiResult(aiResult)
                .sensitiveResult(sensitiveResult)
                .priority(PRIORITY_NORMAL)
                .auditStage(STAGE_AI_AUDIT)
                .totalProcessTimeMs(processTimeMs)
                .blockedByBlacklist(false)
                .auditTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建需人工复审结果
     */
    public static ModerationResult pendingReview(Long contentId, double riskScore,
                                                  List<String> riskReasons,
                                                  AiAuditResult aiResult,
                                                  SensitiveWordResult sensitiveResult,
                                                  int priority,
                                                  long processTimeMs) {
        return ModerationResult.builder()
                .contentId(contentId)
                .decision(AuditDecision.REVIEW)
                .riskScore(riskScore)
                .riskReasons(riskReasons != null ? riskReasons : new ArrayList<>())
                .aiResult(aiResult)
                .sensitiveResult(sensitiveResult)
                .priority(priority)
                .auditStage(STAGE_MANUAL_REVIEW)
                .totalProcessTimeMs(processTimeMs)
                .blockedByBlacklist(false)
                .auditTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建自动拒绝结果
     */
    public static ModerationResult autoReject(Long contentId, double riskScore,
                                               List<String> riskReasons,
                                               AiAuditResult aiResult,
                                               SensitiveWordResult sensitiveResult,
                                               long processTimeMs) {
        return ModerationResult.builder()
                .contentId(contentId)
                .decision(AuditDecision.REJECT)
                .riskScore(riskScore)
                .riskReasons(riskReasons != null ? riskReasons : new ArrayList<>())
                .aiResult(aiResult)
                .sensitiveResult(sensitiveResult)
                .priority(PRIORITY_NORMAL)
                .auditStage(STAGE_AI_AUDIT)
                .totalProcessTimeMs(processTimeMs)
                .blockedByBlacklist(false)
                .auditTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建黑名单拦截结果
     */
    public static ModerationResult blockedByBlacklist(Long contentId, String reason) {
        return ModerationResult.builder()
                .contentId(contentId)
                .decision(AuditDecision.REJECT)
                .riskScore(100.0)
                .riskReasons(List.of("用户已被封禁: " + reason))
                .priority(PRIORITY_NORMAL)
                .auditStage(STAGE_BLACKLIST_CHECK)
                .totalProcessTimeMs(0)
                .blockedByBlacklist(true)
                .blacklistReason(reason)
                .auditTime(LocalDateTime.now())
                .build();
    }

    /**
     * 添加风险原因
     */
    public void addRiskReason(String reason) {
        if (this.riskReasons == null) {
            this.riskReasons = new ArrayList<>();
        }
        this.riskReasons.add(reason);
    }

    /**
     * 判断是否通过
     */
    public boolean isPassed() {
        return decision == AuditDecision.PASS;
    }

    /**
     * 判断是否需要人工复审
     */
    public boolean needsManualReview() {
        return decision == AuditDecision.REVIEW;
    }

    /**
     * 判断是否被拒绝
     */
    public boolean isRejected() {
        return decision == AuditDecision.REJECT;
    }
}
