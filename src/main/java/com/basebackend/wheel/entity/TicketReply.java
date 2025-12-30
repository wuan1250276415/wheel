package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 工单回复表
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ticket_reply")
public class TicketReply extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 工单ID
     */
    @TableField("ticket_id")
    private Long ticketId;

    /**
     * 是否客服回复：0-用户 1-客服
     */
    @TableField("is_staff")
    private Integer isStaff;

    /**
     * 发送者ID
     */
    @TableField("sender_id")
    private Long senderId;

    /**
     * 发送者姓名
     */
    @TableField("sender_name")
    private String senderName;

    /**
     * 回复内容
     */
    @TableField("content")
    private String content;

    /**
     * 附件URL（JSON数组）
     */
    @TableField("attachments")
    private String attachments;
}
