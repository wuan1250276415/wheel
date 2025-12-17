package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 转盘内容表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.wheel_content")
public class WheelContent extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 分类ID
     */
    @TableField(value = "category_id")
    @NotNull(message = "分类ID不能为null")
    private Long categoryId;

    /**
     * 内容文本
     */
    @TableField(value = "content_text")
    @Size(max = 200, message = "内容文本最大长度要小于 200")
    @NotBlank(message = "内容文本不能为空")
    private String contentText;

    /**
     * 权重值
     */
    @TableField(value = "weight")
    private Double weight;

    /**
     * 创建用户ID
     */
    @TableField(value = "create_user_id")
    private Long createUserId;

    /**
     * 审核状态：0-待审核，1-已通过，2-已拒绝
     */
    @TableField(value = "audit_status")
    private int auditStatus;

    /**
     * 审核人ID
     */
    @TableField(value = "auditor_id")
    private Long auditorId;

    /**
     * 审核时间
     */
    @TableField(value = "audited_at")
    private Date auditedAt;

    /**
     * 审核意见
     */
    @TableField(value = "audit_comment")
    private String auditComment;

    /**
     * 标签（JSON）
     */
    @TableField(value = "tags")
    @Size(max = 500, message = "标签（JSON）最大长度要小于 500")
    private String tags;

    /**
     * 是否系统内置：0-否，1-是
     */
    @TableField(value = "is_system")
    private Boolean isSystem;

    /**
     * 内容状态：0-禁用，1-启用
     */
    @TableField(value = "`status`")
    private int status;

    /**
     * 创建时间
     */
    @TableField(value = "created_time")
    @NotNull(message = "创建时间不能为null")
    private Date createdTime;

    /**
     * 创建人
     */
    @TableField(value = "created_by")
    private Long createdBy;

    /**
     * 更新人
     */
    @TableField(value = "updated_by")
    private Long updatedBy;
}