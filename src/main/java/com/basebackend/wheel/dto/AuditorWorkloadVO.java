package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AuditorWorkloadVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long auditorId;
    private String auditorName;
    private Long totalCount;
    private Long approvedCount;
    private Long rejectedCount;
    private Double approvalRate;
    private Long avgProcessTime;
}
