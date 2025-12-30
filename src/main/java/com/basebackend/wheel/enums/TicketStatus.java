package com.basebackend.wheel.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 工单状态枚举
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Getter
public enum TicketStatus {
    /**
     * 待处理
     */
    PENDING(1, "待处理"),

    /**
     * 处理中
     */
    IN_PROGRESS(2, "处理中"),

    /**
     * 待回复
     */
    AWAITING_REPLY(3, "待回复"),

    /**
     * 已解决
     */
    RESOLVED(4, "已解决"),

    /**
     * 已关闭
     */
    CLOSED(5, "已关闭");

    @EnumValue
    @JsonValue
    private final Integer value;
    private final String description;

    TicketStatus(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
}
