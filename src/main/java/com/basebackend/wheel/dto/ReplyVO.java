package com.basebackend.wheel.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单回复VO
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Data
public class ReplyVO {
    /**
     * 回复ID
     */
    private Long id;

    /**
     * 是否客服回复
     */
    private Boolean isStaff;

    /**
     * 发送者姓名
     */
    private String senderName;

    /**
     * 回复内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
