package com.basebackend.wheel.dto;

import lombok.Data;

@Data
public class AchievementVO {
    private Long id;
    private String achievementCode;
    private String achievementName;
    private String description;
    private String iconUrl;
    private Integer rarity;
    private String rarityDesc;
    private Integer category;
    private String categoryDesc;
    private Boolean isHidden;
    private Integer progress;
    private Integer targetValue;
    private Boolean isUnlocked;
    private String unlockedAt;
    private Integer progressPercent;
}
