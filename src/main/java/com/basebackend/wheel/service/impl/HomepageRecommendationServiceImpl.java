package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.dto.CategoryRankVO;
import com.basebackend.wheel.dto.CoupleRecommendationVO;
import com.basebackend.wheel.dto.HomepageRecommendationVO;
import com.basebackend.wheel.dto.RecommendationVO;
import com.basebackend.wheel.engine.RecommendationEngine;
import com.basebackend.wheel.entity.CoupleRelationship;
import com.basebackend.wheel.entity.UserProfile;
import com.basebackend.wheel.entity.WheelCategory;
import com.basebackend.wheel.mapper.CoupleRelationshipMapper;
import com.basebackend.wheel.mapper.WheelCategoryMapper;
import com.basebackend.wheel.service.HomepageRecommendationService;
import com.basebackend.wheel.service.UserProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 首页推荐服务实现类
 * 负责生成个性化首页推荐数据，包括分类排名、为你推荐和情侣精选
 *
 * @author wheel-api
 * @since 2025-01-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HomepageRecommendationServiceImpl implements HomepageRecommendationService {

    private final UserProfileService userProfileService;
    private final RecommendationEngine recommendationEngine;
    private final WheelCategoryMapper wheelCategoryMapper;
    private final CoupleRelationshipMapper coupleRelationshipMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // 缓存键前缀
    private static final String HOMEPAGE_CACHE_PREFIX = "recommendation:homepage:";
    
    // 缓存过期时间（秒）- 6小时
    private static final long HOMEPAGE_CACHE_TTL = HOMEPAGE_CACHE_HOURS * 3600L;


    @Override
    public HomepageRecommendationVO getHomepageRecommendations(Long userId) {
        if (userId == null) {
            log.warn("获取首页推荐失败：用户ID为空");
            return buildDefaultHomepageRecommendations();
        }

        log.info("获取首页推荐: userId={}", userId);

        // 1. 尝试从缓存获取
        HomepageRecommendationVO cached = getFromCache(userId);
        if (cached != null) {
            log.debug("从缓存获取首页推荐: userId={}", userId);
            return cached;
        }

        // 2. 生成新的推荐数据
        HomepageRecommendationVO recommendations = generateHomepageRecommendations(userId);

        // 3. 缓存结果
        cacheHomepageRecommendations(userId, recommendations);

        return recommendations;
    }

    @Override
    public HomepageRecommendationVO refreshHomepageRecommendations(Long userId) {
        if (userId == null) {
            log.warn("刷新首页推荐失败：用户ID为空");
            return buildDefaultHomepageRecommendations();
        }

        log.info("刷新首页推荐: userId={}", userId);

        // 1. 清除旧缓存
        clearHomepageCache(userId);

        // 2. 生成新的推荐数据
        HomepageRecommendationVO recommendations = generateHomepageRecommendations(userId);

        // 3. 缓存结果
        cacheHomepageRecommendations(userId, recommendations);

        return recommendations;
    }

    @Override
    public void clearHomepageCache(Long userId) {
        if (userId == null) {
            return;
        }
        try {
            String cacheKey = HOMEPAGE_CACHE_PREFIX + userId;
            redisTemplate.delete(cacheKey);
            log.debug("清除首页推荐缓存: userId={}", userId);
        } catch (Exception e) {
            log.warn("清除首页推荐缓存失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    @Override
    public boolean isHomepageCacheExpired(Long userId) {
        if (userId == null) {
            return true;
        }
        try {
            String cacheKey = HOMEPAGE_CACHE_PREFIX + userId;
            Long ttl = redisTemplate.getExpire(cacheKey, TimeUnit.SECONDS);
            return ttl == null || ttl <= 0;
        } catch (Exception e) {
            log.warn("检查首页推荐缓存过期失败: userId={}, error={}", userId, e.getMessage());
            return true;
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 生成首页推荐数据
     */
    private HomepageRecommendationVO generateHomepageRecommendations(Long userId) {
        HomepageRecommendationVO vo = new HomepageRecommendationVO();
        vo.setRefreshTime(LocalDateTime.now());

        try {
            // 1. 生成个性化分类排名
            List<CategoryRankVO> categoryRanking = generatePersonalizedCategoryRanking(userId);
            vo.setCategoryRanking(categoryRanking);

            // 2. 生成"为你推荐"内容（Top 5）
            List<RecommendationVO> recommendedForYou = generateRecommendedForYou(userId);
            vo.setRecommendedForYou(recommendedForYou);

            // 3. 检查是否是情侣用户，如果是则生成"情侣精选"
            List<RecommendationVO> couplePicks = generateCouplePicks(userId);
            vo.setCouplePicks(couplePicks);

            log.info("首页推荐生成完成: userId={}, categories={}, recommended={}, couplePicks={}",
                    userId, categoryRanking.size(), recommendedForYou.size(),
                    couplePicks != null ? couplePicks.size() : 0);

        } catch (Exception e) {
            log.error("生成首页推荐失败: userId={}, error={}", userId, e.getMessage(), e);
            return buildDefaultHomepageRecommendations();
        }

        return vo;
    }


    /**
     * 生成个性化分类排名
     * 根据用户偏好权重对分类进行排序
     */
    private List<CategoryRankVO> generatePersonalizedCategoryRanking(Long userId) {
        List<CategoryRankVO> ranking = new ArrayList<>();

        try {
            // 1. 获取所有启用的分类
            List<WheelCategory> allCategories = wheelCategoryMapper.selectEnabledCategoriesOrderBySort();
            if (allCategories == null || allCategories.isEmpty()) {
                log.warn("没有启用的分类");
                return ranking;
            }

            // 2. 获取用户画像
            UserProfile profile = userProfileService.getProfile(userId);
            Map<Long, Double> userPreferences = profile != null ? profile.getFavoriteCategories() : null;

            // 3. 构建分类排名
            if (userPreferences != null && !userPreferences.isEmpty()) {
                // 有用户偏好，按偏好权重排序
                ranking = buildPersonalizedRanking(allCategories, userPreferences);
            } else {
                // 无用户偏好，使用默认排序
                ranking = buildDefaultRanking(allCategories);
            }

        } catch (Exception e) {
            log.error("生成个性化分类排名失败: userId={}, error={}", userId, e.getMessage());
        }

        return ranking;
    }

    /**
     * 构建个性化分类排名
     */
    private List<CategoryRankVO> buildPersonalizedRanking(List<WheelCategory> categories, 
                                                           Map<Long, Double> userPreferences) {
        // 为每个分类计算排序分数
        List<CategoryWithScore> scoredCategories = new ArrayList<>();
        
        for (WheelCategory category : categories) {
            double weight = userPreferences.getOrDefault(category.getId(), 0.0);
            scoredCategories.add(new CategoryWithScore(category, weight));
        }

        // 按权重降序排序
        scoredCategories.sort((a, b) -> Double.compare(b.weight, a.weight));

        // 转换为VO
        List<CategoryRankVO> ranking = new ArrayList<>();
        int rank = 1;
        for (CategoryWithScore scored : scoredCategories) {
            CategoryRankVO vo = new CategoryRankVO();
            vo.setCategoryId(scored.category.getId());
            vo.setCategoryName(scored.category.getCategoryName());
            vo.setIconUrl(scored.category.getIconUrl());
            vo.setRank(rank++);
            vo.setWeight(scored.weight);
            ranking.add(vo);
        }

        return ranking;
    }

    /**
     * 构建默认分类排名（无用户偏好时使用）
     */
    private List<CategoryRankVO> buildDefaultRanking(List<WheelCategory> categories) {
        List<CategoryRankVO> ranking = new ArrayList<>();
        int rank = 1;
        for (WheelCategory category : categories) {
            CategoryRankVO vo = new CategoryRankVO();
            vo.setCategoryId(category.getId());
            vo.setCategoryName(category.getCategoryName());
            vo.setIconUrl(category.getIconUrl());
            vo.setRank(rank++);
            vo.setWeight(0.0);
            ranking.add(vo);
        }
        return ranking;
    }

    /**
     * 生成"为你推荐"内容（Top 5）
     */
    private List<RecommendationVO> generateRecommendedForYou(Long userId) {
        try {
            // 使用推荐引擎获取个性化推荐
            List<RecommendationVO> recommendations = recommendationEngine.getPersonalRecommendations(
                    userId, RECOMMENDED_FOR_YOU_COUNT);

            // 确保每个推荐都有推荐原因
            for (RecommendationVO rec : recommendations) {
                if (rec.getReason() == null || rec.getReason().isEmpty()) {
                    rec.setReason("为你推荐");
                }
            }

            return recommendations;

        } catch (Exception e) {
            log.error("生成为你推荐失败: userId={}, error={}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }


    /**
     * 生成"情侣精选"内容
     * 仅对有情侣关系的用户生成
     */
    private List<RecommendationVO> generateCouplePicks(Long userId) {
        try {
            // 1. 检查用户是否有情侣关系
            CoupleRelationship relationship = coupleRelationshipMapper.selectConfirmedByUserId(userId);
            if (relationship == null) {
                log.debug("用户无情侣关系，不生成情侣精选: userId={}", userId);
                return null; // 返回null表示不显示情侣精选区域
            }

            // 2. 获取情侣推荐
            List<CoupleRecommendationVO> coupleRecommendations = 
                    recommendationEngine.getCoupleRecommendations(userId, 3);

            if (coupleRecommendations == null || coupleRecommendations.isEmpty()) {
                log.debug("情侣推荐为空: userId={}", userId);
                return Collections.emptyList();
            }

            // 3. 从情侣推荐中提取内容，取每个分类的第一个内容
            List<RecommendationVO> couplePicks = new ArrayList<>();
            for (CoupleRecommendationVO coupleRec : coupleRecommendations) {
                if (coupleRec.getContents() != null && !coupleRec.getContents().isEmpty()) {
                    RecommendationVO pick = coupleRec.getContents().get(0);
                    // 更新推荐原因为情侣相关
                    pick.setReason(coupleRec.getMatchReason() != null 
                            ? coupleRec.getMatchReason() 
                            : "情侣精选");
                    couplePicks.add(pick);
                }
            }

            // 限制最多5个
            if (couplePicks.size() > RECOMMENDED_FOR_YOU_COUNT) {
                couplePicks = couplePicks.subList(0, RECOMMENDED_FOR_YOU_COUNT);
            }

            return couplePicks;

        } catch (Exception e) {
            log.error("生成情侣精选失败: userId={}, error={}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * 构建默认首页推荐（降级方案）
     */
    private HomepageRecommendationVO buildDefaultHomepageRecommendations() {
        HomepageRecommendationVO vo = new HomepageRecommendationVO();
        vo.setRefreshTime(LocalDateTime.now());

        try {
            // 使用默认分类排名
            List<WheelCategory> categories = wheelCategoryMapper.selectEnabledCategoriesOrderBySort();
            if (categories != null) {
                vo.setCategoryRanking(buildDefaultRanking(categories));
            } else {
                vo.setCategoryRanking(Collections.emptyList());
            }

            // 空的推荐列表
            vo.setRecommendedForYou(Collections.emptyList());
            vo.setCouplePicks(null);

        } catch (Exception e) {
            log.error("构建默认首页推荐失败: error={}", e.getMessage());
            vo.setCategoryRanking(Collections.emptyList());
            vo.setRecommendedForYou(Collections.emptyList());
            vo.setCouplePicks(null);
        }

        return vo;
    }

    /**
     * 从缓存获取首页推荐
     */
    private HomepageRecommendationVO getFromCache(Long userId) {
        try {
            String cacheKey = HOMEPAGE_CACHE_PREFIX + userId;
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return objectMapper.convertValue(cached, HomepageRecommendationVO.class);
            }
        } catch (Exception e) {
            log.warn("从缓存获取首页推荐失败: userId={}, error={}", userId, e.getMessage());
        }
        return null;
    }

    /**
     * 缓存首页推荐
     */
    private void cacheHomepageRecommendations(Long userId, HomepageRecommendationVO recommendations) {
        if (userId == null || recommendations == null) {
            return;
        }
        try {
            String cacheKey = HOMEPAGE_CACHE_PREFIX + userId;
            redisTemplate.opsForValue().set(cacheKey, recommendations, HOMEPAGE_CACHE_TTL, TimeUnit.SECONDS);
            log.debug("缓存首页推荐: userId={}", userId);
        } catch (Exception e) {
            log.warn("缓存首页推荐失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 分类带分数的辅助类
     */
    private static class CategoryWithScore {
        final WheelCategory category;
        final double weight;

        CategoryWithScore(WheelCategory category, double weight) {
            this.category = category;
            this.weight = weight;
        }
    }
}
