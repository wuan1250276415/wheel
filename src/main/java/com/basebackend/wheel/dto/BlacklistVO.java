package com.basebackend.wheel.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 黑名单视图对象
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
public class BlacklistVO {

    /**
     * 黑名单ID
     */
    private Long id;

    /**
     * 类型：1-用户 2-IP 3-设备
     */
    private Integer type;

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 目标ID（用户ID/IP地址/设备ID）
     */
    private String targetId;

    /**
     * 封禁原因
     */
    private String reason;

    /**
     * 封禁时长（分钟），0表示永久
     */
    private Integer duration;

    /**
     * 过期时间
     */
    private LocalDateTime expireAt;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人名称
     */
    private String operatorName;

    /**
     * 状态：0-已解除 1-生效中
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 申诉状态：0-未申诉 1-申诉中 2-申诉通过 3-申诉驳回
     */
    private Integer appealStatus;

    /**
     * 申诉状态名称
     */
    private String appealStatusName;

    /**
     * 申诉理由
     */
    private String appealReason;

    /**
     * 申诉时间
     */
    private LocalDateTime appealTime;

    /**
     * 申诉处理人ID
     */
    private Long appealHandlerId;

    /**
     * 申诉处理人名称
     */
    private String appealHandlerName;

    /**
     * 申诉处理时间
     */
    private LocalDateTime appealHandledAt;

    /**
     * 申诉处理结果
     */
    private String appealResult;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否为永久封禁
     */
    private boolean permanent;

    /**
     * 是否可以申诉
     */
    private boolean canAppeal;

    /**
     * 获取类型名称
     */
    public static String getTypeName(Integer type) {
        if (type == null) return "未知";
        switch (type) {
            case 1: return "用户";
            case 2: return "IP";
            case 3: return "设备";
            default: return "未知";
        }
    }

    /**
     * 获取状态名称
     */
    public static String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "已解除";
            case 1: return "生效中";
            default: return "未知";
        }
    }

    /**
     * 获取申诉状态名称
     */
    public static String getAppealStatusName(Integer appealStatus) {
        if (appealStatus == null) return "未申诉";
        switch (appealStatus) {
            case 0: return "未申诉";
            case 1: return "申诉中";
            case 2: return "申诉通过";
            case 3: return "申诉驳回";
            default: return "未知";
        }
    }
}
