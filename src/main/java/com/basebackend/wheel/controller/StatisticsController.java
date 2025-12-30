package com.basebackend.wheel.controller;

import com.basebackend.common.context.UserContextHolder;
import com.basebackend.common.exception.BusinessException;
import com.basebackend.common.model.Result;
import com.basebackend.wheel.service.StatisticsService;
import com.basebackend.wheel.service.StatisticsExportService;
import com.basebackend.wheel.util.AuditHelper;
import com.basebackend.wheel.util.MembershipPrivilegeHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;

/**
 * 统计分析控制器
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/statistics")
@Tag(name = "统计分析", description = "数据统计和分析相关接口")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private StatisticsExportService statisticsExportService;

    @Autowired
    private MembershipPrivilegeHelper membershipPrivilegeHelper;

    @Operation(summary = "获取用户统计", description = "获取当前用户的详细统计数据")
    @GetMapping("/user")
    public Result<StatisticsService.UserStatistics> getUserStatistics(HttpServletRequest request) {
        try {
            Long userId = UserContextHolder.getUserId();
            StatisticsService.UserStatistics stats = statisticsService.getUserStatistics(userId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取用户统计失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "获取系统概览", description = "获取系统整体统计数据（仅管理员）")
    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<StatisticsService.SystemOverview> getSystemOverview(HttpServletRequest request) {
        try {
            StatisticsService.SystemOverview overview = statisticsService.getSystemOverview();
            return Result.success(overview);
        } catch (Exception e) {
            log.error("获取系统概览失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "获取转盘使用趋势", description = "获取指定时间范围内的转盘使用趋势（VIP会员专享）")
    @GetMapping("/spin-trend")
    public Result<List<StatisticsService.SpinTrend>> getSpinTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            // VIP权益检查
            Long userId = UserContextHolder.getUserId();
            if (!membershipPrivilegeHelper.hasAdvancedStatistics(userId)) {
                throw new BusinessException("该功能需要VIP会员权限");
            }

            List<StatisticsService.SpinTrend> trendList = statisticsService.getSpinTrend(startDate, endDate);
            return Result.success(trendList);
        } catch (Exception e) {
            log.error("获取转盘使用趋势失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "获取热门内容", description = "获取最受欢迎的内容排行（VIP会员专享）")
    @GetMapping("/popular-content")
    public Result<List<StatisticsService.PopularContent>> getPopularContent(
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            // VIP权益检查
            Long userId = UserContextHolder.getUserId();
            if (!membershipPrivilegeHelper.hasAdvancedStatistics(userId)) {
                throw new BusinessException("该功能需要VIP会员权限");
            }

            List<StatisticsService.PopularContent> contentList = statisticsService.getPopularContent(limit);
            return Result.success(contentList);
        } catch (Exception e) {
            log.error("获取热门内容失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "获取分类统计", description = "获取各分类的使用统计（VIP会员专享）")
    @GetMapping("/category")
    public Result<List<StatisticsService.CategoryStatistics>> getCategoryStatistics() {
        try {
            // VIP权益检查
            Long userId = UserContextHolder.getUserId();
            if (!membershipPrivilegeHelper.hasAdvancedStatistics(userId)) {
                throw new BusinessException("该功能需要VIP会员权限");
            }

            List<StatisticsService.CategoryStatistics> statsList = statisticsService.getCategoryStatistics();
            return Result.success(statsList);
        } catch (Exception e) {
            log.error("获取分类统计失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "获取情侣关系统计", description = "获取情侣关系相关统计数据（仅管理员）")
    @GetMapping("/couple")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<StatisticsService.CoupleStatistics> getCoupleStatistics(HttpServletRequest request) {
        try {
            StatisticsService.CoupleStatistics stats = statisticsService.getCoupleStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取情侣关系统计失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "导出统计数据", description = "导出统计数据为Excel文件（VIP会员专享）")
    @GetMapping("/export")
    public void exportStatistics(
            @RequestParam String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            HttpServletResponse response) {
        try {
            // VIP权益检查
            Long userId = UserContextHolder.getUserId();
            if (!membershipPrivilegeHelper.hasAdvancedStatistics(userId)) {
                throw new BusinessException("数据导出功能需要VIP会员权限");
            }

            byte[] excelData;
            String filename;

            switch (type) {
                case "user":
                    excelData = statisticsExportService.exportUserStatisticsToExcel(userId);
                    filename = "用户统计_" + LocalDate.now() + ".xlsx";
                    break;
                case "trend":
                    if (startDate == null || endDate == null) {
                        throw new BusinessException("导出趋势数据需要指定开始和结束日期");
                    }
                    excelData = statisticsExportService.exportSpinTrendToExcel(startDate, endDate);
                    filename = "转盘趋势_" + startDate + "_" + endDate + ".xlsx";
                    break;
                case "popular":
                    excelData = statisticsExportService.exportPopularContentToExcel(limit);
                    filename = "热门内容_TOP" + limit + "_" + LocalDate.now() + ".xlsx";
                    break;
                case "category":
                    excelData = statisticsExportService.exportCategoryStatisticsToExcel();
                    filename = "分类统计_" + LocalDate.now() + ".xlsx";
                    break;
                case "all":
                    if (startDate == null || endDate == null) {
                        throw new BusinessException("导出全部数据需要指定开始和结束日期");
                    }
                    excelData = statisticsExportService.exportAllStatisticsToExcel(userId, startDate, endDate);
                    filename = "综合统计报表_" + LocalDate.now() + ".xlsx";
                    break;
                default:
                    throw new BusinessException("不支持的导出类型: " + type);
            }

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            response.setContentLength(excelData.length);

            // 写入响应
            response.getOutputStream().write(excelData);
            response.getOutputStream().flush();

            log.info("统计数据导出成功: userId={}, type={}, filename={}", userId, type, filename);
        } catch (Exception e) {
            log.error("导出统计数据失败: userId={}, type={}, error={}",
                UserContextHolder.getUserId(), type, e.getMessage(), e);
            throw new BusinessException("导出失败: " + e.getMessage());
        }
    }
}
