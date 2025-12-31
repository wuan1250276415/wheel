package com.basebackend.wheel.service;

import com.basebackend.wheel.dto.HomepageRecommendationVO;

/**
 * 首页推荐服务接口
 * 负责生成个性化首页推荐数据，包括分类排名、为你推荐和情侣精选
 *
 * @author wheel-api
 * @since 2025-01-31
 */
public interface HomepageRecommendationService {

    /**
     * 获取首页推荐数据
     * 包含个性化分类排名、为你推荐（Top 5）和情侣精选（如果是情侣用户）
     *
     * @param userId 用户ID
     * @return 首页推荐VO
     */
    HomepageRecommendationVO getHomepageRecommendations(Long userId);

    /**
     * 刷新用户的首页推荐缓存
     * 强制重新生成推荐数据并更新缓存
     *
     * @param userId 用户ID
     * @return 刷新后的首页推荐VO
     */
    HomepageRecommendationVO refreshHomepageRecommendations(Long userId);

    /**
     * 清除用户的首页推荐缓存
     *
     * @param userId 用户ID
     */
    void clearHomepageCache(Long userId);

    /**
     * 检查用户的首页推荐缓存是否过期
     * 缓存有效期为6小时
     *
     * @param userId 用户ID
     * @return true表示缓存已过期或不存在
     */
    boolean isHomepageCacheExpired(Long userId);

    /**
     * 首页推荐缓存有效期（小时）
     */
    int HOMEPAGE_CACHE_HOURS = 6;

    /**
     * 为你推荐数量
     */
    int RECOMMENDED_FOR_YOU_COUNT = 5;
}
