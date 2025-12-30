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

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.membership_plan")
public class MembershipPlan extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableField(value = "plan_name")
    @Size(max = 100, message = "套餐名称最大长度要小于 100")
    @NotBlank(message = "套餐名称不能为空")
    private String planName;

    @TableField(value = "plan_key")
    @Size(max = 50, message = "套餐标识key最大长度要小于 50")
    @NotBlank(message = "套餐标识key不能为空")
    private String planKey;

    @TableField(value = "tier")
    @NotNull(message = "会员等级不能为空")
    private Integer tier;

    @TableField(value = "duration_days")
    @NotNull(message = "时长不能为空")
    private Integer durationDays;

    @TableField(value = "price")
    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    @TableField(value = "original_price")
    private BigDecimal originalPrice;

    @TableField(value = "benefits")
    @NotBlank(message = "权益配置不能为空")
    private String benefits;

    @TableField(value = "description")
    private String description;

    @TableField(value = "is_recommended")
    private Boolean isRecommended;

    @TableField(value = "`status`")
    private Integer status;

    @TableField(value = "sort_order")
    private Integer sortOrder;
}
