package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.basebackend.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 内容举报表
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("content_report")
public class ContentReport extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 内容ID
     */
    @TableField("content_id")
    private Long contentId;

    /**
     * 举报人用户ID
     */
    @TableField("reporter_user_id")
    private Long reporterUserId;

    /**
     * 举报原因：0-色情低俗，1-暴力血腥，2-政治敏感，3-其他
     */
    @TableField("report_reason")
    private Integer reportReason;

    /**
     * 举报详细描述
     */
    @TableField("report_description")
    private String reportDescription;

    /**
     * 处理状态：0-待处理，1-已处理，2-已忽略
     */
    @TableField("handle_status")
    private Integer handleStatus;

    /**
     * 处理人ID
     */
    @TableField("handler_id")
    private Long handlerId;

    /**
     * 处理时间
     */
    @TableField("handled_at")
    private LocalDateTime handledAt;

    /**
     * 处理结果
     */
    @TableField("handle_result")
    private String handleResult;

}
