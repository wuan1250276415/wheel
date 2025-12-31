package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.basebackend.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 用户偏好调查表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.user_preference_survey", autoResultMap = true)
public class UserPreferenceSurvey extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 选择的分类ID列表
     */
    @TableField(value = "selected_categories", typeHandler = JacksonTypeHandler.class)
    private List<Long> selectedCategories;

    /**
     * 是否跳过：0-否，1-是
     */
    @TableField(value = "skipped")
    private Boolean skipped;
}
