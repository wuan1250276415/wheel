package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserMembershipUpgradeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer targetTier;
    private Integer durationDays;
    private String reason;
}
