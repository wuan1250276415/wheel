package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class MembershipPlanCreateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String planName;
    private String planKey;
    private Integer tier;
    private Integer durationDays;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String benefits;
    private String description;
    private Boolean isRecommended;
    private Integer sortOrder;
}
