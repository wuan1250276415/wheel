package com.basebackend.wheel.controller;

import com.basebackend.common.context.UserContextHolder;
import com.basebackend.common.model.Result;
import com.basebackend.wheel.dto.*;
import com.basebackend.wheel.service.MembershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/membership")
@Tag(name = "会员订阅管理", description = "会员订阅相关接口")
public class MembershipController {

    @Autowired
    private MembershipService membershipService;

    @Operation(summary = "获取所有会员套餐", description = "获取所有可用的会员套餐列表")
    @GetMapping("/plans")
    public Result<List<MembershipPlanVO>> getAllPlans() {
        try {
            List<MembershipPlanVO> plans = membershipService.getAllPlans();
            return Result.success(plans);
        } catch (Exception e) {
            log.error("获取会员套餐列表失败: error={}", e.getMessage(), e);
            return Result.error(500, "获取会员套餐列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取会员状态", description = "获取当前用户的会员状态")
    @GetMapping("/status")
    public Result<MembershipStatusVO> getMembershipStatus() {
        try {
            Long userId = UserContextHolder.getUserId();
            if (Objects.isNull(userId)) {
                return Result.error(401, "未登录，请先登录");
            }

            MembershipStatusVO status = membershipService.getMembershipStatus(userId);
            return Result.success(status);
        } catch (Exception e) {
            log.error("获取会员状态失败: error={}", e.getMessage(), e);
            return Result.error(500, "获取会员状态失败: " + e.getMessage());
        }
    }

    @Operation(summary = "订阅会员", description = "购买会员套餐")
    @PostMapping("/subscribe")
    public Result<PaymentResultVO> subscribe(@Valid @RequestBody SubscribeRequest request) {
        try {
            Long userId = UserContextHolder.getUserId();
            if (Objects.isNull(userId)) {
                return Result.error(401, "未登录，请先登录");
            }

            PaymentResultVO result = membershipService.subscribe(
                userId,
                request.getPlanId(),
                request.getPaymentMethod()
            );
            return Result.success(result);
        } catch (Exception e) {
            log.error("订阅会员失败: userId={}, error={}", UserContextHolder.getUserId(), e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @Operation(summary = "更新自动续费", description = "开启或关闭自动续费")
    @PutMapping("/auto-renew")
    public Result<String> updateAutoRenew(@RequestParam Boolean autoRenew) {
        try {
            Long userId = UserContextHolder.getUserId();
            if (Objects.isNull(userId)) {
                return Result.error(401, "未登录，请先登录");
            }

            membershipService.updateAutoRenew(userId, autoRenew);
            return Result.success("更新成功");
        } catch (Exception e) {
            log.error("更新自动续费失败: userId={}, error={}", UserContextHolder.getUserId(), e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @Operation(summary = "支付回调", description = "处理支付平台的回调通知")
    @PostMapping("/payment/callback")
    public Result<String> paymentCallback(
        @RequestParam String orderNo,
        @RequestParam Integer paymentStatus,
        @RequestParam(required = false) String tradeNo
    ) {
        try {
            membershipService.handlePaymentCallback(orderNo, paymentStatus, tradeNo);
            return Result.success(null, "处理成功");
        } catch (Exception e) {
            log.error("支付回调处理失败: orderNo={}, error={}", orderNo, e.getMessage(), e);
            return Result.error(500, "处理失败: " + e.getMessage());
        }
    }
}
