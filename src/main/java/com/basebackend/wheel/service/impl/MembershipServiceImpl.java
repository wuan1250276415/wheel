package com.basebackend.wheel.service.impl;

import com.alibaba.fastjson2.JSON;
import com.basebackend.wheel.dto.MembershipPlanVO;
import com.basebackend.wheel.dto.MembershipStatusVO;
import com.basebackend.wheel.dto.PaymentResultVO;
import com.basebackend.wheel.entity.MembershipPlan;
import com.basebackend.wheel.entity.PaymentOrder;
import com.basebackend.wheel.entity.UserMembership;
import com.basebackend.wheel.enums.MembershipStatus;
import com.basebackend.wheel.enums.MembershipTier;
import com.basebackend.wheel.enums.PaymentStatus;
import com.basebackend.wheel.mapper.MembershipPlanMapper;
import com.basebackend.wheel.mapper.PaymentOrderMapper;
import com.basebackend.wheel.mapper.UserMembershipMapper;
import com.basebackend.wheel.service.MembershipService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MembershipServiceImpl implements MembershipService {

    @Autowired
    private MembershipPlanMapper membershipPlanMapper;

    @Autowired
    private UserMembershipMapper userMembershipMapper;

    @Autowired
    private PaymentOrderMapper paymentOrderMapper;

    @Override
    public List<MembershipPlanVO> getAllPlans() {
        List<MembershipPlan> plans = membershipPlanMapper.selectEnabledPlans();

        return plans.stream().map(plan -> {
            MembershipPlanVO vo = new MembershipPlanVO();
            BeanUtils.copyProperties(plan, vo);

            vo.setTierName(MembershipTier.fromCode(plan.getTier()).getDescription());

            if (plan.getBenefits() != null) {
                vo.setBenefits(JSON.parseObject(plan.getBenefits(), Map.class));
            }

            if (plan.getOriginalPrice() != null && plan.getOriginalPrice().compareTo(plan.getPrice()) > 0) {
                BigDecimal discount = BigDecimal.ONE.subtract(
                    plan.getPrice().divide(plan.getOriginalPrice(), 4, RoundingMode.HALF_UP)
                ).multiply(BigDecimal.valueOf(100));
                vo.setDiscount(String.format("省%.0f%%", discount.doubleValue()));
            }

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public MembershipStatusVO getMembershipStatus(Long userId) {
        UserMembership membership = userMembershipMapper.selectActiveByUserId(userId);

        MembershipStatusVO vo = new MembershipStatusVO();
        vo.setUserId(userId);

        if (membership == null || membership.getEndTime().isBefore(LocalDateTime.now())) {
            vo.setTier(0);
            vo.setTierName("普通用户");
            vo.setIsActive(false);
            vo.setAutoRenew(false);
        } else {
            vo.setTier(membership.getTier());
            vo.setTierName(MembershipTier.fromCode(membership.getTier()).getDescription());
            vo.setStartTime(membership.getStartTime());
            vo.setEndTime(membership.getEndTime());
            vo.setAutoRenew(membership.getAutoRenew());
            vo.setIsActive(true);

            long remainingDays = ChronoUnit.DAYS.between(LocalDateTime.now(), membership.getEndTime());
            vo.setRemainingDays(Math.max(0, remainingDays));
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResultVO subscribe(Long userId, Long planId, Integer paymentMethod) {
        MembershipPlan plan = membershipPlanMapper.selectById(planId);
        if (plan == null || plan.getStatus() != 1) {
            throw new RuntimeException("套餐不存在或已下线");
        }

        String orderNo = generateOrderNo();

        PaymentOrder order = new PaymentOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setPlanId(planId);
        order.setAmount(plan.getPrice());
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus(PaymentStatus.PENDING.getCode());
        order.setCreateBy(userId);
        order.setUpdateBy(userId);

        paymentOrderMapper.insert(order);

        PaymentResultVO result = new PaymentResultVO();
        result.setOrderNo(orderNo);
        result.setPaymentStatus(PaymentStatus.PENDING.getCode());
        result.setMessage("订单创建成功");
        result.setPrepayData("模拟预支付数据");

        log.info("创建会员订单成功: userId={}, planId={}, orderNo={}", userId, planId, orderNo);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAutoRenew(Long userId, Boolean autoRenew) {
        UserMembership membership = userMembershipMapper.selectActiveByUserId(userId);
        if (membership == null) {
            throw new RuntimeException("未找到有效会员");
        }

        membership.setAutoRenew(autoRenew);
        membership.setUpdateBy(userId);
        userMembershipMapper.updateById(membership);

        log.info("更新自动续费状态: userId={}, autoRenew={}", userId, autoRenew);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePaymentCallback(String orderNo, Integer paymentStatus, String tradeNo) {
        PaymentOrder order = paymentOrderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getPaymentStatus().equals(PaymentStatus.SUCCESS.getCode())) {
            log.warn("订单已支付，忽略回调: orderNo={}", orderNo);
            return;
        }

        order.setPaymentStatus(paymentStatus);
        order.setTradeNo(tradeNo);

        if (paymentStatus.equals(PaymentStatus.SUCCESS.getCode())) {
            order.setPaidAt(LocalDateTime.now());
            paymentOrderMapper.updateById(order);

            activateMembership(order);

            log.info("支付成功，激活会员: orderNo={}, userId={}", orderNo, order.getUserId());
        } else {
            paymentOrderMapper.updateById(order);
            log.warn("支付失败: orderNo={}, status={}", orderNo, paymentStatus);
        }
    }

    private void activateMembership(PaymentOrder order) {
        MembershipPlan plan = membershipPlanMapper.selectById(order.getPlanId());
        if (plan == null) {
            throw new RuntimeException("套餐不存在");
        }

        UserMembership existingMembership = userMembershipMapper.selectActiveByUserId(order.getUserId());

        LocalDateTime startTime;
        LocalDateTime endTime;

        if (existingMembership != null && existingMembership.getEndTime().isAfter(LocalDateTime.now())) {
            startTime = existingMembership.getEndTime();
        } else {
            startTime = LocalDateTime.now();
        }

        endTime = startTime.plusDays(plan.getDurationDays());

        if (existingMembership != null) {
            userMembershipMapper.updateStatusByUserId(order.getUserId(), MembershipStatus.EXPIRED.getCode());
        }

        UserMembership newMembership = new UserMembership();
        newMembership.setUserId(order.getUserId());
        newMembership.setPlanId(order.getPlanId());
        newMembership.setTier(plan.getTier());
        newMembership.setStartTime(startTime);
        newMembership.setEndTime(endTime);
        newMembership.setStatus(MembershipStatus.ACTIVE.getCode());
        newMembership.setAutoRenew(false);
        newMembership.setOrderId(order.getId());
        newMembership.setCreateBy(order.getUserId());
        newMembership.setUpdateBy(order.getUserId());

        userMembershipMapper.insert(newMembership);
    }

    private String generateOrderNo() {
        return "MBR" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
