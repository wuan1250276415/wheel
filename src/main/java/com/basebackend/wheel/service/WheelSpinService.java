package com.basebackend.wheel.service;

import com.basebackend.wheel.dto.WheelSpinDTO;
import com.basebackend.wheel.dto.WheelSpinResultDTO;
import com.basebackend.wheel.entity.WheelCategory;
import com.basebackend.wheel.entity.WheelContent;
import com.basebackend.wheel.entity.WheelSpinRecord;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 转盘服务接口
 *
 * @author wheel-api
 * @since 2025-12-16
 */
public interface WheelSpinService {

    /**
     * 获取转盘分类列表
     *
     * @return 分类列表
     */
    List<WheelCategory> getCategories();

    /**
     * 根据分类获取转盘内容
     *
     * @param categoryIds 分类ID列表
     * @return 内容列表
     */
    List<WheelContent> getContents(List<Long> categoryIds);

    /**
     * 执行转盘
     *
     * @param userId 用户ID
     * @param spinDTO 转盘参数
     * @return 转盘结果
     */
    WheelSpinResultDTO spin(Long userId, WheelSpinDTO spinDTO);

    /**
     * 获取用户历史记录
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 历史记录列表
     */
    List<WheelSpinRecord> getHistory(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户使用统计
     *
     * @param userId 用户ID
     * @return 统计数据
     */
    UserStats getStats(Long userId);

    /**
     * 校验转盘频率
     *
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @return 验证结果
     */
    boolean validateSpinFrequency(Long userId, String ipAddress);

    /**
     * 用户统计信息
     */
    class UserStats {
        private Long totalSpins;
        private Long todaySpins;
        private Long weekSpins;
        private Long monthSpins;
        private String favoriteCategory;
        private Long avgSpinDuration;

        // Getters and Setters
        public Long getTotalSpins() {
            return totalSpins;
        }

        public void setTotalSpins(Long totalSpins) {
            this.totalSpins = totalSpins;
        }

        public Long getTodaySpins() {
            return todaySpins;
        }

        public void setTodaySpins(Long todaySpins) {
            this.todaySpins = todaySpins;
        }

        public Long getWeekSpins() {
            return weekSpins;
        }

        public void setWeekSpins(Long weekSpins) {
            this.weekSpins = weekSpins;
        }

        public Long getMonthSpins() {
            return monthSpins;
        }

        public void setMonthSpins(Long monthSpins) {
            this.monthSpins = monthSpins;
        }

        public String getFavoriteCategory() {
            return favoriteCategory;
        }

        public void setFavoriteCategory(String favoriteCategory) {
            this.favoriteCategory = favoriteCategory;
        }

        public Long getAvgSpinDuration() {
            return avgSpinDuration;
        }

        public void setAvgSpinDuration(Long avgSpinDuration) {
            this.avgSpinDuration = avgSpinDuration;
        }
    }
}
