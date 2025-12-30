package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 工单评价表
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ticket_rating")
public class TicketRating extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 工单ID
     */
    @TableField("ticket_id")
    private Long ticketId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 评分：1-5星
     */
    @TableField("rating")
    private Integer rating;

    /**
     * 评价内容
     */
    @TableField("comment")
    private String comment;

    /**
     * 评价标签（JSON数组）
     */
    @TableField("tags")
    private String tags;
}
