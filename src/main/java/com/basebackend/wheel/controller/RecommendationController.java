package com.basebackend.wheel.controller;

import com.basebackend.common.context.UserContextHolder;
import com.basebackend.common.model.PageResult;
import com.basebackend.common.model.Result;
import com.basebackend.wheel.dto.CoupleRecommendationVO;
import com.basebackend.wheel.dto.HomepageRecommendationVO;
import com.basebackend.wheel.dto.PreferenceSurveyDTO;
import com.basebackend.wheel.dto.RecommendationFeedbackDTO;
import com.basebackend.wheel.dto.RecommendationVO;
import com.basebackend.wheel.engine.RecommendationEngine;
import com.basebackend.wheel.service.ColdStartService;
import com.basebackend.wheel.service.HomepageRecommendationService;
import com.basebackend.wheel.service.RecommendationFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Objects;

/**
 * 推荐控制器
 * 提供个性化推荐、情侣推荐、首页推荐等API端点
 *
 * @author wheel-api
 * @since 2025-01-31
 */
@Slf4j
@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "推荐管理", description = "AI推荐相关接口")
@RequiredArgsConstructor
@Validated
public class RecommendationController {

    private final RecommendationEngine recommendationEngine;
    private final HomepageRecommendationService homepageRecommendationService;
    private final RecommendationFeedbackService recommendationFeedbackService;
    private final ColdStartService coldStartService;

    /**
     * 默认分页大小
     */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大分页大小
     */
    private static final int MAX_PAGE_SIZE = 50;


    /**
     * 获取个性化推荐
     * 根据用户画像和行为数据，返回个性化的内容推荐列表
     *
     * @param pageNum 页码，默认1
     * @param pageSize 每页数量，默认10，最大50
     * @return 分页的推荐结果
     */
    @Operation(summary = "获取个性化推荐", description = "根据用户画像返回个性化内容推荐，支持分页")
    @GetMapping("/personal")
    public Result<PageResult<RecommendationVO>> getPersonalRecommendations(
            @Parameter(description = "页码，从1开始")
            @RequestParam(defaultValue = "1") @Min(1) Integer pageNum,
            @Parameter(description = "每页数量，默认10，最大50")
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) Integer pageSize) {
        try {
            Long userId = UserContextHolder.getUserId();
            if (Objects.isNull(userId)) {
                return Result.error(401, "未登录，请先登录");
            }

            // 限制pageSize最大值
            int actualPageSize = Math.min(pageSize, MAX_PAGE_SIZE);
            
            // 获取推荐列表
            List<RecommendationVO> recommendations = recommendationEngine.getPersonalRecommendations(
                    userId, actualPageSize * pageNum);
            
            // 计算分页
            int total = recommendations.size();
            int fromIndex = (pageNum - 1) * actualPageSize;
            int toIndex = Math.min(fromIndex + actualPageSize, total);
            
            List<RecommendationVO> pageData;
            if (fromIndex >= total) {
                pageData = List.of();
            } else {
                pageData = recommendations.subList(fromIndex, toIndex);
            }
            
            PageResult<RecommendationVO> pageResult = PageResult.of(pageData, (long) total, pageNum, actualPageSize);
            
            log.info("获取个性化推荐成功: userId={}, pageNum={}, pageSize={}, resultCount={}",
                    userId, pageNum, actualPageSize, pageData.size());
            return Result.success(pageResult);
            
        } catch (Exception e) {
            log.error("获取个性化推荐失败: error={}", e.getMessage(), e);
            return Result.error(500, "获取推荐失败: " + e.getMessage());
        }
    }

    /**
     * 获取情侣推荐
     * 分析双方画像，返回适合情侣共同参与的活动推荐
     *
     * @return 情侣推荐列表，按分类组织
     */
    @Operation(summary = "获取情侣推荐", description = "分析双方画像，返回情侣活动推荐")
    @GetMapping("/couple")
    public Result<List<CoupleRecommendationVO>> getCoupleRecommendations() {
        try {
            Long userId = UserContextHolder.getUserId();
            if (Objects.isNull(userId)) {
                return Result.error(401, "未登录，请先登录");
            }

            List<CoupleRecommendationVO> recommendations = recommendationEngine.getCoupleRecommendations(
                    userId, DEFAULT_PAGE_SIZE);
            
            if (recommendations.isEmpty()) {
                log.info("用户无情侣关系或无推荐结果: userId={}", userId);
            }
            
            log.info("获取情侣推荐成功: userId={}, categoryCount={}", userId, recommendations.size());
            return Result.success(recommendations);
            
        } catch (Exception e) {
            log.error("获取情侣推荐失败: error={}", e.getMessage(), e);
            return Result.error(500, "获取情侣推荐失败: " + e.getMessage());
        }
    }

    /**
     * 获取首页推荐数据
     * 包含个性化分类排名、为你推荐、情侣精选等板块
     *
     * @return 首页推荐数据
     */
    @Operation(summary = "获取首页推荐", description = "获取首页个性化推荐数据，包含分类排名和推荐内容")
    @GetMapping("/homepage")
    public Result<HomepageRecommendationVO> getHomepageRecommendations() {
        try {
            Long userId = UserContextHolder.getUserId();
            if (Objects.isNull(userId)) {
                return Result.error(401, "未登录，请先登录");
            }

            HomepageRecommendationVO homepage = homepageRecommendationService.getHomepageRecommendations(userId);
            
            log.info("获取首页推荐成功: userId={}", userId);
            return Result.success(homepage);
            
        } catch (Exception e) {
            log.error("获取首页推荐失败: error={}", e.getMessage(), e);
            return Result.error(500, "获取首页推荐失败: " + e.getMessage());
        }
    }

    /**
     * 记录推荐反馈
     * 记录用户对推荐内容的点击、忽略等行为，用于优化推荐质量
     *
     * @param feedbackDTO 反馈数据
     * @return 操作结果
     */
    @Operation(summary = "记录推荐反馈", description = "记录用户对推荐的点击、忽略等反馈")
    @PostMapping("/feedback")
    public Result<Void> recordFeedback(@Valid @RequestBody RecommendationFeedbackDTO feedbackDTO) {
        try {
            Long userId = UserContextHolder.getUserId();
            if (Objects.isNull(userId)) {
                return Result.error(401, "未登录，请先登录");
            }

            // 验证反馈类型
            String feedbackType = feedbackDTO.getFeedbackType();
            if (!isValidFeedbackType(feedbackType)) {
                return Result.error(400, "无效的反馈类型，支持: CLICK, IGNORE, LIKE, DISLIKE");
            }

            recommendationFeedbackService.recordFeedback(userId, feedbackDTO);
            
            log.info("记录推荐反馈成功: userId={}, contentId={}, feedbackType={}",
                    userId, feedbackDTO.getContentId(), feedbackType);
            return Result.success();
            
        } catch (Exception e) {
            log.error("记录推荐反馈失败: error={}", e.getMessage(), e);
            return Result.error(500, "记录反馈失败: " + e.getMessage());
        }
    }

    /**
     * 提交偏好调查
     * 新用户可以通过偏好调查快速建立初始画像
     *
     * @param surveyDTO 调查数据
     * @return 操作结果
     */
    @Operation(summary = "提交偏好调查", description = "新用户提交偏好调查，用于冷启动推荐")
    @PostMapping("/survey")
    public Result<Void> submitPreferenceSurvey(@Valid @RequestBody PreferenceSurveyDTO surveyDTO) {
        try {
            Long userId = UserContextHolder.getUserId();
            if (Objects.isNull(userId)) {
                return Result.error(401, "未登录，请先登录");
            }

            // 验证调查数据
            if (!surveyDTO.isSkipped() && 
                (surveyDTO.getSelectedCategoryIds() == null || surveyDTO.getSelectedCategoryIds().isEmpty())) {
                return Result.error(400, "请至少选择一个分类，或选择跳过调查");
            }

            coldStartService.handlePreferenceSurvey(userId, surveyDTO);
            
            log.info("提交偏好调查成功: userId={}, skipped={}, categoryCount={}",
                    userId, surveyDTO.isSkipped(),
                    surveyDTO.getSelectedCategoryIds() != null ? surveyDTO.getSelectedCategoryIds().size() : 0);
            return Result.success();
            
        } catch (Exception e) {
            log.error("提交偏好调查失败: error={}", e.getMessage(), e);
            return Result.error(500, "提交调查失败: " + e.getMessage());
        }
    }

    /**
     * 刷新首页推荐
     * 强制刷新首页推荐缓存，获取最新推荐数据
     *
     * @return 刷新后的首页推荐数据
     */
    @Operation(summary = "刷新首页推荐", description = "强制刷新首页推荐缓存")
    @PostMapping("/homepage/refresh")
    public Result<HomepageRecommendationVO> refreshHomepageRecommendations() {
        try {
            Long userId = UserContextHolder.getUserId();
            if (Objects.isNull(userId)) {
                return Result.error(401, "未登录，请先登录");
            }

            HomepageRecommendationVO homepage = homepageRecommendationService.refreshHomepageRecommendations(userId);
            
            log.info("刷新首页推荐成功: userId={}", userId);
            return Result.success(homepage);
            
        } catch (Exception e) {
            log.error("刷新首页推荐失败: error={}", e.getMessage(), e);
            return Result.error(500, "刷新推荐失败: " + e.getMessage());
        }
    }

    /**
     * 检查是否需要偏好调查
     * 用于前端判断是否显示偏好调查弹窗
     *
     * @return 是否需要调查
     */
    @Operation(summary = "检查是否需要偏好调查", description = "检查用户是否需要完成偏好调查")
    @GetMapping("/survey/check")
    public Result<Boolean> checkSurveyNeeded() {
        try {
            Long userId = UserContextHolder.getUserId();
            if (Objects.isNull(userId)) {
                return Result.error(401, "未登录，请先登录");
            }

            // 检查是否处于冷启动阶段且未完成调查
            boolean inColdStart = coldStartService.isInColdStartPhase(userId);
            boolean hasCompletedSurvey = coldStartService.hasCompletedSurvey(userId);
            boolean needsSurvey = inColdStart && !hasCompletedSurvey;
            
            log.debug("检查偏好调查需求: userId={}, inColdStart={}, hasCompletedSurvey={}, needsSurvey={}",
                    userId, inColdStart, hasCompletedSurvey, needsSurvey);
            return Result.success(needsSurvey);
            
        } catch (Exception e) {
            log.error("检查偏好调查需求失败: error={}", e.getMessage(), e);
            return Result.error(500, "检查失败: " + e.getMessage());
        }
    }

    /**
     * 验证反馈类型是否有效
     */
    private boolean isValidFeedbackType(String feedbackType) {
        if (feedbackType == null) {
            return false;
        }
        return feedbackType.equals("CLICK") || 
               feedbackType.equals("IGNORE") || 
               feedbackType.equals("LIKE") || 
               feedbackType.equals("DISLIKE");
    }
}
