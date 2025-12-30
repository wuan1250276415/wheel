package com.basebackend.wheel.dto;

import com.basebackend.wheel.enums.TicketPriority;
import com.basebackend.wheel.enums.TicketStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单VO
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Data
public class TicketVO {
    /**
     * 工单ID
     */
    private Long id;

    /**
     * 工单编号
     */
    private String ticketNo;

    /**
     * 工单标题
     */
    private String title;

    /**
     * 问题描述
     */
    private String description;

    /**
     * 问题分类
     */
    private String category;

    /**
     * 优先级
     */
    private TicketPriority priority;

    /**
     * 状态
     */
    private TicketStatus status;

    /**
     * 分配客服姓名
     */
    private String assignedStaffName;

    /**
     * 是否VIP专属客服
     */
    private Boolean isVipDedicated;

    /**
     * 满意度评分
     */
    private Integer rating;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 解决时间
     */
    private LocalDateTime resolvedAt;

    /**
     * 未读回复数
     */
    private Integer unreadCount;
}
