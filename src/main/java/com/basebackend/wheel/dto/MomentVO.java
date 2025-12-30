package com.basebackend.wheel.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 动态响应 DTO
 */
@Data
public class MomentVO {

    /**
     * 动态ID
     */
    private Long id;

    /**
     * 情侣关系ID
     */
    private Long coupleId;

    /**
     * 发布者用户ID
     */
    private Long userId;

    /**
     * 发布者昵称
     */
    private String nickname;

    /**
     * 发布者头像
     */
    private String avatarUrl;

    /**
     * 动态文本内容
     */
    private String content;

    /**
     * 图片URL列表
     */
    private List<String> images;

    /**
     * 可见性：0-仅情侣可见，1-公开
     */
    private Integer visibility;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
