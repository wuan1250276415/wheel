package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.dto.PreferenceSurveyDTO;
import com.basebackend.wheel.dto.RecommendationVO;
import com.basebackend.wheel.entity.UserPreferenceSurvey;
import com.basebackend.wheel.entity.UserProfile;
import com.basebackend.wheel.entity.WheelCategory;
import com.basebackend.wheel.entity.WheelContent;
import com.basebackend.wheel.entity.WheelUser;
import com.basebackend.wheel.mapper.UserPreferenceSurveyMapper;
import com.basebackend.wheel.mapper.UserProfileMapper;
import com.basebackend.wheel.mapper.WheelCategoryMapper;
import com.basebackend.wheel.mapper.WheelUserMapper;
import com.basebackend.wheel.service.ColdStartService;
import com.basebackend.wheel.service.HotContentCacheService;
import com.basebackend.wheel.util.AuditHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 冷启动服务实现类
 * 负责处理新用户的推荐策略
 *
 * @author wheel-api
 * @since 2025-01-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ColdStartServiceImpl implements ColdStartService {

    private final UserPreferenceSurveyMapper userPreferenceSurveyMapper;
    private final UserProfileMapper userProfileMapper;
    private final WheelUserMapper wheelUserMapper;
    private final WheelCategoryMapper wheelCategoryMapper;
    private final HotContentCacheService hotContentCacheService;

    @Override
    @Transactional
    public void handlePreferenceSurvey(Long userId, PreferenceSurveyDTO surveyDTO) {
        if (userId == null || surveyDTO == null) {
            log.warn("处理偏好调查失败：参数为空");
            return;
        }

        log.info("处理用户偏好调查: userId={}, skipped={}, categories={}",
                userId, surveyDTO.isSkipped(),
                surveyDTO.getSelectedCategoryIds() != null ? surveyDTO.getSelectedCategoryIds().size() : 0);

        // 检查是否已存在调查记录
        UserPreferenceSurvey existingSurvey = userPreferenceSurveyMapper.selectByUserId(userId);

        if (existingSurvey != null) {
            // 更新现有记录
            existingSurvey.setSelectedCategories(surveyDTO.getSelectedCategoryIds());
            existingSurvey.setSkipped(surveyDTO.isSkipped());
            AuditHelper.setUpdateAuditFields(existingSurvey, userId);
            userPreferenceSurveyMapper.updateById(existingSurvey);
            log.info("更新用户偏好调查: userId={}", userId);
        } else {
            // 创建新记录
            UserPreferenceSurvey survey = new UserPreferenceSurvey();
            survey.setUserId(userId);
            survey.setSelectedCategories(surveyDTO.getSelectedCategoryIds());
            survey.setSkipped(surveyDTO.isSkipped());
            AuditHelper.setCreateAuditFields(survey, userId);
            userPreferenceSurveyMapper.insert(survey);
            log.info("创建用户偏好调查: userId={}", userId);
        }

        // 如果用户选择了分类，初始化用户画像
        if (!surveyDTO.isSkipped() && surveyDTO.getSelectedCategoryIds() != null
                && !surveyDTO.getSelectedCategoryIds().isEmpty()) {
            initializeUserProfileFromSurvey(userId, surveyDTO.getSelectedCategoryIds());
        }
    }

    @Override
    public UserPreferenceSurvey getPreferenceSurvey(Long userId) {
        if (userId == null) {
            return null;
        }
        return userPreferenceSurveyMapper.selectByUserId(userId);
    }

    @Override
    public boolean hasCompletedSurvey(Long userId) {
        if (userId == null) {
            return false;
        }
        return userPreferenceSurveyMapper.existsByUserId(userId);
    }

    @Override
    public List<RecommendationVO> getColdStartRecommendations(Long userId, int limit) {
        if (userId == null) {
            log.warn("获取冷启动推荐失败：用户ID为空");
            return getPopularContentRecommendations(limit);
        }

        log.info("获取冷启动推荐: userId={}, limit={}", userId, limit);

        try {
            // 1. 检查用户是否完成了偏好调查
            UserPreferenceSurvey survey = getPreferenceSurvey(userId);

            if (survey != null) {
                if (Boolean.TRUE.equals(survey.getSkipped())) {
                    // 用户跳过了调查，使用热门内容 + 人口统计相似度
                    log.debug("用户跳过调查，使用热门内容推荐: userId={}", userId);
                    return getCombinedRecommendations(userId, limit);
                } else if (survey.getSelectedCategories() != null && !survey.getSelectedCategories().isEmpty()) {
                    // 用户选择了分类，基于选择的分类推荐
                    log.debug("基于用户选择的分类推荐: userId={}, categories={}",
                            userId, survey.getSelectedCategories().size());
                    return getSurveyBasedRecommendations(survey.getSelectedCategories(), limit);
                }
            }

            // 2. 尝试使用人口统计相似度推荐
            List<RecommendationVO> demographicRecs = getDemographicBasedRecommendations(userId, limit);
            if (!demographicRecs.isEmpty()) {
                log.debug("使用人口统计相似度推荐: userId={}, count={}", userId, demographicRecs.size());
                return demographicRecs;
            }

            // 3. 降级到热门内容推荐
            log.debug("降级到热门内容推荐: userId={}", userId);
            return getPopularContentRecommendations(limit);

        } catch (Exception e) {
            log.error("获取冷启动推荐失败: userId={}, error={}", userId, e.getMessage(), e);
            return getPopularContentRecommendations(limit);
        }
    }

    @Override
    public boolean isInColdStartPhase(Long userId) {
        if (userId == null) {
            return true;
        }

        try {
            UserProfile profile = userProfileMapper.selectByUserId(userId);
            if (profile == null) {
                return true;
            }

            Integer totalSpins = profile.getTotalSpins();
            return totalSpins == null || totalSpins < COLD_START_SPIN_THRESHOLD;

        } catch (Exception e) {
            log.error("检查冷启动阶段失败: userId={}, error={}", userId, e.getMessage());
            return true;
        }
    }

    @Override
    public boolean shouldTransitionToPersonalized(Long userId) {
        if (userId == null) {
            return false;
        }

        try {
            UserProfile profile = userProfileMapper.selectByUserId(userId);
            if (profile == null) {
                return false;
            }

            Integer totalSpins = profile.getTotalSpins();
            return totalSpins != null && totalSpins >= COLD_START_SPIN_THRESHOLD;

        } catch (Exception e) {
            log.error("检查是否应过渡到个性化推荐失败: userId={}, error={}", userId, e.getMessage());
            return false;
        }
    }

    @Override
    public List<RecommendationVO> getDemographicBasedRecommendations(Long userId, int limit) {
        if (userId == null) {
            return Collections.emptyList();
        }

        try {
            // 1. 获取当前用户的人口统计信息
            WheelUser currentUser = wheelUserMapper.selectById(userId);
            if (currentUser == null) {
                log.debug("用户不存在: userId={}", userId);
                return Collections.emptyList();
            }

            // 2. 查找人口统计相似的用户
            List<Long> similarUserIds = findDemographicallySimilarUsers(currentUser, 20);
            if (similarUserIds.isEmpty()) {
                log.debug("未找到人口统计相似用户: userId={}", userId);
                return Collections.emptyList();
            }

            // 3. 获取相似用户的偏好分类
            Map<Long, Double> categoryScores = aggregateSimilarUserPreferences(similarUserIds);
            if (categoryScores.isEmpty()) {
                return Collections.emptyList();
            }

            // 4. 基于聚合的偏好获取推荐
            return getRecommendationsFromCategoryScores(categoryScores, limit, "相似用户都在玩");

        } catch (Exception e) {
            log.error("获取人口统计推荐失败: userId={}, error={}", userId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<RecommendationVO> getPopularContentRecommendations(int limit) {
        try {
            List<WheelContent> hotContents = hotContentCacheService.getAllHotContent(limit);
            return convertToRecommendationVOs(hotContents, "热门推荐");
        } catch (Exception e) {
            log.error("获取热门内容推荐失败: error={}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 基于调查选择的分类获取推荐
     */
    private List<RecommendationVO> getSurveyBasedRecommendations(List<Long> categoryIds, int limit) {
        List<RecommendationVO> recommendations = new ArrayList<>();

        // 每个分类获取一定数量的内容
        int perCategoryLimit = Math.max(2, limit / categoryIds.size());

        for (Long categoryId : categoryIds) {
            List<WheelContent> contents = hotContentCacheService.getHotContentByCategory(categoryId, perCategoryLimit);
            String reason = generateCategoryReason(categoryId);

            for (WheelContent content : contents) {
                RecommendationVO vo = convertToRecommendationVO(content, reason);
                recommendations.add(vo);
            }
        }

        // 按热度分数排序
        recommendations.sort((r1, r2) -> Double.compare(
                r2.getScore() != null ? r2.getScore() : 0,
                r1.getScore() != null ? r1.getScore() : 0));

        return recommendations.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * 获取组合推荐（热门 + 人口统计）
     */
    private List<RecommendationVO> getCombinedRecommendations(Long userId, int limit) {
        List<RecommendationVO> combined = new ArrayList<>();

        // 50% 来自人口统计相似度
        int demographicLimit = limit / 2;
        List<RecommendationVO> demographicRecs = getDemographicBasedRecommendations(userId, demographicLimit);
        combined.addAll(demographicRecs);

        // 50% 来自热门内容
        int popularLimit = limit - combined.size();
        List<RecommendationVO> popularRecs = getPopularContentRecommendations(popularLimit);

        // 去重添加热门内容
        Set<Long> existingIds = combined.stream()
                .map(RecommendationVO::getContentId)
                .collect(Collectors.toSet());

        for (RecommendationVO rec : popularRecs) {
            if (!existingIds.contains(rec.getContentId())) {
                combined.add(rec);
                existingIds.add(rec.getContentId());
            }
        }

        return combined.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * 初始化用户画像（基于调查结果）
     */
    private void initializeUserProfileFromSurvey(Long userId, List<Long> categoryIds) {
        try {
            UserProfile profile = userProfileMapper.selectByUserId(userId);

            if (profile == null) {
                profile = new UserProfile();
                profile.setUserId(userId);
                profile.setTotalSpins(0);
                profile.setIsStale(false);
                profile.setBehaviorTags(new ArrayList<>());
                profile.setActiveTimeSlots(new ArrayList<>());
                AuditHelper.setCreateAuditFields(profile, userId);
            }

            // 基于调查结果初始化偏好分类
            Map<Long, Double> favoriteCategories = new HashMap<>();
            double weight = 1.0 / categoryIds.size(); // 平均分配权重
            for (Long categoryId : categoryIds) {
                favoriteCategories.put(categoryId, weight);
            }
            profile.setFavoriteCategories(favoriteCategories);

            if (profile.getId() == null) {
                userProfileMapper.insert(profile);
            } else {
                AuditHelper.setUpdateAuditFields(profile, userId);
                userProfileMapper.updateById(profile);
            }

            log.info("基于调查初始化用户画像: userId={}, categories={}", userId, categoryIds.size());

        } catch (Exception e) {
            log.error("初始化用户画像失败: userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    /**
     * 查找人口统计相似的用户
     */
    private List<Long> findDemographicallySimilarUsers(WheelUser currentUser, int limit) {
        List<Long> similarUserIds = new ArrayList<>();

        try {
            // 基于性别、年龄段、城市查找相似用户
            Integer gender = currentUser.getGender();
            Integer age = currentUser.getAge();
            String city = currentUser.getCity();

            // 计算年龄段（±5岁）
            Integer minAge = age != null ? Math.max(0, age - 5) : null;
            Integer maxAge = age != null ? age + 5 : null;

            // 查询相似用户的画像
            List<UserProfile> similarProfiles = userProfileMapper.selectByDemographics(
                    gender, minAge, maxAge, city, currentUser.getId(), limit);

            if (similarProfiles != null) {
                similarUserIds = similarProfiles.stream()
                        .map(UserProfile::getUserId)
                        .collect(Collectors.toList());
            }

        } catch (Exception e) {
            log.error("查找人口统计相似用户失败: userId={}, error={}",
                    currentUser.getId(), e.getMessage(), e);
        }

        return similarUserIds;
    }

    /**
     * 聚合相似用户的偏好
     */
    private Map<Long, Double> aggregateSimilarUserPreferences(List<Long> userIds) {
        Map<Long, Double> categoryScores = new HashMap<>();

        try {
            for (Long userId : userIds) {
                UserProfile profile = userProfileMapper.selectByUserId(userId);
                if (profile != null && profile.getFavoriteCategories() != null) {
                    for (Map.Entry<Long, Double> entry : profile.getFavoriteCategories().entrySet()) {
                        categoryScores.merge(entry.getKey(), entry.getValue(), Double::sum);
                    }
                }
            }

            // 归一化分数
            if (!categoryScores.isEmpty()) {
                double maxScore = categoryScores.values().stream()
                        .mapToDouble(Double::doubleValue)
                        .max()
                        .orElse(1.0);

                if (maxScore > 0) {
                    categoryScores.replaceAll((k, v) -> v / maxScore);
                }
            }

        } catch (Exception e) {
            log.error("聚合相似用户偏好失败: error={}", e.getMessage(), e);
        }

        return categoryScores;
    }

    /**
     * 基于分类分数获取推荐
     */
    private List<RecommendationVO> getRecommendationsFromCategoryScores(
            Map<Long, Double> categoryScores, int limit, String reasonPrefix) {

        List<RecommendationVO> recommendations = new ArrayList<>();

        // 按分数排序分类
        List<Map.Entry<Long, Double>> sortedCategories = categoryScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(5) // 取前5个分类
                .collect(Collectors.toList());

        int perCategoryLimit = Math.max(2, limit / sortedCategories.size());

        for (Map.Entry<Long, Double> entry : sortedCategories) {
            Long categoryId = entry.getKey();
            Double score = entry.getValue();

            List<WheelContent> contents = hotContentCacheService.getHotContentByCategory(categoryId, perCategoryLimit);
            String reason = reasonPrefix;

            for (WheelContent content : contents) {
                RecommendationVO vo = convertToRecommendationVO(content, reason);
                // 调整分数
                vo.setScore(score * (content.getPopularityScore() != null ? content.getPopularityScore() : 0.5));
                recommendations.add(vo);
            }
        }

        // 按分数排序
        recommendations.sort((r1, r2) -> Double.compare(
                r2.getScore() != null ? r2.getScore() : 0,
                r1.getScore() != null ? r1.getScore() : 0));

        return recommendations.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * 生成分类推荐原因
     */
    private String generateCategoryReason(Long categoryId) {
        try {
            WheelCategory category = wheelCategoryMapper.selectById(categoryId);
            if (category != null) {
                return "基于你对「" + category.getCategoryName() + "」的兴趣";
            }
        } catch (Exception e) {
            log.warn("获取分类名称失败: categoryId={}", categoryId);
        }
        return "为你推荐";
    }

    /**
     * 转换内容列表为推荐VO列表
     */
    private List<RecommendationVO> convertToRecommendationVOs(List<WheelContent> contents, String reason) {
        return contents.stream()
                .map(content -> convertToRecommendationVO(content, reason))
                .collect(Collectors.toList());
    }

    /**
     * 转换单个内容为推荐VO
     */
    private RecommendationVO convertToRecommendationVO(WheelContent content, String reason) {
        RecommendationVO vo = new RecommendationVO();
        vo.setContentId(content.getId());
        vo.setContentText(content.getContentText());
        vo.setCategoryId(content.getCategoryId());
        vo.setScore(content.getPopularityScore() != null ? content.getPopularityScore() : 0.5);
        vo.setReason(reason);

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
}
