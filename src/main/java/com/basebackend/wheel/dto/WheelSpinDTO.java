package com.basebackend.wheel.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 转盘请求DTO
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Data
public class WheelSpinDTO {

    /**
     * 分类ID列表（为空则使用所有分类）
     */
    private List<Long> categoryIds;

    /**
     * 转盘半径
     */
    @NotNull(message = "转盘半径不能为空")
    private Integer radius;

    /**
     * 动画时长（毫秒）
     */
    private Integer animationDuration;

    /**
     * 主题风格
     */
    private Integer themeStyle;
}
