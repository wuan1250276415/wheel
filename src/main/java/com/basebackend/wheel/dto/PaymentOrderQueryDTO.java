package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PaymentOrderQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String orderNo;
    private Long userId;
    private Integer paymentStatus;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
