package com.basebackend.wheel.service;

import com.basebackend.wheel.dto.PreferenceSurveyDTO;
import com.basebackend.wheel.dto.RecommendationVO;
import com.basebackend.wheel.entity.UserPreferenceSurvey;

import java.util.List;

/**
 * 冷启动服务接口
 * 负责处理新用户的推荐策略，包括偏好调查、热门内容推荐和人口统计相似度匹配
 *
 * @author wheel-api
 * @since 2025-01-31
 */
public interface ColdStartService {

    /**
     * 处理用户偏好调查
     * 保存用户选择的分类偏好或跳过状态
     *
     * @param userId 用户ID
     * @param surveyDTO 偏好调查数据
     */
    void handlePreferenceSurvey(Long userId, PreferenceSurveyDTO surveyDTO);

    /**
     * 获取用户的偏好调查结果
     *
     * @param userId 用户ID
     * @return 偏好调查结果，如果不存在返回null
     */
    UserPreferenceSurvey getPreferenceSurvey(Long userId);

    /**
     * 检查用户是否已完成偏好调查
     *
     * @param userId 用户ID
     * @return true表示已完成调查（包括跳过）
     */
    boolean hasCompletedSurvey(Long userId);

    /**
     * 获取冷启动推荐
     * 根据用户状态选择合适的推荐策略：
     * 1. 如果用户完成了偏好调查，基于选择的分类推荐
     * 2. 如果用户跳过了调查，推荐全局热门内容
     * 3. 如果有人口统计数据，使用相似用户的偏好
     *
     * @param userId 用户ID
     * @param limit 返回数量限制
     * @return 推荐内容列表
     */
    List<RecommendationVO> getColdStartRecommendations(Long userId, int limit);

    /**
     * 检查用户是否处于冷启动阶段
     * 用户转盘次数少于5次时处于冷启动阶段
     *
     * @param userId 用户ID
     * @return true表示用户处于冷启动阶段
     */
    boolean isInColdStartPhase(Long userId);

    /**
     * 检查用户是否应该过渡到个性化推荐
     * 当用户完成5次转盘后，应该从冷启动过渡到个性化推荐
     *
     * @param userId 用户ID
     * @return true表示应该过渡到个性化推荐
     */
    boolean shouldTransitionToPersonalized(Long userId);

    /**
     * 获取基于人口统计相似度的推荐
     * 根据用户的性别、年龄、城市等信息，找到相似用户群体的偏好
     *
     * @param userId 用户ID
     * @param limit 返回数量限制
     * @return 推荐内容列表
     */
    List<RecommendationVO> getDemographicBasedRecommendations(Long userId, int limit);

    /**
     * 获取全局热门内容推荐
     * 用于跳过调查的用户或无法获取其他推荐时的降级方案
     *
     * @param limit 返回数量限制
     * @return 推荐内容列表
     */
    List<RecommendationVO> getPopularContentRecommendations(int limit);

    /**
     * 冷启动转盘次数阈值
     */
    int COLD_START_SPIN_THRESHOLD = 5;
}
