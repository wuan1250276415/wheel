package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.basebackend.database.entity.BaseEntity;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 内容举报表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.content_report", autoResultMap = true)
public class ContentReport extends BaseEntity {

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
     * 举报类型：0-内容 1-用户 2-评论
     */
    @TableField(value = "report_type")
    private Integer reportType;

    /**
     * 举报时间
     */
    @TableField(value = "report_time")
    private LocalDateTime reportTime;

    /**
     * 举报原因：0-色情低俗，1-暴力血腥，2-政治敏感，3-广告骚扰，4-侵权内容，5-其他
     */
    @TableField(value = "report_reason")
    @NotNull(message = "举报原因不能为null")
    private Integer reportReason;

    /**
     * 举报详细描述
     */
    @TableField(value = "report_description")
    private String reportDescription;

    /**
     * 证据截图URL列表
     */
    @TableField(value = "evidence_urls", typeHandler = JacksonTypeHandler.class)
    private List<String> evidenceUrls;

    /**
     * 举报人信誉分
     */
    @TableField(value = "reporter_credibility")
    private Integer reporterCredibility;

    /**
     * 优先级：1-普通 2-高 3-紧急
     */
    @TableField(value = "priority")
    private Integer priority;

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

    /**
     * 举报原因枚举
     */
    public static final int REASON_PORN = 0;           // 色情低俗
    public static final int REASON_VIOLENCE = 1;       // 暴力血腥
    public static final int REASON_POLITICAL = 2;      // 政治敏感
    public static final int REASON_AD = 3;             // 广告骚扰
    public static final int REASON_INFRINGEMENT = 4;   // 侵权内容
    public static final int REASON_OTHER = 5;          // 其他

    /**
     * 举报类型枚举
     */
    public static final int TYPE_CONTENT = 0;          // 内容举报
    public static final int TYPE_USER = 1;             // 用户举报
    public static final int TYPE_COMMENT = 2;          // 评论举报

    /**
     * 处理状态枚举
     */
    public static final int STATUS_PENDING = 0;        // 待处理
    public static final int STATUS_HANDLED = 1;        // 已处理
    public static final int STATUS_IGNORED = 2;        // 已忽略

    /**
     * 优先级枚举
     */
    public static final int PRIORITY_NORMAL = 1;       // 普通
    public static final int PRIORITY_HIGH = 2;         // 高
    public static final int PRIORITY_URGENT = 3;       // 紧急
}