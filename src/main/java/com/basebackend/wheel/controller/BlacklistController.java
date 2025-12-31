package com.basebackend.wheel.controller;

import com.basebackend.common.context.UserContextHolder;
import com.basebackend.common.model.Result;
import com.basebackend.wheel.dto.BlacklistAppealDTO;
import com.basebackend.wheel.dto.BlacklistCheckResult;
import com.basebackend.wheel.dto.BlacklistVO;
import com.basebackend.wheel.service.BlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 黑名单用户端控制器
 * 提供黑名单检查和申诉功能
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Slf4j
@RestController
@RequestMapping("/api/blacklist")
@RequiredArgsConstructor
@Tag(name = "黑名单管理", description = "黑名单检查和申诉相关接口")
public class BlacklistController {

    private final BlacklistService blacklistService;

    @Operation(summary = "检查当前用户是否被封禁", description = "综合检查用户ID、IP、设备是否在黑名单中")
    @GetMapping("/check")
    public Result<BlacklistCheckResult> checkBlacklist(HttpServletRequest request) {
        try {
            Long userId = UserContextHolder.getUserId();
            String ip = getClientIp(request);
            String deviceId = request.getHeader("X-Device-Id");
            
            BlacklistCheckResult result = blacklistService.checkAll(userId, ip, deviceId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("检查黑名单状态失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "提交黑名单申诉", description = "用户提交封禁申诉")
    @PostMapping("/appeal")
    public Result<Void> submitAppeal(
            @Valid @RequestBody BlacklistAppealDTO appealDTO,
            HttpServletRequest request) {
        try {
            Long userId = UserContextHolder.getUserId();
            appealDTO.setUserId(userId);
            
            blacklistService.submitAppeal(appealDTO);
            log.info("用户提交申诉成功: userId={}, blacklistId={}", userId, appealDTO.getBlacklistId());
            return Result.success();
        } catch (Exception e) {
            log.error("提交申诉失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "获取申诉状态", description = "查询申诉处理状态")
    @GetMapping("/appeal/status")
    public Result<BlacklistVO> getAppealStatus(@RequestParam Long blacklistId) {
        try {
            BlacklistVO blacklist = blacklistService.getById(blacklistId);
            if (blacklist == null) {
                return Result.error("记录不存在");
            }
            return Result.success(blacklist);
        } catch (Exception e) {
            log.error("获取申诉状态失败: error={}", e.getMessage());
            throw e;
        }
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
