package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.basebackend.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 情侣动态表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.couple_moment", autoResultMap = true)
public class CoupleMoment extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 情侣关系ID
     */
    @TableField(value = "couple_id")
    private Long coupleId;

    /**
     * 发布者用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 动态文本内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 图片URL数组（最多9张）
     */
    @TableField(value = "images", typeHandler = JacksonTypeHandler.class)
    private List<String> images;

    /**
     * 可见性：0-仅情侣可见，1-公开
     */
    @TableField(value = "visibility")
    private Integer visibility;

    /**
     * 点赞数
     */
    @TableField(value = "like_count")
    private Integer likeCount;

    /**
     * 评论数
     */
    @TableField(value = "comment_count")
    private Integer commentCount;
}
