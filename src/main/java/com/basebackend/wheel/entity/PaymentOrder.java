package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.payment_order")
public class PaymentOrder extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableField(value = "order_no")
    @Size(max = 50, message = "订单号最大长度要小于 50")
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @TableField(value = "user_id")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @TableField(value = "plan_id")
    @NotNull(message = "套餐ID不能为空")
    private Long planId;

    @TableField(value = "amount")
    @NotNull(message = "订单金额不能为空")
    private BigDecimal amount;

    @TableField(value = "payment_method")
    @NotNull(message = "支付方式不能为空")
    private Integer paymentMethod;

    @TableField(value = "payment_status")
    private Integer paymentStatus;

    @TableField(value = "trade_no")
    @Size(max = 100, message = "第三方交易号最大长度要小于 100")
    private String tradeNo;

    @TableField(value = "paid_at")
    private LocalDateTime paidAt;

    @TableField(value = "refunded_at")
    private LocalDateTime refundedAt;

    @TableField(value = "refund_reason")
    @Size(max = 500, message = "退款原因最大长度要小于 500")
    private String refundReason;

    @TableField(value = "callback_data")
    private String callbackData;

    @TableField(value = "remark")
    @Size(max = 500, message = "备注最大长度要小于 500")
    private String remark;
}
