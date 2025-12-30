package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "chat_message")
public class ChatMessage extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 情侣关系ID
     */
    @TableField(value = "couple_id")
    @NotNull(message = "情侣关系ID不能为空")
    private Long coupleId;

    /**
     * 发送者用户ID
     */
    @TableField(value = "sender_id")
    @NotNull(message = "发送者ID不能为空")
    private Long senderId;

    /**
     * 接收者用户ID
     */
    @TableField(value = "receiver_id")
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    /**
     * 消息类型：1-文字，2-图片，3-转盘结果，4-系统消息
     */
    @TableField(value = "message_type")
    private Integer messageType;

    /**
     * 消息内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 扩展数据（图片URL、转盘结果等）
     */
    @TableField(value = "extra_data")
    private String extraData;

    /**
     * 是否已读：0-未读，1-已读
     */
    @TableField(value = "is_read")
    private Integer isRead;

    /**
     * 阅读时间
     */
    @TableField(value = "read_time")
    private LocalDateTime readTime;

    /**
     * 消息状态：0-已删除，1-正常，2-已撤回
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 撤回操作用户ID
     */
    @TableField(value = "recalled_by")
    private Long recalledBy;

    /**
     * 撤回时间
     */
    @TableField(value = "recalled_at")
    private LocalDateTime recalledAt;
}
