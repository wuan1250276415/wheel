package com.basebackend.wheel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 纪念日创建参数DTO
 */
@Data
public class AnniversaryCreateDTO {

    /**
     * 纪念日类型: 1-恋爱纪念日, 2-生日, 3-自定义
     */
    @NotNull(message = "纪念日类型不能为空")
    private Integer anniversaryType;

    /**
     * 纪念日日期
     */
    @NotNull(message = "纪念日日期不能为空")
    private LocalDate anniversaryDate;

    /**
     * 纪念日名称
     */
    @NotBlank(message = "纪念日名称不能为空")
    @Size(max = 100, message = "纪念日名称不能超过100个字符")
    private String name;

    /**
     * 提前提醒天数，默认7天
     */
    private Integer remindDays = 7;
}
