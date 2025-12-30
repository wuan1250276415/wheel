package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.user_task")
public class UserTask extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableField(value = "user_id")
    @NotNull(message = "用户ID不能为null")
    private Long userId;

    @TableField(value = "task_id")
    @NotNull(message = "任务ID不能为null")
    private Long taskId;

    @TableField(value = "current_count")
    private Integer currentCount;

    @TableField(value = "target_count")
    @NotNull(message = "目标进度不能为null")
    private Integer targetCount;

    @TableField(value = "`status`")
    private Integer status;

    @TableField(value = "period_start")
    @NotNull(message = "周期开始日期不能为null")
    private LocalDate periodStart;

    @TableField(value = "period_end")
    @NotNull(message = "周期结束日期不能为null")
    private LocalDate periodEnd;

    @TableField(value = "completed_at")
    private LocalDateTime completedAt;

    @TableField(value = "claimed_at")
    private LocalDateTime claimedAt;
}
