package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.basebackend.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 敏感词库表
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sensitive_word", autoResultMap = true)
public class SensitiveWord extends BaseEntity {

    /**
     * 敏感词
     */
    @TableField("word")
    private String word;

    /**
     * 分类：1-色情 2-暴力 3-政治 4-广告 5-其他
     */
    @TableField("category")
    private Integer category;

    /**
     * 风险等级：1-低 2-中 3-高
     */
    @TableField("level")
    private Integer level;

    /**
     * 变体词列表（JSON数组）
     */
    @TableField(value = "variants", typeHandler = JacksonTypeHandler.class)
    private List<String> variants;

    /**
     * 状态：0-禁用 1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 敏感词分类常量
     */
    public static final int CATEGORY_PORN = 1;
    public static final int CATEGORY_VIOLENCE = 2;
    public static final int CATEGORY_POLITICS = 3;
    public static final int CATEGORY_AD = 4;
    public static final int CATEGORY_OTHER = 5;

    /**
     * 风险等级常量
     */
    public static final int LEVEL_LOW = 1;
    public static final int LEVEL_MEDIUM = 2;
    public static final int LEVEL_HIGH = 3;

    /**
     * 状态常量
     */
    public static final int STATUS_DISABLED = 0;
    public static final int STATUS_ENABLED = 1;
}
