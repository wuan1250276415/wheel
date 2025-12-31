package com.basebackend.wheel.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 黑名单条目DTO - 用于添加黑名单
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
public class BlacklistEntryDTO {

    /**
     * 类型：1-用户 2-IP 3-设备
     */
    @NotNull(message = "黑名单类型不能为空")
    private Integer type;

    /**
     * 目标ID（用户ID/IP地址/设备ID）
     */
    @NotBlank(message = "目标ID不能为空")
    private String targetId;

    /**
     * 封禁原因
     */
    @NotBlank(message = "封禁原因不能为空")
    private String reason;

    /**
     * 封禁时长（分钟），0表示永久
     */
    private Integer duration;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 黑名单类型常量
     */
    public static final int TYPE_USER = 1;
    public static final int TYPE_IP = 2;
    public static final int TYPE_DEVICE = 3;

    /**
     * 永久封禁时长常量
     */
    public static final int DURATION_PERMANENT = 0;

    /**
     * 判断是否为永久封禁
     */
    public boolean isPermanent() {
        return this.duration == null || this.duration == DURATION_PERMANENT;
    }
}
