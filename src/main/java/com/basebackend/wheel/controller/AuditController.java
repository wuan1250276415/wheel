package com.basebackend.wheel.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basebackend.common.model.Result;
import com.basebackend.security.annotation.RequiresPermission;
import com.basebackend.wheel.dto.*;
import com.basebackend.wheel.entity.ContentAuditLog;
import com.basebackend.wheel.entity.WheelContent;
import com.basebackend.wheel.mapper.ContentAuditLogMapper;
import com.basebackend.wheel.mapper.WheelContentMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 内容审核控制器
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/audit")
//@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "内容审核", description = "内容审核管理接口（仅管理员）")
public class AuditController {

    @Autowired
    private WheelContentMapper contentMapper;

    @Autowired
    private ContentAuditLogMapper auditLogMapper;

    @Operation(summary = "获取审核队列", description = "按优先级排序的待审核内容列表")
    @GetMapping("/queue")
//    @RequiresPermission("audit:queue:view")
    public Result<List<WheelContent>> getAuditQueue(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            int offset = (pageNum - 1) * pageSize;
            List<WheelContent> queue = contentMapper.selectAuditQueue(offset, pageSize);
            log.info("获取审核队列成功: pageNum={}, pageSize={}, count={}", pageNum, pageSize, queue.size());
            return Result.success(queue);
        } catch (Exception e) {
            log.error("获取审核队列失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "获取待审核数量", description = "获取各优先级待审核内容数量统计")
    @GetMapping("/pending-count")
//    @RequiresPermission("audit:queue:view")
    public Result<PendingCountVO> getPendingCount() {
        try {
            Integer svipCount = contentMapper.countByAuditStatusAndPriority(0, 3);
            Integer vipCount = contentMapper.countByAuditStatusAndPriority(0, 2);
            Integer normalCount = contentMapper.countByAuditStatusAndPriority(0, 1);

            PendingCountVO countVO = new PendingCountVO();
            countVO.setSvipPending(svipCount);
            countVO.setVipPending(vipCount);
            countVO.setNormalPending(normalCount);
            countVO.setTotalPending(svipCount + vipCount + normalCount);

            log.info("获取待审核数量统计成功: total={}, svip={}, vip={}, normal={}",
                    countVO.getTotalPending(), svipCount, vipCount, normalCount);
            return Result.success(countVO);
        } catch (Exception e) {
            log.error("获取待审核数量统计失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "批量通过审核")
    @PostMapping("/batch-approve")
//    @RequiresPermission("audit:queue:approve")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> batchApprove(@RequestBody BatchAuditDTO batchDTO) {
        try {
            Long auditorId = getCurrentAdminId();
            LocalDateTime now = LocalDateTime.now();

            for (Long contentId : batchDTO.getContentIds()) {
                WheelContent content = contentMapper.selectById(contentId);
                if (content != null && content.getAuditStatus() == 0) {
                    LocalDateTime submitTime = content.getCreateTime();
                    long processTime = ChronoUnit.MILLIS.between(submitTime, now);

                    content.setAuditStatus(1);
                    content.setAuditorId(auditorId);
                    content.setAuditedAt(new Date());
                    content.setAuditComment(batchDTO.getAuditComment());
                    contentMapper.updateById(content);

                    ContentAuditLog log = new ContentAuditLog();
                    log.setContentId(contentId);
                    log.setSubmitUserId(content.getCreateUserId());
                    log.setAuditorId(auditorId);
                    log.setAuditStatus(1);
                    log.setAuditComment(batchDTO.getAuditComment());
                    log.setAuditedAt(now);
                    log.setAuditType(1);
                    log.setProcessTime(processTime);
                    log.setAuditSource("WEB");
                    auditLogMapper.insert(log);
                }
            }

            log.info("批量通过审核成功: count={}, auditorId={}", batchDTO.getContentIds().size(), auditorId);
            return Result.success(null);
        } catch (Exception e) {
            log.error("批量通过审核失败: error={}", e.getMessage(), e);
            return Result.error(500, "批量通过审核失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量拒绝审核")
    @PostMapping("/batch-reject")
//    @RequiresPermission("audit:queue:reject")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> batchReject(@RequestBody BatchAuditDTO batchDTO) {
        try {
            Long auditorId = getCurrentAdminId();
            LocalDateTime now = LocalDateTime.now();

            for (Long contentId : batchDTO.getContentIds()) {
                WheelContent content = contentMapper.selectById(contentId);
                if (content != null && content.getAuditStatus() == 0) {
                    LocalDateTime submitTime = content.getCreateTime();
                    long processTime = ChronoUnit.MILLIS.between(submitTime, now);

                    content.setAuditStatus(2);
                    content.setAuditorId(auditorId);
                    content.setAuditedAt(new Date());
                    content.setAuditComment(batchDTO.getAuditComment());
                    contentMapper.updateById(content);

                    ContentAuditLog log = new ContentAuditLog();
                    log.setContentId(contentId);
                    log.setSubmitUserId(content.getCreateUserId());
                    log.setAuditorId(auditorId);
                    log.setAuditStatus(2);
                    log.setAuditComment(batchDTO.getAuditComment());
                    log.setAuditedAt(now);
                    log.setAuditType(1);
                    log.setProcessTime(processTime);
                    log.setAuditSource("WEB");
                    auditLogMapper.insert(log);
                }
            }

            log.info("批量拒绝审核成功: count={}, auditorId={}", batchDTO.getContentIds().size(), auditorId);
            return Result.success(null);
        } catch (Exception e) {
            log.error("批量拒绝审核失败: error={}", e.getMessage(), e);
            return Result.error(500, "批量拒绝审核失败: " + e.getMessage());
        }
    }

    @Operation(summary = "审核历史记录")
    @GetMapping("/history")
//    @RequiresPermission("audit:history:view")
    public Result<IPage<AuditHistoryVO>> getAuditHistory(AuditHistoryQueryDTO queryDTO) {
        try {
            Page<ContentAuditLog> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

            LambdaQueryWrapper<ContentAuditLog> wrapper = new LambdaQueryWrapper<>();

            if (queryDTO.getAuditorId() != null) {
                wrapper.eq(ContentAuditLog::getAuditorId, queryDTO.getAuditorId());
            }

            if (queryDTO.getAuditStatus() != null) {
                wrapper.eq(ContentAuditLog::getAuditStatus, queryDTO.getAuditStatus());
            }

            if (queryDTO.getStartTime() != null) {
                wrapper.ge(ContentAuditLog::getAuditedAt, queryDTO.getStartTime());
            }

            if (queryDTO.getEndTime() != null) {
                wrapper.le(ContentAuditLog::getAuditedAt, queryDTO.getEndTime());
            }

            wrapper.orderByDesc(ContentAuditLog::getAuditedAt);

            IPage<ContentAuditLog> auditLogPage = auditLogMapper.selectPage(page, wrapper);

            IPage<AuditHistoryVO> result = auditLogPage.convert(auditLog -> {
                AuditHistoryVO vo = new AuditHistoryVO();
                BeanUtils.copyProperties(auditLog, vo);

                WheelContent content = contentMapper.selectById(auditLog.getContentId());
                if (content != null) {
                    vo.setContentText(content.getContentText());
                }

                vo.setAuditStatusName(getAuditStatusName(auditLog.getAuditStatus()));
                vo.setAuditTypeName(getAuditTypeName(auditLog.getAuditType()));

                if (auditLog.getAiAuditResult() != null) {
                    vo.setAiAuditResult(JSON.parseObject(auditLog.getAiAuditResult(), Map.class));
                }

                return vo;
            });

            return Result.success(result);
        } catch (Exception e) {
            log.error("查询审核历史失败: error={}", e.getMessage(), e);
            return Result.error(500, "查询审核历史失败: " + e.getMessage());
        }
    }

    @Operation(summary = "审核统计")
    @GetMapping("/statistics")
//    @RequiresPermission("audit:history:view")
    public Result<AuditStatisticsVO> getAuditStatistics(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            List<ContentAuditLog> auditLogs = auditLogMapper.selectAuditStatistics(startTime, endTime);

            long totalAudited = auditLogs.size();
            long approvedCount = auditLogs.stream().filter(log -> log.getAuditStatus() == 1).count();
            long rejectedCount = auditLogs.stream().filter(log -> log.getAuditStatus() == 2).count();

            double approvalRate = totalAudited > 0
                    ? (double) approvedCount / totalAudited * 100
                    : 0.0;

            Map<Long, List<ContentAuditLog>> auditorGroups = auditLogs.stream()
                    .filter(log -> log.getAuditorId() != null)
                    .collect(Collectors.groupingBy(ContentAuditLog::getAuditorId));

            Map<Long, AuditorWorkloadVO> auditorWorkload = new HashMap<>();
            for (Map.Entry<Long, List<ContentAuditLog>> entry : auditorGroups.entrySet()) {
                Long auditorId = entry.getKey();
                List<ContentAuditLog> logs = entry.getValue();

                long auditorTotal = logs.size();
                long auditorApproved = logs.stream().filter(log -> log.getAuditStatus() == 1).count();
                long auditorRejected = logs.stream().filter(log -> log.getAuditStatus() == 2).count();
                long avgProcessTime = (long) logs.stream()
                        .filter(log -> log.getProcessTime() != null)
                        .mapToLong(ContentAuditLog::getProcessTime)
                        .average()
                        .orElse(0);

                AuditorWorkloadVO workloadVO = new AuditorWorkloadVO();
                workloadVO.setAuditorId(auditorId);
                workloadVO.setTotalCount(auditorTotal);
                workloadVO.setApprovedCount(auditorApproved);
                workloadVO.setRejectedCount(auditorRejected);
                workloadVO.setApprovalRate(auditorTotal > 0 ? (double) auditorApproved / auditorTotal * 100 : 0.0);
                workloadVO.setAvgProcessTime(avgProcessTime);

                auditorWorkload.put(auditorId, workloadVO);
            }

            AuditStatisticsVO statisticsVO = new AuditStatisticsVO();
            statisticsVO.setTotalAudited(totalAudited);
            statisticsVO.setApprovedCount(approvedCount);
            statisticsVO.setRejectedCount(rejectedCount);
            statisticsVO.setApprovalRate(approvalRate);
            statisticsVO.setAuditorWorkload(auditorWorkload);
            statisticsVO.setDailyCount(new HashMap<>());

            return Result.success(statisticsVO);
        } catch (Exception e) {
            log.error("审核统计失败: error={}", e.getMessage(), e);
            return Result.error(500, "审核统计失败: " + e.getMessage());
        }
    }

    private Long getCurrentAdminId() {
        return 1L;
    }

    private String getAuditStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待审核";
            case 1: return "已通过";
            case 2: return "已拒绝";
            default: return "未知";
        }
    }

    private String getAuditTypeName(Integer type) {
        if (type == null) return "未知";
        switch (type) {
            case 0: return "自动审核";
            case 1: return "人工审核";
            default: return "未知";
        }
    }

    /**
     * 待审核数量统计VO
     */
    public static class PendingCountVO {
        private Integer svipPending;
        private Integer vipPending;
        private Integer normalPending;
        private Integer totalPending;

        public Integer getSvipPending() {
            return svipPending;
        }

        public void setSvipPending(Integer svipPending) {
            this.svipPending = svipPending;
        }

        public Integer getVipPending() {
            return vipPending;
        }

        public void setVipPending(Integer vipPending) {
            this.vipPending = vipPending;
        }

        public Integer getNormalPending() {
            return normalPending;
        }

        public void setNormalPending(Integer normalPending) {
            this.normalPending = normalPending;
        }

        public Integer getTotalPending() {
            return totalPending;
        }

        public void setTotalPending(Integer totalPending) {
            this.totalPending = totalPending;
        }
    }
}
