package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 黑名单表
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("blacklist")
public class Blacklist extends BaseEntity {

    /**
     * 类型：1-用户 2-IP 3-设备
     */
    @TableField("type")
    private Integer type;

    /**
     * 目标ID（用户ID/IP地址/设备ID）
     */
    @TableField("target_id")
    private String targetId;

    /**
     * 封禁原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 封禁时长（分钟），0表示永久
     */
    @TableField("duration")
    private Integer duration;

    /**
     * 过期时间
     */
    @TableField("expire_at")
    private LocalDateTime expireAt;

    /**
     * 操作人ID
     */
    @TableField("operator_id")
    private Long operatorId;

    /**
     * 状态：0-已解除 1-生效中
     */
    @TableField("status")
    private Integer status;

    /**
     * 申诉状态：0-未申诉 1-申诉中 2-申诉通过 3-申诉驳回
     */
    @TableField("appeal_status")
    private Integer appealStatus;

    /**
     * 申诉理由
     */
    @TableField("appeal_reason")
    private String appealReason;

    /**
     * 申诉时间
     */
    @TableField("appeal_time")
    private LocalDateTime appealTime;

    /**
     * 申诉处理人ID
     */
    @TableField("appeal_handler_id")
    private Long appealHandlerId;

    /**
     * 申诉处理时间
     */
    @TableField("appeal_handled_at")
    private LocalDateTime appealHandledAt;

    /**
     * 申诉处理结果
     */
    @TableField("appeal_result")
    private String appealResult;

    /**
     * 黑名单类型常量
     */
    public static final int TYPE_USER = 1;
    public static final int TYPE_IP = 2;
    public static final int TYPE_DEVICE = 3;

    /**
     * 状态常量
     */
    public static final int STATUS_RELEASED = 0;
    public static final int STATUS_ACTIVE = 1;

    /**
     * 申诉状态常量
     */
    public static final int APPEAL_NONE = 0;
    public static final int APPEAL_PENDING = 1;
    public static final int APPEAL_APPROVED = 2;
    public static final int APPEAL_REJECTED = 3;

    /**
     * 永久封禁时长常量
     */
    public static final int DURATION_PERMANENT = 0;

    /**
     * 判断是否为永久封禁
     */
    public boolean isPermanent() {
        return this.duration != null && this.duration == DURATION_PERMANENT;
    }

    /**
     * 判断是否已过期
     */
    public boolean isExpired() {
        if (isPermanent()) {
            return false;
        }
        return this.expireAt != null && LocalDateTime.now().isAfter(this.expireAt);
    }

    /**
     * 判断是否可以申诉
     */
    public boolean canAppeal() {
        return this.status == STATUS_ACTIVE && this.appealStatus == APPEAL_NONE;
    }
}
