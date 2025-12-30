package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class AuditStatisticsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long totalAudited;
    private Long approvedCount;
    private Long rejectedCount;
    private Double approvalRate;
    private Map<Long, AuditorWorkloadVO> auditorWorkload;
    private Map<String, Long> dailyCount;
}
