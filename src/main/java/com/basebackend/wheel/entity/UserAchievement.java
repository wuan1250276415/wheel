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
@TableName(value = "wheel_api.user_achievement")
public class UserAchievement extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableField(value = "user_id")
    @NotNull(message = "用户ID不能为null")
    private Long userId;

    @TableField(value = "achievement_id")
    @NotNull(message = "成就ID不能为null")
    private Long achievementId;

    @TableField(value = "progress")
    private Integer progress;

    @TableField(value = "target_value")
    private Integer targetValue;

    @TableField(value = "is_unlocked")
    private Integer isUnlocked;

    @TableField(value = "unlocked_at")
    private LocalDateTime unlockedAt;
}
