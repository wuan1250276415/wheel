package com.basebackend.wheel.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basebackend.wheel.dto.ContentReportQueryDTO;
import com.basebackend.wheel.dto.ReportRequest;
import com.basebackend.wheel.dto.ReportResult;
import com.basebackend.wheel.entity.ContentReport;
import com.basebackend.wheel.entity.UserReportCredibility;
import com.basebackend.wheel.mapper.ContentReportMapper;
import com.basebackend.wheel.mapper.UserReportCredibilityMapper;
import com.basebackend.wheel.service.AuditConfigService;
import com.basebackend.wheel.service.ReportHandlerService;
import com.basebackend.wheel.service.ReportNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 举报处理服务实现
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportHandlerServiceImpl implements ReportHandlerService {

    private final ContentReportMapper contentReportMapper;
    private final UserReportCredibilityMapper userReportCredibilityMapper;
    private final AuditConfigService auditConfigService;
    private final ReportNotificationService reportNotificationService;

    /**
     * 低信誉阈值配置键
     */
    private static final String CONFIG_LOW_CREDIBILITY_THRESHOLD = "low_credibility_threshold";
    
    /**
     * 举报优先级提升阈值配置键
     */
    private static final String CONFIG_REPORT_PRIORITY_THRESHOLD = "report_priority_threshold";
    
    /**
     * 有效举报信誉分增加值配置键
     */
    private static final String CONFIG_CREDIBILITY_INCREASE_ON_VALID = "credibility_increase_on_valid";
    
    /**
     * 无效举报信誉分扣减值配置键
     */
    private static final String CONFIG_CREDIBILITY_DECREASE_ON_INVALID = "credibility_decrease_on_invalid";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportResult submitReport(ReportRequest request) {
        // 1. 验证举报请求
        if (!request.isValidReason()) {
            return ReportResult.invalidReason();
        }
        if (!request.isValidType()) {
            return ReportResult.fail("INVALID_TYPE", "无效的举报类型");
        }

        // 2. 检查重复举报
        if (hasReported(request.getContentId(), request.getReporterId())) {
            log.info("用户 {} 重复举报内容 {}", request.getReporterId(), request.getContentId());
            return ReportResult.duplicateReport();
        }

        // 3. 获取举报人信誉分
        int credibility = getUserReportCredibility(request.getReporterId());
        int lowCredibilityThreshold = auditConfigService.getIntConfig(CONFIG_LOW_CREDIBILITY_THRESHOLD, 30);
        
        // 4. 检查低信誉用户
        if (credibility < lowCredibilityThreshold) {
            log.info("低信誉用户 {} 尝试举报，信誉分: {}", request.getReporterId(), credibility);
            return ReportResult.lowCredibility();
        }

        // 5. 创建举报记录
        ContentReport report = new ContentReport();
        report.setContentId(request.getContentId());
        report.setReporterUserId(request.getReporterId());
        report.setReportType(request.getReportType() != null ? request.getReportType() : ContentReport.TYPE_CONTENT);
        report.setReportReason(request.getReportReason());
        report.setReportDescription(request.getDescription());
        report.setEvidenceUrls(request.getEvidenceUrls());
        report.setReporterCredibility(credibility);
        report.setReportTime(LocalDateTime.now());
        report.setHandleStatus(ContentReport.STATUS_PENDING);
        
        // 6. 根据信誉分设置初始优先级
        int priority = calculateInitialPriority(credibility);
        report.setPriority(priority);

        // 7. 保存举报记录
        contentReportMapper.insert(report);
        log.info("举报提交成功，举报ID: {}, 内容ID: {}, 举报人: {}", 
                report.getId(), request.getContentId(), request.getReporterId());

        // 8. 检查是否需要提升优先级
        updateContentPriorityByReportCount(request.getContentId());

        return ReportResult.success(report.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleReport(Long reportId, Long handlerId, Integer handleStatus, 
                            String handleResult, boolean isValid) {
        // 1. 更新举报状态
        int updated = contentReportMapper.updateStatusById(reportId, handleStatus, handlerId, handleResult);
        if (updated == 0) {
            log.warn("举报记录不存在或已被处理，reportId: {}", reportId);
            return;
        }

        // 2. 获取举报记录以更新举报人信誉
        ContentReport report = contentReportMapper.selectById(reportId);
        if (report != null && report.getReporterUserId() != null) {
            updateReporterCredibility(report.getReporterUserId(), isValid);
            
            // 3. 通知举报人处理结果
            reportNotificationService.notifyReporter(report, handleResult, isValid);
        }

        log.info("举报处理完成，reportId: {}, handleStatus: {}, isValid: {}", 
                reportId, handleStatus, isValid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchHandleReport(List<Long> reportIds, Long handlerId, Integer handleStatus, 
                                  String handleResult, boolean isValid) {
        if (reportIds == null || reportIds.isEmpty()) {
            return;
        }

        // 1. 批量更新举报状态
        int updated = contentReportMapper.batchUpdateStatus(reportIds, handleStatus, handlerId, handleResult);
        log.info("批量处理举报，数量: {}, 实际更新: {}", reportIds.size(), updated);

        // 2. 更新每个举报人的信誉分并发送通知
        for (Long reportId : reportIds) {
            ContentReport report = contentReportMapper.selectById(reportId);
            if (report != null && report.getReporterUserId() != null) {
                updateReporterCredibility(report.getReporterUserId(), isValid);
                // 通知举报人处理结果
                reportNotificationService.notifyReporter(report, handleResult, isValid);
            }
        }
    }

    @Override
    public Page<ContentReport> getReportList(ContentReportQueryDTO query) {
        Page<ContentReport> page = new Page<>(query.getPageNum(), query.getPageSize());
        return contentReportMapper.selectReportPage(page, query);
    }

    @Override
    public boolean hasReported(Long contentId, Long reporterId) {
        Integer count = contentReportMapper.checkDuplicateReport(contentId, reporterId);
        return count != null && count > 0;
    }

    @Override
    public int countReportsByContent(Long contentId) {
        Integer count = contentReportMapper.countByContentId(contentId);
        return count != null ? count : 0;
    }

    @Override
    public int getUserReportCredibility(Long userId) {
        // 确保用户有信誉记录
        ensureUserCredibilityExists(userId);
        
        UserReportCredibility credibility = userReportCredibilityMapper.selectByUserId(userId);
        if (credibility == null) {
            return UserReportCredibility.DEFAULT_CREDIBILITY_SCORE;
        }
        return credibility.getCredibilityScore() != null ? 
                credibility.getCredibilityScore() : UserReportCredibility.DEFAULT_CREDIBILITY_SCORE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateContentPriorityByReportCount(Long contentId) {
        int reportCount = countReportsByContent(contentId);
        int threshold = auditConfigService.getReportPriorityThreshold();
        
        if (reportCount >= threshold) {
            // 提升优先级为高
            int newPriority = ContentReport.PRIORITY_HIGH;
            if (reportCount >= threshold * 2) {
                // 举报数达到阈值2倍，提升为紧急
                newPriority = ContentReport.PRIORITY_URGENT;
            }
            
            int updated = contentReportMapper.updatePriorityByContentId(contentId, newPriority);
            if (updated > 0) {
                log.info("内容 {} 举报数达到 {}，优先级提升为 {}", contentId, reportCount, newPriority);
            }
        }
    }

    /**
     * 计算初始优先级（基于举报人信誉分）
     */
    private int calculateInitialPriority(int credibility) {
        if (credibility >= 80) {
            // 高信誉用户的举报优先级较高
            return ContentReport.PRIORITY_HIGH;
        } else if (credibility >= 50) {
            return ContentReport.PRIORITY_NORMAL;
        } else {
            // 低信誉用户的举报优先级较低
            return ContentReport.PRIORITY_NORMAL;
        }
    }

    /**
     * 更新举报人信誉分
     */
    private void updateReporterCredibility(Long userId, boolean isValid) {
        ensureUserCredibilityExists(userId);
        
        if (isValid) {
            int increment = auditConfigService.getIntConfig(CONFIG_CREDIBILITY_INCREASE_ON_VALID, 5);
            userReportCredibilityMapper.incrementValidReport(userId, increment);
            log.debug("用户 {} 有效举报，信誉分增加 {}", userId, increment);
        } else {
            int decrement = auditConfigService.getIntConfig(CONFIG_CREDIBILITY_DECREASE_ON_INVALID, 10);
            userReportCredibilityMapper.incrementInvalidReport(userId, decrement);
            log.debug("用户 {} 无效举报，信誉分减少 {}", userId, decrement);
        }
    }

    /**
     * 确保用户信誉记录存在
     */
    private void ensureUserCredibilityExists(Long userId) {
        UserReportCredibility existing = userReportCredibilityMapper.selectByUserId(userId);
        if (existing == null) {
            userReportCredibilityMapper.initUserCredibility(userId);
            log.debug("初始化用户 {} 的信誉记录", userId);
        }
    }
}
