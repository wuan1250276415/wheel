package com.basebackend.wheel.entity;

import com.basebackend.database.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 转盘分类表
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "转盘分类表")
@Data
public class WheelCategory extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * 分类名称
     */
    @Schema(description = "分类名称")
    @Size(max = 100, message = "分类名称最大长度要小于 100")
    @NotBlank(message = "分类名称不能为空")
    private String categoryName;

    /**
     * 分类描述
     */
    @Schema(description = "分类描述")
    private String description;

    /**
     * 排序权重
     */
    @Schema(description = "排序权重")
    private Integer sortOrder;

    /**
     * 分类图标URL
     */
    @Schema(description = "分类图标URL")
    @Size(max = 500, message = "分类图标URL最大长度要小于 500")
    private String iconUrl;

    /**
     * 主题色
     */
    @Schema(description = "主题色")
    @Size(max = 20, message = "主题色最大长度要小于 20")
    private String themeColor;

    /**
     * 分类状态：0-禁用，1-启用
     */
    @Schema(description = "分类状态：0-禁用，1-启用")
    private Boolean status;

    /**
     * 是否系统内置：0-否，1-是
     */
    @Schema(description = "是否系统内置：0-否，1-是")
    private Boolean isSystem;

}