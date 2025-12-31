package com.basebackend.wheel.controller;

import com.basebackend.common.context.UserContextHolder;
import com.basebackend.common.model.Result;
import com.basebackend.wheel.dto.ContentAppealDTO;
import com.basebackend.wheel.dto.ContentSubmitDTO;
import com.basebackend.wheel.dto.ReportRequest;
import com.basebackend.wheel.dto.ReportResult;
import com.basebackend.wheel.entity.ContentAuditLog;
import com.basebackend.wheel.entity.WheelContent;
import com.basebackend.wheel.service.ContentModerationService;
import com.basebackend.wheel.service.ContentService;
import com.basebackend.wheel.service.ReportHandlerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 内容管理控制器
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/content")
@Tag(name = "内容管理", description = "内容提交、审核、举报相关接口")
public class ContentController {

    @Autowired
    private ContentService contentService;

    @Autowired
    private ReportHandlerService reportHandlerService;

    @Autowired
    private ContentModerationService contentModerationService;

    @Operation(summary = "提交内容", description = "提交转盘内容，需要审核通过后才能使用")
    @PostMapping("/submit")
    public Result<ContentService.SubmitResult> submitContent(
            @Valid @RequestBody ContentSubmitDTO submitDTO,
            HttpServletRequest request) {
        try {
            Long userId = UserContextHolder.getUserId();
            ContentService.SubmitResult result = contentService.submitContent(userId, submitDTO);
            log.info("用户提交内容成功: userId={}, contentId={}", userId, result.getContentId());
            return Result.success(result);
        } catch (Exception e) {
            log.error("用户提交内容失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "获取我的内容", description = "获取当前用户提交的内容列表")
    @GetMapping("/my")
    public Result<List<WheelContent>> getMyContents(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        try {
            Long userId = UserContextHolder.getUserId();
            List<WheelContent> contents = contentService.getMyContents(userId, pageNum, pageSize);
            return Result.success(contents);
        } catch (Exception e) {
            log.error("获取我的内容失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "编辑内容", description = "编辑用户提交的内容（仅待审核或已拒绝的内容）")
    @PutMapping("/{contentId}")
    public Result<Void> updateContent(
            @PathVariable Long contentId,
            @Valid @RequestBody ContentSubmitDTO submitDTO,
            HttpServletRequest request) {
        try {
            Long userId = UserContextHolder.getUserId();
            boolean success = contentService.updateContent(userId, contentId, submitDTO);
            if (success) {
                log.info("用户更新内容成功: userId={}, contentId={}", userId, contentId);
                return Result.success();
            } else {
                return Result.error();
            }
        } catch (Exception e) {
            log.error("用户更新内容失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "删除内容", description = "软删除用户提交的内容")
    @DeleteMapping("/{contentId}")
    public Result<Void> deleteContent(
            @PathVariable Long contentId,
            HttpServletRequest request) {
        try {
            Long userId = UserContextHolder.getUserId();
            boolean success = contentService.deleteContent(userId, contentId);
            if (success) {
                log.info("用户删除内容成功: userId={}, contentId={}", userId, contentId);
                return Result.success();
            } else {
                return Result.error();
            }
        } catch (Exception e) {
            log.error("用户删除内容失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "举报内容", description = "举报不当内容")
    @PostMapping("/report")
    public Result<Void> reportContent(
            @RequestParam Long contentId,
            @RequestParam Integer reason,
            @RequestParam(required = false) String description,
            HttpServletRequest request) {
        try {
            Long userId = UserContextHolder.getUserId();
            boolean success = contentService.reportContent(userId, contentId, reason, description);
            if (success) {
                log.info("用户举报内容成功: reporterId={}, contentId={}, reason={}", userId, contentId, reason);
                return Result.success();
            } else {
                return Result.error();
            }
        } catch (Exception e) {
            log.error("用户举报内容失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "增强举报内容", description = "支持多类型举报和证据上传的增强举报接口")
    @PostMapping("/report/submit")
    public Result<ReportResult> submitReport(
            @Valid @RequestBody ReportRequest reportRequest,
            HttpServletRequest request) {
        try {
            Long userId = UserContextHolder.getUserId();
            reportRequest.setReporterId(userId);
            
            ReportResult result = reportHandlerService.submitReport(reportRequest);
            log.info("用户举报提交成功: reporterId={}, contentId={}, reportType={}, reason={}", 
                    userId, reportRequest.getContentId(), reportRequest.getReportType(), reportRequest.getReportReason());
            return Result.success(result);
        } catch (Exception e) {
            log.error("用户举报提交失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "检查是否已举报", description = "检查当前用户是否已举报过该内容")
    @GetMapping("/report/check")
    public Result<Boolean> checkReported(@RequestParam Long contentId) {
        try {
            Long userId = UserContextHolder.getUserId();
            boolean reported = reportHandlerService.hasReported(contentId, userId);
            return Result.success(reported);
        } catch (Exception e) {
            log.error("检查举报状态失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "获取内容审核状态", description = "获取指定内容的审核状态")
    @GetMapping("/{contentId}/audit-status")
    public Result<ContentService.AuditStatus> getAuditStatus(@PathVariable Long contentId) {
        try {
            ContentService.AuditStatus status = contentService.getAuditStatus(contentId);
            return Result.success(status);
        } catch (Exception e) {
            log.error("获取审核状态失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "搜索内容", description = "搜索转盘内容")
    @GetMapping("/search")
    public Result<List<WheelContent>> searchContents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            List<WheelContent> contents = contentService.searchContents(keyword, categoryId, pageNum, pageSize);
            return Result.success(contents);
        } catch (Exception e) {
            log.error("搜索内容失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "提交内容申诉", description = "对被拒绝的内容提交申诉")
    @PostMapping("/appeal")
    public Result<Void> submitAppeal(
            @Valid @RequestBody ContentAppealDTO appealDTO,
            HttpServletRequest request) {
        try {
            Long userId = UserContextHolder.getUserId();
            boolean success = contentModerationService.appealContent(
                    appealDTO.getContentId(), 
                    userId, 
                    appealDTO.getAppealReason()
            );
            if (success) {
                log.info("用户提交申诉成功: userId={}, contentId={}", userId, appealDTO.getContentId());
                return Result.success();
            } else {
                return Result.error("申诉提交失败，请稍后重试");
            }
        } catch (Exception e) {
            log.error("提交申诉失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "获取申诉状态", description = "获取内容申诉的处理状态")
    @GetMapping("/appeal/status")
    public Result<ContentAuditLog> getAppealStatus(@RequestParam Long contentId) {
        try {
            ContentAuditLog auditLog = contentModerationService.getAuditStatus(contentId);
            return Result.success(auditLog);
        } catch (Exception e) {
            log.error("获取申诉状态失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "检查是否可以申诉", description = "检查内容是否可以提交申诉")
    @GetMapping("/appeal/check")
    public Result<Boolean> checkCanAppeal(@RequestParam Long contentId) {
        try {
            Long userId = UserContextHolder.getUserId();
            // 检查内容是否属于当前用户且状态为已拒绝
            WheelContent content = contentService.getContentById(contentId);
            if (content == null) {
                return Result.error("内容不存在");
            }
            if (!content.getCreateUserId().equals(userId)) {
                return Result.error("无权申诉此内容");
            }
            // auditStatus 2表示已拒绝
            boolean canAppeal = content.getAuditStatus() == 2;
            return Result.success(canAppeal);
        } catch (Exception e) {
            log.error("检查申诉状态失败: error={}", e.getMessage());
            throw e;
        }
    }
}
