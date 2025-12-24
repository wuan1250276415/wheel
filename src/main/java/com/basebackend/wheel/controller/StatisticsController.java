package com.basebackend.wheel.controller;

import com.basebackend.common.model.Result;
import com.basebackend.wheel.service.StatisticsService;
import com.basebackend.wheel.util.AuditHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
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

    @Operation(summary = "获取用户统计", description = "获取当前用户的详细统计数据")
    @GetMapping("/user")
    public Result<StatisticsService.UserStatistics> getUserStatistics(HttpServletRequest request) {
        try {
            Long userId = AuditHelper.getCurrentUserId();
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

    @Operation(summary = "获取转盘使用趋势", description = "获取指定时间范围内的转盘使用趋势")
    @GetMapping("/spin-trend")
    public Result<List<StatisticsService.SpinTrend>> getSpinTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<StatisticsService.SpinTrend> trendList = statisticsService.getSpinTrend(startDate, endDate);
            return Result.success(trendList);
        } catch (Exception e) {
            log.error("获取转盘使用趋势失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "获取热门内容", description = "获取最受欢迎的内容排行")
    @GetMapping("/popular-content")
    public Result<List<StatisticsService.PopularContent>> getPopularContent(
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<StatisticsService.PopularContent> contentList = statisticsService.getPopularContent(limit);
            return Result.success(contentList);
        } catch (Exception e) {
            log.error("获取热门内容失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "获取分类统计", description = "获取各分类的使用统计")
    @GetMapping("/category")
    public Result<List<StatisticsService.CategoryStatistics>> getCategoryStatistics() {
        try {
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
}
