package com.basebackend.wheel.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息视图对象
 */
@Data
public class ChatMessageVO {
    /**
     * 消息ID
     */
    private Long id;

    /**
     * 情侣关系ID
     */
    private Long coupleId;

    /**
     * 发送者用户ID
     */
    private Long senderId;

    /**
     * 发送者昵称
     */
    private String senderNickname;

    /**
     * 发送者头像
     */
    private String senderAvatar;

    /**
     * 接收者用户ID
     */
    private Long receiverId;

    /**
     * 消息类型：1-文字，2-图片，3-转盘结果，4-系统消息
     */
    private Integer messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 扩展数据（图片URL、转盘结果等）
     */
    private String extraData;

    /**
     * 消息状态：0-已删除，1-正常，2-已撤回
     */
    private Integer status;

    /**
     * 撤回操作用户ID
     */
    private Long recalledBy;

    /**
     * 撤回时间
     */
    private LocalDateTime recalledAt;

    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer isRead;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 是否是自己发送的
     */
    private Boolean isSelf;
}
