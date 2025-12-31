package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.dto.ReportRequest;
import com.basebackend.wheel.dto.ReportResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 举报处理服务单元测试
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@DisplayName("举报处理服务测试")
class ReportHandlerServiceTest {

    @Test
    @DisplayName("ReportRequest - 验证有效举报原因")
    void testValidReportReason() {
        ReportRequest request = new ReportRequest();
        
        // 测试所有有效的举报原因
        for (int reason = ReportRequest.REASON_PORN; reason <= ReportRequest.REASON_OTHER; reason++) {
            request.setReportReason(reason);
            assertTrue(request.isValidReason(), "举报原因 " + reason + " 应该有效");
        }
        
        // 测试无效的举报原因
        request.setReportReason(-1);
        assertFalse(request.isValidReason(), "举报原因 -1 应该无效");
        
        request.setReportReason(6);
        assertFalse(request.isValidReason(), "举报原因 6 应该无效");
        
        request.setReportReason(null);
        assertFalse(request.isValidReason(), "举报原因 null 应该无效");
    }

    @Test
    @DisplayName("ReportRequest - 验证有效举报类型")
    void testValidReportType() {
        ReportRequest request = new ReportRequest();
        
        // null类型应该有效（默认为内容举报）
        request.setReportType(null);
        assertTrue(request.isValidType(), "举报类型 null 应该有效");
        
        // 测试所有有效的举报类型
        for (int type = ReportRequest.TYPE_CONTENT; type <= ReportRequest.TYPE_COMMENT; type++) {
            request.setReportType(type);
            assertTrue(request.isValidType(), "举报类型 " + type + " 应该有效");
        }
        
        // 测试无效的举报类型
        request.setReportType(-1);
        assertFalse(request.isValidType(), "举报类型 -1 应该无效");
        
        request.setReportType(3);
        assertFalse(request.isValidType(), "举报类型 3 应该无效");
    }

    @Test
    @DisplayName("ReportResult - 成功结果创建")
    void testSuccessResult() {
        ReportResult result = ReportResult.success(123L);
        
        assertTrue(result.isSuccess());
        assertEquals(123L, result.getReportId());
        assertEquals("举报提交成功", result.getMessage());
        assertNull(result.getErrorCode());
    }

    @Test
    @DisplayName("ReportResult - 重复举报错误")
    void testDuplicateReportResult() {
        ReportResult result = ReportResult.duplicateReport();
        
        assertFalse(result.isSuccess());
        assertEquals("DUPLICATE_REPORT", result.getErrorCode());
        assertNotNull(result.getMessage());
    }

    @Test
    @DisplayName("ReportResult - 低信誉用户错误")
    void testLowCredibilityResult() {
        ReportResult result = ReportResult.lowCredibility();
        
        assertFalse(result.isSuccess());
        assertEquals("LOW_CREDIBILITY", result.getErrorCode());
        assertNotNull(result.getMessage());
    }

    @Test
    @DisplayName("ReportResult - 无效举报原因错误")
    void testInvalidReasonResult() {
        ReportResult result = ReportResult.invalidReason();
        
        assertFalse(result.isSuccess());
        assertEquals("INVALID_REASON", result.getErrorCode());
        assertNotNull(result.getMessage());
    }

    @Test
    @DisplayName("ReportResult - 内容不存在错误")
    void testContentNotFoundResult() {
        ReportResult result = ReportResult.contentNotFound();
        
        assertFalse(result.isSuccess());
        assertEquals("CONTENT_NOT_FOUND", result.getErrorCode());
        assertNotNull(result.getMessage());
    }

    @Test
    @DisplayName("ReportResult - 自定义失败结果")
    void testCustomFailResult() {
        ReportResult result = ReportResult.fail("CUSTOM_ERROR", "自定义错误消息");
        
        assertFalse(result.isSuccess());
        assertEquals("CUSTOM_ERROR", result.getErrorCode());
        assertEquals("自定义错误消息", result.getMessage());
    }
}
