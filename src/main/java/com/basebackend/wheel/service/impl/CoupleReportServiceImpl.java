package com.basebackend.wheel.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basebackend.common.exception.BusinessException;
import com.basebackend.wheel.dto.CompatibilityResult;
import com.basebackend.wheel.dto.CoupleReportVO;
import com.basebackend.wheel.dto.ReportData;
import com.basebackend.wheel.dto.ReportQueryDTO;
import com.basebackend.wheel.engine.CompatibilityCalculator;
import com.basebackend.wheel.engine.ReportGenerator;
import com.basebackend.wheel.entity.CoupleRelationship;
import com.basebackend.wheel.entity.CoupleReport;
import com.basebackend.wheel.enums.ReportType;
import com.basebackend.wheel.mapper.CoupleRelationshipMapper;
import com.basebackend.wheel.mapper.CoupleReportMapper;
import com.basebackend.wheel.service.CoupleReportService;
import com.basebackend.wheel.util.AuditHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 情侣报告服务实现类
 *
 * @author wheel-api
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoupleReportServiceImpl implements CoupleReportService {

    private final CoupleReportMapper coupleReportMapper;
    private final CoupleRelationshipMapper coupleRelationshipMapper;
    private final ReportGenerator reportGenerator;
    private final CompatibilityCalculator compatibilityCalculator;

    // 情侣关系状态：已确认
    private static final int RELATIONSHIP_STATUS_CONFIRMED = 1;

    @Override
    @Transactional
    public CoupleReport generateReport(Long coupleId, ReportType type, LocalDate startDate, LocalDate endDate) {
        log.info("开始生成报告: coupleId={}, type={}, startDate={}, endDate={}", 
                coupleId, type, startDate, endDate);

        // 1. 检查是否已存在相同日期范围的报告
        CoupleReport existingReport = coupleReportMapper.selectByDateRange(
                coupleId, type.getCode(), startDate, endDate);
        if (existingReport != null) {
            log.info("报告已存在，返回现有报告: reportId={}", existingReport.getId());
            return existingReport;
        }

        // 2. 聚合报告数据
        ReportData reportData = reportGenerator.aggregateData(coupleId, startDate, endDate);
        if (reportData == null) {
            log.warn("无法聚合报告数据: coupleId={}", coupleId);
            throw new BusinessException("报告生成失败，请稍后重试");
        }

        // 3. 计算默契度评分
        CompatibilityResult compatibilityResult = compatibilityCalculator.calculate(coupleId, startDate, endDate);
        reportData.setCompatibilityScore(compatibilityResult.getTotalScore());
        reportData.setCompatibilityDesc(compatibilityResult.getDescription());

        // 4. 创建报告实体
        CoupleReport report = new CoupleReport();
        report.setCoupleId(coupleId);
        report.setReportType(type.getCode());
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setReportData(reportData);
        report.setCompatibilityScore(compatibilityResult.getTotalScore());
        report.setGeneratedAt(LocalDateTime.now());

        // 5. 设置审计字段
        AuditHelper.setCreateAuditFields(report, null);

        // 6. 保存报告
        coupleReportMapper.insert(report);
        log.info("报告生成成功: reportId={}, coupleId={}", report.getId(), coupleId);

        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public CoupleReportVO getCurrentReport(Long userId, ReportType type) {
        log.info("获取当前报告: userId={}, type={}", userId, type);

        // 1. 验证情侣关系
        Long coupleId = validateCoupleRelationship(userId);

        // 2. 查询最新报告
        CoupleReport report = coupleReportMapper.selectLatestByType(coupleId, type.getCode());
        
        if (report == null) {
            // 如果没有报告，生成一个新的
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(type.getDays() - 1);
            report = generateReport(coupleId, type, startDate, endDate);
        }

        return convertToVO(report);
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<CoupleReportVO> getReportHistory(Long userId, ReportQueryDTO query) {
        log.info("查询历史报告: userId={}, query={}", userId, query);

        // 1. 验证情侣关系
        Long coupleId = validateCoupleRelationship(userId);

        // 2. 构建分页参数
        Page<CoupleReport> page = new Page<>(query.getPageNum(), query.getPageSize());

        // 3. 查询报告
        IPage<CoupleReport> reportPage = coupleReportMapper.selectPageByCondition(
                page,
                coupleId,
                query.getReportType(),
                query.getStartDate(),
                query.getEndDate()
        );

        // 4. 转换为VO
        return reportPage.convert(this::convertToVO);
    }

    @Override
    @Transactional(readOnly = true)
    public CoupleReportVO getReportById(Long reportId, Long userId) {
        log.info("获取报告详情: reportId={}, userId={}", reportId, userId);

        // 1. 验证情侣关系
        Long coupleId = validateCoupleRelationship(userId);

        // 2. 查询报告（同时验证权限）
        CoupleReport report = coupleReportMapper.selectByIdAndCoupleId(reportId, coupleId);
        if (report == null) {
            throw new BusinessException("报告不存在或已删除");
        }

        return convertToVO(report);
    }

    @Override
    @Transactional
    public void generateScheduledReports(ReportType type) {
        log.info("开始批量生成定时报告: type={}", type);

        // 1. 计算日期范围
        LocalDate endDate = LocalDate.now().minusDays(1); // 昨天
        LocalDate startDate = endDate.minusDays(type.getDays() - 1);

        // 2. 查询所有已确认的情侣关系
        List<CoupleRelationship> couples = getActiveCouples();
        log.info("找到 {} 对活跃情侣", couples.size());

        int successCount = 0;
        int failCount = 0;

        // 3. 为每对情侣生成报告
        for (CoupleRelationship couple : couples) {
            try {
                // 检查是否有活动（只为有活动的情侣生成报告）
                ReportData data = reportGenerator.aggregateData(couple.getId(), startDate, endDate);
                if (data != null && hasActivity(data)) {
                    generateReport(couple.getId(), type, startDate, endDate);
                    successCount++;
                } else {
                    log.debug("情侣无活动，跳过报告生成: coupleId={}", couple.getId());
                }
            } catch (Exception e) {
                failCount++;
                log.error("报告生成失败: coupleId={}, error={}", couple.getId(), e.getMessage());
            }
        }

        log.info("定时报告生成完成: type={}, success={}, fail={}", type, successCount, failCount);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCoupleIdByUserId(Long userId) {
        CoupleRelationship relationship = coupleRelationshipMapper.selectConfirmedByUserId(userId);
        return relationship != null ? relationship.getId() : null;
    }

    @Override
    @Transactional(readOnly = true)
    public Long validateCoupleRelationship(Long userId) {
        CoupleRelationship relationship = coupleRelationshipMapper.selectConfirmedByUserId(userId);
        if (relationship == null) {
            throw new BusinessException("您还没有绑定情侣关系");
        }
        return relationship.getId();
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 将实体转换为VO
     */
    private CoupleReportVO convertToVO(CoupleReport report) {
        if (report == null) {
            return null;
        }

        CoupleReportVO vo = new CoupleReportVO();
        vo.setId(report.getId());
        vo.setReportType(report.getReportType());
        vo.setReportTypeName(getReportTypeName(report.getReportType()));
        vo.setStartDate(report.getStartDate());
        vo.setEndDate(report.getEndDate());
        vo.setReportData(report.getReportData());
        vo.setCompatibilityScore(report.getCompatibilityScore());
        vo.setGeneratedAt(report.getGeneratedAt());

        return vo;
    }

    /**
     * 获取报告类型名称
     */
    private String getReportTypeName(Integer reportType) {
        if (reportType == null) {
            return "未知";
        }
        try {
            return ReportType.fromCode(reportType).getName();
        } catch (IllegalArgumentException e) {
            return "未知";
        }
    }

    /**
     * 获取所有活跃的情侣关系
     */
    private List<CoupleRelationship> getActiveCouples() {
        // 查询所有已确认状态的情侣关系
        return coupleRelationshipMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CoupleRelationship>()
                        .eq(CoupleRelationship::getStatus, RELATIONSHIP_STATUS_CONFIRMED)
                        .eq(CoupleRelationship::getDeleted, false)
        );
    }

    /**
     * 检查报告数据是否有活动
     */
    private boolean hasActivity(ReportData data) {
        if (data == null) {
            return false;
        }
        // 有转盘记录、聊天消息或动态则认为有活动
        return (data.getTotalSpins() != null && data.getTotalSpins() > 0)
                || (data.getChatMessageCount() != null && data.getChatMessageCount() > 0)
                || (data.getMomentCount() != null && data.getMomentCount() > 0);
    }
}
