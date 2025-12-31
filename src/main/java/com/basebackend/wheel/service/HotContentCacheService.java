package com.basebackend.wheel.service;

import com.basebackend.wheel.entity.WheelContent;

import java.util.List;

/**
 * 热门内容缓存服务接口
 * 负责管理热门内容的Redis缓存，支持推荐系统的快速访问
 *
 * @author wheel-api
 * @since 2025-01-31
 */
public interface HotContentCacheService {

    /**
     * 获取分类热门内容
     * 从Redis缓存获取指定分类的热门内容，如果缓存不存在则从数据库加载
     *
     * @param categoryId 分类ID
     * @param limit 返回数量限制（最大100）
     * @return 热门内容列表
     */
    List<WheelContent> getHotContentByCategory(Long categoryId, int limit);

    /**
     * 刷新缓存
     * 基于最近转盘统计刷新所有分类的热门内容缓存
     * 用于定时任务，每小时执行一次
     */
    void refreshCache();

    /**
     * 失效指定内容
     * 当内容状态变更（禁用、删除）时，从缓存中移除该内容
     *
     * @param contentId 内容ID
     */
    void invalidateContent(Long contentId);

    /**
     * 获取缓存命中率
     * 用于监控缓存效果，当命中率低于80%时应记录警告
     *
     * @return 缓存命中率（0.0-1.0）
     */
    double getCacheHitRate();

    /**
     * 获取指定分类的热门内容（带缓存统计）
     * 内部方法，用于统计缓存命中情况
     *
     * @param categoryId 分类ID
     * @return 热门内容列表
     */
    List<WheelContent> getHotContentByCategoryWithStats(Long categoryId);

    /**
     * 重置缓存统计
     * 用于测试或定期重置统计数据
     */
    void resetCacheStats();

    /**
     * 获取所有分类的热门内容
     * 用于首页推荐等场景
     *
     * @param limit 每个分类返回的数量限制
     * @return 所有分类的热门内容列表
     */
    List<WheelContent> getAllHotContent(int limit);

    /**
     * 预热缓存
     * 启动时或缓存失效后预热所有分类的热门内容缓存
     */
    void warmUpCache();
}
