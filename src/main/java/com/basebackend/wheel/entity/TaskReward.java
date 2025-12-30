package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.task_reward")
public class TaskReward extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableField(value = "user_id")
    @NotNull(message = "用户ID不能为null")
    private Long userId;

    @TableField(value = "task_id")
    @NotNull(message = "任务ID不能为null")
    private Long taskId;

    @TableField(value = "user_task_id")
    @NotNull(message = "用户任务ID不能为null")
    private Long userTaskId;

    @TableField(value = "reward_type")
    @NotNull(message = "奖励类型不能为null")
    private Integer rewardType;

    @TableField(value = "reward_value")
    @NotNull(message = "奖励值不能为null")
    private Integer rewardValue;

    @TableField(value = "claimed_at")
    private LocalDateTime claimedAt;
}
