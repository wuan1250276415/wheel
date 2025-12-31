package com.basebackend.wheel.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 纪念日视图对象
 */
@Data
public class AnniversaryVO {

    /**
     * 纪念日ID
     */
    private Long id;

    /**
     * 纪念日类型: 1-恋爱纪念日, 2-生日, 3-自定义
     */
    private Integer anniversaryType;

    /**
     * 纪念日类型名称
     */
    private String anniversaryTypeName;

    /**
     * 纪念日日期
     */
    private LocalDate anniversaryDate;

    /**
     * 纪念日名称
     */
    private String name;

    /**
     * 提前提醒天数
     */
    private Integer remindDays;

    /**
     * 距离纪念日还有多少天
     */
    private Long daysUntil;
}
