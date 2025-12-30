package com.basebackend.wheel.util;

import com.basebackend.wheel.event.AchievementEvent;
import com.basebackend.wheel.event.AchievementEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AchievementEventPublisher {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void publishSpinEvent(Long userId, Integer totalSpinCount) {
        publishEvent(userId, AchievementEventType.SPIN_COUNT, totalSpinCount);
    }

    public void publishCoupleSpinEvent(Long userId, Integer coupleSpinCount) {
        publishEvent(userId, AchievementEventType.COUPLE_SPIN, coupleSpinCount);
    }

    public void publishCouplePairedEvent(Long userId) {
        publishEvent(userId, AchievementEventType.COUPLE_PAIRED, 1);
    }

    public void publishCategoryExploreEvent(Long userId, Integer categoryCount) {
        publishEvent(userId, AchievementEventType.CATEGORY_COUNT, categoryCount);
    }

    public void publishContentCreateEvent(Long userId, Integer contentCount) {
        publishEvent(userId, AchievementEventType.CONTENT_CREATE, contentCount);
    }

    public void publishShareEvent(Long userId, Integer shareCount) {
        publishEvent(userId, AchievementEventType.SHARE_COUNT, shareCount);
    }

    public void publishConsecutiveDaysEvent(Long userId, Integer consecutiveDays) {
        publishEvent(userId, AchievementEventType.CONSECUTIVE_DAYS, consecutiveDays);
    }

    public void publishDailySpinEvent(Long userId, Integer dailySpinCount) {
        publishEvent(userId, AchievementEventType.DAILY_SPIN, dailySpinCount);
    }

    private void publishEvent(Long userId, String eventType, Object eventData) {
        try {
            AchievementEvent event = new AchievementEvent(this, userId, eventType, eventData);
            eventPublisher.publishEvent(event);
            log.debug("发布成就事件: userId={}, eventType={}, eventData={}", userId, eventType, eventData);
        } catch (Exception e) {
            log.error("发布成就事件失败: userId={}, eventType={}, error={}", userId, eventType, e.getMessage());
        }
    }
}
