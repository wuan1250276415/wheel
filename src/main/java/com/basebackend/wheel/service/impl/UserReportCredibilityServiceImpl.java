package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.entity.UserReportCredibility;
import com.basebackend.wheel.mapper.UserReportCredibilityMapper;
import com.basebackend.wheel.service.AuditConfigService;
import com.basebackend.wheel.service.UserReportCredibilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 用户举报信誉服务实现
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserReportCredibilityServiceImpl implements UserReportCredibilityService {

    private final UserReportCredibilityMapper userReportCredibilityMapper;
    private final AuditConfigService auditConfigService;

    /**
     * 低信誉阈值配置键
     */
    private static final String CONFIG_LOW_CREDIBILITY_THRESHOLD = "low_credibility_threshold";
    
    /**
     * 有效举报信誉分增加值配置键
     */
    private static final String CONFIG_CREDIBILITY_INCREASE_ON_VALID = "credibility_increase_on_valid";
    
    /**
     * 无效举报信誉分扣减值配置键
     */
    private static final String CONFIG_CREDIBILITY_DECREASE_ON_INVALID = "credibility_decrease_on_invalid";

    @Override
    public UserReportCredibility getUserCredibility(Long userId) {
        UserReportCredibility credibility = userReportCredibilityMapper.selectByUserId(userId);
        if (credibility == null) {
            // 自动初始化
            initUserCredibility(userId);
            credibility = userReportCredibilityMapper.selectByUserId(userId);
        }
        return credibility;
    }

    @Override
    public int getCredibilityScore(Long userId) {
        UserReportCredibility credibility = getUserCredibility(userId);
        if (credibility == null || credibility.getCredibilityScore() == null) {
            return UserReportCredibility.DEFAULT_CREDIBILITY_SCORE;
        }
        return credibility.getCredibilityScore();
    }

    @Override
    public boolean isLowCredibilityUser(Long userId) {
        int score = getCredibilityScore(userId);
        int threshold = auditConfigService.getIntConfig(CONFIG_LOW_CREDIBILITY_THRESHOLD, 
                UserReportCredibility.LOW_CREDIBILITY_THRESHOLD);
        return score < threshold;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementValidReport(Long userId) {
        ensureUserCredibilityExists(userId);
        int increment = auditConfigService.getIntConfig(CONFIG_CREDIBILITY_INCREASE_ON_VALID, 5);
        int updated = userReportCredibilityMapper.incrementValidReport(userId, increment);
        if (updated > 0) {
            log.debug("用户 {} 有效举报，信誉分增加 {}", userId, increment);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementInvalidReport(Long userId) {
        ensureUserCredibilityExists(userId);
        int decrement = auditConfigService.getIntConfig(CONFIG_CREDIBILITY_DECREASE_ON_INVALID, 10);
        int updated = userReportCredibilityMapper.incrementInvalidReport(userId, decrement);
        if (updated > 0) {
            log.debug("用户 {} 无效举报，信誉分减少 {}", userId, decrement);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initUserCredibility(Long userId) {
        userReportCredibilityMapper.initUserCredibility(userId);
        log.debug("初始化用户 {} 的信誉记录", userId);
    }

    @Override
    public List<UserReportCredibility> getLowCredibilityUsers() {
        int threshold = auditConfigService.getIntConfig(CONFIG_LOW_CREDIBILITY_THRESHOLD, 
                UserReportCredibility.LOW_CREDIBILITY_THRESHOLD);
        return userReportCredibilityMapper.selectLowCredibilityUsers(threshold);
    }

    @Override
    public List<UserReportCredibility> getActiveReporters(int limit) {
        return userReportCredibilityMapper.selectActiveReporters(limit);
    }

    @Override
    public List<Map<String, Object>> countByCredibilityLevel() {
        return userReportCredibilityMapper.countByCredibilityLevel();
    }

    @Override
    public double calculatePriorityFactor(Long userId) {
        int score = getCredibilityScore(userId);
        int threshold = auditConfigService.getIntConfig(CONFIG_LOW_CREDIBILITY_THRESHOLD, 
                UserReportCredibility.LOW_CREDIBILITY_THRESHOLD);
        
        if (score >= 80) {
            // 高信誉用户，优先级不降低
            return 1.0;
        } else if (score >= 50) {
            // 中等信誉用户，轻微降低
            return 0.8;
        } else if (score >= threshold) {
            // 低信誉用户，明显降低
            return 0.5;
        } else {
            // 极低信誉用户，大幅降低
            return 0.2;
        }
    }

    /**
     * 确保用户信誉记录存在
     */
    private void ensureUserCredibilityExists(Long userId) {
        UserReportCredibility existing = userReportCredibilityMapper.selectByUserId(userId);
        if (existing == null) {
            initUserCredibility(userId);
        }
    }
}
