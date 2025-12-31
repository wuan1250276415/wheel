package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.basebackend.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户画像表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.user_profile", autoResultMap = true)
public class UserProfile extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 偏好分类及权重 {"1": 0.8, "2": 0.5}
     */
    @TableField(value = "favorite_categories", typeHandler = JacksonTypeHandler.class)
    private Map<Long, Double> favoriteCategories;

    /**
     * 活跃时段 [{"hour": 20, "weight": 0.9}]
     */
    @TableField(value = "active_time_slots", typeHandler = JacksonTypeHandler.class)
    private List<TimeSlot> activeTimeSlots;

    /**
     * 行为标签 ["romantic", "adventurous"]
     */
    @TableField(value = "behavior_tags", typeHandler = JacksonTypeHandler.class)
    private List<String> behaviorTags;

    /**
     * 总转盘次数
     */
    @TableField(value = "total_spins")
    private Integer totalSpins;

    /**
     * 最后活跃时间
     */
    @TableField(value = "last_active_time")
    private LocalDateTime lastActiveTime;

    /**
     * 是否过期：0-否，1-是
     */
    @TableField(value = "is_stale")
    private Boolean isStale;

    /**
     * 时间段内部类
     */
    @Data
    public static class TimeSlot {
        /**
         * 小时 (0-23)
         */
        private Integer hour;
        
        /**
         * 权重
         */
        private Double weight;
    }
}
