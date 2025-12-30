package com.basebackend.wheel.dto;

import com.basebackend.wheel.enums.AdType;
import lombok.Data;

/**
 * 广告VO
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Data
public class AdVO {
    /**
     * 广告ID
     */
    private Long id;

    /**
     * 广告类型
     */
    private AdType adType;

    /**
     * 广告内容URL
     */
    private String contentUrl;

    /**
     * 广告文本内容
     */
    private String contentText;

    /**
     * 点击跳转链接
     */
    private String linkUrl;

    /**
     * 广告位标识
     */
    private String placementKey;
}
