package com.basebackend.wheel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建评论请求 DTO
 */
@Data
public class CommentCreateDTO {

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容不能超过1000个字符")
    private String content;

    /**
     * 父评论ID（回复时使用）
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
     * 被@提及的用户ID列表
     */
    private List<Long> mentionedUserIds;
}
