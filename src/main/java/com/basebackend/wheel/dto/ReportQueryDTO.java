package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 报告查询参数DTO
 */
@Data
public class ReportQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 报告类型: 1-周报, 2-月报, 3-年报
     */
    private Integer reportType;

    /**
     * 查询开始日期
     */
    private LocalDate startDate;

    /**
     * 查询结束日期
     */
    private LocalDate endDate;

    /**
     * 页码，默认1
     */
    private Integer pageNum = 1;

    /**
     * 每页大小，默认10
     */
    private Integer pageSize = 10;
}
