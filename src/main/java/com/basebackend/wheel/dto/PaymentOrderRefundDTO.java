package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PaymentOrderRefundDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String refundReason;
    private BigDecimal refundAmount;
}
