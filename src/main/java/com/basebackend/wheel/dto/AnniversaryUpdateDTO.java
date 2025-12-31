package com.basebackend.wheel.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 纪念日更新参数DTO
 */
@Data
public class AnniversaryUpdateDTO {

    /**
     * 纪念日类型: 1-恋爱纪念日, 2-生日, 3-自定义
     */
    private Integer anniversaryType;

    /**
     * 纪念日日期
     */
    private LocalDate anniversaryDate;

    /**
     * 纪念日名称
     */
    @Size(max = 100, message = "纪念日名称不能超过100个字符")
    private String name;

    /**
     * 提前提醒天数
     */
    private Integer remindDays;
}
