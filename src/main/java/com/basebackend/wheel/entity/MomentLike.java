package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 动态点赞表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.moment_like")
public class MomentLike extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 动态ID
     */
    @TableField(value = "moment_id")
    private Long momentId;

    /**
     * 点赞用户ID
     */
    @TableField(value = "user_id")
    private Long userId;
}
