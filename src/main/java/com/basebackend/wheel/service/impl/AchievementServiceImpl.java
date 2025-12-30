package com.basebackend.wheel.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.basebackend.wheel.achievement.AchievementChecker;
import com.basebackend.wheel.cache.AchievementProgressCache;
import com.basebackend.wheel.dto.AchievementVO;
import com.basebackend.wheel.entity.AchievementTemplate;
import com.basebackend.wheel.entity.UserAchievement;
import com.basebackend.wheel.enums.AchievementCategory;
import com.basebackend.wheel.enums.AchievementRarity;
import com.basebackend.wheel.mapper.AchievementTemplateMapper;
import com.basebackend.wheel.mapper.UserAchievementMapper;
import com.basebackend.wheel.service.AchievementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AchievementServiceImpl implements AchievementService {

    @Autowired
    private AchievementTemplateMapper achievementTemplateMapper;

    @Autowired
    private UserAchievementMapper userAchievementMapper;

    @Autowired
    private AchievementProgressCache progressCache;

    @Autowired
    private List<AchievementChecker> checkers;

    @Override
    public List<AchievementVO> getUserAchievements(Long userId) {
        List<AchievementTemplate> templates = achievementTemplateMapper.selectList(
            new LambdaQueryWrapper<AchievementTemplate>()
                .eq(AchievementTemplate::getStatus, 1)
        );

        List<UserAchievement> userAchievements = userAchievementMapper.selectByUserId(userId);

        return templates.stream().map(template -> {
            UserAchievement userAchievement = userAchievements.stream()
                .filter(ua -> ua.getAchievementId().equals(template.getId()))
                .findFirst()
                .orElse(null);

            return convertToVO(template, userAchievement);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void checkAndUnlockAchievements(Long userId, String eventType, Object eventData) {
        List<AchievementTemplate> templates = achievementTemplateMapper.selectList(
            new LambdaQueryWrapper<AchievementTemplate>()
                .eq(AchievementTemplate::getStatus, 1)
        );

        for (AchievementTemplate template : templates) {
            try {
                JSONObject condition = JSON.parseObject(template.getUnlockCondition());
                String conditionType = condition.getString("type");

                if (!eventType.equals(conditionType)) {
                    continue;
                }

                UserAchievement userAchievement = userAchievementMapper.selectByUserIdAndAchievementId(
                    userId, template.getId()
                );

                if (userAchievement == null) {
                    userAchievement = new UserAchievement();
                    userAchievement.setUserId(userId);
                    userAchievement.setAchievementId(template.getId());
                    userAchievement.setProgress(0);
                    userAchievement.setTargetValue(condition.getIntValue("value"));
                    userAchievement.setIsUnlocked(0);
                    userAchievementMapper.insert(userAchievement);
                }

                if (userAchievement.getIsUnlocked() == 1) {
                    continue;
                }

                AchievementChecker checker = findChecker(conditionType);
                if (checker == null) {
                    log.warn("未找到成就检查器: conditionType={}", conditionType);
                    continue;
                }

                Long cachedProgress = progressCache.get(userId, eventType);
                int newProgress = cachedProgress != null ? cachedProgress.intValue() : 0;

                if (eventData instanceof Integer) {
                    newProgress = (Integer) eventData;
                }

                userAchievement.setProgress(newProgress);

                if (checker.check(template, userAchievement, eventData)) {
                    userAchievement.setIsUnlocked(1);
                    userAchievement.setUnlockedAt(LocalDateTime.now());
                    log.info("成就解锁: userId={}, achievementId={}, achievementName={}",
                        userId, template.getId(), template.getAchievementName());
                }

                userAchievementMapper.updateById(userAchievement);
                progressCache.set(userId, eventType, (long) newProgress);

            } catch (Exception e) {
                log.error("检查成就失败: userId={}, templateId={}, error={}",
                    userId, template.getId(), e.getMessage(), e);
            }
        }
    }

    private AchievementChecker findChecker(String conditionType) {
        return checkers.stream()
            .filter(checker -> checker.supports(conditionType))
            .findFirst()
            .orElse(null);
    }

    private AchievementVO convertToVO(AchievementTemplate template, UserAchievement userAchievement) {
        AchievementVO vo = new AchievementVO();
        vo.setId(template.getId());
        vo.setAchievementCode(template.getAchievementCode());
        vo.setAchievementName(template.getAchievementName());
        vo.setDescription(template.getDescription());
        vo.setIconUrl(template.getIconUrl());
        vo.setRarity(template.getRarity());
        vo.setRarityDesc(AchievementRarity.fromCode(template.getRarity()).getDescription());

        if (template.getCategory() != null) {
            vo.setCategory(template.getCategory());
            vo.setCategoryDesc(AchievementCategory.fromCode(template.getCategory()).getDescription());
        }

        vo.setIsHidden(template.getIsHidden() == 1);

        if (userAchievement != null) {
            vo.setProgress(userAchievement.getProgress());
            vo.setTargetValue(userAchievement.getTargetValue());
            vo.setIsUnlocked(userAchievement.getIsUnlocked() == 1);

            if (userAchievement.getTargetValue() != null && userAchievement.getTargetValue() > 0) {
                vo.setProgressPercent((int) ((userAchievement.getProgress() * 100.0) / userAchievement.getTargetValue()));
            } else {
                vo.setProgressPercent(0);
            }

            if (userAchievement.getUnlockedAt() != null) {
                vo.setUnlockedAt(userAchievement.getUnlockedAt()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
        } else {
            vo.setProgress(0);
            vo.setTargetValue(0);
            vo.setIsUnlocked(false);
            vo.setProgressPercent(0);
        }

        return vo;
    }
}
