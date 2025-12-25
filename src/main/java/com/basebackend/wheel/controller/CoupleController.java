package com.basebackend.wheel.controller;

import com.basebackend.common.context.UserContextHolder;
import com.basebackend.common.model.Result;
import com.basebackend.wheel.dto.CoupleAcceptDTO;
import com.basebackend.wheel.dto.CoupleInviteDTO;
import com.basebackend.wheel.service.CoupleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 情侣关系控制器
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/couple")
@Tag(name = "情侣关系管理", description = "情侣配对、邀请、确认相关接口")
public class CoupleController {

    @Autowired
    private CoupleService coupleService;

    @Operation(summary = "邀请情侣", description = "邀请指定手机号的用户成为情侣")
    @PostMapping("/invite")
    public Result<CoupleService.InviteResult> inviteCouple(
            @Valid @RequestBody CoupleInviteDTO inviteDTO,
            HttpServletRequest request) {
        try {
            Long userId = UserContextHolder.getUserId();
            CoupleService.InviteResult result = coupleService.inviteCouple(userId, inviteDTO);
            log.info("用户邀请情侣成功: userId={}, phoneNumber={}", userId, inviteDTO.getPartnerPhoneNumber());
            return Result.success(result);
        } catch (Exception e) {
            log.error("用户邀请情侣失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "接受邀请", description = "使用邀请码接受情侣邀请")
    @PostMapping("/accept")
    public Result<CoupleService.AcceptResult> acceptInvite(
            @Valid @RequestBody CoupleAcceptDTO acceptDTO,
            HttpServletRequest request) {
        try {
            Long userId = UserContextHolder.getUserId();
            CoupleService.AcceptResult result = coupleService.acceptInvite(userId, acceptDTO.getInviteCode());
            log.info("用户接受邀请成功: userId={}, inviteCode={}", userId, acceptDTO.getInviteCode());
            return Result.success(result);
        } catch (Exception e) {
            log.error("用户接受邀请失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "获取情侣状态", description = "获取当前用户的情侣关系状态")
    @GetMapping("/status")
    public Result<CoupleService.CoupleStatus> getCoupleStatus(HttpServletRequest request) {
        try {
            Long userId = UserContextHolder.getUserId();
            CoupleService.CoupleStatus status = coupleService.getCoupleStatus(userId);
            return Result.success(status);
        } catch (Exception e) {
            log.error("获取情侣状态失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "获取情侣信息", description = "获取当前用户的情侣详细信息（仅已确认关系）")
    @GetMapping("/info")
    public Result<CoupleService.CoupleInfo> getCoupleInfo(HttpServletRequest request) {
        try {
            Long userId = UserContextHolder.getUserId();
            CoupleService.CoupleInfo info = coupleService.getCoupleInfo(userId);
            return Result.success(info);
        } catch (Exception e) {
            log.error("获取情侣信息失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "解除情侣关系", description = "解除当前用户的情侣关系")
    @DeleteMapping("/unbind")
    public Result<Void> unbindCouple(HttpServletRequest request) {
        try {
            Long userId = UserContextHolder.getUserId();
            boolean success = coupleService.unbindCouple(userId);
            if (success) {
                log.info("用户解除关系成功: userId={}", userId);
                return Result.success();
            } else {
                return Result.error();
            }
        } catch (Exception e) {
            log.error("用户解除关系失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "验证邀请码", description = "验证邀请码是否有效")
    @GetMapping("/validate-invite-code")
    public Result<Boolean> validateInviteCode(@RequestParam String inviteCode) {
        try {
            boolean valid = coupleService.validateInviteCode(inviteCode);
            return Result.success(valid);
        } catch (Exception e) {
            log.error("验证邀请码失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "获取情侣转盘历史", description = "获取情侣转盘记录")
    @GetMapping("/history")
    public Result<CoupleService.CoupleSpinHistory> getCoupleHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            Long userId = UserContextHolder.getUserId();
            CoupleService.CoupleSpinHistory history = coupleService.getCoupleSpinHistory(userId, page, pageSize);
            return Result.success(history);
        } catch (Exception e) {
            log.error("获取情侣转盘历史失败: error={}", e.getMessage());
            throw e;
        }
    }
}
