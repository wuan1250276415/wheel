package com.basebackend.wheel.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 情侣报告视图对象
 */
@Data
public class CoupleReportVO {

    /**
     * 报告ID
     */
    private Long id;

    /**
     * 报告类型: 1-周报, 2-月报, 3-年报
     */
    private Integer reportType;

    /**
     * 报告类型名称
     */
    private String reportTypeName;

    /**
     * 统计开始日期
     */
    private LocalDate startDate;

    /**
     * 统计结束日期
     */
    private LocalDate endDate;

    /**
     * 报告数据
     */
    private ReportData reportData;

    /**
     * 默契度评分(0-100)
     */
    private Integer compatibilityScore;

    /**
     * 生成时间
     */
    private LocalDateTime generatedAt;
}
