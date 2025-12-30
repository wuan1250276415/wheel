package com.basebackend.wheel.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AchievementEvent extends ApplicationEvent {

    private final Long userId;
    private final String eventType;
    private final Object eventData;

    public AchievementEvent(Object source, Long userId, String eventType, Object eventData) {
        super(source);
        this.userId = userId;
        this.eventType = eventType;
        this.eventData = eventData;
    }
}
