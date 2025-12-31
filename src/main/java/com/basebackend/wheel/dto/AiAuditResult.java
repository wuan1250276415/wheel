package com.basebackend.wheel.dto;

import com.basebackend.wheel.enums.AuditDecision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * AI审核结果
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAuditResult {

    /**
     * 是否通过
     */
    private boolean pass;

    /**
     * 审核决策
     */
    private AuditDecision decision;

    /**
     * 风险评分 (0-100)
     */
    private double riskScore;

    /**
     * 风险详情列表
     */
    @Builder.Default
    private List<RiskDetail> risks = new ArrayList<>();

    /**
     * 服务提供商
     * 如：ALIYUN（阿里云）、TENCENT（腾讯云）、LOCAL_FALLBACK（本地降级）
     */
    private String provider;

    /**
     * 审核耗时（毫秒）
     */
    private long auditTimeMs;

    /**
     * 请求ID（用于追踪）
     */
    private String requestId;

    /**
     * 错误信息（如果有）
     */
    private String errorMessage;

    /**
     * 是否为降级结果
     */
    private boolean fallback;

    /**
     * 服务提供商常量
     */
    public static final String PROVIDER_ALIYUN = "ALIYUN";
    public static final String PROVIDER_TENCENT = "TENCENT";
    public static final String PROVIDER_LOCAL_FALLBACK = "LOCAL_FALLBACK";

    /**
     * 创建通过结果
     */
    public static AiAuditResult pass(String provider, long auditTimeMs) {
        return AiAuditResult.builder()
                .pass(true)
                .decision(AuditDecision.PASS)
                .riskScore(0.0)
                .risks(new ArrayList<>())
                .provider(provider)
                .auditTimeMs(auditTimeMs)
                .fallback(false)
                .build();
    }

    /**
     * 创建需人工复审结果
     */
    public static AiAuditResult review(String provider, double riskScore, List<RiskDetail> risks, long auditTimeMs) {
        return AiAuditResult.builder()
                .pass(false)
                .decision(AuditDecision.REVIEW)
                .riskScore(riskScore)
                .risks(risks != null ? risks : new ArrayList<>())
                .provider(provider)
                .auditTimeMs(auditTimeMs)
                .fallback(false)
                .build();
    }

    /**
     * 创建拒绝结果
     */
    public static AiAuditResult reject(String provider, double riskScore, List<RiskDetail> risks, long auditTimeMs) {
        return AiAuditResult.builder()
                .pass(false)
                .decision(AuditDecision.REJECT)
                .riskScore(riskScore)
                .risks(risks != null ? risks : new ArrayList<>())
                .provider(provider)
                .auditTimeMs(auditTimeMs)
                .fallback(false)
                .build();
    }

    /**
     * 创建降级结果
     */
    public static AiAuditResult fallback(AuditDecision decision, double riskScore, List<RiskDetail> risks, long auditTimeMs, String errorMessage) {
        return AiAuditResult.builder()
                .pass(decision == AuditDecision.PASS)
                .decision(decision)
                .riskScore(riskScore)
                .risks(risks != null ? risks : new ArrayList<>())
                .provider(PROVIDER_LOCAL_FALLBACK)
                .auditTimeMs(auditTimeMs)
                .fallback(true)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 创建错误结果
     */
    public static AiAuditResult error(String provider, String errorMessage, long auditTimeMs) {
        return AiAuditResult.builder()
                .pass(false)
                .decision(AuditDecision.REVIEW)
                .riskScore(50.0)
                .risks(new ArrayList<>())
                .provider(provider)
                .auditTimeMs(auditTimeMs)
                .fallback(false)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 添加风险详情
     */
    public void addRisk(RiskDetail risk) {
        if (this.risks == null) {
            this.risks = new ArrayList<>();
        }
        this.risks.add(risk);
    }

    /**
     * 获取最高风险等级
     */
    public int getMaxRiskLevel() {
        if (risks == null || risks.isEmpty()) {
            return 0;
        }
        return risks.stream()
                .filter(r -> r.getRiskLevel() != null)
                .mapToInt(RiskDetail::getRiskLevel)
                .max()
                .orElse(0);
    }

    /**
     * 获取主要风险类型
     */
    public String getPrimaryRiskType() {
        if (risks == null || risks.isEmpty()) {
            return null;
        }
        return risks.stream()
                .max((r1, r2) -> Double.compare(r1.getConfidence(), r2.getConfidence()))
                .map(RiskDetail::getRiskType)
                .orElse(null);
    }

    /**
     * 检查是否包含特定风险类型
     */
    public boolean hasRiskType(String riskType) {
        if (risks == null || risks.isEmpty()) {
            return false;
        }
        return risks.stream().anyMatch(r -> riskType.equals(r.getRiskType()));
    }
}
