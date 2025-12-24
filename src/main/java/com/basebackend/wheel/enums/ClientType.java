package com.basebackend.wheel.enums;

import lombok.Getter;

@Getter
public enum ClientType {

    WECHAT_MINI(1, "微信小程序"),
    H5(2, "H5网页"),
    APP(3, "App客户端");

    private final int code;
    private final String description;

    ClientType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ClientType fromCode(int code) {
        for (ClientType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown client type code: " + code);
    }
}
