package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 转盘配置表
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("wheel_config")
public class WheelConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置ID - 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 转盘半径（像素）
     */
    @TableField("wheel_radius")
    private Integer wheelRadius;

    /**
     * 选项数量
     */
    @TableField("option_count")
    private Integer optionCount;

    /**
     * 动画时长（毫秒）
     */
    @TableField("animation_duration")
    private Integer animationDuration;

    /**
     * 转盘主题：0-默认，1-浪漫，2-可爱，3-简约
     */
    @TableField("theme_style")
    private Integer themeStyle;

    /**
     * 是否显示历史记录：0-否，1-是
     */
    @TableField("show_history")
    private Integer showHistory;

    /**
     * 声音效果：0-关闭，1-开启
     */
    @TableField("sound_effect")
    private Integer soundEffect;

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
