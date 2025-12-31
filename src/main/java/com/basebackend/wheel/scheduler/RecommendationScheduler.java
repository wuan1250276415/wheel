package com.basebackend.wheel.scheduler;

import com.basebackend.wheel.entity.UserProfile;
import com.basebackend.wheel.mapper.RecommendationLogMapper;
import com.basebackend.wheel.mapper.UserProfileMapper;
import com.basebackend.wheel.service.ContentFeatureService;
import com.basebackend.wheel.service.HotContentCacheService;
import com.basebackend.wheel.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 推荐系统定时调度器
 * 负责定时执行推荐相关的后台任务
 *
 * @author wheel-api
 * @since 2025-01-31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationScheduler {

    private final HotContentCacheService hotContentCacheService;
    private final ContentFeatureService contentFeatureService;
    private final UserProfileService userProfileService;
    private final UserProfileMapper userProfileMapper;
    private final RecommendationLogMapper recommendationLogMapper;

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * 重试间隔（毫秒）
     */
    private static final long RETRY_DELAY_MS = 1000;

    /**
     * 用户画像批量更新每批数量
     */
    private static final int PROFILE_BATCH_SIZE = 100;

    /**
     * 推荐日志保留天数
     */
    private static final int LOG_RETENTION_DAYS = 90;

    /**
     * 每小时刷新热门内容缓存
     * Cron表达式: 0 0 * * * ? = 每小时整点执行
     * 
     * Requirements: 8.2
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void refreshHotContentCache() {
        log.info("开始执行热门内容缓存刷新任务");
        try {
            executeWithRetry(this::doRefreshHotContentCache, "热门内容缓存刷新");
            log.info("热门内容缓存刷新任务执行完成");
        } catch (Exception e) {
            log.error("热门内容缓存刷新任务执行失败（已重试{}次）", MAX_RETRY_ATTEMPTS, e);
        }
    }

    /**
     * 每天凌晨2:00-6:00批量更新用户画像
     * Cron表达式: 0 0 2 * * ? = 每天凌晨2:00执行
     * 
     * Requirements: 10.2
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void batchUpdateUserProfiles() {
        log.info("开始执行用户画像批量更新任务");
        try {
            executeWithRetry(this::doBatchUpdateUserProfiles, "用户画像批量更新");
            log.info("用户画像批量更新任务执行完成");
        } catch (Exception e) {
            log.error("用户画像批量更新任务执行失败（已重试{}次）", MAX_RETRY_ATTEMPTS, e);
        }
    }

    /**
     * 每天凌晨3:00重新计算内容热度分数
     * Cron表达式: 0 0 3 * * ? = 每天凌晨3:00执行
     * 
     * Requirements: 8.2
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void recalculatePopularityScores() {
        log.info("开始执行内容热度分数重新计算任务");
        try {
            executeWithRetry(this::doRecalculatePopularityScores, "内容热度分数重新计算");
            log.info("内容热度分数重新计算任务执行完成");
        } catch (Exception e) {
            log.error("内容热度分数重新计算任务执行失败（已重试{}次）", MAX_RETRY_ATTEMPTS, e);
        }
    }

    /**
     * 每天凌晨4:00清理90天前的推荐日志
     * Cron表达式: 0 0 4 * * ? = 每天凌晨4:00执行
     * 
     * Requirements: 7.5
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanupOldRecommendationLogs() {
        log.info("开始执行推荐日志清理任务");
        try {
            executeWithRetry(this::doCleanupOldRecommendationLogs, "推荐日志清理");
            log.info("推荐日志清理任务执行完成");
        } catch (Exception e) {
            log.error("推荐日志清理任务执行失败（已重试{}次）", MAX_RETRY_ATTEMPTS, e);
        }
    }

    // ==================== 任务执行方法 ====================

    /**
     * 执行热门内容缓存刷新
     */
    private void doRefreshHotContentCache() {
        log.info("刷新热门内容缓存...");
        hotContentCacheService.refreshCache();
        
        // 检查缓存命中率
        double hitRate = hotContentCacheService.getCacheHitRate();
        if (hitRate < 0.8) {
            log.warn("缓存命中率低于80%: hitRate={}", String.format("%.2f%%", hitRate * 100));
        }
    }

    /**
     * 执行用户画像批量更新
     */
    private void doBatchUpdateUserProfiles() {
        log.info("批量更新用户画像...");
        
        // 1. 计算过期阈值（7天前）
        LocalDateTime staleThreshold = LocalDateTime.now().minusDays(7);
        
        // 2. 先标记过期的画像
        int markedCount = userProfileMapper.batchMarkStale(staleThreshold);
        log.info("标记过期画像数量: {}", markedCount);
        
        // 3. 分批获取并重建过期画像
        int totalRebuilt = 0;
        int failedCount = 0;
        
        while (true) {
            List<UserProfile> staleProfiles = userProfileMapper.selectStaleProfiles(staleThreshold, PROFILE_BATCH_SIZE);
            if (staleProfiles == null || staleProfiles.isEmpty()) {
                break;
            }
            
            log.info("获取到{}个过期画像，开始重建...", staleProfiles.size());
            
            for (UserProfile profile : staleProfiles) {
                try {
                    userProfileService.rebuildProfile(profile.getUserId());
                    totalRebuilt++;
                } catch (Exception e) {
                    failedCount++;
                    log.error("重建用户画像失败: userId={}, error={}", profile.getUserId(), e.getMessage());
                }
            }
            
            // 如果获取的数量小于批次大小，说明已经处理完毕
            if (staleProfiles.size() < PROFILE_BATCH_SIZE) {
                break;
            }
            
            // 避免过度占用资源，每批次间隔100ms
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("用户画像批量更新被中断");
                break;
            }
        }
        
        log.info("用户画像批量更新完成: 成功={}, 失败={}", totalRebuilt, failedCount);
    }

    /**
     * 执行内容热度分数重新计算
     */
    private void doRecalculatePopularityScores() {
        log.info("重新计算内容热度分数...");
        contentFeatureService.batchUpdatePopularityScores();
    }

    /**
     * 执行推荐日志清理
     */
    private void doCleanupOldRecommendationLogs() {
        log.info("清理{}天前的推荐日志...", LOG_RETENTION_DAYS);
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(LOG_RETENTION_DAYS);
        int deletedCount = recommendationLogMapper.deleteOldLogs(cutoffTime);
        
        log.info("推荐日志清理完成: 删除记录数={}", deletedCount);
    }

    // ==================== 辅助方法 ====================

    /**
     * 带重试机制的任务执行方法
     *
     * @param task 任务执行逻辑
     * @param taskName 任务名称
     */
    private void executeWithRetry(Runnable task, String taskName) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                log.info("执行任务: {}, 尝试次数={}/{}", taskName, attempt, MAX_RETRY_ATTEMPTS);
                task.run();
                return; // 成功则直接返回
            } catch (Exception e) {
                lastException = e;
                log.warn("任务执行失败: {}, 尝试次数={}/{}, 错误={}", 
                        taskName, attempt, MAX_RETRY_ATTEMPTS, e.getMessage());
                
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试被中断", ie);
                    }
                }
            }
        }
        
        // 所有重试都失败，抛出最后一个异常
        throw new RuntimeException(taskName + "失败，已重试" + MAX_RETRY_ATTEMPTS + "次", lastException);
    }

    // ==================== 手动触发方法（供管理接口调用） ====================

    /**
     * 手动触发热门内容缓存刷新
     */
    public void triggerHotContentCacheRefresh() {
        log.info("手动触发热门内容缓存刷新");
        try {
            executeWithRetry(this::doRefreshHotContentCache, "热门内容缓存刷新");
            log.info("手动触发热门内容缓存刷新完成");
        } catch (Exception e) {
            log.error("手动触发热门内容缓存刷新失败", e);
            throw e;
        }
    }

    /**
     * 手动触发用户画像批量更新
     */
    public void triggerUserProfileBatchUpdate() {
        log.info("手动触发用户画像批量更新");
        try {
            executeWithRetry(this::doBatchUpdateUserProfiles, "用户画像批量更新");
            log.info("手动触发用户画像批量更新完成");
        } catch (Exception e) {
            log.error("手动触发用户画像批量更新失败", e);
            throw e;
        }
    }

    /**
     * 手动触发内容热度分数重新计算
     */
    public void triggerPopularityScoreRecalculation() {
        log.info("手动触发内容热度分数重新计算");
        try {
            executeWithRetry(this::doRecalculatePopularityScores, "内容热度分数重新计算");
            log.info("手动触发内容热度分数重新计算完成");
        } catch (Exception e) {
            log.error("手动触发内容热度分数重新计算失败", e);
            throw e;
        }
    }

    /**
     * 手动触发推荐日志清理
     */
    public void triggerRecommendationLogCleanup() {
        log.info("手动触发推荐日志清理");
        try {
            executeWithRetry(this::doCleanupOldRecommendationLogs, "推荐日志清理");
            log.info("手动触发推荐日志清理完成");
        } catch (Exception e) {
            log.error("手动触发推荐日志清理失败", e);
            throw e;
        }
    }
}
