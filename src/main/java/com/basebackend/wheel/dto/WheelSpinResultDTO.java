package com.basebackend.wheel.dto;

import lombok.Data;

/**
 * 转盘结果DTO
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Data
public class WheelSpinResultDTO {

    /**
     * 内容ID
     */
    private Long contentId;

    /**
     * 结果文本
     */
    private String resultText;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 旋转角度
     */
    private Double rotationAngle;

    /**
     * 旋转时长（毫秒）
     */
    private Long spinDuration;

    /**
     * 是否中奖
     */
    private Boolean isWinning;
}
