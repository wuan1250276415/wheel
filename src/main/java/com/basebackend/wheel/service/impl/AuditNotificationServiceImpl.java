package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.service.AuditNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 审核通知服务实现
 * 
 * 通过系统消息和推送通知用户审核结果
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditNotificationServiceImpl implements AuditNotificationService {

    /**
     * 通知类型常量
     */
    private static final String NOTIFICATION_TYPE_AUDIT = "AUDIT";
    private static final String NOTIFICATION_TYPE_APPEAL = "APPEAL";

    @Override
    public void sendPassNotification(Long userId, Long contentId) {
        if (userId == null) {
            return;
        }
        
        String title = "内容审核通过";
        String content = "您提交的内容已通过审核，现已发布。";
        
        sendNotification(userId, contentId, title, content, NOTIFICATION_TYPE_AUDIT);
        
        log.info("发送审核通过通知: userId={}, contentId={}", userId, contentId);
    }

    @Override
    public void sendRejectNotification(Long userId, Long contentId, String rejectReason) {
        if (userId == null) {
            return;
        }
        
        String title = "内容审核未通过";
        String content = buildRejectContent(rejectReason);
        
        sendNotification(userId, contentId, title, content, NOTIFICATION_TYPE_AUDIT);
        
        log.info("发送审核拒绝通知: userId={}, contentId={}, reason={}", userId, contentId, rejectReason);
    }

    @Override
    public void sendAppealResultNotification(Long userId, Long contentId, boolean approved, String message) {
        if (userId == null) {
            return;
        }
        
        String title = approved ? "申诉已通过" : "申诉被驳回";
        String content = approved 
                ? "您的内容申诉已通过审核，内容已恢复发布。"
                : "您的内容申诉未通过。" + (message != null ? "原因：" + message : "");
        
        sendNotification(userId, contentId, title, content, NOTIFICATION_TYPE_APPEAL);
        
        log.info("发送申诉结果通知: userId={}, contentId={}, approved={}", userId, contentId, approved);
    }

    @Override
    public void sendPendingReviewNotification(Long userId, Long contentId) {
        if (userId == null) {
            return;
        }
        
        String title = "内容审核中";
        String content = "您提交的内容正在审核中，请耐心等待。审核结果将通过消息通知您。";
        
        sendNotification(userId, contentId, title, content, NOTIFICATION_TYPE_AUDIT);
        
        log.info("发送待审核通知: userId={}, contentId={}", userId, contentId);
    }

    /**
     * 构建拒绝原因内容
     */
    private String buildRejectContent(String rejectReason) {
        StringBuilder sb = new StringBuilder();
        sb.append("您提交的内容未通过审核。");
        
        if (rejectReason != null && !rejectReason.isEmpty()) {
            sb.append("\n\n拒绝原因：").append(rejectReason);
        }
        
        sb.append("\n\n如有疑问，您可以提交申诉。");
        
        return sb.toString();
    }

    /**
     * 发送通知
     * 
     * 这里可以集成多种通知渠道：
     * 1. 系统消息（存储到数据库）
     * 2. 推送通知（JPush等）
     * 3. 微信模板消息
     * 4. 短信通知
     */
    private void sendNotification(Long userId, Long contentId, String title, String content, String type) {
        // TODO: 集成实际的通知服务
        // 1. 保存系统消息到数据库
        // saveSystemMessage(userId, contentId, title, content, type);
        
        // 2. 发送推送通知
        // pushService.sendNotification(userId, title, content);
        
        // 3. 发送微信模板消息（如果是小程序用户）
        // wechatService.sendTemplateMessage(userId, title, content);
        
        log.debug("通知已发送: userId={}, contentId={}, title={}, type={}", 
                userId, contentId, title, type);
    }
}
