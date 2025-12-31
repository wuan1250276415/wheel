package com.basebackend.wheel.engine;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.basebackend.wheel.dto.ReportData;
import com.basebackend.wheel.entity.*;
import com.basebackend.wheel.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报告数据聚合与生成组件
 * 负责聚合转盘记录、聊天消息、动态分享等数据，生成报告内容
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportGenerator {

    private final CoupleRelationshipMapper coupleRelationshipMapper;
    private final WheelSpinRecordMapper wheelSpinRecordMapper;
    private final WheelCategoryMapper wheelCategoryMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final CoupleMomentMapper coupleMomentMapper;

    /**
     * 聚合报告数据
     *
     * @param coupleId  情侣关系ID
     * @param startDate 统计开始日期
     * @param endDate   统计结束日期
     * @return 报告数据
     */
    public ReportData aggregateData(Long coupleId, LocalDate startDate, LocalDate endDate) {
        log.info("开始聚合报告数据: coupleId={}, startDate={}, endDate={}", coupleId, startDate, endDate);

        // 获取情侣关系信息
        CoupleRelationship couple = coupleRelationshipMapper.selectById(coupleId);
        if (couple == null) {
            log.warn("情侣关系不存在: coupleId={}", coupleId);
            return null;
        }

        ReportData reportData = new ReportData();

        // 1. 计算认识天数
        reportData.setDaysTogether(calculateDaysTogether(couple));

        // 2. 统计转盘数据
        SpinStatistics spinStats = calculateSpinStats(coupleId, couple.getUserId1(), couple.getUserId2(), startDate, endDate);
        reportData.setTotalSpins(spinStats.getTotalSpins());
        reportData.setUser1Spins(spinStats.getUser1Spins());
        reportData.setUser2Spins(spinStats.getUser2Spins());

        // 3. 计算活跃时间分布
        Map<Integer, Long> activeTimeDistribution = calculateActiveTimeDistribution(
                couple.getUserId1(), couple.getUserId2(), startDate, endDate);
        reportData.setActiveTimeDistribution(activeTimeDistribution);

        // 4. 获取最常用分类
        CategoryInfo categoryInfo = getMostUsedCategory(couple.getUserId1(), couple.getUserId2(), startDate, endDate);
        if (categoryInfo != null) {
            reportData.setFavoriteCategory(categoryInfo.getCategoryName());
            reportData.setFavoriteCategoryCount(categoryInfo.getCount());
        }

        // 5. 统计聊天消息数
        reportData.setChatMessageCount(countChatMessages(coupleId, startDate, endDate));

        // 6. 统计动态数量
        reportData.setMomentCount(countMoments(coupleId, startDate, endDate));

        log.info("报告数据聚合完成: coupleId={}", coupleId);
        return reportData;
    }

    /**
     * 计算认识天数
     *
     * @param couple 情侣关系
     * @return 认识天数
     */
    public long calculateDaysTogether(CoupleRelationship couple) {
        if (couple == null || couple.getConfirmedAt() == null) {
            return 0L;
        }
        LocalDate confirmedDate = couple.getConfirmedAt().toLocalDate();
        LocalDate today = LocalDate.now();
        return ChronoUnit.DAYS.between(confirmedDate, today);
    }

    /**
     * 统计转盘数据
     *
     * @param coupleId  情侣关系ID
     * @param userId1   用户1 ID
     * @param userId2   用户2 ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 转盘统计结果
     */
    public SpinStatistics calculateSpinStats(Long coupleId, Long userId1, Long userId2,
                                              LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 查询用户1的转盘记录
        LambdaQueryWrapper<WheelSpinRecord> query1 = new LambdaQueryWrapper<>();
        query1.eq(WheelSpinRecord::getUserId, userId1)
                .ge(WheelSpinRecord::getSpinTime, startDateTime)
                .le(WheelSpinRecord::getSpinTime, endDateTime)
                .eq(WheelSpinRecord::getDeleted, false);
        long user1Spins = wheelSpinRecordMapper.selectCount(query1);

        // 查询用户2的转盘记录
        LambdaQueryWrapper<WheelSpinRecord> query2 = new LambdaQueryWrapper<>();
        query2.eq(WheelSpinRecord::getUserId, userId2)
                .ge(WheelSpinRecord::getSpinTime, startDateTime)
                .le(WheelSpinRecord::getSpinTime, endDateTime)
                .eq(WheelSpinRecord::getDeleted, false);
        long user2Spins = wheelSpinRecordMapper.selectCount(query2);

        SpinStatistics stats = new SpinStatistics();
        stats.setUser1Spins(user1Spins);
        stats.setUser2Spins(user2Spins);
        stats.setTotalSpins(user1Spins + user2Spins);

        return stats;
    }

    /**
     * 计算活跃时间分布
     *
     * @param userId1   用户1 ID
     * @param userId2   用户2 ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 小时->次数的映射
     */
    public Map<Integer, Long> calculateActiveTimeDistribution(Long userId1, Long userId2,
                                                               LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 查询两个用户的所有转盘记录
        LambdaQueryWrapper<WheelSpinRecord> query = new LambdaQueryWrapper<>();
        query.in(WheelSpinRecord::getUserId, Arrays.asList(userId1, userId2))
                .ge(WheelSpinRecord::getSpinTime, startDateTime)
                .le(WheelSpinRecord::getSpinTime, endDateTime)
                .eq(WheelSpinRecord::getDeleted, false);

        List<WheelSpinRecord> records = wheelSpinRecordMapper.selectList(query);

        // 按小时统计
        Map<Integer, Long> distribution = records.stream()
                .collect(Collectors.groupingBy(
                        record -> record.getSpinTime().getHour(),
                        Collectors.counting()
                ));

        // 确保所有小时都有值（0-23）
        for (int hour = 0; hour < 24; hour++) {
            distribution.putIfAbsent(hour, 0L);
        }

        return new TreeMap<>(distribution);
    }

    /**
     * 获取最常用分类
     *
     * @param userId1   用户1 ID
     * @param userId2   用户2 ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 最常用分类信息
     */
    public CategoryInfo getMostUsedCategory(Long userId1, Long userId2,
                                             LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 查询两个用户的所有转盘记录
        LambdaQueryWrapper<WheelSpinRecord> query = new LambdaQueryWrapper<>();
        query.in(WheelSpinRecord::getUserId, Arrays.asList(userId1, userId2))
                .ge(WheelSpinRecord::getSpinTime, startDateTime)
                .le(WheelSpinRecord::getSpinTime, endDateTime)
                .eq(WheelSpinRecord::getDeleted, false);

        List<WheelSpinRecord> records = wheelSpinRecordMapper.selectList(query);

        if (records.isEmpty()) {
            return null;
        }

        // 按分类ID统计次数
        Map<Long, Long> categoryCountMap = records.stream()
                .collect(Collectors.groupingBy(
                        WheelSpinRecord::getCategoryId,
                        Collectors.counting()
                ));

        // 找出使用次数最多的分类
        Map.Entry<Long, Long> maxEntry = categoryCountMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        if (maxEntry == null) {
            return null;
        }

        // 获取分类名称
        WheelCategory category = wheelCategoryMapper.selectById(maxEntry.getKey());
        if (category == null) {
            return null;
        }

        CategoryInfo info = new CategoryInfo();
        info.setCategoryId(maxEntry.getKey());
        info.setCategoryName(category.getCategoryName());
        info.setCount(maxEntry.getValue());

        return info;
    }

    /**
     * 统计聊天消息数
     *
     * @param coupleId  情侣关系ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 消息数量
     */
    public long countChatMessages(Long coupleId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        LambdaQueryWrapper<ChatMessage> query = new LambdaQueryWrapper<>();
        query.eq(ChatMessage::getCoupleId, coupleId)
                .ge(ChatMessage::getCreateTime, startDateTime)
                .le(ChatMessage::getCreateTime, endDateTime)
                .eq(ChatMessage::getStatus, 1); // 只统计正常状态的消息

        return chatMessageMapper.selectCount(query);
    }

    /**
     * 统计动态数量
     *
     * @param coupleId  情侣关系ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 动态数量
     */
    public long countMoments(Long coupleId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        LambdaQueryWrapper<CoupleMoment> query = new LambdaQueryWrapper<>();
        query.eq(CoupleMoment::getCoupleId, coupleId)
                .ge(CoupleMoment::getCreateTime, startDateTime)
                .le(CoupleMoment::getCreateTime, endDateTime)
                .eq(CoupleMoment::getDeleted, false);

        return coupleMomentMapper.selectCount(query);
    }

    /**
     * 转盘统计结果内部类
     */
    @lombok.Data
    public static class SpinStatistics {
        private Long totalSpins;
        private Long user1Spins;
        private Long user2Spins;
    }

    /**
     * 分类信息内部类
     */
    @lombok.Data
    public static class CategoryInfo {
        private Long categoryId;
        private String categoryName;
        private Long count;
    }
}
