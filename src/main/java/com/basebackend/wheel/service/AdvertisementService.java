package com.basebackend.wheel.service;

import com.basebackend.wheel.dto.AdVO;
import com.basebackend.wheel.enums.ImpressionType;

import java.util.List;

/**
 * 广告服务
 *
 * @author wheel-api
 * @since 2025-01-29
 */
public interface AdvertisementService {

    /**
     * 获取指定广告位的广告列表
     * VIP用户自动返回空列表
     *
     * @param userId       用户ID
     * @param placementKey 广告位标识
     * @return 广告列表
     */
    List<AdVO> getAdsForPlacement(Long userId, String placementKey);

    /**
     * 记录广告展示/点击
     *
     * @param adId           广告ID
     * @param userId         用户ID（可为null）
     * @param placementKey   广告位标识
     * @param impressionType 展示类型
     */
    void recordImpression(Long adId, Long userId, String placementKey, ImpressionType impressionType);
}
