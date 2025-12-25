package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户行为统计表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.user_behavior_stats")
public class UserBehaviorStats extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    @NotNull(message = "用户ID不能为null")
    private Long userId;

    /**
     * 统计日期
     */
    @TableField(value = "stats_date")
    @NotNull(message = "统计日期不能为null")
    private LocalDate statsDate;

    /**
     * 转盘次数
     */
    @TableField(value = "spin_count")
    private Integer spinCount;

    /**
     * 使用时长（分钟）
     */
    @TableField(value = "usage_duration")
    private Integer usageDuration;

    /**
     * 偏好分类ID
     */
    @TableField(value = "preferred_category_id")
    private Long preferredCategoryId;

    /**
     * 平均转盘时长（毫秒）
     */
    @TableField(value = "avg_spin_duration")
    private Long avgSpinDuration;
}