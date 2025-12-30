package com.basebackend.wheel.service;

import com.basebackend.wheel.dto.TaskProgressRequest;
import com.basebackend.wheel.dto.TaskRewardVO;
import com.basebackend.wheel.dto.TaskSummaryVO;
import com.basebackend.wheel.dto.TaskVO;

import java.util.List;

public interface TaskService {

    TaskSummaryVO getUserTasks(Long userId);

    TaskVO updateProgress(Long userId, Long userTaskId, Integer progressDelta);

    TaskVO claimReward(Long userId, Long userTaskId);

    List<TaskRewardVO> getUserRewards(Long userId);

    void initializeDailyTasks();

    void initializeWeeklyTasks();

    void expireTasks();
}
