package com.basebackend.wheel.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {

    WECHAT(1, "微信支付"),
    ALIPAY(2, "支付宝");

    private final int code;
    private final String description;

    PaymentMethod(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static PaymentMethod fromCode(int code) {
        for (PaymentMethod method : values()) {
            if (method.code == code) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown payment method code: " + code);
    }
}
