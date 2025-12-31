package com.basebackend.wheel.enums;

import lombok.Getter;

/**
 * 审核决策枚举
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Getter
public enum AuditDecision {

    /**
     * 通过 - 内容安全，可以发布
     */
    PASS(1, "通过", "内容安全，自动通过"),

    /**
     * 需人工复审 - 内容存在风险，需要人工审核
     */
    REVIEW(2, "需人工复审", "内容存在风险，需要人工审核"),

    /**
     * 拒绝 - 内容违规，直接拒绝
     */
    REJECT(3, "拒绝", "内容违规，自动拒绝");

    private final int code;
    private final String name;
    private final String description;

    AuditDecision(int code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static AuditDecision fromCode(int code) {
        for (AuditDecision decision : values()) {
            if (decision.code == code) {
                return decision;
            }
        }
        return REVIEW;
    }

    /**
     * 根据名称获取枚举
     */
    public static AuditDecision fromName(String name) {
        for (AuditDecision decision : values()) {
            if (decision.name.equals(name)) {
                return decision;
            }
        }
        return REVIEW;
    }

    /**
     * 根据风险分数和阈值决定审核结果
     *
     * @param riskScore       风险分数 (0-100)
     * @param passThreshold   通过阈值
     * @param rejectThreshold 拒绝阈值
     * @return 审核决策
     */
    public static AuditDecision fromRiskScore(double riskScore, int passThreshold, int rejectThreshold) {
        if (riskScore < passThreshold) {
            return PASS;
        } else if (riskScore >= rejectThreshold) {
            return REJECT;
        } else {
            return REVIEW;
        }
    }
}
