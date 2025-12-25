package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 内容举报表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.content_report")
public class ContentReport extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 内容ID
     */
    @TableField(value = "content_id")
    @NotNull(message = "内容ID不能为null")
    private Long contentId;

    /**
     * 举报人用户ID
     */
    @TableField(value = "reporter_user_id")
    @NotNull(message = "举报人用户ID不能为null")
    private Long reporterUserId;

    /**
     * 举报类型
     */
    @TableField(value = "report_type")
    private Boolean reportType;

    /**
     * 举报时间
     */
    @TableField(value = "report_time")
    private LocalDateTime reportTime;

    /**
     * 举报原因：0-色情低俗，1-暴力血腥，2-政治敏感，3-其他
     */
    @TableField(value = "report_reason")
    @NotNull(message = "举报原因：0-色情低俗，1-暴力血腥，2-政治敏感，3-其他不能为null")
    private Integer reportReason;

    /**
     * 举报详细描述
     */
    @TableField(value = "report_description")
    private String reportDescription;

    /**
     * 处理状态：0-待处理，1-已处理，2-已忽略
     */
    @TableField(value = "handle_status")
    private Integer handleStatus;

    /**
     * 处理人ID
     */
    @TableField(value = "handler_id")
    private Long handlerId;

    /**
     * 处理时间
     */
    @TableField(value = "handled_at")
    private Date handledAt;

    /**
     * 处理结果
     */
    @TableField(value = "handle_result")
    private String handleResult;
}