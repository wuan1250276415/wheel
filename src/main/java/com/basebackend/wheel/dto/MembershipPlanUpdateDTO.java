package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class MembershipPlanUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String planName;
    private Integer durationDays;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String benefits;
    private String description;
    private Boolean isRecommended;
    private Integer sortOrder;
}
