package com.basebackend.wheel.service;

import com.basebackend.wheel.dto.ContentSubmitDTO;
import com.basebackend.wheel.entity.WheelContent;
import com.basebackend.wheel.entity.ContentAuditLog;
import com.basebackend.wheel.entity.ContentReport;

import java.util.List;

/**
 * 内容服务接口
 *
 * @author wheel-api
 * @since 2025-12-16
 */
public interface ContentService {

    /**
     * 提交内容（需审核）
     *
     * @param userId 用户ID
     * @param submitDTO 提交信息
     * @return 提交结果
     */
    SubmitResult submitContent(Long userId, ContentSubmitDTO submitDTO);

    /**
     * 获取我的内容列表
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 内容列表
     */
    List<WheelContent> getMyContents(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 编辑内容
     *
     * @param userId 用户ID
     * @param contentId 内容ID
     * @param submitDTO 更新信息
     * @return 是否成功
     */
    boolean updateContent(Long userId, Long contentId, ContentSubmitDTO submitDTO);

    /**
     * 删除内容
     *
     * @param userId 用户ID
     * @param contentId 内容ID
     * @return 是否成功
     */
    boolean deleteContent(Long userId, Long contentId);

    /**
     * 举报内容
     *
     * @param userId 举报人ID
     * @param contentId 被举报内容ID
     * @param reason 举报原因
     * @param description 详细描述
     * @return 是否成功
     */
    boolean reportContent(Long userId, Long contentId, Integer reason, String description);

    /**
     * 获取内容审核状态
     *
     * @param contentId 内容ID
     * @return 审核状态
     */
    AuditStatus getAuditStatus(Long contentId);

    /**
     * 搜索内容
     *
     * @param keyword 关键词
     * @param categoryId 分类ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 内容列表
     */
    List<WheelContent> searchContents(String keyword, Long categoryId, Integer pageNum, Integer pageSize);

    /**
     * 提交结果
     */
    class SubmitResult {
        private Long contentId;
        private String status; // pending, rejected
        private String message;
        private String auditId;

        // Getters and Setters
        public Long getContentId() {
            return contentId;
        }

        public void setContentId(Long contentId) {
            this.contentId = contentId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getAuditId() {
            return auditId;
        }

        public void setAuditId(String auditId) {
            this.auditId = auditId;
        }
    }

    /**
     * 审核状态
     */
    class AuditStatus {
        private Integer status; // 0-待审核，1-已通过，2-已拒绝
        private String statusText;
        private String auditComment;
        private String auditor;
        private String auditedAt;

        // Getters and Setters
        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getStatusText() {
            return statusText;
        }

        public void setStatusText(String statusText) {
            this.statusText = statusText;
        }

        public String getAuditComment() {
            return auditComment;
        }

        public void setAuditComment(String auditComment) {
            this.auditComment = auditComment;
        }

        public String getAuditor() {
            return auditor;
        }

        public void setAuditor(String auditor) {
            this.auditor = auditor;
        }

        public String getAuditedAt() {
            return auditedAt;
        }

        public void setAuditedAt(String auditedAt) {
            this.auditedAt = auditedAt;
        }
    }
}
