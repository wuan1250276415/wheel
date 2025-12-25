package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 转盘配置表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.wheel_config")
public class WheelConfig extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    @NotNull(message = "用户ID不能为null")
    private Long userId;

    /**
     * 转盘半径（像素）
     */
    @TableField(value = "wheel_radius")
    private Integer wheelRadius;

    /**
     * 选项数量
     */
    @TableField(value = "option_count")
    private Integer optionCount;

    /**
     * 动画时长（毫秒）
     */
    @TableField(value = "animation_duration")
    private Integer animationDuration;

    /**
     * 主题风格：0-默认，1-浪漫，2-可爱，3-简约
     */
    @TableField(value = "theme_style")
    private Integer themeStyle;

    /**
     * 是否显示历史：0-否，1-是
     */
    @TableField(value = "show_history")
    private Boolean showHistory;

    /**
     * 声音效果：0-关闭，1-开启
     */
    @TableField(value = "sound_effect")
    private Boolean soundEffect;

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