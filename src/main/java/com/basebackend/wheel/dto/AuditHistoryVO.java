package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class AuditHistoryVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long contentId;
    private String contentText;
    private Long submitUserId;
    private String submitUserName;
    private Integer auditStatus;
    private String auditStatusName;
    private Long auditorId;
    private String auditorName;
    private LocalDateTime auditedAt;
    private String auditComment;
    private Map<String, Object> aiAuditResult;
    private Integer auditType;
    private String auditTypeName;
    private Long processTime;
    private String auditSource;
    private LocalDateTime createTime;
}
