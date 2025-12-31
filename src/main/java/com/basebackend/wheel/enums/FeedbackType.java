package com.basebackend.wheel.enums;

import lombok.Getter;

/**
 * 推荐反馈类型枚举
 * 
 * @author wheel-api
 * @since 2025-01-31
 */
@Getter
public enum FeedbackType {

    CLICK("CLICK", "点击"),
    IGNORE("IGNORE", "忽略"),
    LIKE("LIKE", "喜欢"),
    DISLIKE("DISLIKE", "不喜欢");

    private final String code;
    private final String description;

    FeedbackType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static FeedbackType fromCode(String code) {
        for (FeedbackType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown feedback type code: " + code);
    }
}
