package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 情侣纪念日表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.couple_anniversary", autoResultMap = true)
public class CoupleAnniversary extends BaseEntity {

    /**
     * 情侣关系ID
     */
    @TableField(value = "couple_id")
    private Long coupleId;

    /**
     * 纪念日类型: 1-恋爱纪念日, 2-生日, 3-自定义
     */
    @TableField(value = "anniversary_type")
    private Integer anniversaryType;

    /**
     * 纪念日日期
     */
    @TableField(value = "anniversary_date")
    private LocalDate anniversaryDate;

    /**
     * 纪念日名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 提前提醒天数
     */
    @TableField(value = "remind_days")
    private Integer remindDays;

    /**
     * 创建者用户ID
     */
    @TableField(value = "created_by")
    private Long createdBy;
}
