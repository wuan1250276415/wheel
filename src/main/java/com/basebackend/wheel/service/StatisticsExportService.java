package com.basebackend.wheel.service;

import java.time.LocalDate;

/**
 * 统计数据导出服务
 *
 * @author wheel-api
 * @since 2025-01-29
 */
public interface StatisticsExportService {

    /**
     * 导出用户统计数据为Excel
     *
     * @param userId 用户ID
     * @return Excel文件字节数组
     */
    byte[] exportUserStatisticsToExcel(Long userId);

    /**
     * 导出转盘趋势数据为Excel
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return Excel文件字节数组
     */
    byte[] exportSpinTrendToExcel(LocalDate startDate, LocalDate endDate);

    /**
     * 导出热门内容数据为Excel
     *
     * @param limit 限制数量
     * @return Excel文件字节数组
     */
    byte[] exportPopularContentToExcel(Integer limit);

    /**
     * 导出分类统计数据为Excel
     *
     * @return Excel文件字节数组
     */
    byte[] exportCategoryStatisticsToExcel();

    /**
     * 导出全部统计数据为Excel（综合报表）
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return Excel文件字节数组
     */
    byte[] exportAllStatisticsToExcel(Long userId, LocalDate startDate, LocalDate endDate);
}
