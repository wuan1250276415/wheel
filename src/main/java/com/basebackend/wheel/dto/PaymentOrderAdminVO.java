package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class PaymentOrderAdminVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String orderNo;
    private Long userId;
    private String userNickname;
    private Long planId;
    private String planName;
    private BigDecimal amount;
    private Integer paymentMethod;
    private String paymentMethodName;
    private Integer paymentStatus;
    private String paymentStatusName;
    private String tradeNo;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private String refundReason;
    private Map<String, Object> callbackData;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
