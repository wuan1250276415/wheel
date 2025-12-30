package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import com.basebackend.wheel.enums.ImpressionType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 广告展示记录表
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ad_impression")
public class AdImpression extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 广告ID
     */
    @TableField("ad_id")
    private Long adId;

    /**
     * 用户ID（未登录时为NULL）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 广告位标识
     */
    @TableField("placement_key")
    private String placementKey;

    /**
     * 类型：1-展示 2-点击
     */
    @TableField("impression_type")
    private ImpressionType impressionType;

    /**
     * 设备类型
     */
    @TableField("device_type")
    private String deviceType;

    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * User Agent
     */
    @TableField("user_agent")
    private String userAgent;
}
