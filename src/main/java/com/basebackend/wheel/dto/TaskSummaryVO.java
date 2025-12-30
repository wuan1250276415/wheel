package com.basebackend.wheel.dto;

import lombok.Data;
import java.util.List;

@Data
public class TaskSummaryVO {
    private List<TaskVO> dailyTasks;
    private List<TaskVO> weeklyTasks;
    private Integer totalCompleted;
    private Integer totalRewards;
}
