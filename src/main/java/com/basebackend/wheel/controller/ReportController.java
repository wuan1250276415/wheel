package com.basebackend.wheel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.basebackend.common.context.UserContextHolder;
import com.basebackend.common.model.Result;
import com.basebackend.wheel.dto.*;
import com.basebackend.wheel.engine.ReportShareService;
import com.basebackend.wheel.entity.CoupleReport;
import com.basebackend.wheel.enums.ReportType;
import com.basebackend.wheel.service.AnniversaryService;
import com.basebackend.wheel.service.CoupleReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 情侣报告控制器
 * 提供报告查询、纪念日管理、报告分享等功能
 *
 * @author wheel-api
 */
@Slf4j
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Tag(name = "情侣报告", description = "情侣报告查询、纪念日管理、报告分享相关接口")
public class ReportController {

    private final CoupleReportService coupleReportService;
    private final AnniversaryService anniversaryService;
    private final ReportShareService reportShareService;

    // ==================== 报告相关接口 ====================

    @Operation(summary = "获取当前报告", description = "获取当前用户的最新情侣报告")
    @GetMapping("/current")
    public Result<CoupleReportVO> getCurrentReport(
            @Parameter(description = "报告类型: 1-周报, 2-月报, 3-年报")
            @RequestParam(defaultValue = "1") Integer type) {
        try {
            Long userId = UserContextHolder.getUserId();
            ReportType reportType = ReportType.fromCode(type);
            CoupleReportVO report = coupleReportService.getCurrentReport(userId, reportType);
            return Result.success(report);
        } catch (IllegalArgumentException e) {
            log.warn("无效的报告类型: type={}", type);
            return Result.error(400, "无效的报告类型");
        } catch (Exception e) {
            log.error("获取当前报告失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "获取历史报告列表", description = "分页查询历史报告")
    @GetMapping("/history")
    public Result<IPage<CoupleReportVO>> getReportHistory(ReportQueryDTO query) {
        try {
            Long userId = UserContextHolder.getUserId();
            IPage<CoupleReportVO> reports = coupleReportService.getReportHistory(userId, query);
            return Result.success(reports);
        } catch (Exception e) {
            log.error("获取历史报告失败: error={}", e.getMessage());
            throw e;
        }
    }


    @Operation(summary = "获取报告详情", description = "根据报告ID获取报告详情")
    @GetMapping("/{reportId}")
    public Result<CoupleReportVO> getReportById(
            @Parameter(description = "报告ID") @PathVariable Long reportId) {
        try {
            Long userId = UserContextHolder.getUserId();
            CoupleReportVO report = coupleReportService.getReportById(reportId, userId);
            return Result.success(report);
        } catch (Exception e) {
            log.error("获取报告详情失败: reportId={}, error={}", reportId, e.getMessage());
            throw e;
        }
    }

    // ==================== 分享相关接口 ====================

    @Operation(summary = "生成分享图片", description = "生成报告的分享图片")
    @PostMapping("/{reportId}/share")
    public Result<ShareImageVO> generateShareImage(
            @Parameter(description = "报告ID") @PathVariable Long reportId,
            @Parameter(description = "主题: default, blue, purple")
            @RequestParam(defaultValue = "default") String theme) {
        try {
            Long userId = UserContextHolder.getUserId();
            // 先验证用户有权访问该报告
            CoupleReportVO reportVO = coupleReportService.getReportById(reportId, userId);
            if (reportVO == null) {
                return Result.error(404, "报告不存在或无权访问");
            }
            
            // 构建CoupleReport对象用于生成图片
            CoupleReport report = new CoupleReport();
            report.setId(reportVO.getId());
            report.setReportType(reportVO.getReportType());
            report.setStartDate(reportVO.getStartDate());
            report.setEndDate(reportVO.getEndDate());
            report.setReportData(reportVO.getReportData());
            report.setCompatibilityScore(reportVO.getCompatibilityScore());
            report.setGeneratedAt(reportVO.getGeneratedAt());
            
            ShareImageVO shareImage = reportShareService.generateShareImage(report, theme);
            if (shareImage == null) {
                return Result.error(500, "生成分享图片失败");
            }
            return Result.success(shareImage);
        } catch (Exception e) {
            log.error("生成分享图片失败: reportId={}, error={}", reportId, e.getMessage());
            throw e;
        }
    }

    // ==================== 纪念日相关接口 ====================

    @Operation(summary = "获取纪念日列表", description = "获取当前用户的所有纪念日")
    @GetMapping("/anniversary")
    public Result<List<AnniversaryVO>> getAnniversaries() {
        try {
            Long userId = UserContextHolder.getUserId();
            List<AnniversaryVO> anniversaries = anniversaryService.getAnniversaries(userId);
            return Result.success(anniversaries);
        } catch (Exception e) {
            log.error("获取纪念日列表失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "添加纪念日", description = "添加新的纪念日")
    @PostMapping("/anniversary")
    public Result<AnniversaryVO> addAnniversary(@Valid @RequestBody AnniversaryCreateDTO dto) {
        try {
            Long userId = UserContextHolder.getUserId();
            var anniversary = anniversaryService.addAnniversary(userId, dto);
            
            // 转换为VO返回
            AnniversaryVO vo = convertToAnniversaryVO(anniversary);
            log.info("添加纪念日成功: userId={}, name={}", userId, dto.getName());
            return Result.success(vo);
        } catch (Exception e) {
            log.error("添加纪念日失败: error={}", e.getMessage());
            throw e;
        }
    }


    @Operation(summary = "更新纪念日", description = "更新指定的纪念日")
    @PutMapping("/anniversary/{id}")
    public Result<AnniversaryVO> updateAnniversary(
            @Parameter(description = "纪念日ID") @PathVariable Long id,
            @Valid @RequestBody AnniversaryUpdateDTO dto) {
        try {
            Long userId = UserContextHolder.getUserId();
            var anniversary = anniversaryService.updateAnniversary(userId, id, dto);
            
            // 转换为VO返回
            AnniversaryVO vo = convertToAnniversaryVO(anniversary);
            log.info("更新纪念日成功: userId={}, anniversaryId={}", userId, id);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("更新纪念日失败: anniversaryId={}, error={}", id, e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "删除纪念日", description = "删除指定的纪念日")
    @DeleteMapping("/anniversary/{id}")
    public Result<Void> deleteAnniversary(
            @Parameter(description = "纪念日ID") @PathVariable Long id) {
        try {
            Long userId = UserContextHolder.getUserId();
            anniversaryService.deleteAnniversary(userId, id);
            log.info("删除纪念日成功: userId={}, anniversaryId={}", userId, id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除纪念日失败: anniversaryId={}, error={}", id, e.getMessage());
            throw e;
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 将CoupleAnniversary实体转换为AnniversaryVO
     */
    private AnniversaryVO convertToAnniversaryVO(com.basebackend.wheel.entity.CoupleAnniversary anniversary) {
        if (anniversary == null) {
            return null;
        }
        
        AnniversaryVO vo = new AnniversaryVO();
        vo.setId(anniversary.getId());
        vo.setAnniversaryType(anniversary.getAnniversaryType());
        vo.setAnniversaryTypeName(getAnniversaryTypeName(anniversary.getAnniversaryType()));
        vo.setAnniversaryDate(anniversary.getAnniversaryDate());
        vo.setName(anniversary.getName());
        vo.setRemindDays(anniversary.getRemindDays());
        
        // 计算距离纪念日还有多少天
        if (anniversary.getAnniversaryDate() != null) {
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate nextAnniversary = anniversary.getAnniversaryDate()
                    .withYear(today.getYear());
            if (nextAnniversary.isBefore(today)) {
                nextAnniversary = nextAnniversary.plusYears(1);
            }
            vo.setDaysUntil(java.time.temporal.ChronoUnit.DAYS.between(today, nextAnniversary));
        }
        
        return vo;
    }

    /**
     * 获取纪念日类型名称
     */
    private String getAnniversaryTypeName(Integer type) {
        if (type == null) {
            return "未知";
        }
        return switch (type) {
            case 1 -> "恋爱纪念日";
            case 2 -> "生日";
            case 3 -> "自定义";
            default -> "未知";
        };
    }
}
