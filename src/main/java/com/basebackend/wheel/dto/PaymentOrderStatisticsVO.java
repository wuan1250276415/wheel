package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class PaymentOrderStatisticsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private BigDecimal totalAmount;
    private Long totalCount;
    private Long successCount;
    private BigDecimal successRate;
    private Map<String, BigDecimal> dailyAmount;
    private Map<String, Long> dailyCount;
}
