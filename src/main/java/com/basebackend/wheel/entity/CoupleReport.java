package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.basebackend.database.entity.BaseEntity;
import com.basebackend.wheel.dto.ReportData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 情侣报告表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.couple_report", autoResultMap = true)
public class CoupleReport extends BaseEntity {

    /**
     * 情侣关系ID
     */
    @TableField(value = "couple_id")
    private Long coupleId;

    /**
     * 报告类型: 1-周报, 2-月报, 3-年报
     */
    @TableField(value = "report_type")
    private Integer reportType;

    /**
     * 统计开始日期
     */
    @TableField(value = "start_date")
    private LocalDate startDate;

    /**
     * 统计结束日期
     */
    @TableField(value = "end_date")
    private LocalDate endDate;

    /**
     * 报告数据JSON
     */
    @TableField(value = "report_data", typeHandler = JacksonTypeHandler.class)
    private ReportData reportData;

    /**
     * 默契度评分(0-100)
     */
    @TableField(value = "compatibility_score")
    private Integer compatibilityScore;

    /**
     * 生成时间
     */
    @TableField(value = "generated_at")
    private LocalDateTime generatedAt;
}
