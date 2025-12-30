package com.basebackend.wheel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.basebackend.common.exception.BusinessException;
import com.basebackend.wheel.dto.TaskRewardVO;
import com.basebackend.wheel.dto.TaskSummaryVO;
import com.basebackend.wheel.dto.TaskVO;
import com.basebackend.wheel.entity.TaskReward;
import com.basebackend.wheel.entity.TaskTemplate;
import com.basebackend.wheel.entity.UserTask;
import com.basebackend.wheel.enums.*;
import com.basebackend.wheel.mapper.TaskRewardMapper;
import com.basebackend.wheel.mapper.TaskTemplateMapper;
import com.basebackend.wheel.mapper.UserTaskMapper;
import com.basebackend.wheel.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskTemplateMapper taskTemplateMapper;

    @Autowired
    private UserTaskMapper userTaskMapper;

    @Autowired
    private TaskRewardMapper taskRewardMapper;

    @Override
    public TaskSummaryVO getUserTasks(Long userId) {
        TaskSummaryVO summary = new TaskSummaryVO();

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<UserTask> dailyTasks = userTaskMapper.selectByUserIdAndPeriod(
                userId, TaskPeriodType.DAILY.getCode(), today);
        List<UserTask> weeklyTasks = userTaskMapper.selectByUserIdAndPeriod(
                userId, TaskPeriodType.WEEKLY.getCode(), weekStart);

        summary.setDailyTasks(convertToVO(dailyTasks));
        summary.setWeeklyTasks(convertToVO(weeklyTasks));

        int totalCompleted = (int) Stream.concat(
                dailyTasks.stream(),
                weeklyTasks.stream()
        ).filter(t -> t.getStatus() >= TaskStatus.COMPLETED.getCode()).count();

        summary.setTotalCompleted(totalCompleted);
        summary.setTotalRewards(taskRewardMapper.selectByUserId(userId).size());

        return summary;
    }

    @Override
    @Transactional
    public TaskVO updateProgress(Long userId, Long userTaskId, Integer progressDelta) {
        UserTask userTask = userTaskMapper.selectById(userTaskId);
        if (userTask == null || !userTask.getUserId().equals(userId)) {
            throw new BusinessException("任务不存在");
        }

        if (userTask.getStatus() != TaskStatus.IN_PROGRESS.getCode()) {
            throw new BusinessException("任务状态不允许更新进度");
        }

        int newCount = userTask.getCurrentCount() + progressDelta;
        int newStatus = userTask.getStatus();

        if (newCount >= userTask.getTargetCount()) {
            newCount = userTask.getTargetCount();
            newStatus = TaskStatus.COMPLETED.getCode();
            userTask.setCompletedAt(LocalDateTime.now());
        }

        userTask.setCurrentCount(newCount);
        userTask.setStatus(newStatus);
        userTaskMapper.updateById(userTask);

        TaskTemplate template = taskTemplateMapper.selectById(userTask.getTaskId());
        return convertToVO(userTask, template);
    }

    @Override
    @Transactional
    public TaskVO claimReward(Long userId, Long userTaskId) {
        UserTask userTask = userTaskMapper.selectById(userTaskId);
        if (userTask == null || !userTask.getUserId().equals(userId)) {
            throw new BusinessException("任务不存在");
        }

        if (userTask.getStatus() != TaskStatus.COMPLETED.getCode()) {
            throw new BusinessException("任务未完成,无法领取奖励");
        }

        TaskTemplate template = taskTemplateMapper.selectById(userTask.getTaskId());
        if (template == null) {
            throw new BusinessException("任务模板不存在");
        }

        TaskReward reward = new TaskReward();
        reward.setUserId(userId);
        reward.setTaskId(template.getId());
        reward.setUserTaskId(userTaskId);
        reward.setRewardType(template.getRewardType());
        reward.setRewardValue(template.getRewardValue());
        reward.setClaimedAt(LocalDateTime.now());
        taskRewardMapper.insert(reward);

        userTask.setStatus(TaskStatus.CLAIMED.getCode());
        userTask.setClaimedAt(LocalDateTime.now());
        userTaskMapper.updateById(userTask);

        log.info("用户领取任务奖励: userId={}, taskId={}, rewardType={}, rewardValue={}",
                userId, template.getId(), template.getRewardType(), template.getRewardValue());

        return convertToVO(userTask, template);
    }

    @Override
    public List<TaskRewardVO> getUserRewards(Long userId) {
        List<TaskReward> rewards = taskRewardMapper.selectByUserId(userId);
        return rewards.stream().map(this::convertToRewardVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void initializeDailyTasks() {
        log.info("开始初始化每日任务");
        List<TaskTemplate> templates = taskTemplateMapper.selectActiveTemplates(TaskPeriodType.DAILY.getCode());

        LocalDate today = LocalDate.now();

        for (TaskTemplate template : templates) {
            log.info("初始化任务模板: {}", template.getTaskName());
        }

        log.info("每日任务初始化完成,共处理 {} 个模板", templates.size());
    }

    @Override
    @Transactional
    public void initializeWeeklyTasks() {
        log.info("开始初始化每周任务");
        List<TaskTemplate> templates = taskTemplateMapper.selectActiveTemplates(TaskPeriodType.WEEKLY.getCode());

        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        for (TaskTemplate template : templates) {
            log.info("初始化任务模板: {}", template.getTaskName());
        }

        log.info("每周任务初始化完成,共处理 {} 个模板", templates.size());
    }

    @Override
    @Transactional
    public void expireTasks() {
        log.info("开始清理过期任务");
        LocalDate currentDate = LocalDate.now();
        List<UserTask> expiredTasks = userTaskMapper.selectExpiredTasks(currentDate);

        for (UserTask task : expiredTasks) {
            task.setStatus(TaskStatus.EXPIRED.getCode());
            userTaskMapper.updateById(task);
        }

        log.info("过期任务清理完成,共处理 {} 个任务", expiredTasks.size());
    }

    private List<TaskVO> convertToVO(List<UserTask> userTasks) {
        return userTasks.stream().map(userTask -> {
            TaskTemplate template = taskTemplateMapper.selectById(userTask.getTaskId());
            return convertToVO(userTask, template);
        }).collect(Collectors.toList());
    }

    private TaskVO convertToVO(UserTask userTask, TaskTemplate template) {
        TaskVO vo = new TaskVO();
        vo.setId(template.getId());
        vo.setTaskCode(template.getTaskCode());
        vo.setTaskName(template.getTaskName());
        vo.setTaskType(template.getTaskType());
        vo.setTaskTypeDesc(TaskType.fromCode(template.getTaskType()).getDescription());
        vo.setDescription(template.getDescription());
        vo.setDifficulty(template.getDifficulty());
        vo.setDifficultyDesc(TaskDifficulty.fromCode(template.getDifficulty()).getDescription());
        vo.setPeriodType(template.getPeriodType());
        vo.setPeriodTypeDesc(TaskPeriodType.fromCode(template.getPeriodType()).getDescription());
        vo.setTargetCount(template.getTargetCount());
        vo.setRewardType(template.getRewardType());
        vo.setRewardTypeDesc(RewardType.fromCode(template.getRewardType()).getDescription());
        vo.setRewardValue(template.getRewardValue());

        vo.setUserTaskId(userTask.getId());
        vo.setCurrentCount(userTask.getCurrentCount());
        vo.setStatus(userTask.getStatus());
        vo.setStatusDesc(TaskStatus.fromCode(userTask.getStatus()).getDescription());
        vo.setProgress((int)((userTask.getCurrentCount() * 100.0) / userTask.getTargetCount()));
        vo.setPeriodStart(userTask.getPeriodStart().format(DateTimeFormatter.ISO_DATE));
        vo.setPeriodEnd(userTask.getPeriodEnd().format(DateTimeFormatter.ISO_DATE));

        return vo;
    }

    private TaskRewardVO convertToRewardVO(TaskReward reward) {
        TaskRewardVO vo = new TaskRewardVO();
        vo.setId(reward.getId());
        vo.setUserId(reward.getUserId());
        vo.setTaskId(reward.getTaskId());

        TaskTemplate template = taskTemplateMapper.selectById(reward.getTaskId());
        if (template != null) {
            vo.setTaskName(template.getTaskName());
        }

        vo.setRewardType(reward.getRewardType());
        vo.setRewardTypeDesc(RewardType.fromCode(reward.getRewardType()).getDescription());
        vo.setRewardValue(reward.getRewardValue());
        vo.setClaimedAt(reward.getClaimedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return vo;
    }
}
