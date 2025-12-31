package com.basebackend.wheel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basebackend.wheel.dto.*;
import com.basebackend.wheel.entity.ContentAuditLog;
import com.basebackend.wheel.enums.AuditDecision;
import com.basebackend.wheel.mapper.ContentAuditLogMapper;
import com.basebackend.wheel.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 内容审核服务实现
 * 
 * 实现三级审核流程：
 * 1. 黑名单检查 - 检查用户/IP/设备是否在黑名单
 * 2. AI预审 - 调用AI服务进行内容审核
 * 3. 敏感词过滤 - 使用DFA算法检测敏感词
 * 4. 决策判定 - 根据风险评分决定通过/复审/拒绝
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentModerationServiceImpl implements ContentModerationService {

    private final BlacklistService blacklistService;
    private final AiContentAuditService aiContentAuditService;
    private final SensitiveWordFilter sensitiveWordFilter;
    private final AuditConfigService auditConfigService;
    private final ContentAuditLogMapper contentAuditLogMapper;
    private final MembershipService membershipService;
    private final AuditNotificationService auditNotificationService;
    private final ObjectMapper objectMapper;

    /**
     * 审核状态常量
     */
    private static final int AUDIT_STATUS_PENDING = 0;
    private static final int AUDIT_STATUS_PASSED = 1;
    private static final int AUDIT_STATUS_REJECTED = 2;
    private static final int AUDIT_STATUS_APPEALING = 3;

    /**
     * 审核类型常量
     */
    private static final int AUDIT_TYPE_AUTO = 0;
    private static final int AUDIT_TYPE_MANUAL = 1;

    @Override
    @Transactional
    public ModerationResult moderateContent(ModerationRequest request) {
        long startTime = System.currentTimeMillis();
        
        log.info("开始内容审核: contentId={}, userId={}", request.getContentId(), request.getUserId());
        
        // 阶段1: 黑名单检查
        ModerationResult blacklistResult = checkBlacklist(request);
        if (blacklistResult != null) {
            saveAuditLog(request, blacklistResult, AUDIT_TYPE_AUTO);
            return blacklistResult;
        }
        
        // 阶段2: AI预审
        AiAuditResult aiResult = performAiAudit(request);
        
        // 阶段3: 敏感词过滤
        SensitiveWordResult sensitiveResult = performSensitiveWordFilter(request);
        
        // 阶段4: 综合决策
        ModerationResult result = makeDecision(request, aiResult, sensitiveResult, startTime);
        
        // 保存审核日志
        saveAuditLog(request, result, 
                result.getDecision() == AuditDecision.REVIEW ? AUDIT_TYPE_MANUAL : AUDIT_TYPE_AUTO);
        
        log.info("内容审核完成: contentId={}, decision={}, riskScore={}, processTime={}ms",
                request.getContentId(), result.getDecision(), result.getRiskScore(), 
                result.getTotalProcessTimeMs());
        
        return result;
    }

    /**
     * 阶段1: 黑名单检查
     */
    private ModerationResult checkBlacklist(ModerationRequest request) {
        BlacklistCheckResult blacklistResult = blacklistService.checkAll(
                request.getUserId(),
                request.getUserIp(),
                request.getDeviceId()
        );
        
        if (blacklistResult.isBlocked()) {
            log.warn("用户在黑名单中，直接拒绝: userId={}, reason={}", 
                    request.getUserId(), blacklistResult.getReason());
            return ModerationResult.blockedByBlacklist(
                    request.getContentId(), 
                    blacklistResult.getReason()
            );
        }
        
        return null;
    }

    /**
     * 阶段2: AI预审
     */
    private AiAuditResult performAiAudit(ModerationRequest request) {
        AiAuditResult combinedResult = null;
        
        // 文本审核
        if (request.hasText()) {
            AiAuditResult textResult = aiContentAuditService.auditText(request.getContentText());
            combinedResult = textResult;
        }
        
        // 图片审核
        if (request.hasImages()) {
            List<AiAuditResult> imageResults = aiContentAuditService.batchAuditImage(request.getImageUrls());
            
            // 合并图片审核结果（取最高风险）
            for (AiAuditResult imageResult : imageResults) {
                if (combinedResult == null) {
                    combinedResult = imageResult;
                } else if (imageResult.getRiskScore() > combinedResult.getRiskScore()) {
                    combinedResult = mergeAuditResults(combinedResult, imageResult);
                }
            }
        }
        
        // 如果没有内容需要审核，返回通过结果
        if (combinedResult == null) {
            combinedResult = AiAuditResult.pass(AiAuditResult.PROVIDER_LOCAL_FALLBACK, 0);
        }
        
        return combinedResult;
    }

    /**
     * 阶段3: 敏感词过滤
     */
    private SensitiveWordResult performSensitiveWordFilter(ModerationRequest request) {
        if (!request.hasText()) {
            return SensitiveWordResult.empty(0);
        }
        
        return sensitiveWordFilter.detect(request.getContentText());
    }

    /**
     * 阶段4: 综合决策
     */
    private ModerationResult makeDecision(ModerationRequest request, 
                                          AiAuditResult aiResult,
                                          SensitiveWordResult sensitiveResult,
                                          long startTime) {
        // 计算综合风险评分
        double combinedRiskScore = calculateCombinedRiskScore(aiResult, sensitiveResult);
        
        // 收集风险原因
        List<String> riskReasons = collectRiskReasons(aiResult, sensitiveResult);
        
        // 获取阈值配置
        int passThreshold = auditConfigService.getAiPassThreshold();
        int rejectThreshold = auditConfigService.getAiRejectThreshold();
        
        // 根据阈值决定审核结果
        AuditDecision decision = AuditDecision.fromRiskScore(combinedRiskScore, passThreshold, rejectThreshold);
        
        // 计算优先级
        int priority = calculatePriority(request.getUserId(), request.getContentId(), 0);
        
        long processTime = System.currentTimeMillis() - startTime;
        
        // 构建结果
        ModerationResult result;
        switch (decision) {
            case PASS:
                result = ModerationResult.autoPass(
                        request.getContentId(), combinedRiskScore, 
                        aiResult, sensitiveResult, processTime);
                break;
            case REJECT:
                result = ModerationResult.autoReject(
                        request.getContentId(), combinedRiskScore, riskReasons,
                        aiResult, sensitiveResult, processTime);
                // 发送拒绝通知
                sendAuditNotification(request.getUserId(), request.getContentId(), false, 
                        String.join("; ", riskReasons));
                break;
            case REVIEW:
            default:
                result = ModerationResult.pendingReview(
                        request.getContentId(), combinedRiskScore, riskReasons,
                        aiResult, sensitiveResult, priority, processTime);
                break;
        }
        
        return result;
    }

    /**
     * 计算综合风险评分
     */
    private double calculateCombinedRiskScore(AiAuditResult aiResult, SensitiveWordResult sensitiveResult) {
        double aiScore = aiResult != null ? aiResult.getRiskScore() : 0;
        double sensitiveScore = sensitiveResult != null ? sensitiveResult.getRiskScore() : 0;
        
        // 取两者中的较高分，并加权
        // AI审核权重0.6，敏感词权重0.4
        double combinedScore = Math.max(aiScore, sensitiveScore);
        
        // 如果两者都有风险，额外增加分数
        if (aiScore > 0 && sensitiveScore > 0) {
            combinedScore = Math.min(100, combinedScore + 10);
        }
        
        return combinedScore;
    }

    /**
     * 收集风险原因
     */
    private List<String> collectRiskReasons(AiAuditResult aiResult, SensitiveWordResult sensitiveResult) {
        List<String> reasons = new ArrayList<>();
        
        // AI审核风险原因
        if (aiResult != null && aiResult.getRisks() != null) {
            for (RiskDetail risk : aiResult.getRisks()) {
                reasons.add(String.format("[%s] %s (置信度: %.2f)", 
                        risk.getRiskType(), risk.getRiskLabel(), risk.getConfidence()));
            }
        }
        
        // 敏感词风险原因
        if (sensitiveResult != null && sensitiveResult.isHasSensitiveWord()) {
            for (MatchedWord word : sensitiveResult.getMatchedWords()) {
                reasons.add(String.format("检测到敏感词: %s (分类: %d, 等级: %d)", 
                        word.getWord(), word.getCategory(), word.getLevel()));
            }
        }
        
        return reasons;
    }

    /**
     * 合并审核结果
     */
    private AiAuditResult mergeAuditResults(AiAuditResult result1, AiAuditResult result2) {
        // 取风险分更高的结果作为基础
        AiAuditResult base = result1.getRiskScore() >= result2.getRiskScore() ? result1 : result2;
        AiAuditResult other = result1.getRiskScore() >= result2.getRiskScore() ? result2 : result1;
        
        // 合并风险详情
        if (other.getRisks() != null) {
            for (RiskDetail risk : other.getRisks()) {
                base.addRisk(risk);
            }
        }
        
        return base;
    }

    @Override
    @Transactional
    public void manualAudit(Long contentId, Long auditorId, boolean passed, String auditComment) {
        ContentAuditLog auditLog = contentAuditLogMapper.selectByContentId(contentId)
                .stream()
                .filter(log -> log.getAuditStatus() == AUDIT_STATUS_PENDING)
                .findFirst()
                .orElse(null);
        
        if (auditLog == null) {
            log.warn("未找到待审核的内容: contentId={}", contentId);
            return;
        }
        
        // 更新审核状态
        auditLog.setAuditStatus(passed ? AUDIT_STATUS_PASSED : AUDIT_STATUS_REJECTED);
        auditLog.setAuditorId(auditorId);
        auditLog.setAuditedAt(LocalDateTime.now());
        auditLog.setAuditComment(auditComment);
        auditLog.setAuditType(AUDIT_TYPE_MANUAL);
        
        // 计算处理时长
        if (auditLog.getCreateTime() != null) {
            long processTime = java.time.Duration.between(
                    auditLog.getCreateTime(), LocalDateTime.now()).toMillis();
            auditLog.setProcessTime(processTime);
        }
        
        contentAuditLogMapper.updateById(auditLog);
        
        // 发送通知
        sendAuditNotification(auditLog.getSubmitUserId(), contentId, passed, 
                passed ? null : auditComment);
        
        log.info("人工审核完成: contentId={}, auditorId={}, passed={}", contentId, auditorId, passed);
    }

    @Override
    @Transactional
    public void batchManualAudit(Long[] contentIds, Long auditorId, boolean passed, String auditComment) {
        if (contentIds == null || contentIds.length == 0) {
            return;
        }
        
        for (Long contentId : contentIds) {
            try {
                manualAudit(contentId, auditorId, passed, auditComment);
            } catch (Exception e) {
                log.error("批量审核失败: contentId={}, error={}", contentId, e.getMessage());
            }
        }
        
        log.info("批量审核完成: count={}, auditorId={}, passed={}", contentIds.length, auditorId, passed);
    }

    @Override
    public Page<ContentAuditLog> getAuditQueue(int page, int pageSize, Integer contentType, Integer priority) {
        Page<ContentAuditLog> pageParam = new Page<>(page, pageSize);
        
        LambdaQueryWrapper<ContentAuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContentAuditLog::getAuditStatus, AUDIT_STATUS_PENDING);
        
        // 按优先级降序，创建时间升序排列
        wrapper.orderByDesc(ContentAuditLog::getCreateTime);
        
        return contentAuditLogMapper.selectPage(pageParam, wrapper);
    }

    @Override
    @Transactional
    public boolean appealContent(Long contentId, Long userId, String appealReason) {
        List<ContentAuditLog> logs = contentAuditLogMapper.selectByContentId(contentId);
        
        ContentAuditLog rejectedLog = logs.stream()
                .filter(log -> log.getAuditStatus() == AUDIT_STATUS_REJECTED)
                .findFirst()
                .orElse(null);
        
        if (rejectedLog == null) {
            log.warn("未找到被拒绝的内容: contentId={}", contentId);
            return false;
        }
        
        // 检查是否是内容提交者
        if (!userId.equals(rejectedLog.getSubmitUserId())) {
            log.warn("非内容提交者不能申诉: contentId={}, userId={}", contentId, userId);
            return false;
        }
        
        // 更新为申诉状态
        rejectedLog.setAuditStatus(AUDIT_STATUS_APPEALING);
        rejectedLog.setAuditComment(rejectedLog.getAuditComment() + " | 申诉理由: " + appealReason);
        contentAuditLogMapper.updateById(rejectedLog);
        
        log.info("内容申诉提交成功: contentId={}, userId={}", contentId, userId);
        return true;
    }

    @Override
    @Transactional
    public void handleAppeal(Long contentId, Long auditorId, boolean approved, String auditComment) {
        List<ContentAuditLog> logs = contentAuditLogMapper.selectByContentId(contentId);
        
        ContentAuditLog appealingLog = logs.stream()
                .filter(log -> log.getAuditStatus() == AUDIT_STATUS_APPEALING)
                .findFirst()
                .orElse(null);
        
        if (appealingLog == null) {
            log.warn("未找到申诉中的内容: contentId={}", contentId);
            return;
        }
        
        // 更新审核状态
        appealingLog.setAuditStatus(approved ? AUDIT_STATUS_PASSED : AUDIT_STATUS_REJECTED);
        appealingLog.setAuditorId(auditorId);
        appealingLog.setAuditedAt(LocalDateTime.now());
        appealingLog.setAuditComment(appealingLog.getAuditComment() + " | 申诉处理: " + auditComment);
        appealingLog.setAuditType(AUDIT_TYPE_MANUAL);
        
        contentAuditLogMapper.updateById(appealingLog);
        
        // 发送申诉结果通知
        auditNotificationService.sendAppealResultNotification(
                appealingLog.getSubmitUserId(), 
                contentId, 
                approved, 
                auditComment
        );
        
        log.info("申诉处理完成: contentId={}, auditorId={}, approved={}", contentId, auditorId, approved);
    }

    @Override
    public ContentAuditLog getAuditStatus(Long contentId) {
        List<ContentAuditLog> logs = contentAuditLogMapper.selectByContentId(contentId);
        return logs.isEmpty() ? null : logs.get(0);
    }

    @Override
    public int calculatePriority(Long userId, Long contentId, int reportCount) {
        int priority = ModerationResult.PRIORITY_NORMAL;
        
        // 根据举报数量提升优先级
        int reportThreshold = auditConfigService.getReportPriorityThreshold();
        if (reportCount >= reportThreshold) {
            priority = ModerationResult.PRIORITY_URGENT;
        }
        
        // 根据用户会员等级提升优先级
        if (userId != null) {
            try {
                MembershipStatusVO membershipStatus = membershipService.getMembershipStatus(userId);
                if (membershipStatus != null && membershipStatus.getIsActive()) {
                    Integer tier = membershipStatus.getTier();
                    if (tier != null) {
                        if (tier >= 2) {
                            // SVIP
                            priority = Math.max(priority, auditConfigService.getSvipAuditPriority());
                        } else if (tier >= 1) {
                            // VIP
                            priority = Math.max(priority, auditConfigService.getVipAuditPriority());
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("获取用户会员状态失败: userId={}, error={}", userId, e.getMessage());
            }
        }
        
        return priority;
    }

    @Override
    public void sendAuditNotification(Long userId, Long contentId, boolean passed, String rejectReason) {
        if (userId == null) {
            return;
        }
        
        if (passed) {
            auditNotificationService.sendPassNotification(userId, contentId);
        } else {
            auditNotificationService.sendRejectNotification(userId, contentId, rejectReason);
        }
    }

    /**
     * 保存审核日志
     */
    private void saveAuditLog(ModerationRequest request, ModerationResult result, int auditType) {
        ContentAuditLog auditLog = new ContentAuditLog();
        auditLog.setContentId(request.getContentId());
        auditLog.setSubmitUserId(request.getUserId());
        auditLog.setAuditType(auditType);
        auditLog.setProcessTime(result.getTotalProcessTimeMs());
        
        // 设置审核状态
        switch (result.getDecision()) {
            case PASS:
                auditLog.setAuditStatus(AUDIT_STATUS_PASSED);
                auditLog.setAuditedAt(LocalDateTime.now());
                break;
            case REJECT:
                auditLog.setAuditStatus(AUDIT_STATUS_REJECTED);
                auditLog.setAuditedAt(LocalDateTime.now());
                auditLog.setAuditComment(String.join("; ", result.getRiskReasons()));
                break;
            case REVIEW:
            default:
                auditLog.setAuditStatus(AUDIT_STATUS_PENDING);
                break;
        }
        
        // 保存AI审核结果
        if (result.getAiResult() != null) {
            try {
                auditLog.setAiAuditResult(objectMapper.writeValueAsString(result.getAiResult()));
            } catch (JsonProcessingException e) {
                log.warn("序列化AI审核结果失败: {}", e.getMessage());
            }
        }
        
        contentAuditLogMapper.insert(auditLog);
        result.setAuditLogId(auditLog.getId());
    }
}
