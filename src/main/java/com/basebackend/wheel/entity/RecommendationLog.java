package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 推荐记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.recommendation_log")
public class RecommendationLog extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 内容ID
     */
    @TableField(value = "content_id")
    private Long contentId;

    /**
     * 推荐分数
     */
    @TableField(value = "recommendation_score")
    private Double recommendationScore;

    /**
     * 推荐原因
     */
    @TableField(value = "recommendation_reason")
    private String recommendationReason;

    /**
     * 是否点击：0-否，1-是
     */
    @TableField(value = "is_clicked")
    private Boolean isClicked;

    /**
     * 点击时间
     */
    @TableField(value = "click_time")
    private LocalDateTime clickTime;

    /**
     * 会话ID
     */
    @TableField(value = "session_id")
    private String sessionId;
}
