package com.basebackend.wheel.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class SubscribeRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "套餐ID不能为空")
    private Long planId;

    @NotNull(message = "支付方式不能为空")
    private Integer paymentMethod;
}
