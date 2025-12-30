package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class MembershipPlanVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String planName;
    private String planKey;
    private Integer tier;
    private String tierName;
    private Integer durationDays;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Map<String, Object> benefits;
    private String description;
    private Boolean isRecommended;
    private String discount;
}
