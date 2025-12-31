package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量审核DTO
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
public class BatchAuditDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 内容ID列表
     */
    private List<Long> contentIds;

    /**
     * 审核意见
     */
    private String auditComment;

    /**
     * 拒绝原因模板ID（用于批量拒绝）
     */
    private Integer rejectTemplateId;

    /**
     * 违规类型代码（用于批量拒绝）
     */
    private Integer violationType;

    /**
     * 是否通知用户
     */
    private Boolean notifyUser = true;
}
