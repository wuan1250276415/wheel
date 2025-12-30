package com.basebackend.wheel.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论响应 DTO
 */
@Data
public class CommentVO {

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 动态ID
     */
    private Long momentId;

    /**
     * 父评论ID
     */
    private Long parentCommentId;

    /**
     * 回复的目标用户ID
     */
    private Long replyToUserId;

    /**
     * 回复的目标用户昵称
     */
    private String replyToNickname;

    /**
     * 评论用户ID
     */
    private Long userId;

    /**
     * 评论用户昵称
     */
    private String nickname;

    /**
     * 评论用户头像
     */
    private String avatarUrl;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 被@提及的用户列表
     */
    private List<UserBriefVO> mentionedUsers;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 回复数量
     */
    private Integer replyCount;
}
