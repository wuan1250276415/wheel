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
 * 动态评论表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.moment_comment", autoResultMap = true)
public class MomentComment extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 动态ID
     */
    @TableField(value = "moment_id")
    private Long momentId;

    /**
     * 父评论ID（用于回复）
     */
    @TableField(value = "parent_comment_id")
    private Long parentCommentId;

    /**
     * 回复的目标用户ID
     */
    @TableField(value = "reply_to_user_id")
    private Long replyToUserId;

    /**
     * 回复的目标用户昵称
     */
    @TableField(value = "reply_to_nickname")
    private String replyToNickname;

    /**
     * 评论用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 评论内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 被@提及的用户ID列表
     */
    @TableField(value = "mentioned_user_ids", typeHandler = JacksonTypeHandler.class)
    private List<Long> mentionedUserIds;
}
