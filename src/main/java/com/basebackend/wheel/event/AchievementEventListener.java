package com.basebackend.wheel.event;

import com.basebackend.wheel.service.AchievementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AchievementEventListener {

    @Autowired
    private AchievementService achievementService;

    @Async
    @EventListener
    public void handleAchievementEvent(AchievementEvent event) {
        try {
            log.info("处理成就事件: userId={}, eventType={}", event.getUserId(), event.getEventType());
            achievementService.checkAndUnlockAchievements(
                event.getUserId(),
                event.getEventType(),
                event.getEventData()
            );
        } catch (Exception e) {
            log.error("处理成就事件失败: userId={}, eventType={}, error={}",
                event.getUserId(), event.getEventType(), e.getMessage(), e);
        }
    }
}
