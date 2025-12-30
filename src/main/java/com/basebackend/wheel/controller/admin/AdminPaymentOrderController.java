package com.basebackend.wheel.controller.admin;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basebackend.common.model.Result;
import com.basebackend.security.annotation.RequiresPermission;
import com.basebackend.wheel.dto.*;
import com.basebackend.wheel.entity.MembershipPlan;
import com.basebackend.wheel.entity.PaymentOrder;
import com.basebackend.wheel.enums.PaymentMethod;
import com.basebackend.wheel.enums.PaymentStatus;
import com.basebackend.wheel.mapper.MembershipPlanMapper;
import com.basebackend.wheel.mapper.PaymentOrderMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/membership/orders")
@Tag(name = "管理端-支付订单管理", description = "管理员管理支付订单")
public class AdminPaymentOrderController {

    @Autowired
    private PaymentOrderMapper paymentOrderMapper;

    @Autowired
    private MembershipPlanMapper membershipPlanMapper;

    @Operation(summary = "分页查询订单列表")
    @GetMapping
    @RequiresPermission("membership:order:view")
    public Result<IPage<PaymentOrderAdminVO>> queryOrders(PaymentOrderQueryDTO queryDTO) {
        try {
            Page<PaymentOrder> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

            LambdaQueryWrapper<PaymentOrder> wrapper = new LambdaQueryWrapper<>();

            if (StringUtils.hasText(queryDTO.getOrderNo())) {
                wrapper.eq(PaymentOrder::getOrderNo, queryDTO.getOrderNo());
            }

            if (queryDTO.getUserId() != null) {
                wrapper.eq(PaymentOrder::getUserId, queryDTO.getUserId());
            }

            if (queryDTO.getPaymentStatus() != null) {
                wrapper.eq(PaymentOrder::getPaymentStatus, queryDTO.getPaymentStatus());
            }

            if (queryDTO.getStartTime() != null) {
                wrapper.ge(PaymentOrder::getCreateTime, queryDTO.getStartTime());
            }

            if (queryDTO.getEndTime() != null) {
                wrapper.le(PaymentOrder::getCreateTime, queryDTO.getEndTime());
            }

            wrapper.orderByDesc(PaymentOrder::getCreateTime);

            IPage<PaymentOrder> orderPage = paymentOrderMapper.selectPage(page, wrapper);

            IPage<PaymentOrderAdminVO> result = orderPage.convert(order -> {
                PaymentOrderAdminVO vo = new PaymentOrderAdminVO();
                BeanUtils.copyProperties(order, vo);

                vo.setPaymentMethodName(PaymentMethod.fromCode(order.getPaymentMethod()).getDescription());
                vo.setPaymentStatusName(PaymentStatus.fromCode(order.getPaymentStatus()).getDescription());

                MembershipPlan plan = membershipPlanMapper.selectById(order.getPlanId());
                if (plan != null) {
                    vo.setPlanName(plan.getPlanName());
                }

                if (order.getCallbackData() != null) {
                    vo.setCallbackData(JSON.parseObject(order.getCallbackData(), Map.class));
                }

                return vo;
            });

            return Result.success(result);
        } catch (Exception e) {
            log.error("查询订单列表失败: error={}", e.getMessage(), e);
            return Result.error(500, "查询订单列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/{id}")
    @RequiresPermission("membership:order:view")
    public Result<PaymentOrderAdminVO> getOrderDetail(@PathVariable Long id) {
        try {
            PaymentOrder order = paymentOrderMapper.selectById(id);

            if (order == null) {
                return Result.error(404, "订单不存在");
            }

            PaymentOrderAdminVO vo = new PaymentOrderAdminVO();
            BeanUtils.copyProperties(order, vo);

            vo.setPaymentMethodName(PaymentMethod.fromCode(order.getPaymentMethod()).getDescription());
            vo.setPaymentStatusName(PaymentStatus.fromCode(order.getPaymentStatus()).getDescription());

            MembershipPlan plan = membershipPlanMapper.selectById(order.getPlanId());
            if (plan != null) {
                vo.setPlanName(plan.getPlanName());
            }

            if (order.getCallbackData() != null) {
                vo.setCallbackData(JSON.parseObject(order.getCallbackData(), Map.class));
            }

            return Result.success(vo);
        } catch (Exception e) {
            log.error("获取订单详情失败: id={}, error={}", id, e.getMessage(), e);
            return Result.error(500, "获取订单详情失败: " + e.getMessage());
        }
    }

    @Operation(summary = "订单统计")
    @GetMapping("/statistics")
    @RequiresPermission("membership:order:view")
    public Result<PaymentOrderStatisticsVO> getOrderStatistics(PaymentOrderQueryDTO queryDTO) {
        try {
            LambdaQueryWrapper<PaymentOrder> wrapper = new LambdaQueryWrapper<>();

            if (queryDTO.getStartTime() != null) {
                wrapper.ge(PaymentOrder::getCreateTime, queryDTO.getStartTime());
            }

            if (queryDTO.getEndTime() != null) {
                wrapper.le(PaymentOrder::getCreateTime, queryDTO.getEndTime());
            }

            List<PaymentOrder> orders = paymentOrderMapper.selectList(wrapper);

            BigDecimal totalAmount = BigDecimal.ZERO;
            long totalCount = orders.size();
            long successCount = 0;

            for (PaymentOrder order : orders) {
                if (order.getPaymentStatus().equals(PaymentStatus.SUCCESS.getCode())) {
                    totalAmount = totalAmount.add(order.getAmount());
                    successCount++;
                }
            }

            BigDecimal successRate = totalCount > 0
                ? BigDecimal.valueOf(successCount).divide(BigDecimal.valueOf(totalCount), 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

            PaymentOrderStatisticsVO vo = new PaymentOrderStatisticsVO();
            vo.setTotalAmount(totalAmount);
            vo.setTotalCount(totalCount);
            vo.setSuccessCount(successCount);
            vo.setSuccessRate(successRate);
            vo.setDailyAmount(new HashMap<>());
            vo.setDailyCount(new HashMap<>());

            return Result.success(vo);
        } catch (Exception e) {
            log.error("订单统计失败: error={}", e.getMessage(), e);
            return Result.error(500, "订单统计失败: " + e.getMessage());
        }
    }

    @Operation(summary = "订单退款")
    @PostMapping("/{id}/refund")
    @RequiresPermission("membership:order:refund")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> refundOrder(@PathVariable Long id, @RequestBody PaymentOrderRefundDTO refundDTO) {
        try {
            PaymentOrder order = paymentOrderMapper.selectById(id);

            if (order == null) {
                return Result.error(404, "订单不存在");
            }

            if (!order.getPaymentStatus().equals(PaymentStatus.SUCCESS.getCode())) {
                return Result.error(400, "只有支付成功的订单才能退款");
            }

            if (order.getPaymentStatus().equals(PaymentStatus.REFUNDED.getCode())) {
                return Result.error(400, "订单已退款，不能重复退款");
            }

            order.setPaymentStatus(PaymentStatus.REFUNDED.getCode());
            order.setRefundedAt(LocalDateTime.now());
            order.setRefundReason(refundDTO.getRefundReason());

            paymentOrderMapper.updateById(order);

            log.info("订单退款成功: orderId={}, orderNo={}, reason={}", id, order.getOrderNo(), refundDTO.getRefundReason());
            return Result.success(null);
        } catch (Exception e) {
            log.error("订单退款失败: orderId={}, error={}", id, e.getMessage(), e);
            return Result.error(500, "订单退款失败: " + e.getMessage());
        }
    }

    @Operation(summary = "添加订单备注")
    @PutMapping("/{id}/remark")
    @RequiresPermission("membership:order:view")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateOrderRemark(@PathVariable Long id, @RequestParam String remark) {
        try {
            PaymentOrder order = paymentOrderMapper.selectById(id);

            if (order == null) {
                return Result.error(404, "订单不存在");
            }

            order.setRemark(remark);
            paymentOrderMapper.updateById(order);

            log.info("添加订单备注成功: orderId={}, orderNo={}", id, order.getOrderNo());
            return Result.success(null);
        } catch (Exception e) {
            log.error("添加订单备注失败: orderId={}, error={}", id, e.getMessage(), e);
            return Result.error(500, "添加订单备注失败: " + e.getMessage());
        }
    }
}
