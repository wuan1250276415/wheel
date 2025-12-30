package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 广告位配置表
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ad_placement")
public class AdPlacement extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 广告位标识key
     */
    @TableField("placement_key")
    private String placementKey;

    /**
     * 广告位名称
     */
    @TableField("placement_name")
    private String placementName;

    /**
     * 广告位描述
     */
    @TableField("description")
    private String description;

    /**
     * 最大同时展示广告数量
     */
    @TableField("max_ads")
    private Integer maxAds;

    /**
     * 推荐宽度（px）
     */
    @TableField("width")
    private Integer width;

    /**
     * 推荐高度（px）
     */
    @TableField("height")
    private Integer height;

    /**
     * 状态：0-禁用 1-启用
     */
    @TableField("status")
    private Integer status;
}
