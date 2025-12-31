package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.basebackend.database.entity.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 转盘内容表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.wheel_content", autoResultMap = true)
public class WheelContent extends BaseEntity {
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
     * 审核优先级：1-普通，2-VIP，3-SVIP
     */
    @TableField(value = "audit_priority")
    private Integer auditPriority;

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
     * 热度分数
     */
    @TableField(value = "popularity_score")
    private Double popularityScore;

    /**
     * 难度等级：1-简单，2-中等，3-困难
     */
    @TableField(value = "difficulty_level")
    private Integer difficultyLevel;

    /**
     * 特征向量
     */
    @TableField(value = "feature_vector", typeHandler = JacksonTypeHandler.class)
    private List<Double> featureVector;

}