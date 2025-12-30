package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import com.basebackend.wheel.enums.AdType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 广告内容表
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("advertisement")
public class Advertisement extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 广告名称
     */
    @TableField("ad_name")
    private String adName;

    /**
     * 广告位ID
     */
    @TableField("placement_id")
    private Long placementId;

    /**
     * 广告类型：1-图片 2-视频 3-文字
     */
    @TableField("ad_type")
    private AdType adType;

    /**
     * 广告内容URL（图片/视频地址）
     */
    @TableField("content_url")
    private String contentUrl;

    /**
     * 广告文本内容
     */
    @TableField("content_text")
    private String contentText;

    /**
     * 点击跳转链接
     */
    @TableField("link_url")
    private String linkUrl;

    /**
     * 优先级（数值越大优先级越高）
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 广告开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 广告结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 状态：0-禁用 1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 展示次数
     */
    @TableField("impression_count")
    private Long impressionCount;

    /**
     * 点击次数
     */
    @TableField("click_count")
    private Long clickCount;
}
