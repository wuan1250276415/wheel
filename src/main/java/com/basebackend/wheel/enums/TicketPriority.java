package com.basebackend.wheel.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 工单优先级枚举
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Getter
public enum TicketPriority {
    /**
     * 低优先级
     */
    LOW(1, "低"),

    /**
     * 中优先级
     */
    MEDIUM(2, "中"),

    /**
     * 高优先级
     */
    HIGH(3, "高"),

    /**
     * 紧急
     */
    URGENT(4, "紧急");

    @EnumValue
    @JsonValue
    private final Integer value;
    private final String description;

    TicketPriority(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
}
