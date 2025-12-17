package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户行为统计表
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_behavior_stats")
public class UserBehaviorStats implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 统计ID - 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 统计日期
     */
    @TableField("stats_date")
    private LocalDate statsDate;

    /**
     * 转盘次数
     */
    @TableField("spin_count")
    private Integer spinCount;

    /**
     * 使用时长（分钟）
     */
    @TableField("usage_duration")
    private Integer usageDuration;

    /**
     * 偏好分类ID（出现次数最多的分类）
     */
    @TableField("preferred_category_id")
    private Long preferredCategoryId;

    /**
     * 平均转盘时长（毫秒）
     */
    @TableField("avg_spin_duration")
    private Long avgSpinDuration;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
