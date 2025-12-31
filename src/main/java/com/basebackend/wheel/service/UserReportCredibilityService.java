package com.basebackend.wheel.service;

import com.basebackend.wheel.entity.UserReportCredibility;

import java.util.List;
import java.util.Map;

/**
 * 用户举报信誉服务接口
 *
 * @author wheel-api
 * @since 2025-02-01
 */
public interface UserReportCredibilityService {

    /**
     * 获取用户信誉记录
     *
     * @param userId 用户ID
     * @return 信誉记录
     */
    UserReportCredibility getUserCredibility(Long userId);

    /**
     * 获取用户信誉分
     *
     * @param userId 用户ID
     * @return 信誉分（0-100）
     */
    int getCredibilityScore(Long userId);

    /**
     * 判断用户是否为低信誉用户
     *
     * @param userId 用户ID
     * @return true-低信誉用户，false-正常用户
     */
    boolean isLowCredibilityUser(Long userId);

    /**
     * 增加有效举报记录
     *
     * @param userId 用户ID
     */
    void incrementValidReport(Long userId);

    /**
     * 增加无效举报记录
     *
     * @param userId 用户ID
     */
    void incrementInvalidReport(Long userId);

    /**
     * 初始化用户信誉记录
     *
     * @param userId 用户ID
     */
    void initUserCredibility(Long userId);

    /**
     * 获取低信誉用户列表
     *
     * @return 低信誉用户列表
     */
    List<UserReportCredibility> getLowCredibilityUsers();

    /**
     * 获取举报活跃用户列表
     *
     * @param limit 限制数量
     * @return 活跃用户列表
     */
    List<UserReportCredibility> getActiveReporters(int limit);

    /**
     * 统计各信誉等级用户数量
     *
     * @return 信誉等级统计
     */
    List<Map<String, Object>> countByCredibilityLevel();

    /**
     * 计算举报优先级降级因子
     * 低信誉用户的举报优先级会被降低
     *
     * @param userId 用户ID
     * @return 优先级降级因子（0.0-1.0，1.0表示不降级）
     */
    double calculatePriorityFactor(Long userId);
}
