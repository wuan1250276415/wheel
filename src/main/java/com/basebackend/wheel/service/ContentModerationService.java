package com.basebackend.wheel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basebackend.wheel.dto.ModerationRequest;
import com.basebackend.wheel.dto.ModerationResult;
import com.basebackend.wheel.entity.ContentAuditLog;

/**
 * 内容审核服务接口（核心服务）
 * 
 * 实现三级审核流程：AI预审 → 敏感词过滤 → 人工复审
 *
 * @author wheel-api
 * @since 2025-02-01
 */
public interface ContentModerationService {

    /**
     * 审核内容（三级审核流程）
     * 
     * 流程：
     * 1. 黑名单检查 - 检查用户/IP/设备是否在黑名单
     * 2. AI预审 - 调用AI服务进行内容审核
     * 3. 敏感词过滤 - 使用DFA算法检测敏感词
     * 4. 决策判定 - 根据风险评分决定通过/复审/拒绝
     *
     * @param request 审核请求
     * @return 审核结果
     */
    ModerationResult moderateContent(ModerationRequest request);

    /**
     * 人工审核
     *
     * @param contentId    内容ID
     * @param auditorId    审核员ID
     * @param passed       是否通过
     * @param auditComment 审核意见
     */
    void manualAudit(Long contentId, Long auditorId, boolean passed, String auditComment);

    /**
     * 批量人工审核
     *
     * @param contentIds   内容ID列表
     * @param auditorId    审核员ID
     * @param passed       是否通过
     * @param auditComment 审核意见
     */
    void batchManualAudit(Long[] contentIds, Long auditorId, boolean passed, String auditComment);

    /**
     * 获取审核队列
     *
     * @param page        页码
     * @param pageSize    每页大小
     * @param contentType 内容类型（可为null）
     * @param priority    优先级（可为null）
     * @return 分页结果
     */
    Page<ContentAuditLog> getAuditQueue(int page, int pageSize, Integer contentType, Integer priority);

    /**
     * 内容申诉
     *
     * @param contentId    内容ID
     * @param userId       用户ID
     * @param appealReason 申诉理由
     * @return 申诉是否提交成功
     */
    boolean appealContent(Long contentId, Long userId, String appealReason);

    /**
     * 处理申诉
     *
     * @param contentId    内容ID
     * @param auditorId    审核员ID
     * @param approved     是否批准申诉
     * @param auditComment 处理意见
     */
    void handleAppeal(Long contentId, Long auditorId, boolean approved, String auditComment);

    /**
     * 获取内容审核状态
     *
     * @param contentId 内容ID
     * @return 审核日志
     */
    ContentAuditLog getAuditStatus(Long contentId);

    /**
     * 计算审核优先级
     * 
     * 根据用户会员等级和内容举报数计算优先级
     *
     * @param userId      用户ID
     * @param contentId   内容ID
     * @param reportCount 举报数量
     * @return 优先级：1-普通 2-高 3-紧急
     */
    int calculatePriority(Long userId, Long contentId, int reportCount);

    /**
     * 发送审核结果通知
     *
     * @param userId       用户ID
     * @param contentId    内容ID
     * @param passed       是否通过
     * @param rejectReason 拒绝原因（通过时为null）
     */
    void sendAuditNotification(Long userId, Long contentId, boolean passed, String rejectReason);
}
