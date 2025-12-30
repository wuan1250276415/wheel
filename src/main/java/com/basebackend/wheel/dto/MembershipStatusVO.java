package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class MembershipStatusVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private Integer tier;
    private String tierName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean autoRenew;
    private Boolean isActive;
    private Long remainingDays;
}
