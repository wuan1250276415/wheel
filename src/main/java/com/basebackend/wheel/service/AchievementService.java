package com.basebackend.wheel.service;

import com.basebackend.wheel.dto.AchievementVO;
import java.util.List;

public interface AchievementService {
    List<AchievementVO> getUserAchievements(Long userId);
    void checkAndUnlockAchievements(Long userId, String eventType, Object eventData);
}
