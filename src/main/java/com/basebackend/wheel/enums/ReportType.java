package com.basebackend.wheel.enums;

import lombok.Getter;

/**
 * 报告类型枚举
 */
@Getter
public enum ReportType {

    WEEKLY(1, "周报", 7),
    MONTHLY(2, "月报", 30),
    YEARLY(3, "年报", 365);

    private final int code;
    private final String name;
    private final int days;

    ReportType(int code, String name, int days) {
        this.code = code;
        this.name = name;
        this.days = days;
    }

    public static ReportType fromCode(int code) {
        for (ReportType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown report type code: " + code);
    }
}
