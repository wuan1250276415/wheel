package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 审核统计VO
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
public class AuditStatisticsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 总审核数
     */
    private Long totalAudited;

    /**
     * 通过数
     */
    private Long approvedCount;

    /**
     * 拒绝数
     */
    private Long rejectedCount;

    /**
     * 待审核数
     */
    private Long pendingCount;

    /**
     * 通过率（百分比）
     */
    private Double approvalRate;

    /**
     * 平均处理时长（毫秒）
     */
    private Long avgProcessTime;

    /**
     * 今日审核量
     */
    private Long todayAuditCount;

    /**
     * 今日通过率
     */
    private Double todayApprovalRate;

    /**
     * 今日平均处理时长
     */
    private Long todayAvgProcessTime;

    /**
     * 审核员工作量统计
     */
    private Map<Long, AuditorWorkloadVO> auditorWorkload;

    /**
     * 审核员工作量列表
     */
    private List<AuditorWorkloadVO> auditorWorkloadList;

    /**
     * 每日审核数量统计
     */
    private Map<String, Long> dailyCount;

    /**
     * 违规类型分布
     */
    private List<ViolationDistributionVO> violationDistribution;
}
