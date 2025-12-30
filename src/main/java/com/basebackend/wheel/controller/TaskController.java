package com.basebackend.wheel.controller;

import com.basebackend.common.context.UserContextHolder;
import com.basebackend.common.model.Result;
import com.basebackend.wheel.dto.TaskProgressRequest;
import com.basebackend.wheel.dto.TaskRewardVO;
import com.basebackend.wheel.dto.TaskSummaryVO;
import com.basebackend.wheel.dto.TaskVO;
import com.basebackend.wheel.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "任务挑战", description = "任务挑战相关接口")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Operation(summary = "获取用户任务列表", description = "获取当前用户的每日和每周任务")
    @GetMapping("/summary")
    public Result<TaskSummaryVO> getUserTasks() {
        try {
            Long userId = UserContextHolder.getUserId();
            if (userId == null) {
                return Result.error(401, "未登录,请先登录");
            }

            TaskSummaryVO summary = taskService.getUserTasks(userId);
            return Result.success(summary);
        } catch (Exception e) {
            log.error("获取任务列表失败: error={}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "更新任务进度", description = "更新指定任务的进度")
    @PostMapping("/{userTaskId}/progress")
    public Result<TaskVO> updateProgress(
            @PathVariable Long userTaskId,
            @Valid @RequestBody TaskProgressRequest request) {
        try {
            Long userId = UserContextHolder.getUserId();
            if (userId == null) {
                return Result.error(401, "未登录,请先登录");
            }

            TaskVO task = taskService.updateProgress(userId, userTaskId, request.getProgressDelta());
            return Result.success(task);
        } catch (Exception e) {
            log.error("更新任务进度失败: error={}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "领取任务奖励", description = "领取已完成任务的奖励")
    @PostMapping("/{userTaskId}/claim")
    public Result<TaskVO> claimReward(@PathVariable Long userTaskId) {
        try {
            Long userId = UserContextHolder.getUserId();
            if (userId == null) {
                return Result.error(401, "未登录,请先登录");
            }

            TaskVO task = taskService.claimReward(userId, userTaskId);
            return Result.success(task);
        } catch (Exception e) {
            log.error("领取任务奖励失败: error={}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "获取用户奖励记录", description = "获取用户已领取的所有奖励")
    @GetMapping("/rewards")
    public Result<List<TaskRewardVO>> getUserRewards() {
        try {
            Long userId = UserContextHolder.getUserId();
            if (userId == null) {
                return Result.error(401, "未登录,请先登录");
            }

            List<TaskRewardVO> rewards = taskService.getUserRewards(userId);
            return Result.success(rewards);
        } catch (Exception e) {
            log.error("获取奖励记录失败: error={}", e.getMessage(), e);
            throw e;
        }
    }
}
