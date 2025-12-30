package com.basebackend.wheel.achievement;

import com.basebackend.wheel.entity.AchievementTemplate;
import com.basebackend.wheel.entity.UserAchievement;

public interface AchievementChecker {
    boolean supports(String conditionType);
    boolean check(AchievementTemplate template, UserAchievement userAchievement, Object eventData);
    int calculateProgress(AchievementTemplate template, Object eventData);
}
