package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 客服人员表
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("support_staff")
public class SupportStaff extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 客服姓名
     */
    @TableField("staff_name")
    private String staffName;

    /**
     * 客服邮箱
     */
    @TableField("staff_email")
    private String staffEmail;

    /**
     * 是否为VIP专属客服：0-否 1-是
     */
    @TableField("is_vip_dedicated")
    private Integer isVipDedicated;

    /**
     * 最大同时处理工单数
     */
    @TableField("max_tickets")
    private Integer maxTickets;

    /**
     * 当前处理工单数
     */
    @TableField("current_tickets")
    private Integer currentTickets;

    /**
     * 状态：0-离线 1-在线
     */
    @TableField("status")
    private Integer status;
}
