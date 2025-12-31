package com.basebackend.wheel.service;

import com.basebackend.wheel.entity.UserProfile;

/**
 * 用户画像服务接口
 * 负责管理用户偏好数据，支持个性化推荐
 *
 * @author wheel-api
 */
public interface UserProfileService {

    /**
     * 获取用户画像
     * 优先从Redis缓存获取，缓存未命中则从数据库加载
     *
     * @param userId 用户ID
     * @return 用户画像，如果不存在则创建新画像
     */
    UserProfile getProfile(Long userId);

    /**
     * 更新用户画像（基于新的交互）
     * 当用户完成转盘、查看或分享内容时调用
     *
     * @param userId 用户ID
     * @param interaction 用户交互信息
     */
    void updateProfile(Long userId, UserInteraction interaction);

    /**
     * 重新计算用户画像
     * 基于最近30天的用户活动数据完全重建画像
     *
     * @param userId 用户ID
     */
    void rebuildProfile(Long userId);

    /**
     * 检查画像是否过期
     * 如果用户超过7天没有活动，画像被标记为过期
     *
     * @param userId 用户ID
     * @return true表示画像过期，需要重新计算
     */
    boolean isProfileStale(Long userId);

    /**
     * 用户交互信息
     */
    class UserInteraction {
        private Long contentId;
        private Long categoryId;
        private InteractionType type;
        private java.time.LocalDateTime timestamp;

        public UserInteraction() {}

        public UserInteraction(Long contentId, Long categoryId, InteractionType type) {
            this.contentId = contentId;
            this.categoryId = categoryId;
            this.type = type;
            this.timestamp = java.time.LocalDateTime.now();
        }

        public Long getContentId() { return contentId; }
        public void setContentId(Long contentId) { this.contentId = contentId; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public InteractionType getType() { return type; }
        public void setType(InteractionType type) { this.type = type; }
        public java.time.LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(java.time.LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    /**
     * 交互类型枚举
     */
    enum InteractionType {
        SPIN(1.0),      // 转盘
        VIEW(0.3),      // 查看
        SHARE(0.5);     // 分享

        private final double weight;

        InteractionType(double weight) {
            this.weight = weight;
        }

        public double getWeight() {
            return weight;
        }
    }
}
