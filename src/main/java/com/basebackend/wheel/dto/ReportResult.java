package com.basebackend.wheel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 举报结果DTO
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResult {

    /**
     * 举报ID
     */
    private Long reportId;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 结果消息
     */
    private String message;

    /**
     * 错误码（失败时）
     */
    private String errorCode;

    /**
     * 创建成功结果
     */
    public static ReportResult success(Long reportId) {
        return ReportResult.builder()
                .reportId(reportId)
                .success(true)
                .message("举报提交成功")
                .build();
    }

    /**
     * 创建成功结果（带自定义消息）
     */
    public static ReportResult success(Long reportId, String message) {
        return ReportResult.builder()
                .reportId(reportId)
                .success(true)
                .message(message)
                .build();
    }

    /**
     * 创建失败结果
     */
    public static ReportResult fail(String message) {
        return ReportResult.builder()
                .success(false)
                .message(message)
                .build();
    }

    /**
     * 创建失败结果（带错误码）
     */
    public static ReportResult fail(String errorCode, String message) {
        return ReportResult.builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .build();
    }

    /**
     * 重复举报错误
     */
    public static ReportResult duplicateReport() {
        return fail("DUPLICATE_REPORT", "您已举报过该内容，请勿重复举报");
    }

    /**
     * 低信誉用户错误
     */
    public static ReportResult lowCredibility() {
        return fail("LOW_CREDIBILITY", "您的举报信誉过低，举报功能受限");
    }

    /**
     * 内容不存在错误
     */
    public static ReportResult contentNotFound() {
        return fail("CONTENT_NOT_FOUND", "举报的内容不存在");
    }

    /**
     * 无效举报原因错误
     */
    public static ReportResult invalidReason() {
        return fail("INVALID_REASON", "无效的举报原因");
    }
}
