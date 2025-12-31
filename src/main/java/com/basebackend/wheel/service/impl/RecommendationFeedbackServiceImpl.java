package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.dto.RecommendationFeedbackDTO;
import com.basebackend.wheel.entity.RecommendationLog;
import com.basebackend.wheel.entity.WheelContent;
import com.basebackend.wheel.mapper.RecommendationLogMapper;
import com.basebackend.wheel.mapper.WheelContentMapper;
import com.basebackend.wheel.service.RecommendationFeedbackService;
import com.basebackend.wheel.util.AuditHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 推荐反馈服务实现类
 * 负责收集用户对推荐的反馈，用于改进推荐质量
 *
 * @author wheel-api
 * @since 2025-01-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationFeedbackServiceImpl implements RecommendationFeedbackService {

    private final RecommendationLogMapper recommendationLogMapper;
    private final WheelContentMapper wheelContentMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // CTR计算的最小曝光数
    private static final int MIN_IMPRESSIONS_FOR_CTR = 1000;
    
    // CTR阈值，低于此值将降低推荐分数
    private static final double CTR_THRESHOLD = 0.01; // 1%
    
    // 分数降低因子
    private static final double SCORE_REDUCTION_FACTOR = 0.8;
    
    // 日志保留天数
    private static final int LOG_RETENTION_DAYS = 90;
    
    // 会话忽略记录缓存前缀
    private static final String SESSION_IGNORE_PREFIX = "recommendation:ignore:";
    
    // 会话忽略记录过期时间（秒）
    private static final long SESSION_IGNORE_TTL = 3600; // 1小时

    @Override
    @Transactional
    public void recordClick(Long userId, Long contentId, String sessionId) {
        if (userId == null || contentId == null) {
            log.warn("记录点击事件失败：参数为空, userId={}, contentId={}", userId, contentId);
            return;
        }

        log.info("记录点击事件: userId={}, contentId={}, sessionId={}", userId, contentId, sessionId);

        try {
            LocalDateTime clickTime = LocalDateTime.now();
            
            // 更新推荐日志中的点击状态
            int updated = recommendationLogMapper.updateClickStatus(userId, contentId, sessionId, clickTime);
            
            if (updated > 0) {
                log.debug("点击状态更新成功: userId={}, contentId={}, sessionId={}", userId, contentId, sessionId);
            } else {
                // 如果没有找到对应的推荐记录，创建一个新的点击记录
                log.debug("未找到对应推荐记录，创建新的点击记录: userId={}, contentId={}", userId, contentId);
                createClickLog(userId, contentId, sessionId, clickTime);
            }
        } catch (Exception e) {
            log.error("记录点击事件失败: userId={}, contentId={}, error={}", userId, contentId, e.getMessage(), e);
        }
    }

    @Override
    public void recordIgnore(Long userId, Long contentId, String sessionId) {
        if (userId == null || contentId == null || sessionId == null) {
            log.warn("记录忽略事件失败：参数为空, userId={}, contentId={}, sessionId={}", userId, contentId, sessionId);
            return;
        }

        log.debug("记录忽略事件: userId={}, contentId={}, sessionId={}", userId, contentId, sessionId);

        try {
            // 使用Redis记录会话级别的忽略状态
            String cacheKey = SESSION_IGNORE_PREFIX + sessionId + ":" + contentId;
            
            // 检查是否已经记录过
            Boolean exists = redisTemplate.hasKey(cacheKey);
            if (Boolean.TRUE.equals(exists)) {
                log.debug("忽略事件已记录: sessionId={}, contentId={}", sessionId, contentId);
                return;
            }
            
            // 记录忽略状态
            redisTemplate.opsForValue().set(cacheKey, userId, SESSION_IGNORE_TTL, TimeUnit.SECONDS);
            
            log.debug("忽略事件记录成功: userId={}, contentId={}, sessionId={}", userId, contentId, sessionId);
        } catch (Exception e) {
            log.warn("记录忽略事件失败: userId={}, contentId={}, error={}", userId, contentId, e.getMessage());
        }
    }

    @Override
    @Transactional
    public void recordFeedback(Long userId, RecommendationFeedbackDTO feedbackDTO) {
        if (userId == null || feedbackDTO == null) {
            log.warn("记录反馈失败：参数为空");
            return;
        }

        String feedbackType = feedbackDTO.getFeedbackType();
        Long contentId = feedbackDTO.getContentId();
        String sessionId = feedbackDTO.getSessionId();

        log.info("记录推荐反馈: userId={}, contentId={}, type={}, sessionId={}", 
                userId, contentId, feedbackType, sessionId);

        try {
            switch (feedbackType.toUpperCase()) {
                case "CLICK":
                    recordClick(userId, contentId, sessionId);
                    break;
                case "IGNORE":
                    recordIgnore(userId, contentId, sessionId);
                    break;
                case "LIKE":
                    recordLike(userId, contentId, sessionId);
                    break;
                case "DISLIKE":
                    recordDislike(userId, contentId, sessionId);
                    break;
                default:
                    log.warn("未知的反馈类型: {}", feedbackType);
            }
        } catch (Exception e) {
            log.error("记录反馈失败: userId={}, contentId={}, type={}, error={}", 
                    userId, contentId, feedbackType, e.getMessage(), e);
        }
    }

    @Override
    public Double calculateCTR(Long contentId) {
        if (contentId == null) {
            log.warn("计算CTR失败：内容ID为空");
            return null;
        }

        try {
            Double ctr = recommendationLogMapper.calculateCTR(contentId, MIN_IMPRESSIONS_FOR_CTR);
            log.debug("计算CTR: contentId={}, ctr={}", contentId, ctr);
            return ctr;
        } catch (Exception e) {
            log.error("计算CTR失败: contentId={}, error={}", contentId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    @Transactional
    public void adjustScoreBasedOnCTR(Long contentId) {
        if (contentId == null) {
            log.warn("调整分数失败：内容ID为空");
            return;
        }

        try {
            // 计算CTR
            Double ctr = calculateCTR(contentId);
            
            // 如果曝光数不足，不调整
            if (ctr == null) {
                log.debug("曝光数不足，跳过分数调整: contentId={}", contentId);
                return;
            }

            // 如果CTR低于阈值，降低推荐分数
            if (ctr < CTR_THRESHOLD) {
                WheelContent content = wheelContentMapper.selectById(contentId);
                if (content != null && content.getPopularityScore() != null) {
                    double currentScore = content.getPopularityScore();
                    double newScore = currentScore * SCORE_REDUCTION_FACTOR;
                    
                    // 更新热度分数
                    wheelContentMapper.updateContentFeatures(contentId, newScore, null, null);
                    
                    log.info("根据CTR调整推荐分数: contentId={}, ctr={}, oldScore={}, newScore={}", 
                            contentId, ctr, currentScore, newScore);
                }
            }
        } catch (Exception e) {
            log.error("调整分数失败: contentId={}, error={}", contentId, e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void batchAdjustScores() {
        log.info("开始批量调整推荐分数");

        try {
            // 获取所有已审核通过且启用的内容
            List<WheelContent> contents = wheelContentMapper.selectAllApprovedAndEnabled();
            
            int adjustedCount = 0;
            for (WheelContent content : contents) {
                try {
                    Long contentId = content.getId();
                    Double ctr = calculateCTR(contentId);
                    
                    // 如果曝光数不足，跳过
                    if (ctr == null) {
                        continue;
                    }
                    
                    // 如果CTR低于阈值，降低推荐分数
                    if (ctr < CTR_THRESHOLD) {
                        Double currentScore = content.getPopularityScore();
                        if (currentScore != null && currentScore > 0.1) {
                            double newScore = currentScore * SCORE_REDUCTION_FACTOR;
                            wheelContentMapper.updateContentFeatures(contentId, newScore, null, null);
                            adjustedCount++;
                            
                            log.debug("调整内容分数: contentId={}, ctr={}, oldScore={}, newScore={}", 
                                    contentId, ctr, currentScore, newScore);
                        }
                    }
                } catch (Exception e) {
                    log.warn("调整单个内容分数失败: contentId={}, error={}", content.getId(), e.getMessage());
                }
            }
            
            log.info("批量调整推荐分数完成: totalContents={}, adjustedCount={}", contents.size(), adjustedCount);
        } catch (Exception e) {
            log.error("批量调整推荐分数失败: error={}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void recordImpression(Long userId, Long contentId, Double score, String reason, String sessionId) {
        if (userId == null || contentId == null) {
            log.warn("记录曝光失败：参数为空, userId={}, contentId={}", userId, contentId);
            return;
        }

        log.debug("记录推荐曝光: userId={}, contentId={}, score={}, sessionId={}", 
                userId, contentId, score, sessionId);

        try {
            RecommendationLog logEntry = new RecommendationLog();
            logEntry.setUserId(userId);
            logEntry.setContentId(contentId);
            logEntry.setRecommendationScore(score);
            logEntry.setRecommendationReason(reason);
            logEntry.setIsClicked(false);
            logEntry.setSessionId(sessionId);
            
            // 设置审计字段
            AuditHelper.setCreateAuditFields(logEntry, userId);
            
            recommendationLogMapper.insert(logEntry);
            
            log.debug("推荐曝光记录成功: userId={}, contentId={}, logId={}", userId, contentId, logEntry.getId());
        } catch (Exception e) {
            log.error("记录曝光失败: userId={}, contentId={}, error={}", userId, contentId, e.getMessage(), e);
        }
    }

    @Override
    public Long getImpressionCount(Long contentId) {
        if (contentId == null) {
            return 0L;
        }

        try {
            Long count = recommendationLogMapper.countImpressions(contentId);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("获取曝光次数失败: contentId={}, error={}", contentId, e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    public Long getClickCount(Long contentId) {
        if (contentId == null) {
            return 0L;
        }

        try {
            Long count = recommendationLogMapper.countClicks(contentId);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("获取点击次数失败: contentId={}, error={}", contentId, e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    @Transactional
    public int cleanupOldLogs() {
        log.info("开始清理过期推荐日志");

        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(LOG_RETENTION_DAYS);
            int deletedCount = recommendationLogMapper.deleteOldLogs(cutoffTime);
            
            log.info("清理过期推荐日志完成: deletedCount={}, cutoffTime={}", deletedCount, cutoffTime);
            return deletedCount;
        } catch (Exception e) {
            log.error("清理过期推荐日志失败: error={}", e.getMessage(), e);
            return 0;
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 创建点击日志记录
     */
    private void createClickLog(Long userId, Long contentId, String sessionId, LocalDateTime clickTime) {
        RecommendationLog logEntry = new RecommendationLog();
        logEntry.setUserId(userId);
        logEntry.setContentId(contentId);
        logEntry.setIsClicked(true);
        logEntry.setClickTime(clickTime);
        logEntry.setSessionId(sessionId);
        logEntry.setRecommendationReason("直接点击");
        
        // 设置审计字段
        AuditHelper.setCreateAuditFields(logEntry, userId);
        
        recommendationLogMapper.insert(logEntry);
    }

    /**
     * 记录喜欢事件
     */
    private void recordLike(Long userId, Long contentId, String sessionId) {
        log.debug("记录喜欢事件: userId={}, contentId={}, sessionId={}", userId, contentId, sessionId);
        
        // 喜欢事件视为点击
        recordClick(userId, contentId, sessionId);
        
        // 可以在这里添加额外的喜欢逻辑，如增加内容权重
    }

    /**
     * 记录不喜欢事件
     */
    private void recordDislike(Long userId, Long contentId, String sessionId) {
        log.debug("记录不喜欢事件: userId={}, contentId={}, sessionId={}", userId, contentId, sessionId);
        
        // 不喜欢事件视为忽略
        recordIgnore(userId, contentId, sessionId);
        
        // 可以在这里添加额外的不喜欢逻辑，如降低内容对该用户的推荐权重
    }
}
