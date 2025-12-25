package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 情侣关系表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.couple_relationship")
public class CoupleRelationship extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 用户ID-发起方
     */
    @TableField(value = "user_id_1")
    @NotNull(message = "用户ID-发起方不能为null")
    private Long userId1;

    /**
     * 用户ID-接受方
     */
    @TableField(value = "user_id_2")
    @NotNull(message = "用户ID-接受方不能为null")
    private Long userId2;

    /**
     * 关系状态：0-待确认，1-已确认，2-已解除
     */
    @TableField(value = "`status`")
    private Integer status;

    /**
     * 邀请码
     */
    @TableField(value = "invite_code")
    @Size(max = 50, message = "邀请码最大长度要小于 50")
    @NotBlank(message = "邀请码不能为空")
    private String inviteCode;

    /**
     * 确认时间
     */
    @TableField(value = "confirmed_at")
    private LocalDateTime confirmedAt;

    /**
     * 解除时间
     */
    @TableField(value = "unbound_at")
    private LocalDateTime unboundAt;
}