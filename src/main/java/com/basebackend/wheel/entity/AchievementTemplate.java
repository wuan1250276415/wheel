package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.achievement_template")
public class AchievementTemplate extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableField(value = "achievement_code")
    @NotBlank(message = "成就编码不能为空")
    @Size(max = 50)
    private String achievementCode;

    @TableField(value = "achievement_name")
    @NotBlank(message = "成就名称不能为空")
    @Size(max = 100)
    private String achievementName;

    @TableField(value = "description")
    private String description;

    @TableField(value = "icon_url")
    @Size(max = 500)
    private String iconUrl;

    @TableField(value = "rarity")
    @NotNull(message = "稀有度不能为null")
    private Integer rarity;

    @TableField(value = "unlock_condition")
    private String unlockCondition;

    @TableField(value = "is_hidden")
    private Integer isHidden;

    @TableField(value = "category")
    private Integer category;

    @TableField(value = "`status`")
    private Integer status;
}
