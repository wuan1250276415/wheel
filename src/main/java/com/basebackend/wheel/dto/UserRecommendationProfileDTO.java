package com.basebackend.wheel.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户推荐画像DTO（内部使用）
 * 
 * @author wheel-api
 * @since 2025-01-31
 */
@Data
public class UserRecommendationProfileDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 偏好分类及权重（分类ID -> 偏好权重）
     */
    private Map<Long, Double> favoriteCategories;

    /**
     * 活跃时段列表
     */
    private List<TimeSlotDTO> activeTimeSlots;

    /**
     * 行为标签
     */
    private List<String> behaviorTags;

    /**
     * 总转盘次数
     */
    private Integer totalSpins;

    /**
     * 最后活跃时间
     */
    private LocalDateTime lastActiveTime;

    /**
     * 画像是否过期
     */
    private boolean stale;

    /**
     * 时段DTO
     */
    @Data
    public static class TimeSlotDTO {
        /**
         * 小时（0-23）
         */
        private Integer hour;

        /**
         * 权重
         */
        private Double weight;
    }
}
