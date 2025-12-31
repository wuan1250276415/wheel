package com.basebackend.wheel.service.impl;

import com.basebackend.common.exception.BusinessException;
import com.basebackend.wheel.dto.ContentSubmitDTO;
import com.basebackend.wheel.entity.ContentAuditLog;
import com.basebackend.wheel.entity.ContentReport;
import com.basebackend.wheel.entity.WheelContent;
import com.basebackend.wheel.mapper.ContentAuditLogMapper;
import com.basebackend.wheel.mapper.ContentReportMapper;
import com.basebackend.wheel.mapper.WheelContentMapper;
import com.basebackend.wheel.service.ContentService;
import com.basebackend.wheel.util.AuditHelper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 内容服务实现
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Slf4j
@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private WheelContentMapper contentMapper;

    @Autowired
    private ContentAuditLogMapper auditLogMapper;

    @Autowired
    private ContentReportMapper reportMapper;

    @Autowired
    private AiAuditService aiAuditService;

    @Autowired
    private com.basebackend.wheel.util.MembershipPrivilegeHelper membershipPrivilegeHelper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 敏感词列表（实际项目中应该从数据库或配置文件加载）
    private static final Pattern[] SENSITIVE_PATTERNS = {
        Pattern.compile("色情|低俗|性感", Pattern.CASE_INSENSITIVE),
        Pattern.compile("暴力|血腥|恐怖", Pattern.CASE_INSENSITIVE),
        Pattern.compile("政治|敏感|反动", Pattern.CASE_INSENSITIVE)
    };

    @Override
    @Transactional
    public SubmitResult submitContent(Long userId, ContentSubmitDTO submitDTO) {
        // 1. 检查分类是否存在
        // TODO: 验证分类ID有效性

        // 2. 检查内容长度
        if (submitDTO.getContentText().length() > 200) {
            throw new BusinessException("内容长度不能超过200字符");
        }

        // 3. 检查权重值
        if (submitDTO.getWeight() <= 0 || submitDTO.getWeight() > 10) {
            throw new BusinessException("权重值必须在0-10之间");
        }

        // 4. 创建内容实体
        WheelContent content = new WheelContent();
        content.setCategoryId(submitDTO.getCategoryId());
        content.setContentText(submitDTO.getContentText());
        content.setWeight(submitDTO.getWeight());
        content.setCreateUserId(userId);
        content.setAuditStatus(0); // 待审核
        content.setStatus(1); // 启用

        // 设置审核优先级（VIP用户优先审核）
        content.setAuditPriority(membershipPrivilegeHelper.getAuditPriority(userId));

        // 处理标签
        if (submitDTO.getTags() != null && !submitDTO.getTags().isEmpty()) {
            String tagsJson = convertTagsToJson(submitDTO.getTags());
            content.setTags(tagsJson);
        }

        // 设置审计字段
        AuditHelper.setCreateAuditFields(content, userId);

        // 5. 保存内容
        contentMapper.insert(content);

        // 6. AI预审
        AiAuditResult aiResult = aiAuditService.auditContent(content.getContentText());

        // 7. 记录审核日志
        ContentAuditLog auditLog = new ContentAuditLog();
        auditLog.setContentId(content.getId());
        auditLog.setSubmitUserId(userId);
        auditLog.setAuditStatus(aiResult.isPass() ? 1 : 2); // AI预审通过或拒绝
        auditLog.setAiAuditResult(convertAiResultToJson(aiResult));
        auditLog.setAuditType(0); // 自动审核

        if (aiResult.isPass()) {
            // AI预审通过，等待人工复审
            auditLog.setAuditStatus(0);
        }

        AuditHelper.setCreateAuditFields(auditLog, userId);
        auditLogMapper.insert(auditLog);

        // 8. 构建返回结果
        SubmitResult result = new SubmitResult();
        result.setContentId(content.getId());
        result.setAuditId(auditLog.getId().toString());

        if (aiResult.isPass()) {
            result.setStatus("pending");
            result.setMessage("内容提交成功，正在等待人工审核");
        } else {
            result.setStatus("rejected");
            result.setMessage("AI预审未通过: " + aiResult.getReason());
        }

        log.info("用户提交内容: userId={}, contentId={}, aiPass={}, reason={}",
                userId, content.getId(), aiResult.isPass(), aiResult.getReason());

        return result;
    }

    @Override
    public List<WheelContent> getMyContents(Long userId, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return contentMapper.selectByCreateUserId(userId, offset, pageSize);
    }

    @Override
    @Transactional
    public boolean updateContent(Long userId, Long contentId, ContentSubmitDTO submitDTO) {
        // 1. 查找内容
        WheelContent content = contentMapper.selectById(contentId);
        if (content == null) {
            throw new BusinessException("内容不存在");
        }

        // 2. 检查权限
        if (!content.getCreateUserId().equals(userId)) {
            throw new BusinessException("无权编辑此内容");
        }

        // 3. 检查状态（只有待审核或已拒绝的内容才能编辑）
        if (content.getAuditStatus() == 1) {
            throw new BusinessException("已通过审核的内容不能编辑");
        }

        // 4. 更新内容
        content.setCategoryId(submitDTO.getCategoryId());
        content.setContentText(submitDTO.getContentText());
        content.setWeight(submitDTO.getWeight());

        if (submitDTO.getTags() != null && !submitDTO.getTags().isEmpty()) {
            String tagsJson = convertTagsToJson(submitDTO.getTags());
            content.setTags(tagsJson);
        }

        // 重置审核状态
        content.setAuditStatus(0);
        content.setAuditorId(null);
        content.setAuditedAt(null);
        content.setAuditComment(null);

        // 重新设置审核优先级
        content.setAuditPriority(membershipPrivilegeHelper.getAuditPriority(userId));

        // 设置更新审计字段
        AuditHelper.setUpdateAuditFields(content, userId);

        int result = contentMapper.updateById(content);

        log.info("用户更新内容: userId={}, contentId={}", userId, contentId);

        return result > 0;
    }

    @Override
    @Transactional
    public boolean deleteContent(Long userId, Long contentId) {
        // 1. 查找内容
        WheelContent content = contentMapper.selectById(contentId);
        if (content == null) {
            throw new BusinessException("内容不存在");
        }

        // 2. 检查权限
        if (!content.getCreateUserId().equals(userId)) {
            throw new BusinessException("无权删除此内容");
        }

        // 3. 软删除（设置为禁用状态）
        content.setStatus(0);
        AuditHelper.setUpdateAuditFields(content, userId);

        int result = contentMapper.updateById(content);

        log.info("用户删除内容: userId={}, contentId={}", userId, contentId);

        return result > 0;
    }

    @Override
    @Transactional
    public boolean reportContent(Long userId, Long contentId, Integer reason, String description) {
        // 1. 检查内容是否存在
        WheelContent content = contentMapper.selectById(contentId);
        if (content == null) {
            throw new BusinessException("内容不存在");
        }

        // 2. 检查不能举报自己的内容
        if (content.getCreateUserId().equals(userId)) {
            throw new BusinessException("不能举报自己发布的内容");
        }

        // 3. 创建举报记录
        ContentReport report = new ContentReport();
        report.setContentId(contentId);
        report.setReporterUserId(userId);
        report.setReportReason(reason);
        report.setReportDescription(description);
        report.setHandleStatus(0); // 待处理

        AuditHelper.setCreateAuditFields(report, userId);

        int result = reportMapper.insert(report);

        log.info("用户举报内容: reporterId={}, contentId={}, reason={}", userId, contentId, reason);

        return result > 0;
    }

    @Override
    public AuditStatus getAuditStatus(Long contentId) {
        WheelContent content = contentMapper.selectById(contentId);
        if (content == null) {
            throw new BusinessException("内容不存在");
        }

        AuditStatus status = new AuditStatus();
        status.setStatus(content.getAuditStatus());
        status.setStatusText(toStatusText(content.getAuditStatus()));

        ContentAuditLog latestLog = auditLogMapper.selectOne(
                new LambdaQueryWrapper<ContentAuditLog>()
                        .eq(ContentAuditLog::getContentId, contentId)
                        .orderByDesc(ContentAuditLog::getCreateTime)
                        .last("LIMIT 1"));

        if (latestLog != null) {
            Integer auditStatus = latestLog.getAuditStatus();
            if (auditStatus != null) {
                status.setStatus(auditStatus);
                status.setStatusText(toStatusText(auditStatus));
            }
            status.setAuditComment(latestLog.getAuditComment());
            if (latestLog.getAuditorId() != null) {
                status.setAuditor(String.valueOf(latestLog.getAuditorId()));
            }
            LocalDateTime auditedAt = latestLog.getAuditedAt() != null
                    ? latestLog.getAuditedAt()
                    : latestLog.getCreateTime();
            if (auditedAt != null) {
                status.setAuditedAt(auditedAt.toString());
            }
        }

        return status;
    }

    @Override
    public List<WheelContent> searchContents(String keyword, Long categoryId, Integer pageNum, Integer pageSize) {
        int pageIndex = pageNum != null && pageNum > 0 ? pageNum : 1;
        int size = pageSize != null && pageSize > 0 ? pageSize : 20;

        LambdaQueryWrapper<WheelContent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WheelContent::getAuditStatus, 1)
                .eq(WheelContent::getStatus, 1);

        if (StringUtils.hasText(keyword)) {
            wrapper.like(WheelContent::getContentText, keyword);
        }
        if (categoryId != null) {
            wrapper.eq(WheelContent::getCategoryId, categoryId);
        }

        wrapper.orderByDesc(WheelContent::getCreateTime);
        Page<WheelContent> page = new Page<>(pageIndex, size);
        contentMapper.selectPage(page, wrapper);
        return page.getRecords();
    }

    @Override
    public WheelContent getContentById(Long contentId) {
        if (contentId == null) {
            return null;
        }
        return contentMapper.selectById(contentId);
    }

    /**
     * 将标签列表转换为JSON字符串
     */
    private String convertTagsToJson(List<String> tags) {
        try {
            Map<String, Object> tagsMap = new HashMap<>();
            tagsMap.put("tags", tags);
            return objectMapper.writeValueAsString(tagsMap);
        } catch (Exception e) {
            log.warn("标签转换JSON失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将AI审核结果转换为JSON字符串
     */
    private String convertAiResultToJson(AiAuditResult result) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("pass", result.isPass());
            resultMap.put("score", result.getScore());
            resultMap.put("reason", result.getReason());
            resultMap.put("riskFactors", result.getRiskFactors());
            return objectMapper.writeValueAsString(resultMap);
        } catch (Exception e) {
            log.warn("AI审核结果转换JSON失败: {}", e.getMessage());
            return null;
        }
    }

    private String toStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "已通过";
            case 2 -> "已拒绝";
            default -> "未知";
        };
    }

    /**
     * AI审核服务接口
     */
    public interface AiAuditService {
        /**
         * 审核内容
         *
         * @param content 内容文本
         * @return 审核结果
         */
        AiAuditResult auditContent(String content);
    }

    /**
     * AI审核结果
     */
    public static class AiAuditResult {
        private boolean pass;
        private double score; // 风险评分（0-1，越高风险越大）
        private String reason;
        private List<String> riskFactors;

        // Getters and Setters
        public boolean isPass() {
            return pass;
        }

        public void setPass(boolean pass) {
            this.pass = pass;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public List<String> getRiskFactors() {
            return riskFactors;
        }

        public void setRiskFactors(List<String> riskFactors) {
            this.riskFactors = riskFactors;
        }
    }

    /**
     * 默认AI审核服务实现（简化版）
     */
    @Service
    public static class DefaultAiAuditService implements AiAuditService {
        @Override
        public AiAuditResult auditContent(String content) {
            AiAuditResult result = new AiAuditResult();
            List<String> riskFactors = new java.util.ArrayList<>();

            // 敏感词检测
            for (Pattern pattern : SENSITIVE_PATTERNS) {
                if (pattern.matcher(content).find()) {
                    riskFactors.add("包含敏感词汇");
                }
            }

            // 长度检测
            if (content.length() > 150) {
                riskFactors.add("内容过长");
            }

            // 重复字符检测
            if (hasRepeatedChars(content)) {
                riskFactors.add("包含重复字符");
            }

            // 计算风险评分
            double score = Math.min(1.0, riskFactors.size() * 0.3);
            result.setScore(score);
            result.setRiskFactors(riskFactors);

            if (riskFactors.isEmpty()) {
                result.setPass(true);
                result.setReason("通过AI预审");
            } else {
                result.setPass(false);
                result.setReason("检测到风险因素: " + String.join(", ", riskFactors));
            }

            return result;
        }

        /**
         * 检查是否包含重复字符
         */
        private boolean hasRepeatedChars(String content) {
            for (int i = 0; i < content.length() - 3; i++) {
                char c = content.charAt(i);
                if (content.charAt(i + 1) == c &&
                    content.charAt(i + 2) == c &&
                    content.charAt(i + 3) == c) {
                    return true;
                }
            }
            return false;
        }
    }
}
