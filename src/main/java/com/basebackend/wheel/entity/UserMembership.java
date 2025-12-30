package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.user_membership")
public class UserMembership extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableField(value = "user_id")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @TableField(value = "plan_id")
    @NotNull(message = "套餐ID不能为空")
    private Long planId;

    @TableField(value = "tier")
    @NotNull(message = "会员等级不能为空")
    private Integer tier;

    @TableField(value = "start_time")
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @TableField(value = "end_time")
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @TableField(value = "`status`")
    private Integer status;

    @TableField(value = "auto_renew")
    private Boolean autoRenew;

    @TableField(value = "order_id")
    private Long orderId;
}
