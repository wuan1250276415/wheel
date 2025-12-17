package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 情侣关系表
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("couple_relationship")
public class CoupleRelationship implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关系ID - 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID - 发起方
     */
    @TableField("user_id_1")
    private Long userId1;

    /**
     * 用户ID - 接受方
     */
    @TableField("user_id_2")
    private Long userId2;

    /**
     * 关系状态：0-待确认，1-已确认，2-已解除
     */
    @TableField("status")
    private Integer status;

    /**
     * 邀请码（用于邀请）
     */
    @TableField("invite_code")
    private String inviteCode;

    /**
     * 确认时间
     */
    @TableField("confirmed_at")
    private LocalDateTime confirmedAt;

    /**
     * 解除时间
     */
    @TableField("unbound_at")
    private LocalDateTime unboundAt;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 创建人
     */
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 更新人
     */
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;
}
