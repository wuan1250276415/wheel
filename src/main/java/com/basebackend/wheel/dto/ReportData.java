package com.basebackend.wheel.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 报告数据结构
 */
@Data
public class ReportData {

    /**
     * 认识天数
     */
    private Long daysTogether;

    /**
     * 总转盘次数
     */
    private Long totalSpins;

    /**
     * 用户1转盘次数
     */
    private Long user1Spins;

    /**
     * 用户2转盘次数
     */
    private Long user2Spins;

    /**
     * 最常用分类
     */
    private String favoriteCategory;

    /**
     * 最常用分类次数
     */
    private Long favoriteCategoryCount;

    /**
     * 活跃时间分布(小时->次数)
     */
    private Map<Integer, Long> activeTimeDistribution;

    /**
     * 聊天消息数
     */
    private Long chatMessageCount;

    /**
     * 动态数量
     */
    private Long momentCount;

    /**
     * 默契度评分
     */
    private Integer compatibilityScore;

    /**
     * 默契度描述
     */
    private String compatibilityDesc;

    /**
     * 即将到来的纪念日
     */
    private List<AnniversaryVO> upcomingAnniversaries;
}
