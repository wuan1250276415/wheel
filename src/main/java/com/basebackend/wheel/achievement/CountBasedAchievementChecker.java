package com.basebackend.wheel.achievement;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.basebackend.wheel.entity.AchievementTemplate;
import com.basebackend.wheel.entity.UserAchievement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class CountBasedAchievementChecker implements AchievementChecker {

    @Override
    public boolean supports(String conditionType) {
        return "spin_count".equals(conditionType) ||
               "couple_spin".equals(conditionType) ||
               "category_count".equals(conditionType) ||
               "content_create".equals(conditionType) ||
               "share_count".equals(conditionType) ||
               "consecutive_days".equals(conditionType) ||
               "couple_days".equals(conditionType) ||
               "daily_spin".equals(conditionType);
    }

    @Override
    public boolean check(AchievementTemplate template, UserAchievement userAchievement, Object eventData) {
        try {
            JSONObject condition = JSON.parseObject(template.getUnlockCondition());
            int targetValue = condition.getIntValue("value");
            int currentProgress = userAchievement.getProgress();

            return currentProgress >= targetValue;
        } catch (Exception e) {
            log.error("检查成就条件失败: templateId={}, error={}", template.getId(), e.getMessage());
            return false;
        }
    }

    @Override
    public int calculateProgress(AchievementTemplate template, Object eventData) {
        if (eventData instanceof Integer) {
            return (Integer) eventData;
        } else if (eventData instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) eventData;
            Object count = dataMap.get("count");
            if (count instanceof Integer) {
                return (Integer) count;
            }
        }
        return 0;
    }
}
