package com.basebackend.wheel.controller;

import com.basebackend.common.model.Result;
import com.basebackend.wheel.dto.ContentSubmitDTO;
import com.basebackend.wheel.entity.WheelContent;
import com.basebackend.wheel.service.ContentService;
import com.basebackend.wheel.util.AuditHelper;
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

    @Operation(summary = "提交内容", description = "提交转盘内容，需要审核通过后才能使用")
    @PostMapping("/submit")
    public Result<ContentService.SubmitResult> submitContent(
            @Valid @RequestBody ContentSubmitDTO submitDTO,
            HttpServletRequest request) {
        try {
            Long userId = AuditHelper.getCurrentUserId();
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
            Long userId = AuditHelper.getCurrentUserId();
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
            Long userId = AuditHelper.getCurrentUserId();
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
            Long userId = AuditHelper.getCurrentUserId();
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
            Long userId = AuditHelper.getCurrentUserId();
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
}
