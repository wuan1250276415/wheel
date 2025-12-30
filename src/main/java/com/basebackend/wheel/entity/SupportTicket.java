package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import com.basebackend.wheel.enums.TicketPriority;
import com.basebackend.wheel.enums.TicketStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工单表
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("support_ticket")
public class SupportTicket extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 工单编号
     */
    @TableField("ticket_no")
    private String ticketNo;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 工单标题
     */
    @TableField("title")
    private String title;

    /**
     * 问题描述
     */
    @TableField("description")
    private String description;

    /**
     * 问题分类
     */
    @TableField("category")
    private String category;

    /**
     * 优先级
     */
    @TableField("priority")
    private TicketPriority priority;

    /**
     * 状态
     */
    @TableField("status")
    private TicketStatus status;

    /**
     * 分配客服ID
     */
    @TableField("assigned_to")
    private Long assignedTo;

    /**
     * 满意度评分：1-5星
     */
    @TableField("rating")
    private Integer rating;

    /**
     * 评价内容
     */
    @TableField("rating_comment")
    private String ratingComment;

    /**
     * 解决时间
     */
    @TableField("resolved_at")
    private LocalDateTime resolvedAt;

    /**
     * 关闭时间
     */
    @TableField("closed_at")
    private LocalDateTime closedAt;
}
