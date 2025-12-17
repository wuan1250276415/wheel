package com.basebackend.wheel.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 统计分析服务接口
 *
 * @author wheel-api
 * @since 2025-12-16
 */
public interface StatisticsService {

    /**
     * 获取用户统计
     *
     * @param userId 用户ID
     * @return 用户统计信息
     */
    UserStatistics getUserStatistics(Long userId);

    /**
     * 获取系统概览统计
     *
     * @return 系统统计信息
     */
    SystemOverview getSystemOverview();

    /**
     * 获取转盘使用趋势
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 使用趋势数据列表
     */
    List<SpinTrend> getSpinTrend(LocalDate startDate, LocalDate endDate);

    /**
     * 获取热门内容排行
     *
     * @param limit 数量限制
     * @return 热门内容列表
     */
    List<PopularContent> getPopularContent(Integer limit);

    /**
     * 获取分类使用统计
     *
     * @return 分类统计列表
     */
    List<CategoryStatistics> getCategoryStatistics();

    /**
     * 获取情侣关系统计
     *
     * @return 情侣关系统计
     */
    CoupleStatistics getCoupleStatistics();

    /**
     * 用户统计信息
     */
    class UserStatistics {
        private Long totalSpins;
        private Long todaySpins;
        private Long weekSpins;
        private Long monthSpins;
        private String favoriteCategory;
        private Long avgSpinDuration;
        private Long consecutiveDays;
        private Long totalCoupleSpins;
        private Long uniqueContentsSeen;
        private String lastSpinTime;

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

        public Long getConsecutiveDays() {
            return consecutiveDays;
        }

        public void setConsecutiveDays(Long consecutiveDays) {
            this.consecutiveDays = consecutiveDays;
        }

        public Long getTotalCoupleSpins() {
            return totalCoupleSpins;
        }

        public void setTotalCoupleSpins(Long totalCoupleSpins) {
            this.totalCoupleSpins = totalCoupleSpins;
        }

        public Long getUniqueContentsSeen() {
            return uniqueContentsSeen;
        }

        public void setUniqueContentsSeen(Long uniqueContentsSeen) {
            this.uniqueContentsSeen = uniqueContentsSeen;
        }

        public String getLastSpinTime() {
            return lastSpinTime;
        }

        public void setLastSpinTime(String lastSpinTime) {
            this.lastSpinTime = lastSpinTime;
        }
    }

    /**
     * 系统概览统计
     */
    class SystemOverview {
        private Long totalUsers;
        private Long totalCouples;
        private Long totalSpins;
        private Long totalContents;
        private Long todayNewUsers;
        private Long todayNewCouples;
        private Long todaySpins;
        private Long activeUsers24h;
        private Double avgSpinsPerUser;
        private Double coupleFormationRate;

        // Getters and Setters
        public Long getTotalUsers() {
            return totalUsers;
        }

        public void setTotalUsers(Long totalUsers) {
            this.totalUsers = totalUsers;
        }

        public Long getTotalCouples() {
            return totalCouples;
        }

        public void setTotalCouples(Long totalCouples) {
            this.totalCouples = totalCouples;
        }

        public Long getTotalSpins() {
            return totalSpins;
        }

        public void setTotalSpins(Long totalSpins) {
            this.totalSpins = totalSpins;
        }

        public Long getTotalContents() {
            return totalContents;
        }

        public void setTotalContents(Long totalContents) {
            this.totalContents = totalContents;
        }

        public Long getTodayNewUsers() {
            return todayNewUsers;
        }

        public void setTodayNewUsers(Long todayNewUsers) {
            this.todayNewUsers = todayNewUsers;
        }

        public Long getTodayNewCouples() {
            return todayNewCouples;
        }

        public void setTodayNewCouples(Long todayNewCouples) {
            this.todayNewCouples = todayNewCouples;
        }

        public Long getTodaySpins() {
            return todaySpins;
        }

        public void setTodaySpins(Long todaySpins) {
            this.todaySpins = todaySpins;
        }

        public Long getActiveUsers24h() {
            return activeUsers24h;
        }

        public void setActiveUsers24h(Long activeUsers24h) {
            this.activeUsers24h = activeUsers24h;
        }

        public Double getAvgSpinsPerUser() {
            return avgSpinsPerUser;
        }

        public void setAvgSpinsPerUser(Double avgSpinsPerUser) {
            this.avgSpinsPerUser = avgSpinsPerUser;
        }

        public Double getCoupleFormationRate() {
            return coupleFormationRate;
        }

        public void setCoupleFormationRate(Double coupleFormationRate) {
            this.coupleFormationRate = coupleFormationRate;
        }
    }

    /**
     * 转盘使用趋势
     */
    class SpinTrend {
        private String date;
        private Long spinCount;
        private Long userCount;
        private Long coupleCount;

        // Getters and Setters
        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Long getSpinCount() {
            return spinCount;
        }

        public void setSpinCount(Long spinCount) {
            this.spinCount = spinCount;
        }

        public Long getUserCount() {
            return userCount;
        }

        public void setUserCount(Long userCount) {
            this.userCount = userCount;
        }

        public Long getCoupleCount() {
            return coupleCount;
        }

        public void setCoupleCount(Long coupleCount) {
            this.coupleCount = coupleCount;
        }
    }

    /**
     * 热门内容
     */
    class PopularContent {
        private Long contentId;
        private String contentText;
        private String categoryName;
        private Long spinCount;
        private Double winRate;

        // Getters and Setters
        public Long getContentId() {
            return contentId;
        }

        public void setContentId(Long contentId) {
            this.contentId = contentId;
        }

        public String getContentText() {
            return contentText;
        }

        public void setContentText(String contentText) {
            this.contentText = contentText;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public Long getSpinCount() {
            return spinCount;
        }

        public void setSpinCount(Long spinCount) {
            this.spinCount = spinCount;
        }

        public Double getWinRate() {
            return winRate;
        }

        public void setWinRate(Double winRate) {
            this.winRate = winRate;
        }
    }

    /**
     * 分类统计
     */
    class CategoryStatistics {
        private Long categoryId;
        private String categoryName;
        private Long contentCount;
        private Long totalSpins;
        private Double avgWeight;
        private Long uniqueUsers;

        // Getters and Setters
        public Long getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public Long getContentCount() {
            return contentCount;
        }

        public void setContentCount(Long contentCount) {
            this.contentCount = contentCount;
        }

        public Long getTotalSpins() {
            return totalSpins;
        }

        public void setTotalSpins(Long totalSpins) {
            this.totalSpins = totalSpins;
        }

        public Double getAvgWeight() {
            return avgWeight;
        }

        public void setAvgWeight(Double avgWeight) {
            this.avgWeight = avgWeight;
        }

        public Long getUniqueUsers() {
            return uniqueUsers;
        }

        public void setUniqueUsers(Long uniqueUsers) {
            this.uniqueUsers = uniqueUsers;
        }
    }

    /**
     * 情侣关系统计
     */
    class CoupleStatistics {
        private Long totalCouples;
        private Long activeCouples;
        private Long avgCoupleAge;
        private Double formationRate;
        private Long mostActiveCoupleSpins;
        private Long coupleRetentionRate7d;
        private Long coupleRetentionRate30d;

        // Getters and Setters
        public Long getTotalCouples() {
            return totalCouples;
        }

        public void setTotalCouples(Long totalCouples) {
            this.totalCouples = totalCouples;
        }

        public Long getActiveCouples() {
            return activeCouples;
        }

        public void setActiveCouples(Long activeCouples) {
            this.activeCouples = activeCouples;
        }

        public Long getAvgCoupleAge() {
            return avgCoupleAge;
        }

        public void setAvgCoupleAge(Long avgCoupleAge) {
            this.avgCoupleAge = avgCoupleAge;
        }

        public Double getFormationRate() {
            return formationRate;
        }

        public void setFormationRate(Double formationRate) {
            this.formationRate = formationRate;
        }

        public Long getMostActiveCoupleSpins() {
            return mostActiveCoupleSpins;
        }

        public void setMostActiveCoupleSpins(Long mostActiveCoupleSpins) {
            this.mostActiveCoupleSpins = mostActiveCoupleSpins;
        }

        public Long getCoupleRetentionRate7d() {
            return coupleRetentionRate7d;
        }

        public void setCoupleRetentionRate7d(Long coupleRetentionRate7d) {
            this.coupleRetentionRate7d = coupleRetentionRate7d;
        }

        public Long getCoupleRetentionRate30d() {
            return coupleRetentionRate30d;
        }

        public void setCoupleRetentionRate30d(Long coupleRetentionRate30d) {
            this.coupleRetentionRate30d = coupleRetentionRate30d;
        }
    }
}
