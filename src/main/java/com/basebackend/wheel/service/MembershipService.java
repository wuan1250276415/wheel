package com.basebackend.wheel.service;

import com.basebackend.wheel.dto.MembershipPlanVO;
import com.basebackend.wheel.dto.MembershipStatusVO;
import com.basebackend.wheel.dto.PaymentResultVO;

import java.util.List;

public interface MembershipService {

    List<MembershipPlanVO> getAllPlans();

    MembershipStatusVO getMembershipStatus(Long userId);

    PaymentResultVO subscribe(Long userId, Long planId, Integer paymentMethod);

    void updateAutoRenew(Long userId, Boolean autoRenew);

    void handlePaymentCallback(String orderNo, Integer paymentStatus, String tradeNo);
}
