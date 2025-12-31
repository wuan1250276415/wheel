package com.basebackend.wheel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 风险详情
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskDetail {

    /**
     * 风险类型
     * 如：porn（色情）、violence（暴力）、politics（政治）、ad（广告）、abuse（辱骂）等
     */
    private String riskType;

    /**
     * 风险标签
     * 如：色情内容、暴力血腥、政治敏感、广告推广等
     */
    private String riskLabel;

    /**
     * 置信度 (0-1)
     */
    private double confidence;

    /**
     * 处理建议
     * 如：pass（通过）、review（人工复审）、block（拒绝）
     */
    private String suggestion;

    /**
     * 风险等级 (1-低, 2-中, 3-高)
     */
    private Integer riskLevel;

    /**
     * 命中的关键词或内容片段
     */
    private String hitContent;

    /**
     * 风险类型常量
     */
    public static final String RISK_TYPE_PORN = "porn";
    public static final String RISK_TYPE_VIOLENCE = "violence";
    public static final String RISK_TYPE_POLITICS = "politics";
    public static final String RISK_TYPE_AD = "ad";
    public static final String RISK_TYPE_ABUSE = "abuse";
    public static final String RISK_TYPE_TERRORISM = "terrorism";
    public static final String RISK_TYPE_CONTRABAND = "contraband";
    public static final String RISK_TYPE_SPAM = "spam";
    public static final String RISK_TYPE_OTHER = "other";

    /**
     * 处理建议常量
     */
    public static final String SUGGESTION_PASS = "pass";
    public static final String SUGGESTION_REVIEW = "review";
    public static final String SUGGESTION_BLOCK = "block";

    /**
     * 创建色情风险详情
     */
    public static RiskDetail pornRisk(double confidence, String hitContent) {
        return RiskDetail.builder()
                .riskType(RISK_TYPE_PORN)
                .riskLabel("色情内容")
                .confidence(confidence)
                .suggestion(confidence > 0.8 ? SUGGESTION_BLOCK : SUGGESTION_REVIEW)
                .riskLevel(confidence > 0.8 ? 3 : 2)
                .hitContent(hitContent)
                .build();
    }

    /**
     * 创建暴力风险详情
     */
    public static RiskDetail violenceRisk(double confidence, String hitContent) {
        return RiskDetail.builder()
                .riskType(RISK_TYPE_VIOLENCE)
                .riskLabel("暴力血腥")
                .confidence(confidence)
                .suggestion(confidence > 0.8 ? SUGGESTION_BLOCK : SUGGESTION_REVIEW)
                .riskLevel(confidence > 0.8 ? 3 : 2)
                .hitContent(hitContent)
                .build();
    }

    /**
     * 创建政治风险详情
     */
    public static RiskDetail politicsRisk(double confidence, String hitContent) {
        return RiskDetail.builder()
                .riskType(RISK_TYPE_POLITICS)
                .riskLabel("政治敏感")
                .confidence(confidence)
                .suggestion(confidence > 0.7 ? SUGGESTION_BLOCK : SUGGESTION_REVIEW)
                .riskLevel(confidence > 0.7 ? 3 : 2)
                .hitContent(hitContent)
                .build();
    }

    /**
     * 创建广告风险详情
     */
    public static RiskDetail adRisk(double confidence, String hitContent) {
        return RiskDetail.builder()
                .riskType(RISK_TYPE_AD)
                .riskLabel("广告推广")
                .confidence(confidence)
                .suggestion(confidence > 0.9 ? SUGGESTION_BLOCK : SUGGESTION_REVIEW)
                .riskLevel(confidence > 0.9 ? 3 : 1)
                .hitContent(hitContent)
                .build();
    }
}
