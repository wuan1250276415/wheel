package com.basebackend.wheel.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 聊天消息DTO
 */
@Data
public class ChatMessageDTO {
    /**
     * 消息ID
     */
    private Long id;

    /**
     * 接收者用户ID
     */
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    /**
     * 消息类型：1-文字，2-图片，3-转盘结果，4-系统消息
     */
    @NotNull(message = "消息类型不能为空")
    private Integer messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 扩展数据（图片URL、转盘结果等）
     */
    private String extraData;
}
