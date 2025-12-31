package com.basebackend.wheel.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 首页推荐VO
 * 
 * @author wheel-api
 * @since 2025-01-31
 */
@Data
public class HomepageRecommendationVO {

    /**
     * 个性化分类排名
     */
    private List<CategoryRankVO> categoryRanking;

    /**
     * 为你推荐（Top 5）
     */
    private List<RecommendationVO> recommendedForYou;

    /**
     * 情侣精选（可选，仅情侣用户显示）
     */
    private List<RecommendationVO> couplePicks;

    /**
     * 刷新时间
     */
    private LocalDateTime refreshTime;
}
