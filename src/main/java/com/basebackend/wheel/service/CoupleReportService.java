package com.basebackend.wheel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.basebackend.wheel.dto.CoupleReportVO;
import com.basebackend.wheel.dto.ReportQueryDTO;
import com.basebackend.wheel.entity.CoupleReport;
import com.basebackend.wheel.enums.ReportType;

import java.time.LocalDate;

/**
 * 情侣报告服务接口
 *
 * @author wheel-api
 */
public interface CoupleReportService {

    /**
     * 生成情侣报告
     *
     * @param coupleId  情侣关系ID
     * @param type      报告类型
     * @param startDate 统计开始日期
     * @param endDate   统计结束日期
     * @return 生成的报告
     */
    CoupleReport generateReport(Long coupleId, ReportType type, LocalDate startDate, LocalDate endDate);

    /**
     * 获取当前用户的最新报告
     *
     * @param userId 用户ID
     * @param type   报告类型
     * @return 报告视图对象
     */
    CoupleReportVO getCurrentReport(Long userId, ReportType type);

    /**
     * 分页查询历史报告
     *
     * @param userId 用户ID
     * @param query  查询参数
     * @return 分页结果
     */
    IPage<CoupleReportVO> getReportHistory(Long userId, ReportQueryDTO query);

    /**
     * 获取报告详情
     *
     * @param reportId 报告ID
     * @param userId   用户ID
     * @return 报告视图对象
     */
    CoupleReportVO getReportById(Long reportId, Long userId);

    /**
     * 批量生成定时报告
     *
     * @param type 报告类型
     */
    void generateScheduledReports(ReportType type);

    /**
     * 获取用户的情侣关系ID
     *
     * @param userId 用户ID
     * @return 情侣关系ID，如果不存在则返回null
     */
    Long getCoupleIdByUserId(Long userId);

    /**
     * 验证用户是否有活跃的情侣关系
     *
     * @param userId 用户ID
     * @return 情侣关系ID
     * @throws com.basebackend.common.exception.BusinessException 如果用户没有情侣关系
     */
    Long validateCoupleRelationship(Long userId);
}
