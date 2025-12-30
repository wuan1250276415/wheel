package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PaymentResultVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String orderNo;
    private Integer paymentStatus;
    private String message;
    private String prepayData;
}
