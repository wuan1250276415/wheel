package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.entity.*;
import com.basebackend.wheel.mapper.*;
import com.basebackend.wheel.service.StatisticsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计分析服务实现
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private WheelUserMapper userMapper;

    @Autowired
    private CoupleRelationshipMapper coupleMapper;

    @Autowired
    private WheelSpinRecordMapper spinRecordMapper;

    @Autowired
    private WheelContentMapper contentMapper;

    @Autowired
    private WheelCategoryMapper categoryMapper;

    @Autowired
    private UserBehaviorStatsMapper statsMapper;

    @Override
    public UserStatistics getUserStatistics(Long userId) {
        try {
            UserStatistics stats = new UserStatistics();

            // 获取总转盘次数
            Long totalSpins = spinRecordMapper.selectCount(
                new LambdaQueryWrapper<WheelSpinRecord>()
                    .eq(WheelSpinRecord::getUserId, userId)
            );
            stats.setTotalSpins(totalSpins != null ? totalSpins : 0L);

            // 获取今日转盘次数
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            Long todaySpins = spinRecordMapper.selectCount(
                new LambdaQueryWrapper<WheelSpinRecord>()
                    .eq(WheelSpinRecord::getUserId, userId)
                    .ge(WheelSpinRecord::getSpinTime, todayStart)
            );
            stats.setTodaySpins(todaySpins != null ? todaySpins : 0L);

            // 获取本周转盘次数
            LocalDateTime weekStart = LocalDate.now().minusDays(7).atStartOfDay();
            Long weekSpins = spinRecordMapper.selectCount(
                new LambdaQueryWrapper<WheelSpinRecord>()
                    .eq(WheelSpinRecord::getUserId, userId)
                    .ge(WheelSpinRecord::getSpinTime, weekStart)
            );
            stats.setWeekSpins(weekSpins != null ? weekSpins : 0L);

            // 获取本月转盘次数
            LocalDateTime monthStart = LocalDate.now().minusDays(30).atStartOfDay();
            Long monthSpins = spinRecordMapper.selectCount(
                new LambdaQueryWrapper<WheelSpinRecord>()
                    .eq(WheelSpinRecord::getUserId, userId)
                    .ge(WheelSpinRecord::getSpinTime, monthStart)
            );
            stats.setMonthSpins(monthSpins != null ? monthSpins : 0L);

            // 获取最偏好的分类
            List<WheelSpinRecord> recentSpins = spinRecordMapper.selectList(
                new LambdaQueryWrapper<WheelSpinRecord>()
                    .eq(WheelSpinRecord::getUserId, userId)
                    .orderByDesc(WheelSpinRecord::getSpinTime)
                    .last("LIMIT 100")
            );

            if (!recentSpins.isEmpty()) {
                Map<Long, Long> categoryCount = recentSpins.stream()
                    .filter(s -> s.getCategoryId() != null)
                    .collect(Collectors.groupingBy(
                        WheelSpinRecord::getCategoryId,
                        Collectors.counting()
                    ));

                Long favoriteCategoryId = categoryCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

                if (favoriteCategoryId != null) {
                    WheelCategory category = categoryMapper.selectById(favoriteCategoryId);
                    if (category != null) {
                        stats.setFavoriteCategory(category.getCategoryName());
                    }
                }
            }

            // 获取平均转盘时长
            QueryWrapper<WheelSpinRecord> avgDurationWrapper = new QueryWrapper<>();
            avgDurationWrapper.select("AVG(spin_duration) as avg_duration")
                .eq("user_id", userId)
                .isNotNull("spin_duration");
            Map<String, Object> avgDurationResult = firstMap(spinRecordMapper.selectMaps(avgDurationWrapper));
            Double avgDuration = getDoubleValue(avgDurationResult, "avg_duration");
            stats.setAvgSpinDuration(avgDuration.longValue());

            // 获取连续使用天数
            List<UserBehaviorStats> statsList = statsMapper.selectList(
                new LambdaQueryWrapper<UserBehaviorStats>()
                    .eq(UserBehaviorStats::getUserId, userId)
                    .orderByDesc(UserBehaviorStats::getStatsDate)
            );

            long consecutiveDays = 0;
            LocalDate currentDate = LocalDate.now();
            for (UserBehaviorStats stat : statsList) {
                if (stat.getSpinCount() > 0) {
                    long daysDiff = ChronoUnit.DAYS.between(stat.getStatsDate(), currentDate);
                    if (daysDiff == consecutiveDays) {
                        consecutiveDays++;
                        currentDate = stat.getStatsDate();
                    } else {
                        break;
                    }
                }
            }
            stats.setConsecutiveDays(consecutiveDays);

            // 获取情侣转盘次数
            Long coupleSpins = spinRecordMapper.selectCount(
                new LambdaQueryWrapper<WheelSpinRecord>()
                    .eq(WheelSpinRecord::getUserId, userId)
                    .isNotNull(WheelSpinRecord::getDeviceId)
            );
            stats.setTotalCoupleSpins(coupleSpins != null ? coupleSpins : 0L);

            // 获取看到的不同内容数量
            QueryWrapper<WheelSpinRecord> uniqueContentWrapper = new QueryWrapper<>();
            uniqueContentWrapper.select("COUNT(DISTINCT content_id) as unique_count")
                .eq("user_id", userId);
            Map<String, Object> uniqueContentResult = firstMap(spinRecordMapper.selectMaps(uniqueContentWrapper));
            Long uniqueContents = getLongValue(uniqueContentResult, "unique_count");
            stats.setUniqueContentsSeen(uniqueContents);

            // 获取最后转盘时间
            WheelSpinRecord lastSpin = spinRecordMapper.selectOne(
                new LambdaQueryWrapper<WheelSpinRecord>()
                    .eq(WheelSpinRecord::getUserId, userId)
                    .orderByDesc(WheelSpinRecord::getSpinTime)
                    .last("LIMIT 1")
            );
            if (lastSpin != null && lastSpin.getSpinTime() != null) {
                stats.setLastSpinTime(lastSpin.getSpinTime().toString());
            } else {
                stats.setLastSpinTime(null);
            }

            log.debug("获取用户统计完成: userId={}, totalSpins={}", userId, totalSpins);
            return stats;
        } catch (Exception e) {
            log.error("获取用户统计失败: userId={}, error={}", userId, e.getMessage(), e);
            return new UserStatistics();
        }
    }

    @Override
    public SystemOverview getSystemOverview() {
        try {
            SystemOverview overview = new SystemOverview();

            // 获取总用户数
            Long totalUsers = userMapper.selectCount(null);
            overview.setTotalUsers(totalUsers != null ? totalUsers : 0L);

            // 获取总情侣关系数
            Long totalCouples = coupleMapper.selectCount(null);
            overview.setTotalCouples(totalCouples != null ? totalCouples : 0L);

            // 获取总转盘次数
            Long totalSpins = spinRecordMapper.selectCount(null);
            overview.setTotalSpins(totalSpins != null ? totalSpins : 0L);

            // 获取总内容数
            Long totalContents = contentMapper.selectCount(
                new LambdaQueryWrapper<WheelContent>()
                    .eq(WheelContent::getAuditStatus, 1)
            );
            overview.setTotalContents(totalContents != null ? totalContents : 0L);

            // 获取今日新增用户数
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            Long todayNewUsers = userMapper.selectCount(
                new LambdaQueryWrapper<WheelUser>()
                    .ge(WheelUser::getCreateTime, todayStart)
            );
            overview.setTodayNewUsers(todayNewUsers != null ? todayNewUsers : 0L);

            // 获取今日新增情侣关系数
            Long todayNewCouples = coupleMapper.selectCount(
                new LambdaQueryWrapper<CoupleRelationship>()
                    .ge(CoupleRelationship::getCreateTime, todayStart)
            );
            overview.setTodayNewCouples(todayNewCouples != null ? todayNewCouples : 0L);

            // 获取今日转盘次数
            Long todaySpins = spinRecordMapper.selectCount(
                new LambdaQueryWrapper<WheelSpinRecord>()
                    .ge(WheelSpinRecord::getSpinTime, todayStart)
            );
            overview.setTodaySpins(todaySpins != null ? todaySpins : 0L);

            // 获取24小时活跃用户数
            LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
            Long activeUsers24h = spinRecordMapper.selectCount(
                new LambdaQueryWrapper<WheelSpinRecord>()
                    .ge(WheelSpinRecord::getSpinTime, yesterday)
            );
            overview.setActiveUsers24h(activeUsers24h != null ? activeUsers24h : 0L);

            // 获取平均每用户转盘次数
            if (totalUsers != null && totalUsers > 0 && totalSpins != null) {
                Double avgSpinsPerUser = (double) totalSpins / totalUsers;
                overview.setAvgSpinsPerUser(avgSpinsPerUser);
            } else {
                overview.setAvgSpinsPerUser(0.0);
            }

            // 获取情侣关系形成率
            if (totalUsers != null && totalUsers > 0 && totalCouples != null) {
                Double coupleFormationRate = ((double) totalCouples * 2 / totalUsers) * 100;
                overview.setCoupleFormationRate(coupleFormationRate);
            } else {
                overview.setCoupleFormationRate(0.0);
            }

            log.debug("获取系统概览统计完成: totalUsers={}, totalSpins={}", totalUsers, totalSpins);
            return overview;
        } catch (Exception e) {
            log.error("获取系统概览统计失败: error={}", e.getMessage(), e);
            return new SystemOverview();
        }
    }

    @Override
    public List<SpinTrend> getSpinTrend(LocalDate startDate, LocalDate endDate) {
        try {
            List<SpinTrend> trendList = new ArrayList<>();
            LocalDate current = startDate;

            while (!current.isAfter(endDate)) {
                LocalDateTime dayStart = current.atStartOfDay();
                LocalDateTime dayEnd = current.atTime(23, 59, 59);

                // 获取当天的转盘次数
                Long spinCount = spinRecordMapper.selectCount(
                    new LambdaQueryWrapper<WheelSpinRecord>()
                        .ge(WheelSpinRecord::getSpinTime, dayStart)
                        .le(WheelSpinRecord::getSpinTime, dayEnd)
                );

                // 获取当天的独立用户数
                QueryWrapper<WheelSpinRecord> userCountWrapper = new QueryWrapper<>();
                userCountWrapper.select("COUNT(DISTINCT user_id) as user_count")
                    .ge("spin_time", dayStart)
                    .le("spin_time", dayEnd);
                Map<String, Object> userCountResult = firstMap(spinRecordMapper.selectMaps(userCountWrapper));
                Long userCount = getLongValue(userCountResult, "user_count");

                // 获取当天的情侣转盘次数（设备ID不为空）
                Long coupleCount = spinRecordMapper.selectCount(
                    new LambdaQueryWrapper<WheelSpinRecord>()
                        .ge(WheelSpinRecord::getSpinTime, dayStart)
                        .le(WheelSpinRecord::getSpinTime, dayEnd)
                        .isNotNull(WheelSpinRecord::getDeviceId)
                );

                SpinTrend trend = new SpinTrend();
                trend.setDate(current.toString());
                trend.setSpinCount(spinCount != null ? spinCount : 0L);
                trend.setUserCount(userCount);
                trend.setCoupleCount(coupleCount != null ? coupleCount : 0L);

                trendList.add(trend);
                current = current.plusDays(1);
            }

            log.debug("获取转盘使用趋势: startDate={}, endDate={}, count={}", startDate, endDate, trendList.size());
            return trendList;
        } catch (Exception e) {
            log.error("获取转盘使用趋势失败: error={}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<PopularContent> getPopularContent(Integer limit) {
        try {
            List<PopularContent> contentList = new ArrayList<>();

            // 查询热门内容（按转盘次数排序）
            QueryWrapper<WheelSpinRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("content_id, result_text, category_id, COUNT(*) as spin_count")
                .isNotNull("content_id")
                .groupBy("content_id")
                .orderByDesc("spin_count")
                .last("LIMIT " + limit);

            List<Map<String, Object>> results = spinRecordMapper.selectMaps(queryWrapper);

            for (Map<String, Object> result : results) {
                Long contentId = getLongValue(result, "content_id");
                String contentText = (String) result.get("result_text");
                Long spinCount = getLongValue(result, "spin_count");
                Long categoryId = getLongValue(result, "category_id");

                // 获取分类名称
                WheelCategory category = categoryMapper.selectById(categoryId);
                String categoryName = category != null ? category.getCategoryName() : "未知分类";

                // 计算命中率（这里简化为转盘次数占比）
                Long totalSpins = spinRecordMapper.selectCount(null);
                Double winRate = totalSpins != null && totalSpins > 0
                    ? ((double) spinCount / totalSpins) * 100
                    : 0.0;

                PopularContent content = new PopularContent();
                content.setContentId(contentId);
                content.setContentText(contentText);
                content.setCategoryName(categoryName);
                content.setSpinCount(spinCount);
                content.setWinRate(winRate);

                contentList.add(content);
            }

            log.debug("获取热门内容排行完成: limit={}, count={}", limit, contentList.size());
            return contentList;
        } catch (Exception e) {
            log.error("获取热门内容排行失败: error={}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<CategoryStatistics> getCategoryStatistics() {
        try {
            List<CategoryStatistics> statsList = new ArrayList<>();

            // 获取所有分类
            List<WheelCategory> categories = categoryMapper.selectList(null);

            for (WheelCategory category : categories) {
                Long categoryId = category.getId();

                // 获取该分类下的内容数量
                Long contentCount = contentMapper.selectCount(
                    new LambdaQueryWrapper<WheelContent>()
                        .eq(WheelContent::getCategoryId, categoryId)
                        .eq(WheelContent::getAuditStatus, 1)
                );

                // 获取该分类的总转盘次数
                Long totalSpins = spinRecordMapper.selectCount(
                    new LambdaQueryWrapper<WheelSpinRecord>()
                        .eq(WheelSpinRecord::getCategoryId, categoryId)
                );

                // 获取该分类的平均权重
                QueryWrapper<WheelContent> avgWeightWrapper = new QueryWrapper<>();
                avgWeightWrapper.select("AVG(weight) as avg_weight")
                    .eq("category_id", categoryId)
                    .eq("audit_status", 1);
                Map<String, Object> avgWeightResult = firstMap(contentMapper.selectMaps(avgWeightWrapper));
                Double avgWeight = getDoubleValue(avgWeightResult, "avg_weight");

                // 获取该分类的独立用户数
                QueryWrapper<WheelSpinRecord> uniqueUsersWrapper = new QueryWrapper<>();
                uniqueUsersWrapper.select("COUNT(DISTINCT user_id) as user_count")
                    .eq("category_id", categoryId);
                Map<String, Object> uniqueUsersResult = firstMap(spinRecordMapper.selectMaps(uniqueUsersWrapper));
                Long uniqueUsers = getLongValue(uniqueUsersResult, "user_count");

                CategoryStatistics stats = new CategoryStatistics();
                stats.setCategoryId(categoryId);
                stats.setCategoryName(category.getCategoryName());
                stats.setContentCount(contentCount != null ? contentCount : 0L);
                stats.setTotalSpins(totalSpins != null ? totalSpins : 0L);
                stats.setAvgWeight(avgWeight);
                stats.setUniqueUsers(uniqueUsers);

                statsList.add(stats);
            }

            log.debug("获取分类使用统计完成: count={}", statsList.size());
            return statsList;
        } catch (Exception e) {
            log.error("获取分类使用统计失败: error={}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public CoupleStatistics getCoupleStatistics() {
        try {
            CoupleStatistics stats = new CoupleStatistics();

            // 获取总情侣关系数
            Long totalCouples = coupleMapper.selectCount(null);
            stats.setTotalCouples(totalCouples != null ? totalCouples : 0L);

            // 获取活跃情侣关系数（近30天有互动的）
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            Long activeCouples = coupleMapper.selectCount(
                new LambdaQueryWrapper<CoupleRelationship>()
                    .ge(CoupleRelationship::getUpdateTime, thirtyDaysAgo)
            );
            stats.setActiveCouples(activeCouples != null ? activeCouples : 0L);

            // 计算平均情侣关系持续天数
            List<CoupleRelationship> couples = coupleMapper.selectList(
                new LambdaQueryWrapper<CoupleRelationship>()
                    .isNotNull(CoupleRelationship::getCreateTime)
            );

            long totalDays = 0;
            long validCouples = 0;
            for (CoupleRelationship couple : couples) {
                if (couple.getCreateTime() != null) {
                    long days = ChronoUnit.DAYS.between(couple.getCreateTime(), LocalDateTime.now());
                    totalDays += days;
                    validCouples++;
                }
            }

            Long avgCoupleAge = validCouples > 0 ? totalDays / validCouples : 0L;
            stats.setAvgCoupleAge(avgCoupleAge);

            // 获取情侣关系形成率
            Long totalUsers = userMapper.selectCount(null);
            Double formationRate = totalUsers != null && totalUsers > 0 && totalCouples != null
                ? ((double) totalCouples * 2 / totalUsers) * 100
                : 0.0;
            stats.setFormationRate(formationRate);

            // 获取最活跃情侣的转盘次数
            QueryWrapper<WheelSpinRecord> mostActiveCoupleWrapper = new QueryWrapper<>();
            mostActiveCoupleWrapper.select("device_id, COUNT(*) as couple_spins")
                .isNotNull("device_id")
                .groupBy("device_id")
                .orderByDesc("couple_spins")
                .last("LIMIT 1");

            Map<String, Object> mostActiveResult = firstMap(spinRecordMapper.selectMaps(mostActiveCoupleWrapper));
            Long mostActiveCoupleSpins = getLongValue(mostActiveResult, "couple_spins");
            stats.setMostActiveCoupleSpins(mostActiveCoupleSpins);

            // 计算7天留存率
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            Long couples7DaysAgo = coupleMapper.selectCount(
                new LambdaQueryWrapper<CoupleRelationship>()
                    .ge(CoupleRelationship::getCreateTime, sevenDaysAgo)
            );
            Long activeCouples7d = coupleMapper.selectCount(
                new LambdaQueryWrapper<CoupleRelationship>()
                    .ge(CoupleRelationship::getCreateTime, sevenDaysAgo)
                    .ge(CoupleRelationship::getUpdateTime, sevenDaysAgo)
            );
            Long retentionRate7d = couples7DaysAgo != null && couples7DaysAgo > 0
                ? (activeCouples7d != null ? (activeCouples7d * 100 / couples7DaysAgo) : 0L)
                : 0L;
            stats.setCoupleRetentionRate7d(retentionRate7d);

            // 计算30天留存率
            Long couples30DaysAgo = coupleMapper.selectCount(
                new LambdaQueryWrapper<CoupleRelationship>()
                    .ge(CoupleRelationship::getCreateTime, thirtyDaysAgo)
            );
            Long activeCouples30d = coupleMapper.selectCount(
                new LambdaQueryWrapper<CoupleRelationship>()
                    .ge(CoupleRelationship::getCreateTime, thirtyDaysAgo)
                    .ge(CoupleRelationship::getUpdateTime, thirtyDaysAgo)
            );
            Long retentionRate30d = couples30DaysAgo != null && couples30DaysAgo > 0
                ? (activeCouples30d != null ? (activeCouples30d * 100 / couples30DaysAgo) : 0L)
                : 0L;
            stats.setCoupleRetentionRate30d(retentionRate30d);

            log.debug("获取情侣关系统计完成: totalCouples={}", totalCouples);
            return stats;
        } catch (Exception e) {
            log.error("获取情侣关系统计失败: error={}", e.getMessage(), e);
            return new CoupleStatistics();
        }
    }

    private Map<String, Object> firstMap(List<Map<String, Object>> maps) {
        return maps != null && !maps.isEmpty() ? maps.get(0) : null;
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Number number = getNumber(map, key);
        return number != null ? number.longValue() : 0L;
    }

    private Double getDoubleValue(Map<String, Object> map, String key) {
        Number number = getNumber(map, key);
        return number != null ? number.doubleValue() : 0.0;
    }

    private Number getNumber(Map<String, Object> map, String key) {
        if (map == null) {
            return null;
        }
        Object value = map.get(key);
        if (value instanceof Number) {
            return (Number) value;
        }
        if (value instanceof String && !((String) value).isBlank()) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
