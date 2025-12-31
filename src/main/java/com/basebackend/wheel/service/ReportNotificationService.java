package com.basebackend.wheel.service;

import com.basebackend.wheel.entity.ContentReport;

/**
 * 举报通知服务接口
 *
 * @author wheel-api
 * @since 2025-02-01
 */
public interface ReportNotificationService {

    /**
     * 通知举报人举报处理结果
     *
     * @param report       举报记录
     * @param handleResult 处理结果描述
     * @param isValid      举报是否有效
     */
    void notifyReporter(ContentReport report, String handleResult, boolean isValid);

    /**
     * 通知内容作者内容被举报
     *
     * @param contentId    内容ID
     * @param authorId     作者ID
     * @param reportReason 举报原因
     */
    void notifyContentAuthor(Long contentId, Long authorId, Integer reportReason);

    /**
     * 通知用户被封禁
     *
     * @param userId 用户ID
     * @param reason 封禁原因
     */
    void notifyUserBlocked(Long userId, String reason);
}
