package com.basebackend.wheel.dto;

import lombok.Data;

@Data
public class TaskRewardVO {
    private Long id;
    private Long userId;
    private Long taskId;
    private String taskName;
    private Integer rewardType;
    private String rewardTypeDesc;
    private Integer rewardValue;
    private String claimedAt;
}
