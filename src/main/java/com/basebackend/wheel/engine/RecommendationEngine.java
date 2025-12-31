package com.basebackend.wheel.engine;

import com.basebackend.wheel.dto.CoupleRecommendationVO;
import com.basebackend.wheel.dto.RecommendationVO;
import com.basebackend.wheel.entity.CoupleRelationship;
import com.basebackend.wheel.entity.UserProfile;
import com.basebackend.wheel.entity.WheelCategory;
import com.basebackend.wheel.entity.WheelContent;
import com.basebackend.wheel.mapper.CoupleRelationshipMapper;
import com.basebackend.wheel.mapper.RecommendationLogMapper;
import com.basebackend.wheel.mapper.UserProfileMapper;
import com.basebackend.wheel.mapper.WheelCategoryMapper;
import com.basebackend.wheel.mapper.WheelContentMapper;
import com.basebackend.wheel.service.ColdStartService;
import com.basebackend.wheel.service.ContentFeatureService;
import com.basebackend.wheel.service.HotContentCacheService;
import com.basebackend.wheel.service.UserProfileService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐引擎
 * 实现混合推荐算法（协同过滤 + 基于内容的推荐）
 *
 * @author wheel-api
 * @since 2025-01-31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationEngine {

    private final UserProfileService userProfileService;
    private final ContentFeatureService contentFeatureService;
    private final HotContentCacheService hotContentCacheService;
    private final UserProfileMapper userProfileMapper;
    private final WheelContentMapper wheelContentMapper;
    private final WheelCategoryMapper wheelCategoryMapper;
    private final RecommendationLogMapper recommendationLogMapper;
    private final CoupleRelationshipMapper coupleRelationshipMapper;
    
    @Lazy
    private final ColdStartService coldStartService;

    // 协同过滤权重
    private static final double CF_WEIGHT = 0.4;
    // 基于内容权重
    private static final double CB_WEIGHT = 0.6;
    // 最小推荐数量
    private static final int MIN_RECOMMENDATIONS = 10;
    // 24小时内已看内容排除
    private static final int SEEN_CONTENT_HOURS = 24;
    // 相似用户数量
    private static final int SIMILAR_USER_COUNT = 20;
    // 情侣推荐最小分类数量
    private static final int MIN_COUPLE_CATEGORIES = 3;
    // 新情侣关系阈值（天数）
    private static final int NEW_COUPLE_DAYS = 30;
    // 中期情侣关系阈值（天数）
    private static final int MID_COUPLE_DAYS = 180;

    /**
     * 混合推荐算法
     * 结合协同过滤(40%)和基于内容的推荐(60%)
     *
     * @param profile 用户画像
     * @param limit 返回数量限制
     * @return 带分数的推荐内容列表
     */
    public List<ScoredContent> hybridRecommend(UserProfile profile, int limit) {
        if (profile == null || profile.getUserId() == null) {
            log.warn("混合推荐失败：用户画像为空");
            return Collections.emptyList();
        }

        Long userId = profile.getUserId();
        log.info("开始混合推荐: userId={}, limit={}", userId, limit);

        // 1. 获取协同过滤推荐
        List<ScoredContent> cfResults = collaborativeFilter(profile, limit * 2);
        log.debug("协同过滤结果数量: {}", cfResults.size());

        // 2. 获取基于内容的推荐
        List<ScoredContent> cbResults = contentBasedFilter(profile, limit * 2);
        log.debug("基于内容推荐结果数量: {}", cbResults.size());

        // 3. 合并并加权计算最终分数
        List<ScoredContent> merged = mergeAndScore(cfResults, cbResults, CF_WEIGHT, CB_WEIGHT, limit);
        log.info("混合推荐完成: userId={}, resultCount={}", userId, merged.size());

        return merged;
    }

    /**
     * 协同过滤：基于相似用户的推荐
     * 找到与当前用户偏好相似的用户，推荐他们喜欢的内容
     *
     * @param profile 用户画像
     * @param limit 返回数量限制
     * @return 带分数的推荐内容列表
     */
    public List<ScoredContent> collaborativeFilter(UserProfile profile, int limit) {
        if (profile == null || profile.getUserId() == null) {
            return Collections.emptyList();
        }

        Long userId = profile.getUserId();
        Map<Long, Double> userCategories = profile.getFavoriteCategories();
        
        if (userCategories == null || userCategories.isEmpty()) {
            log.debug("用户无偏好分类，协同过滤返回空: userId={}", userId);
            return Collections.emptyList();
        }

        try {
            // 1. 查找相似用户
            List<UserProfile> similarUsers = findSimilarUsers(profile, SIMILAR_USER_COUNT);
            if (similarUsers.isEmpty()) {
                log.debug("未找到相似用户: userId={}", userId);
                return Collections.emptyList();
            }

            // 2. 收集相似用户喜欢的分类
            Map<Long, Double> categoryScores = new HashMap<>();
            for (UserProfile similarUser : similarUsers) {
                if (similarUser.getFavoriteCategories() != null) {
                    double similarity = calculateUserSimilarity(profile, similarUser);
                    for (Map.Entry<Long, Double> entry : similarUser.getFavoriteCategories().entrySet()) {
                        Long categoryId = entry.getKey();
                        Double weight = entry.getValue();
                        // 加权累加：相似度 * 用户对该分类的偏好权重
                        categoryScores.merge(categoryId, similarity * weight, Double::sum);
                    }
                }
            }

            // 3. 获取这些分类下的热门内容
            List<ScoredContent> results = new ArrayList<>();
            List<Long> sortedCategories = categoryScores.entrySet().stream()
                    .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                    .map(Map.Entry::getKey)
                    .limit(5) // 取前5个分类
                    .collect(Collectors.toList());

            for (Long categoryId : sortedCategories) {
                List<WheelContent> hotContents = hotContentCacheService.getHotContentByCategory(categoryId, limit / 2);
                double categoryScore = categoryScores.getOrDefault(categoryId, 0.0);
                
                for (WheelContent content : hotContents) {
                    // 排除用户自己已经高频使用的内容
                    double contentScore = categoryScore * (content.getPopularityScore() != null ? content.getPopularityScore() : 0.5);
                    results.add(new ScoredContent(content, contentScore, "协同过滤推荐"));
                }
            }

            // 4. 按分数排序并限制数量
            return results.stream()
                    .sorted(Comparator.comparingDouble(ScoredContent::getScore).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("协同过滤失败: userId={}, error={}", userId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 基于内容的推荐：根据用户偏好和内容特征匹配
     *
     * @param profile 用户画像
     * @param limit 返回数量限制
     * @return 带分数的推荐内容列表
     */
    public List<ScoredContent> contentBasedFilter(UserProfile profile, int limit) {
        if (profile == null || profile.getUserId() == null) {
            return Collections.emptyList();
        }

        Long userId = profile.getUserId();
        Map<Long, Double> userCategories = profile.getFavoriteCategories();

        try {
            List<ScoredContent> results = new ArrayList<>();

            // 1. 如果用户有偏好分类，基于偏好推荐
            if (userCategories != null && !userCategories.isEmpty()) {
                // 按偏好权重排序分类
                List<Map.Entry<Long, Double>> sortedCategories = userCategories.entrySet().stream()
                        .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                        .collect(Collectors.toList());

                for (Map.Entry<Long, Double> entry : sortedCategories) {
                    Long categoryId = entry.getKey();
                    Double categoryWeight = entry.getValue();

                    // 获取该分类下的内容
                    List<WheelContent> contents = wheelContentMapper.selectByCategoryIdAndAuditStatus(categoryId, 1);
                    
                    for (WheelContent content : contents) {
                        // 计算内容与用户偏好的匹配分数
                        double matchScore = calculateContentMatchScore(profile, content, categoryWeight);
                        String reason = generateRecommendationReason(categoryId, categoryWeight);
                        results.add(new ScoredContent(content, matchScore, reason));
                    }
                }
            }

            // 2. 如果结果不足，补充热门内容
            if (results.size() < limit) {
                List<WheelContent> hotContents = hotContentCacheService.getAllHotContent(limit - results.size());
                Set<Long> existingIds = results.stream()
                        .map(sc -> sc.getContent().getId())
                        .collect(Collectors.toSet());

                for (WheelContent content : hotContents) {
                    if (!existingIds.contains(content.getId())) {
                        double score = content.getPopularityScore() != null ? content.getPopularityScore() * 0.5 : 0.25;
                        results.add(new ScoredContent(content, score, "热门推荐"));
                    }
                }
            }

            // 3. 按分数排序并限制数量
            return results.stream()
                    .sorted(Comparator.comparingDouble(ScoredContent::getScore).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("基于内容推荐失败: userId={}, error={}", userId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 合并协同过滤和基于内容的推荐结果
     * 最终分数 = CF_WEIGHT * cf_score + CB_WEIGHT * cb_score
     */
    private List<ScoredContent> mergeAndScore(List<ScoredContent> cfResults, List<ScoredContent> cbResults,
                                               double cfWeight, double cbWeight, int limit) {
        // 使用Map合并相同内容的分数
        Map<Long, MergedScore> scoreMap = new HashMap<>();

        // 处理协同过滤结果
        for (ScoredContent sc : cfResults) {
            Long contentId = sc.getContent().getId();
            scoreMap.computeIfAbsent(contentId, k -> new MergedScore(sc.getContent()))
                    .setCfScore(sc.getScore())
                    .setCfReason(sc.getReason());
        }

        // 处理基于内容的结果
        for (ScoredContent sc : cbResults) {
            Long contentId = sc.getContent().getId();
            scoreMap.computeIfAbsent(contentId, k -> new MergedScore(sc.getContent()))
                    .setCbScore(sc.getScore())
                    .setCbReason(sc.getReason());
        }

        // 计算最终分数并排序
        return scoreMap.values().stream()
                .map(ms -> {
                    double finalScore = cfWeight * ms.getCfScore() + cbWeight * ms.getCbScore();
                    String reason = ms.getCbReason() != null ? ms.getCbReason() : ms.getCfReason();
                    return new ScoredContent(ms.getContent(), finalScore, reason);
                })
                .sorted(Comparator.comparingDouble(ScoredContent::getScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 查找相似用户
     * 基于用户偏好分类的余弦相似度
     */
    private List<UserProfile> findSimilarUsers(UserProfile targetProfile, int limit) {
        Long targetUserId = targetProfile.getUserId();
        Map<Long, Double> targetCategories = targetProfile.getFavoriteCategories();

        if (targetCategories == null || targetCategories.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            // 获取活跃用户画像（最近30天有活动的）
            LocalDateTime threshold = LocalDateTime.now().minusDays(30);
            List<UserProfile> activeProfiles = userProfileMapper.selectStaleProfiles(threshold, 100);
            
            if (activeProfiles == null || activeProfiles.isEmpty()) {
                return Collections.emptyList();
            }

            // 计算相似度并排序
            return activeProfiles.stream()
                    .filter(p -> !p.getUserId().equals(targetUserId)) // 排除自己
                    .filter(p -> p.getFavoriteCategories() != null && !p.getFavoriteCategories().isEmpty())
                    .map(p -> new AbstractMap.SimpleEntry<>(p, calculateUserSimilarity(targetProfile, p)))
                    .filter(e -> e.getValue() > 0.1) // 相似度阈值
                    .sorted(Map.Entry.<UserProfile, Double>comparingByValue().reversed())
                    .limit(limit)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("查找相似用户失败: userId={}, error={}", targetUserId, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 计算两个用户之间的相似度
     * 使用余弦相似度计算偏好分类的相似程度
     */
    private double calculateUserSimilarity(UserProfile profile1, UserProfile profile2) {
        Map<Long, Double> cat1 = profile1.getFavoriteCategories();
        Map<Long, Double> cat2 = profile2.getFavoriteCategories();

        if (cat1 == null || cat2 == null || cat1.isEmpty() || cat2.isEmpty()) {
            return 0.0;
        }

        // 计算余弦相似度
        Set<Long> allCategories = new HashSet<>();
        allCategories.addAll(cat1.keySet());
        allCategories.addAll(cat2.keySet());

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (Long categoryId : allCategories) {
            double v1 = cat1.getOrDefault(categoryId, 0.0);
            double v2 = cat2.getOrDefault(categoryId, 0.0);
            dotProduct += v1 * v2;
            norm1 += v1 * v1;
            norm2 += v2 * v2;
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 计算内容与用户偏好的匹配分数
     */
    private double calculateContentMatchScore(UserProfile profile, WheelContent content, double categoryWeight) {
        double score = categoryWeight; // 基础分数为分类权重

        // 考虑内容热度
        if (content.getPopularityScore() != null) {
            score *= (0.5 + 0.5 * content.getPopularityScore());
        }

        // 考虑活跃时段匹配
        if (profile.getActiveTimeSlots() != null && !profile.getActiveTimeSlots().isEmpty()) {
            int currentHour = LocalDateTime.now().getHour();
            for (UserProfile.TimeSlot slot : profile.getActiveTimeSlots()) {
                if (slot.getHour() != null && slot.getHour() == currentHour) {
                    score *= (1.0 + 0.2 * slot.getWeight()); // 时段匹配加成
                    break;
                }
            }
        }

        return Math.min(1.0, score); // 限制最大分数为1.0
    }

    /**
     * 生成推荐原因
     */
    private String generateRecommendationReason(Long categoryId, Double weight) {
        try {
            WheelCategory category = wheelCategoryMapper.selectById(categoryId);
            if (category != null) {
                if (weight > 0.7) {
                    return "基于你对「" + category.getCategoryName() + "」的热爱";
                } else if (weight > 0.4) {
                    return "你可能喜欢「" + category.getCategoryName() + "」";
                } else {
                    return "为你推荐「" + category.getCategoryName() + "」";
                }
            }
        } catch (Exception e) {
            log.warn("生成推荐原因失败: categoryId={}", categoryId);
        }
        return "为你推荐";
    }

    /**
     * 过滤24小时内已看过的内容
     *
     * @param userId 用户ID
     * @param recommendations 推荐列表
     * @return 过滤后的推荐列表
     */
    public List<ScoredContent> filterSeenContent(Long userId, List<ScoredContent> recommendations) {
        if (userId == null || recommendations == null || recommendations.isEmpty()) {
            return recommendations;
        }

        try {
            // 获取24小时内已看过的内容ID
            LocalDateTime since = LocalDateTime.now().minusHours(SEEN_CONTENT_HOURS);
            List<Long> seenContentIds = recommendationLogMapper.selectSeenContentIds(userId, since);

            if (seenContentIds == null || seenContentIds.isEmpty()) {
                return recommendations;
            }

            Set<Long> seenSet = new HashSet<>(seenContentIds);
            List<ScoredContent> filtered = recommendations.stream()
                    .filter(sc -> !seenSet.contains(sc.getContent().getId()))
                    .collect(Collectors.toList());

            log.debug("过滤已看内容: userId={}, before={}, after={}, seenCount={}",
                    userId, recommendations.size(), filtered.size(), seenContentIds.size());

            return filtered;

        } catch (Exception e) {
            log.error("过滤已看内容失败: userId={}, error={}", userId, e.getMessage());
            return recommendations;
        }
    }

    /**
     * 确保最小推荐数量
     * 如果推荐数量不足，用热门内容补充
     *
     * @param userId 用户ID
     * @param recommendations 当前推荐列表
     * @param minCount 最小数量
     * @return 补充后的推荐列表
     */
    public List<ScoredContent> ensureMinimumRecommendations(Long userId, List<ScoredContent> recommendations, int minCount) {
        if (recommendations == null) {
            recommendations = new ArrayList<>();
        }

        if (recommendations.size() >= minCount) {
            return recommendations;
        }

        try {
            // 获取已有内容ID
            Set<Long> existingIds = recommendations.stream()
                    .map(sc -> sc.getContent().getId())
                    .collect(Collectors.toSet());

            // 获取24小时内已看过的内容ID
            Set<Long> seenIds = new HashSet<>();
            if (userId != null) {
                LocalDateTime since = LocalDateTime.now().minusHours(SEEN_CONTENT_HOURS);
                List<Long> seenContentIds = recommendationLogMapper.selectSeenContentIds(userId, since);
                if (seenContentIds != null) {
                    seenIds.addAll(seenContentIds);
                }
            }

            // 用热门内容补充
            int needed = minCount - recommendations.size();
            List<WheelContent> hotContents = hotContentCacheService.getAllHotContent(needed * 2);

            List<ScoredContent> result = new ArrayList<>(recommendations);
            for (WheelContent content : hotContents) {
                if (result.size() >= minCount) {
                    break;
                }
                if (!existingIds.contains(content.getId()) && !seenIds.contains(content.getId())) {
                    double score = content.getPopularityScore() != null ? content.getPopularityScore() * 0.3 : 0.15;
                    result.add(new ScoredContent(content, score, "热门推荐"));
                    existingIds.add(content.getId());
                }
            }

            // 如果热门内容还不够，从数据库获取所有可用内容
            if (result.size() < minCount) {
                List<WheelContent> allContents = wheelContentMapper.selectAllApprovedAndEnabled();
                for (WheelContent content : allContents) {
                    if (result.size() >= minCount) {
                        break;
                    }
                    if (!existingIds.contains(content.getId()) && !seenIds.contains(content.getId())) {
                        double score = 0.1;
                        result.add(new ScoredContent(content, score, "为你推荐"));
                        existingIds.add(content.getId());
                    }
                }
            }

            log.debug("补充推荐数量: userId={}, before={}, after={}, minCount={}",
                    userId, recommendations.size(), result.size(), minCount);

            return result;

        } catch (Exception e) {
            log.error("补充推荐数量失败: userId={}, error={}", userId, e.getMessage());
            return recommendations;
        }
    }

    /**
     * 获取个性化推荐（完整流程）
     * 包含冷启动检测、混合推荐、过滤已看内容、确保最小数量
     *
     * @param userId 用户ID
     * @param limit 返回数量限制
     * @return 推荐结果VO列表
     */
    public List<RecommendationVO> getPersonalRecommendations(Long userId, int limit) {
        if (userId == null) {
            log.warn("获取个性化推荐失败：用户ID为空");
            return Collections.emptyList();
        }

        try {
            // 1. 检查是否处于冷启动阶段
            if (coldStartService.isInColdStartPhase(userId)) {
                log.info("用户处于冷启动阶段，使用冷启动推荐: userId={}", userId);
                return coldStartService.getColdStartRecommendations(userId, limit);
            }

            // 2. 获取用户画像
            UserProfile profile = userProfileService.getProfile(userId);
            if (profile == null) {
                log.info("用户画像不存在，使用冷启动推荐: userId={}", userId);
                return coldStartService.getColdStartRecommendations(userId, limit);
            }

            // 3. 执行混合推荐
            List<ScoredContent> recommendations = hybridRecommend(profile, limit * 2);

            // 4. 过滤24小时内已看内容
            recommendations = filterSeenContent(userId, recommendations);

            // 5. 确保最小推荐数量
            int minCount = Math.min(MIN_RECOMMENDATIONS, limit);
            recommendations = ensureMinimumRecommendations(userId, recommendations, minCount);

            // 6. 限制返回数量并转换为VO
            return recommendations.stream()
                    .limit(limit)
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取个性化推荐失败: userId={}, error={}", userId, e.getMessage(), e);
            return getHotRecommendations(limit);
        }
    }

    /**
     * 获取热门推荐（降级方案）
     */
    private List<RecommendationVO> getHotRecommendations(int limit) {
        try {
            List<WheelContent> hotContents = hotContentCacheService.getAllHotContent(limit);
            return hotContents.stream()
                    .map(content -> {
                        RecommendationVO vo = new RecommendationVO();
                        vo.setContentId(content.getId());
                        vo.setContentText(content.getContentText());
                        vo.setCategoryId(content.getCategoryId());
                        vo.setScore(content.getPopularityScore() != null ? content.getPopularityScore() : 0.5);
                        vo.setReason("热门推荐");
                        // 获取分类名称
                        try {
                            WheelCategory category = wheelCategoryMapper.selectById(content.getCategoryId());
                            if (category != null) {
                                vo.setCategoryName(category.getCategoryName());
                            }
                        } catch (Exception e) {
                            log.warn("获取分类名称失败: categoryId={}", content.getCategoryId());
                        }
                        return vo;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取热门推荐失败: error={}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 转换ScoredContent为RecommendationVO
     */
    private RecommendationVO convertToVO(ScoredContent scoredContent) {
        WheelContent content = scoredContent.getContent();
        RecommendationVO vo = new RecommendationVO();
        vo.setContentId(content.getId());
        vo.setContentText(content.getContentText());
        vo.setCategoryId(content.getCategoryId());
        vo.setScore(scoredContent.getScore());
        vo.setReason(scoredContent.getReason());

        // 获取分类名称
        try {
            WheelCategory category = wheelCategoryMapper.selectById(content.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }
        } catch (Exception e) {
            log.warn("获取分类名称失败: categoryId={}", content.getCategoryId());
        }

        return vo;
    }

    /**
     * 带分数的内容
     */
    @Data
    public static class ScoredContent {
        private final WheelContent content;
        private final double score;
        private final String reason;

        public ScoredContent(WheelContent content, double score, String reason) {
            this.content = content;
            this.score = score;
            this.reason = reason;
        }
    }

    /**
     * 合并分数辅助类
     */
    @Data
    private static class MergedScore {
        private final WheelContent content;
        private double cfScore = 0.0;
        private double cbScore = 0.0;
        private String cfReason;
        private String cbReason;

        public MergedScore(WheelContent content) {
            this.content = content;
        }

        public MergedScore setCfScore(double score) {
            this.cfScore = score;
            return this;
        }

        public MergedScore setCbScore(double score) {
            this.cbScore = score;
            return this;
        }

        public MergedScore setCfReason(String reason) {
            this.cfReason = reason;
            return this;
        }

        public MergedScore setCbReason(String reason) {
            this.cbReason = reason;
            return this;
        }
    }

    // ==================== 情侣推荐相关方法 ====================

    /**
     * 获取情侣推荐
     * 分析双方画像，生成共同兴趣推荐
     *
     * @param userId 当前用户ID
     * @param limit 每个分类的推荐数量限制
     * @return 情侣推荐VO列表
     */
    public List<CoupleRecommendationVO> getCoupleRecommendations(Long userId, int limit) {
        if (userId == null) {
            log.warn("获取情侣推荐失败：用户ID为空");
            return Collections.emptyList();
        }

        try {
            // 1. 获取情侣关系
            CoupleRelationship relationship = coupleRelationshipMapper.selectConfirmedByUserId(userId);
            if (relationship == null) {
                log.info("用户无情侣关系，无法获取情侣推荐: userId={}", userId);
                return Collections.emptyList();
            }

            // 2. 获取伴侣ID
            Long partnerId = relationship.getUserId1().equals(userId) 
                    ? relationship.getUserId2() 
                    : relationship.getUserId1();

            // 3. 获取双方画像
            UserProfile profile1 = userProfileService.getProfile(userId);
            UserProfile profile2 = userProfileService.getProfile(partnerId);

            // 4. 执行情侣推荐算法
            return coupleRecommend(profile1, profile2, relationship, limit);

        } catch (Exception e) {
            log.error("获取情侣推荐失败: userId={}, error={}", userId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 情侣推荐算法
     * 分析双方画像，优先推荐共同兴趣，处理偏好冲突，考虑关系时长
     *
     * @param profile1 用户1画像
     * @param profile2 用户2画像（伴侣）
     * @param relationship 情侣关系
     * @param limit 每个分类的推荐数量限制
     * @return 情侣推荐VO列表
     */
    public List<CoupleRecommendationVO> coupleRecommend(UserProfile profile1, UserProfile profile2, 
                                                         CoupleRelationship relationship, int limit) {
        log.info("开始情侣推荐: user1={}, user2={}", 
                profile1 != null ? profile1.getUserId() : null,
                profile2 != null ? profile2.getUserId() : null);

        // 1. 分析共同兴趣
        CoupleInterestAnalysis analysis = analyzeSharedInterests(profile1, profile2);
        log.debug("共同兴趣分析完成: sharedCategories={}, conflictCategories={}", 
                analysis.getSharedCategories().size(), analysis.getConflictCategories().size());

        // 2. 计算关系时长并确定推荐难度
        int recommendedDifficulty = calculateRecommendedDifficulty(relationship);
        log.debug("推荐难度等级: {}", recommendedDifficulty);

        // 3. 生成推荐结果
        List<CoupleRecommendationVO> recommendations = new ArrayList<>();

        // 3.1 优先推荐共同兴趣分类
        for (Map.Entry<Long, Double> entry : analysis.getSharedCategories().entrySet()) {
            Long categoryId = entry.getKey();
            Double compatibilityScore = entry.getValue();
            
            CoupleRecommendationVO vo = buildCoupleRecommendation(
                    categoryId, compatibilityScore, recommendedDifficulty, limit,
                    "你们都喜欢这个分类");
            if (vo != null && !vo.getContents().isEmpty()) {
                recommendations.add(vo);
            }
        }

        // 3.2 如果共同兴趣不足，添加中立分类（冲突解决）
        if (recommendations.size() < MIN_COUPLE_CATEGORIES) {
            List<CoupleRecommendationVO> neutralRecs = resolveConflicts(
                    analysis, recommendedDifficulty, limit, 
                    MIN_COUPLE_CATEGORIES - recommendations.size());
            recommendations.addAll(neutralRecs);
        }

        // 3.3 确保分类多样性
        recommendations = ensureCategoryDiversity(recommendations, recommendedDifficulty, limit);

        log.info("情侣推荐完成: resultCategories={}", recommendations.size());
        return recommendations;
    }

    /**
     * 分析双方共同兴趣
     *
     * @param profile1 用户1画像
     * @param profile2 用户2画像
     * @return 兴趣分析结果
     */
    private CoupleInterestAnalysis analyzeSharedInterests(UserProfile profile1, UserProfile profile2) {
        CoupleInterestAnalysis analysis = new CoupleInterestAnalysis();

        Map<Long, Double> cat1 = profile1 != null ? profile1.getFavoriteCategories() : null;
        Map<Long, Double> cat2 = profile2 != null ? profile2.getFavoriteCategories() : null;

        // 如果任一方没有偏好数据，返回空分析
        if (cat1 == null || cat1.isEmpty() || cat2 == null || cat2.isEmpty()) {
            log.debug("一方或双方无偏好数据，使用热门分类");
            return analysis;
        }

        // 收集所有分类
        Set<Long> allCategories = new HashSet<>();
        allCategories.addAll(cat1.keySet());
        allCategories.addAll(cat2.keySet());

        for (Long categoryId : allCategories) {
            Double weight1 = cat1.getOrDefault(categoryId, 0.0);
            Double weight2 = cat2.getOrDefault(categoryId, 0.0);

            // 共同兴趣：双方权重都 > 0.3
            if (weight1 > 0.3 && weight2 > 0.3) {
                // 兼容度分数 = 两者权重的调和平均
                double compatibilityScore = 2 * weight1 * weight2 / (weight1 + weight2);
                analysis.getSharedCategories().put(categoryId, compatibilityScore);
            }
            // 冲突分类：一方喜欢(>0.5)，另一方不喜欢(<0.2)
            else if ((weight1 > 0.5 && weight2 < 0.2) || (weight2 > 0.5 && weight1 < 0.2)) {
                analysis.getConflictCategories().add(categoryId);
            }
            // 中立分类：双方都没有强烈偏好
            else if (weight1 >= 0.2 && weight1 <= 0.5 && weight2 >= 0.2 && weight2 <= 0.5) {
                double neutralScore = (weight1 + weight2) / 2;
                analysis.getNeutralCategories().put(categoryId, neutralScore);
            }
            // 单方兴趣
            else {
                analysis.getSingleInterestCategories().put(categoryId, Math.max(weight1, weight2));
            }
        }

        // 按兼容度排序共同兴趣
        analysis.setSharedCategories(
                analysis.getSharedCategories().entrySet().stream()
                        .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (e1, e2) -> e1,
                                LinkedHashMap::new
                        ))
        );

        return analysis;
    }

    /**
     * 根据关系时长计算推荐难度
     * 新情侣推荐简单活动，老情侣推荐更有挑战性的活动
     *
     * @param relationship 情侣关系
     * @return 推荐难度等级 (1-简单, 2-中等, 3-困难)
     */
    private int calculateRecommendedDifficulty(CoupleRelationship relationship) {
        if (relationship == null || relationship.getConfirmedAt() == null) {
            return 1; // 默认简单
        }

        long daysTogether = ChronoUnit.DAYS.between(
                relationship.getConfirmedAt().toLocalDate(),
                LocalDateTime.now().toLocalDate()
        );

        if (daysTogether < NEW_COUPLE_DAYS) {
            return 1; // 新情侣：简单活动
        } else if (daysTogether < MID_COUPLE_DAYS) {
            return 2; // 中期情侣：中等难度
        } else {
            return 3; // 长期情侣：可以挑战困难活动
        }
    }

    /**
     * 构建单个分类的情侣推荐
     *
     * @param categoryId 分类ID
     * @param compatibilityScore 兼容度分数
     * @param recommendedDifficulty 推荐难度
     * @param limit 内容数量限制
     * @param matchReason 匹配原因
     * @return 情侣推荐VO
     */
    private CoupleRecommendationVO buildCoupleRecommendation(Long categoryId, Double compatibilityScore,
                                                              int recommendedDifficulty, int limit,
                                                              String matchReason) {
        try {
            WheelCategory category = wheelCategoryMapper.selectById(categoryId);
            if (category == null || !Boolean.TRUE.equals(category.getStatus())) {
                return null;
            }

            // 获取该分类下的内容，优先匹配难度
            List<WheelContent> contents = wheelContentMapper.selectByCategoryIdAndAuditStatus(categoryId, 1);
            if (contents == null || contents.isEmpty()) {
                return null;
            }

            // 按难度和热度排序
            List<RecommendationVO> contentVOs = contents.stream()
                    .filter(c -> c.getStatus() == 1) // 只要启用的内容
                    .sorted((c1, c2) -> {
                        // 优先匹配推荐难度
                        int diff1 = Math.abs((c1.getDifficultyLevel() != null ? c1.getDifficultyLevel() : 1) - recommendedDifficulty);
                        int diff2 = Math.abs((c2.getDifficultyLevel() != null ? c2.getDifficultyLevel() : 1) - recommendedDifficulty);
                        if (diff1 != diff2) {
                            return Integer.compare(diff1, diff2);
                        }
                        // 其次按热度排序
                        double pop1 = c1.getPopularityScore() != null ? c1.getPopularityScore() : 0.0;
                        double pop2 = c2.getPopularityScore() != null ? c2.getPopularityScore() : 0.0;
                        return Double.compare(pop2, pop1);
                    })
                    .limit(limit)
                    .map(content -> {
                        RecommendationVO vo = new RecommendationVO();
                        vo.setContentId(content.getId());
                        vo.setContentText(content.getContentText());
                        vo.setCategoryId(content.getCategoryId());
                        vo.setCategoryName(category.getCategoryName());
                        vo.setScore(compatibilityScore * (content.getPopularityScore() != null ? content.getPopularityScore() : 0.5));
                        vo.setReason(generateCoupleRecommendationReason(category.getCategoryName(), matchReason));
                        return vo;
                    })
                    .collect(Collectors.toList());

            if (contentVOs.isEmpty()) {
                return null;
            }

            CoupleRecommendationVO vo = new CoupleRecommendationVO();
            vo.setCategoryId(categoryId);
            vo.setCategoryName(category.getCategoryName());
            vo.setContents(contentVOs);
            vo.setMatchReason(matchReason);
            vo.setCompatibilityScore(compatibilityScore);

            return vo;

        } catch (Exception e) {
            log.error("构建情侣推荐失败: categoryId={}, error={}", categoryId, e.getMessage());
            return null;
        }
    }

    /**
     * 解决偏好冲突
     * 当双方偏好冲突时，推荐中立分类的内容
     *
     * @param analysis 兴趣分析结果
     * @param recommendedDifficulty 推荐难度
     * @param limit 每个分类的内容数量限制
     * @param needed 需要的分类数量
     * @return 中立分类的推荐列表
     */
    private List<CoupleRecommendationVO> resolveConflicts(CoupleInterestAnalysis analysis,
                                                           int recommendedDifficulty, int limit, int needed) {
        List<CoupleRecommendationVO> results = new ArrayList<>();

        // 优先使用中立分类
        for (Map.Entry<Long, Double> entry : analysis.getNeutralCategories().entrySet()) {
            if (results.size() >= needed) {
                break;
            }
            CoupleRecommendationVO vo = buildCoupleRecommendation(
                    entry.getKey(), entry.getValue(), recommendedDifficulty, limit,
                    "这是你们都能接受的选择");
            if (vo != null && !vo.getContents().isEmpty()) {
                results.add(vo);
            }
        }

        // 如果中立分类不够，使用单方兴趣分类（让双方尝试对方的爱好）
        if (results.size() < needed) {
            for (Map.Entry<Long, Double> entry : analysis.getSingleInterestCategories().entrySet()) {
                if (results.size() >= needed) {
                    break;
                }
                CoupleRecommendationVO vo = buildCoupleRecommendation(
                        entry.getKey(), entry.getValue() * 0.7, recommendedDifficulty, limit,
                        "尝试一下对方喜欢的活动");
                if (vo != null && !vo.getContents().isEmpty()) {
                    results.add(vo);
                }
            }
        }

        return results;
    }

    /**
     * 确保分类多样性
     * 保证推荐结果至少包含指定数量的不同分类
     *
     * @param recommendations 当前推荐列表
     * @param recommendedDifficulty 推荐难度
     * @param limit 每个分类的内容数量限制
     * @return 补充后的推荐列表
     */
    private List<CoupleRecommendationVO> ensureCategoryDiversity(List<CoupleRecommendationVO> recommendations,
                                                                   int recommendedDifficulty, int limit) {
        if (recommendations.size() >= MIN_COUPLE_CATEGORIES) {
            return recommendations;
        }

        // 获取已有的分类ID
        Set<Long> existingCategoryIds = recommendations.stream()
                .map(CoupleRecommendationVO::getCategoryId)
                .collect(Collectors.toSet());

        // 获取所有启用的分类
        List<WheelCategory> allCategories = wheelCategoryMapper.selectEnabledCategoriesOrderBySort();
        if (allCategories == null || allCategories.isEmpty()) {
            return recommendations;
        }

        List<CoupleRecommendationVO> result = new ArrayList<>(recommendations);

        // 补充热门分类
        for (WheelCategory category : allCategories) {
            if (result.size() >= MIN_COUPLE_CATEGORIES) {
                break;
            }
            if (!existingCategoryIds.contains(category.getId())) {
                CoupleRecommendationVO vo = buildCoupleRecommendation(
                        category.getId(), 0.5, recommendedDifficulty, limit,
                        "热门情侣活动推荐");
                if (vo != null && !vo.getContents().isEmpty()) {
                    result.add(vo);
                    existingCategoryIds.add(category.getId());
                }
            }
        }

        return result;
    }

    /**
     * 生成情侣推荐原因
     */
    private String generateCoupleRecommendationReason(String categoryName, String baseReason) {
        if (baseReason != null && !baseReason.isEmpty()) {
            return baseReason + " - 「" + categoryName + "」";
        }
        return "情侣精选「" + categoryName + "」";
    }

    /**
     * 情侣兴趣分析结果
     */
    @Data
    public static class CoupleInterestAnalysis {
        /**
         * 共同兴趣分类及兼容度分数
         */
        private Map<Long, Double> sharedCategories = new LinkedHashMap<>();

        /**
         * 冲突分类（一方喜欢，另一方不喜欢）
         */
        private Set<Long> conflictCategories = new HashSet<>();

        /**
         * 中立分类（双方都没有强烈偏好）
         */
        private Map<Long, Double> neutralCategories = new LinkedHashMap<>();

        /**
         * 单方兴趣分类
         */
        private Map<Long, Double> singleInterestCategories = new LinkedHashMap<>();
    }
}
