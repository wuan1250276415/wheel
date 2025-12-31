package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.dto.AiAuditResult;
import com.basebackend.wheel.dto.MatchedWord;
import com.basebackend.wheel.dto.RiskDetail;
import com.basebackend.wheel.dto.SensitiveWordResult;
import com.basebackend.wheel.enums.AuditDecision;
import com.basebackend.wheel.service.SensitiveWordFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AI服务降级处理器
 * 
 * 当AI审核服务调用失败时，降级为本地敏感词过滤
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiServiceFallbackHandler {

    private final SensitiveWordFilter sensitiveWordFilter;

    /**
     * 文本审核降级处理
     *
     * @param text         待审核文本
     * @param originalError 原始错误信息
     * @return 降级审核结果
     */
    public AiAuditResult fallbackTextAudit(String text, Throwable originalError) {
        long startTime = System.currentTimeMillis();
        
        log.warn("AI文本审核服务调用失败，降级为本地敏感词过滤: {}", 
                originalError != null ? originalError.getMessage() : "未知错误");
        
        // 记录降级事件
        logFallbackEvent("TEXT", text, originalError);
        
        if (text == null || text.trim().isEmpty()) {
            return createFallbackResult(AuditDecision.PASS, 0.0, new ArrayList<>(), 
                    System.currentTimeMillis() - startTime, 
                    originalError != null ? originalError.getMessage() : null);
        }
        
        try {
            // 使用本地敏感词过滤
            SensitiveWordResult sensitiveResult = sensitiveWordFilter.detect(text);
            
            // 转换为AI审核结果格式
            List<RiskDetail> risks = convertToRiskDetails(sensitiveResult);
            double riskScore = sensitiveResult.getRiskScore();
            
            // 根据敏感词检测结果决定审核决策
            AuditDecision decision;
            if (!sensitiveResult.isHasSensitiveWord()) {
                decision = AuditDecision.PASS;
            } else if (riskScore >= 80) {
                decision = AuditDecision.REJECT;
            } else {
                // 降级模式下，有敏感词但分数不高时，建议人工复审
                decision = AuditDecision.REVIEW;
            }
            
            return createFallbackResult(decision, riskScore, risks, 
                    System.currentTimeMillis() - startTime,
                    originalError != null ? originalError.getMessage() : null);
                    
        } catch (Exception e) {
            log.error("本地敏感词过滤也失败了: {}", e.getMessage(), e);
            // 如果本地过滤也失败，返回需人工复审
            return createFallbackResult(AuditDecision.REVIEW, 50.0, new ArrayList<>(),
                    System.currentTimeMillis() - startTime,
                    "AI服务和本地过滤均失败: " + e.getMessage());
        }
    }

    /**
     * 图片审核降级处理
     * 
     * 图片无法进行本地审核，直接返回需人工复审
     *
     * @param imageUrl      图片URL
     * @param originalError 原始错误信息
     * @return 降级审核结果
     */
    public AiAuditResult fallbackImageAudit(String imageUrl, Throwable originalError) {
        long startTime = System.currentTimeMillis();
        
        log.warn("AI图片审核服务调用失败，图片无法本地审核，标记为需人工复审: {}", 
                originalError != null ? originalError.getMessage() : "未知错误");
        
        // 记录降级事件
        logFallbackEvent("IMAGE", imageUrl, originalError);
        
        // 图片无法本地审核，直接返回需人工复审
        return createFallbackResult(AuditDecision.REVIEW, 50.0, new ArrayList<>(),
                System.currentTimeMillis() - startTime,
                originalError != null ? originalError.getMessage() : "图片审核服务不可用");
    }

    /**
     * 创建降级结果
     */
    private AiAuditResult createFallbackResult(AuditDecision decision, double riskScore, 
                                                List<RiskDetail> risks, long auditTimeMs, 
                                                String errorMessage) {
        return AiAuditResult.builder()
                .pass(decision == AuditDecision.PASS)
                .decision(decision)
                .riskScore(riskScore)
                .risks(risks)
                .provider(AiAuditResult.PROVIDER_LOCAL_FALLBACK)
                .auditTimeMs(auditTimeMs)
                .fallback(true)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 将敏感词检测结果转换为风险详情列表
     */
    private List<RiskDetail> convertToRiskDetails(SensitiveWordResult sensitiveResult) {
        List<RiskDetail> risks = new ArrayList<>();
        
        if (sensitiveResult == null || sensitiveResult.getMatchedWords() == null) {
            return risks;
        }
        
        for (MatchedWord matchedWord : sensitiveResult.getMatchedWords()) {
            RiskDetail risk = RiskDetail.builder()
                    .riskType(mapCategoryToRiskType(matchedWord.getCategory()))
                    .riskLabel(getCategoryLabel(matchedWord.getCategory()))
                    .confidence(calculateConfidence(matchedWord.getLevel()))
                    .suggestion(getSuggestionByLevel(matchedWord.getLevel()))
                    .riskLevel(matchedWord.getLevel())
                    .hitContent(matchedWord.getWord())
                    .build();
            risks.add(risk);
        }
        
        return risks;
    }

    /**
     * 将敏感词分类映射为风险类型
     */
    private String mapCategoryToRiskType(Integer category) {
        if (category == null) {
            return RiskDetail.RISK_TYPE_OTHER;
        }
        
        switch (category) {
            case 1: // 色情
                return RiskDetail.RISK_TYPE_PORN;
            case 2: // 暴力
                return RiskDetail.RISK_TYPE_VIOLENCE;
            case 3: // 政治
                return RiskDetail.RISK_TYPE_POLITICS;
            case 4: // 广告
                return RiskDetail.RISK_TYPE_AD;
            case 5: // 其他
            default:
                return RiskDetail.RISK_TYPE_OTHER;
        }
    }

    /**
     * 获取分类标签
     */
    private String getCategoryLabel(Integer category) {
        if (category == null) {
            return "其他";
        }
        
        switch (category) {
            case 1:
                return "色情内容";
            case 2:
                return "暴力血腥";
            case 3:
                return "政治敏感";
            case 4:
                return "广告推广";
            case 5:
            default:
                return "其他违规";
        }
    }

    /**
     * 根据风险等级计算置信度
     */
    private double calculateConfidence(Integer level) {
        if (level == null) {
            return 0.7;
        }
        
        switch (level) {
            case 1: // 低
                return 0.6;
            case 2: // 中
                return 0.8;
            case 3: // 高
                return 0.95;
            default:
                return 0.7;
        }
    }

    /**
     * 根据风险等级获取处理建议
     */
    private String getSuggestionByLevel(Integer level) {
        if (level == null) {
            return RiskDetail.SUGGESTION_REVIEW;
        }
        
        switch (level) {
            case 1: // 低
                return RiskDetail.SUGGESTION_REVIEW;
            case 2: // 中
                return RiskDetail.SUGGESTION_REVIEW;
            case 3: // 高
                return RiskDetail.SUGGESTION_BLOCK;
            default:
                return RiskDetail.SUGGESTION_REVIEW;
        }
    }

    /**
     * 记录降级事件日志
     */
    private void logFallbackEvent(String contentType, String content, Throwable error) {
        // 使用结构化日志记录降级事件
        log.info("AI审核服务降级事件 - 类型: {}, 时间: {}, 错误: {}, 内容长度: {}", 
                contentType,
                LocalDateTime.now(),
                error != null ? error.getMessage() : "未知",
                content != null ? content.length() : 0);
        
        // 这里可以扩展为写入数据库或发送告警
        // 例如：auditLogService.logFallbackEvent(contentType, content, error);
    }

    /**
     * 检查敏感词过滤器是否可用
     */
    public boolean isFallbackAvailable() {
        return sensitiveWordFilter != null && sensitiveWordFilter.isInitialized();
    }
}
