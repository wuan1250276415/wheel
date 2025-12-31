package com.basebackend.wheel.service;

import com.basebackend.wheel.dto.AuditStatisticsVO;

import java.io.OutputStream;

/**
 * 审核报表导出服务接口
 *
 * @author wheel-api
 * @since 2025-02-01
 */
public interface AuditReportExportService {

    /**
     * 导出审核报表为Excel格式
     *
     * @param statistics 审核统计数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param outputStream 输出流
     */
    void exportToExcel(AuditStatisticsVO statistics, String startTime, String endTime, OutputStream outputStream);

    /**
     * 导出审核报表为PDF格式
     *
     * @param statistics 审核统计数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param outputStream 输出流
     */
    void exportToPdf(AuditStatisticsVO statistics, String startTime, String endTime, OutputStream outputStream);
}
