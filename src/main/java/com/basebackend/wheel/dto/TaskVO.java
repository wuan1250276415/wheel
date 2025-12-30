package com.basebackend.wheel.dto;

import lombok.Data;

@Data
public class TaskVO {
    private Long id;
    private String taskCode;
    private String taskName;
    private Integer taskType;
    private String taskTypeDesc;
    private String description;
    private Integer difficulty;
    private String difficultyDesc;
    private Integer periodType;
    private String periodTypeDesc;
    private Integer targetCount;
    private Integer rewardType;
    private String rewardTypeDesc;
    private Integer rewardValue;

    private Long userTaskId;
    private Integer currentCount;
    private Integer status;
    private String statusDesc;
    private Integer progress;
    private String periodStart;
    private String periodEnd;
}
