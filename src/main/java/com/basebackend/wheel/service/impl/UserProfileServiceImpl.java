package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.entity.UserProfile;
import com.basebackend.wheel.mapper.UserProfileMapper;
import com.basebackend.wheel.service.UserProfileService;
import com.basebackend.wheel.util.AuditHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 用户画像服务实现类
 * 负责管理用户偏好数据，支持个性化推荐
 *
 * @author wheel-api
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileMapper userProfileMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // 缓存键前缀
    private static final String PROFILE_CACHE_PREFIX = "recommendation:profile:";
    
    // 缓存过期时间（秒）
    private static final long PROFILE_CACHE_TTL = 3600; // 1小时
    
    // 画像过期天数
    private static final int STALE_DAYS = 7;
    
    // 活动分析天数
    private static final int ACTIVITY_WINDOW_DAYS = 30;

    @Override
    public UserProfile getProfile(Long userId) {
        if (userId == null) {
            log.warn("获取用户画像失败：用户ID为空");
            return null;
        }

        // 1. 尝试从Redis缓存获取
        UserProfile cachedProfile = getProfileFromCache(userId);
        if (cachedProfile != null) {
            log.debug("从缓存获取用户画像: userId={}", userId);
            
            // 检查是否过期，如果过期则异步重建
            if (Boolean.TRUE.equals(cachedProfile.getIsStale())) {
                log.info("用户画像已过期，触发重建: userId={}", userId);
                // 返回缓存数据，但标记需要重建
            }
            return cachedProfile;
        }

        // 2. 从数据库加载
        UserProfile profile = userProfileMapper.selectByUserId(userId);
        
        // 3. 如果不存在则创建新画像
        if (profile == null) {
            log.info("用户画像不存在，创建新画像: userId={}", userId);
            profile = createNewProfile(userId);
        }

        // 4. 检查并更新过期状态
        if (isProfileStaleInternal(profile)) {
            profile.setIsStale(true);
            userProfileMapper.updateById(profile);
        }

        // 5. 缓存到Redis
        cacheProfile(profile);

        return profile;
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, UserInteraction interaction) {
        if (userId == null || interaction == null) {
            log.warn("更新用户画像失败：参数为空");
            return;
        }

        log.info("更新用户画像: userId={}, categoryId={}, type={}", 
                userId, interaction.getCategoryId(), interaction.getType());

        // 1. 获取或创建用户画像
        UserProfile profile = getProfile(userId);
        if (profile == null) {
            profile = createNewProfile(userId);
        }

        // 2. 更新偏好分类权重
        updateFavoriteCategories(profile, interaction);

        // 3. 更新活跃时段
        updateActiveTimeSlots(profile, interaction);

        // 4. 更新行为标签
        updateBehaviorTags(profile, interaction);

        // 5. 更新活跃时间和转盘次数
        LocalDateTime now = LocalDateTime.now();
        profile.setLastActiveTime(now);
        profile.setIsStale(false);
        
        if (interaction.getType() == InteractionType.SPIN) {
            profile.setTotalSpins(profile.getTotalSpins() + 1);
        }

        // 6. 设置审计字段
        AuditHelper.setUpdateAuditFields(profile, userId);

        // 7. 保存到数据库
        userProfileMapper.updateById(profile);

        // 8. 更新缓存
        cacheProfile(profile);

        log.debug("用户画像更新完成: userId={}", userId);
    }

    @Override
    @Transactional
    public void rebuildProfile(Long userId) {
        if (userId == null) {
            log.warn("重建用户画像失败：用户ID为空");
            return;
        }

        log.info("开始重建用户画像: userId={}", userId);

        // 1. 获取或创建用户画像
        UserProfile profile = userProfileMapper.selectByUserId(userId);
        if (profile == null) {
            profile = createNewProfile(userId);
        }

        // 2. 计算时间范围（最近30天）
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(ACTIVITY_WINDOW_DAYS);

        // 3. 重新计算偏好分类
        Map<Long, Double> favoriteCategories = calculateFavoriteCategories(userId, startTime, endTime);
        profile.setFavoriteCategories(favoriteCategories);

        // 4. 重新计算活跃时段
        List<UserProfile.TimeSlot> activeTimeSlots = calculateActiveTimeSlots(userId, startTime, endTime);
        profile.setActiveTimeSlots(activeTimeSlots);

        // 5. 重新计算总转盘次数
        Integer totalSpins = userProfileMapper.countSpinsInRange(userId, startTime, endTime);
        profile.setTotalSpins(totalSpins != null ? totalSpins : 0);

        // 6. 更新状态
        profile.setLastActiveTime(endTime);
        profile.setIsStale(false);

        // 7. 设置审计字段
        AuditHelper.setUpdateAuditFields(profile, userId);

        // 8. 保存到数据库
        userProfileMapper.updateById(profile);

        // 9. 更新缓存
        cacheProfile(profile);

        log.info("用户画像重建完成: userId={}, categories={}, timeSlots={}", 
                userId, favoriteCategories.size(), activeTimeSlots.size());
    }

    @Override
    public boolean isProfileStale(Long userId) {
        if (userId == null) {
            return true;
        }

        UserProfile profile = getProfile(userId);
        return profile == null || isProfileStaleInternal(profile);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 创建新的用户画像
     */
    private UserProfile createNewProfile(Long userId) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setFavoriteCategories(new HashMap<>());
        profile.setActiveTimeSlots(new ArrayList<>());
        profile.setBehaviorTags(new ArrayList<>());
        profile.setTotalSpins(0);
        profile.setLastActiveTime(LocalDateTime.now());
        profile.setIsStale(false);

        // 设置审计字段
        AuditHelper.setCreateAuditFields(profile, userId);

        // 保存到数据库
        userProfileMapper.insert(profile);

        log.info("创建新用户画像: userId={}, profileId={}", userId, profile.getId());
        return profile;
    }

    /**
     * 从缓存获取用户画像
     */
    private UserProfile getProfileFromCache(Long userId) {
        try {
            String cacheKey = PROFILE_CACHE_PREFIX + userId;
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return objectMapper.convertValue(cached, UserProfile.class);
            }
        } catch (Exception e) {
            log.warn("从缓存获取用户画像失败: userId={}, error={}", userId, e.getMessage());
        }
        return null;
    }

    /**
     * 缓存用户画像
     */
    private void cacheProfile(UserProfile profile) {
        if (profile == null || profile.getUserId() == null) {
            return;
        }
        try {
            String cacheKey = PROFILE_CACHE_PREFIX + profile.getUserId();
            redisTemplate.opsForValue().set(cacheKey, profile, PROFILE_CACHE_TTL, TimeUnit.SECONDS);
            log.debug("缓存用户画像: userId={}", profile.getUserId());
        } catch (Exception e) {
            log.warn("缓存用户画像失败: userId={}, error={}", profile.getUserId(), e.getMessage());
        }
    }

    /**
     * 检查画像是否过期（内部方法）
     */
    private boolean isProfileStaleInternal(UserProfile profile) {
        if (profile == null) {
            return true;
        }
        
        // 如果已标记为过期
        if (Boolean.TRUE.equals(profile.getIsStale())) {
            return true;
        }

        // 检查最后活跃时间
        LocalDateTime lastActive = profile.getLastActiveTime();
        if (lastActive == null) {
            return true;
        }

        // 超过7天没有活动则认为过期
        long daysSinceLastActive = ChronoUnit.DAYS.between(lastActive, LocalDateTime.now());
        return daysSinceLastActive >= STALE_DAYS;
    }

    /**
     * 更新偏好分类权重
     */
    private void updateFavoriteCategories(UserProfile profile, UserInteraction interaction) {
        if (interaction.getCategoryId() == null) {
            return;
        }

        Map<Long, Double> categories = profile.getFavoriteCategories();
        if (categories == null) {
            categories = new HashMap<>();
            profile.setFavoriteCategories(categories);
        }

        Long categoryId = interaction.getCategoryId();
        double interactionWeight = interaction.getType().getWeight();
        
        // 获取当前权重，如果不存在则为0
        double currentWeight = categories.getOrDefault(categoryId, 0.0);
        
        // 使用衰减公式更新权重：new_weight = old_weight * decay + interaction_weight
        // 衰减因子0.9，确保新交互有足够影响力
        double decayFactor = 0.9;
        double newWeight = currentWeight * decayFactor + interactionWeight;
        
        // 限制权重范围在0-1之间
        newWeight = Math.min(1.0, Math.max(0.0, newWeight));
        
        categories.put(categoryId, newWeight);

        // 归一化所有权重
        normalizeWeights(categories);
    }

    /**
     * 归一化权重
     */
    private void normalizeWeights(Map<Long, Double> weights) {
        if (weights == null || weights.isEmpty()) {
            return;
        }

        double maxWeight = weights.values().stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(1.0);

        if (maxWeight > 0) {
            weights.replaceAll((k, v) -> v / maxWeight);
        }
    }

    /**
     * 更新活跃时段
     */
    private void updateActiveTimeSlots(UserProfile profile, UserInteraction interaction) {
        List<UserProfile.TimeSlot> timeSlots = profile.getActiveTimeSlots();
        if (timeSlots == null) {
            timeSlots = new ArrayList<>();
            profile.setActiveTimeSlots(timeSlots);
        }

        int currentHour = interaction.getTimestamp() != null 
                ? interaction.getTimestamp().getHour() 
                : LocalDateTime.now().getHour();

        // 查找是否已存在该小时的记录
        Optional<UserProfile.TimeSlot> existingSlot = timeSlots.stream()
                .filter(slot -> slot.getHour() != null && slot.getHour() == currentHour)
                .findFirst();

        if (existingSlot.isPresent()) {
            // 增加权重
            UserProfile.TimeSlot slot = existingSlot.get();
            double newWeight = Math.min(1.0, slot.getWeight() + 0.1);
            slot.setWeight(newWeight);
        } else {
            // 添加新时段
            UserProfile.TimeSlot newSlot = new UserProfile.TimeSlot();
            newSlot.setHour(currentHour);
            newSlot.setWeight(0.1);
            timeSlots.add(newSlot);
        }

        // 归一化时段权重
        normalizeTimeSlotWeights(timeSlots);
    }

    /**
     * 归一化时段权重
     */
    private void normalizeTimeSlotWeights(List<UserProfile.TimeSlot> timeSlots) {
        if (timeSlots == null || timeSlots.isEmpty()) {
            return;
        }

        double maxWeight = timeSlots.stream()
                .mapToDouble(UserProfile.TimeSlot::getWeight)
                .max()
                .orElse(1.0);

        if (maxWeight > 0) {
            timeSlots.forEach(slot -> slot.setWeight(slot.getWeight() / maxWeight));
        }
    }

    /**
     * 更新行为标签
     */
    private void updateBehaviorTags(UserProfile profile, UserInteraction interaction) {
        List<String> tags = profile.getBehaviorTags();
        if (tags == null) {
            tags = new ArrayList<>();
            profile.setBehaviorTags(tags);
        }

        // 根据交互类型添加标签
        String newTag = null;
        switch (interaction.getType()) {
            case SPIN:
                newTag = "active_spinner";
                break;
            case SHARE:
                newTag = "social_sharer";
                break;
            case VIEW:
                newTag = "content_explorer";
                break;
        }

        if (newTag != null && !tags.contains(newTag)) {
            tags.add(newTag);
        }

        // 限制标签数量
        if (tags.size() > 20) {
            profile.setBehaviorTags(new ArrayList<>(tags.subList(tags.size() - 20, tags.size())));
        }
    }

    /**
     * 计算偏好分类（基于历史数据）
     */
    private Map<Long, Double> calculateFavoriteCategories(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        Map<Long, Double> categories = new HashMap<>();

        try {
            List<Map<String, Object>> stats = userProfileMapper.selectCategorySpinStats(userId, startTime, endTime);
            if (stats == null || stats.isEmpty()) {
                return categories;
            }

            // 计算总转盘次数
            long totalSpins = stats.stream()
                    .mapToLong(s -> ((Number) s.get("spinCount")).longValue())
                    .sum();

            if (totalSpins == 0) {
                return categories;
            }

            // 计算每个分类的权重
            for (Map<String, Object> stat : stats) {
                Long categoryId = ((Number) stat.get("categoryId")).longValue();
                long spinCount = ((Number) stat.get("spinCount")).longValue();
                double weight = (double) spinCount / totalSpins;
                categories.put(categoryId, weight);
            }
        } catch (Exception e) {
            log.error("计算偏好分类失败: userId={}, error={}", userId, e.getMessage());
        }

        return categories;
    }

    /**
     * 计算活跃时段（基于历史数据）
     */
    private List<UserProfile.TimeSlot> calculateActiveTimeSlots(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        List<UserProfile.TimeSlot> timeSlots = new ArrayList<>();

        try {
            List<Map<String, Object>> stats = userProfileMapper.selectHourlySpinStats(userId, startTime, endTime);
            if (stats == null || stats.isEmpty()) {
                return timeSlots;
            }

            // 计算总转盘次数
            long totalSpins = stats.stream()
                    .mapToLong(s -> ((Number) s.get("spinCount")).longValue())
                    .sum();

            if (totalSpins == 0) {
                return timeSlots;
            }

            // 计算每个小时的权重
            for (Map<String, Object> stat : stats) {
                int hour = ((Number) stat.get("hour")).intValue();
                long spinCount = ((Number) stat.get("spinCount")).longValue();
                double weight = (double) spinCount / totalSpins;

                UserProfile.TimeSlot slot = new UserProfile.TimeSlot();
                slot.setHour(hour);
                slot.setWeight(weight);
                timeSlots.add(slot);
            }

            // 按权重降序排序
            timeSlots.sort((a, b) -> Double.compare(b.getWeight(), a.getWeight()));
        } catch (Exception e) {
            log.error("计算活跃时段失败: userId={}, error={}", userId, e.getMessage());
        }

        return timeSlots;
    }
}
