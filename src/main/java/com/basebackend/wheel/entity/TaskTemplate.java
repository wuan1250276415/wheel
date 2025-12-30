package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.task_template")
public class TaskTemplate extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableField(value = "task_code")
    @NotBlank(message = "任务编码不能为空")
    @Size(max = 50, message = "任务编码最大长度要小于 50")
    private String taskCode;

    @TableField(value = "task_name")
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 100, message = "任务名称最大长度要小于 100")
    private String taskName;

    @TableField(value = "task_type")
    @NotNull(message = "任务类型不能为null")
    private Integer taskType;

    @TableField(value = "description")
    private String description;

    @TableField(value = "difficulty")
    @NotNull(message = "难度不能为null")
    private Integer difficulty;

    @TableField(value = "period_type")
    @NotNull(message = "周期类型不能为null")
    private Integer periodType;

    @TableField(value = "target_count")
    @NotNull(message = "目标次数不能为null")
    private Integer targetCount;

    @TableField(value = "reward_type")
    @NotNull(message = "奖励类型不能为null")
    private Integer rewardType;

    @TableField(value = "reward_value")
    @NotNull(message = "奖励值不能为null")
    private Integer rewardValue;

    @TableField(value = "`status`")
    private Integer status;
}
