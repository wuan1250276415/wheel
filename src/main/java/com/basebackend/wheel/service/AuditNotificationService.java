package com.basebackend.wheel.service;

/**
 * 审核通知服务接口
 * 
 * 负责发送审核结果通知给用户
 *
 * @author wheel-api
 * @since 2025-02-01
 */
public interface AuditNotificationService {

    /**
     * 发送审核通过通知
     *
     * @param userId    用户ID
     * @param contentId 内容ID
     */
    void sendPassNotification(Long userId, Long contentId);

    /**
     * 发送审核拒绝通知
     *
     * @param userId       用户ID
     * @param contentId    内容ID
     * @param rejectReason 拒绝原因
     */
    void sendRejectNotification(Long userId, Long contentId, String rejectReason);

    /**
     * 发送申诉结果通知
     *
     * @param userId    用户ID
     * @param contentId 内容ID
     * @param approved  是否通过申诉
     * @param message   处理消息
     */
    void sendAppealResultNotification(Long userId, Long contentId, boolean approved, String message);

    /**
     * 发送内容进入人工审核通知
     *
     * @param userId    用户ID
     * @param contentId 内容ID
     */
    void sendPendingReviewNotification(Long userId, Long contentId);
}
