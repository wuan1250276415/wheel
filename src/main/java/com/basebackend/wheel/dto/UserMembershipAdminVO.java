package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserMembershipAdminVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String userNickname;
    private Long planId;
    private String planName;
    private Integer tier;
    private String tierName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long remainingDays;
    private Boolean autoRenew;
    private Integer status;
    private Long orderId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
