package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AuditHistoryQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long auditorId;
    private Integer auditStatus;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
