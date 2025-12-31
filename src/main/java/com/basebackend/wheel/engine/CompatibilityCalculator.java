package com.basebackend.wheel.engine;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.basebackend.wheel.dto.CompatibilityResult;
import com.basebackend.wheel.entity.ChatMessage;
import com.basebackend.wheel.entity.CoupleRelationship;
import com.basebackend.wheel.entity.WheelSpinRecord;
import com.basebackend.wheel.mapper.ChatMessageMapper;
import com.basebackend.wheel.mapper.CoupleRelationshipMapper;
import com.basebackend.wheel.mapper.WheelSpinRecordMapper;
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
 * 默契度计算组件
 * 基于互动数据计算情侣默契度评分
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompatibilityCalculator {

    private final CoupleRelationshipMapper coupleRelationshipMapper;
    private final WheelSpinRecordMapper wheelSpinRecordMapper;
    private final ChatMessageMapper chatMessageMapper;

    // 权重常量
    public static final double SPIN_FREQUENCY_WEIGHT = 0.25;
    public static final double CATEGORY_OVERLAP_WEIGHT = 0.25;
    public static final double ACTIVE_TIME_OVERLAP_WEIGHT = 0.30;
    public static final double CHAT_RESPONSE_WEIGHT = 0.20;

    // 评分描述映射
    private static final Map<Integer, String> SCORE_DESCRIPTIONS = new LinkedHashMap<>();
    
    static {
        SCORE_DESCRIPTIONS.put(90, "心有灵犀");
        SCORE_DESCRIPTIONS.put(80, "默契十足");
        SCORE_DESCRIPTIONS.put(70, "配合默契");
        SCORE_DESCRIPTIONS.put(60, "渐入佳境");
        SCORE_DESCRIPTIONS.put(50, "相互了解");
        SCORE_DESCRIPTIONS.put(40, "初步磨合");
        SCORE_DESCRIPTIONS.put(0, "继续加油");
    }

    /**
     * 计算总体默契度评分
     *
     * @param coupleId  情侣关系ID
     * @param startDate 统计开始日期
     * @param endDate   统计结束日期
     * @return 默契度计算结果
     */
    public CompatibilityResult calculate(Long coupleId, LocalDate startDate, LocalDate endDate) {
        log.info("开始计算默契度: coupleId={}, startDate={}, endDate={}", coupleId, startDate, endDate);

        // 获取情侣关系信息
        CoupleRelationship couple = coupleRelationshipMapper.selectById(coupleId);
        if (couple == null) {
            log.warn("情侣关系不存在: coupleId={}", coupleId);
            return createEmptyResult();
        }

        Long userId1 = couple.getUserId1();
        Long userId2 = couple.getUserId2();

        CompatibilityResult result = new CompatibilityResult();

        // 计算各因子评分
        double spinFrequencyScore = calculateSpinFrequencySimilarity(userId1, userId2, startDate, endDate);
        double categoryOverlapScore = calculateCategoryOverlap(userId1, userId2, startDate, endDate);
        double activeTimeOverlapScore = calculateActiveTimeOverlap(userId1, userId2, startDate, endDate);
        double chatResponseScore = calculateChatResponseScore(coupleId, startDate, endDate);

        result.setSpinFrequencyScore(spinFrequencyScore);
        result.setCategoryOverlapScore(categoryOverlapScore);
        result.setActiveTimeOverlapScore(activeTimeOverlapScore);
        result.setChatResponseScore(chatResponseScore);

        // 计算加权总分
        double weightedSum = spinFrequencyScore * SPIN_FREQUENCY_WEIGHT
                + categoryOverlapScore * CATEGORY_OVERLAP_WEIGHT
                + activeTimeOverlapScore * ACTIVE_TIME_OVERLAP_WEIGHT
                + chatResponseScore * CHAT_RESPONSE_WEIGHT;

        int totalScore = (int) Math.round(weightedSum);
        // 确保评分在0-100范围内
        totalScore = Math.max(0, Math.min(100, totalScore));

        result.setTotalScore(totalScore);
        result.setDescription(getScoreDescription(totalScore));

        log.info("默契度计算完成: coupleId={}, totalScore={}", coupleId, totalScore);
        return result;
    }

    /**
     * 计算转盘频率相似度 (权重: 25%)
     * 基于两人转盘使用频率的相似程度
     *
     * @param userId1   用户1 ID
     * @param userId2   用户2 ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 相似度评分(0-100)
     */
    public double calculateSpinFrequencySimilarity(Long userId1, Long userId2,
                                                    LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 查询用户1的转盘次数
        long user1Spins = countUserSpins(userId1, startDateTime, endDateTime);
        // 查询用户2的转盘次数
        long user2Spins = countUserSpins(userId2, startDateTime, endDateTime);

        // 如果两人都没有转盘记录，返回基础分
        if (user1Spins == 0 && user2Spins == 0) {
            return 50.0;
        }

        // 计算相似度：使用较小值/较大值的比例
        long maxSpins = Math.max(user1Spins, user2Spins);
        long minSpins = Math.min(user1Spins, user2Spins);

        // 相似度 = (较小值 / 较大值) * 100
        double similarity = (double) minSpins / maxSpins * 100;

        return Math.round(similarity * 100) / 100.0;
    }

    /**
     * 计算分类偏好重叠度 (权重: 25%)
     * 基于两人使用的分类重叠程度
     *
     * @param userId1   用户1 ID
     * @param userId2   用户2 ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 重叠度评分(0-100)
     */
    public double calculateCategoryOverlap(Long userId1, Long userId2,
                                            LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 获取用户1使用的分类集合
        Set<Long> user1Categories = getUserCategories(userId1, startDateTime, endDateTime);
        // 获取用户2使用的分类集合
        Set<Long> user2Categories = getUserCategories(userId2, startDateTime, endDateTime);

        // 如果两人都没有使用任何分类，返回基础分
        if (user1Categories.isEmpty() && user2Categories.isEmpty()) {
            return 50.0;
        }

        // 如果只有一人有数据，返回较低分
        if (user1Categories.isEmpty() || user2Categories.isEmpty()) {
            return 30.0;
        }

        // 计算交集
        Set<Long> intersection = new HashSet<>(user1Categories);
        intersection.retainAll(user2Categories);

        // 计算并集
        Set<Long> union = new HashSet<>(user1Categories);
        union.addAll(user2Categories);

        // Jaccard相似度 = 交集大小 / 并集大小
        double jaccardSimilarity = (double) intersection.size() / union.size();

        return Math.round(jaccardSimilarity * 100 * 100) / 100.0;
    }

    /**
     * 计算活跃时间重叠度 (权重: 30%)
     * 基于两人活跃时间段的重叠程度
     *
     * @param userId1   用户1 ID
     * @param userId2   用户2 ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 重叠度评分(0-100)
     */
    public double calculateActiveTimeOverlap(Long userId1, Long userId2,
                                              LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 获取用户1的活跃时间分布
        Map<Integer, Long> user1Distribution = getUserActiveTimeDistribution(userId1, startDateTime, endDateTime);
        // 获取用户2的活跃时间分布
        Map<Integer, Long> user2Distribution = getUserActiveTimeDistribution(userId2, startDateTime, endDateTime);

        // 如果两人都没有活动记录，返回基础分
        long user1Total = user1Distribution.values().stream().mapToLong(Long::longValue).sum();
        long user2Total = user2Distribution.values().stream().mapToLong(Long::longValue).sum();

        if (user1Total == 0 && user2Total == 0) {
            return 50.0;
        }

        // 如果只有一人有数据，返回较低分
        if (user1Total == 0 || user2Total == 0) {
            return 30.0;
        }

        // 计算余弦相似度
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int hour = 0; hour < 24; hour++) {
            long count1 = user1Distribution.getOrDefault(hour, 0L);
            long count2 = user2Distribution.getOrDefault(hour, 0L);

            dotProduct += count1 * count2;
            norm1 += count1 * count1;
            norm2 += count2 * count2;
        }

        if (norm1 == 0 || norm2 == 0) {
            return 50.0;
        }

        double cosineSimilarity = dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));

        return Math.round(cosineSimilarity * 100 * 100) / 100.0;
    }

    /**
     * 计算聊天响应速度评分 (权重: 20%)
     * 基于聊天消息的响应时间
     *
     * @param coupleId  情侣关系ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 响应速度评分(0-100)
     */
    public double calculateChatResponseScore(Long coupleId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 查询时间范围内的聊天消息
        LambdaQueryWrapper<ChatMessage> query = new LambdaQueryWrapper<>();
        query.eq(ChatMessage::getCoupleId, coupleId)
                .ge(ChatMessage::getCreateTime, startDateTime)
                .le(ChatMessage::getCreateTime, endDateTime)
                .eq(ChatMessage::getStatus, 1)
                .orderByAsc(ChatMessage::getCreateTime);

        List<ChatMessage> messages = chatMessageMapper.selectList(query);

        // 如果消息数量不足，返回基础分
        if (messages.size() < 2) {
            return 50.0;
        }

        // 计算平均响应时间
        List<Long> responseTimes = new ArrayList<>();
        ChatMessage previousMessage = null;

        for (ChatMessage message : messages) {
            if (previousMessage != null && !previousMessage.getSenderId().equals(message.getSenderId())) {
                // 这是一条回复消息，计算响应时间
                long responseTime = ChronoUnit.SECONDS.between(
                        previousMessage.getCreateTime(),
                        message.getCreateTime()
                );
                // 只统计合理范围内的响应时间（1秒到24小时）
                if (responseTime > 0 && responseTime <= 86400) {
                    responseTimes.add(responseTime);
                }
            }
            previousMessage = message;
        }

        // 如果没有有效的响应时间数据，返回基础分
        if (responseTimes.isEmpty()) {
            return 50.0;
        }

        // 计算平均响应时间（秒）
        double avgResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(3600);

        // 将响应时间转换为评分
        // 响应时间越短，评分越高
        // 1分钟内 = 100分，1小时 = 70分，6小时 = 40分，24小时 = 10分
        double score;
        if (avgResponseTime <= 60) {
            score = 100;
        } else if (avgResponseTime <= 300) {
            // 1-5分钟：100-90分
            score = 100 - (avgResponseTime - 60) / 240 * 10;
        } else if (avgResponseTime <= 1800) {
            // 5-30分钟：90-80分
            score = 90 - (avgResponseTime - 300) / 1500 * 10;
        } else if (avgResponseTime <= 3600) {
            // 30分钟-1小时：80-70分
            score = 80 - (avgResponseTime - 1800) / 1800 * 10;
        } else if (avgResponseTime <= 21600) {
            // 1-6小时：70-40分
            score = 70 - (avgResponseTime - 3600) / 18000 * 30;
        } else {
            // 6-24小时：40-10分
            score = 40 - (avgResponseTime - 21600) / 64800 * 30;
        }

        return Math.max(10, Math.round(score * 100) / 100.0);
    }

    /**
     * 获取评分描述
     *
     * @param score 评分(0-100)
     * @return 评分描述
     */
    public String getScoreDescription(int score) {
        for (Map.Entry<Integer, String> entry : SCORE_DESCRIPTIONS.entrySet()) {
            if (score >= entry.getKey()) {
                return entry.getValue();
            }
        }
        return "继续加油";
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 统计用户转盘次数
     */
    private long countUserSpins(Long userId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        LambdaQueryWrapper<WheelSpinRecord> query = new LambdaQueryWrapper<>();
        query.eq(WheelSpinRecord::getUserId, userId)
                .ge(WheelSpinRecord::getSpinTime, startDateTime)
                .le(WheelSpinRecord::getSpinTime, endDateTime)
                .eq(WheelSpinRecord::getDeleted, false);
        return wheelSpinRecordMapper.selectCount(query);
    }

    /**
     * 获取用户使用的分类集合
     */
    private Set<Long> getUserCategories(Long userId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        LambdaQueryWrapper<WheelSpinRecord> query = new LambdaQueryWrapper<>();
        query.eq(WheelSpinRecord::getUserId, userId)
                .ge(WheelSpinRecord::getSpinTime, startDateTime)
                .le(WheelSpinRecord::getSpinTime, endDateTime)
                .eq(WheelSpinRecord::getDeleted, false);

        List<WheelSpinRecord> records = wheelSpinRecordMapper.selectList(query);
        return records.stream()
                .map(WheelSpinRecord::getCategoryId)
                .collect(Collectors.toSet());
    }

    /**
     * 获取用户活跃时间分布
     */
    private Map<Integer, Long> getUserActiveTimeDistribution(Long userId,
                                                              LocalDateTime startDateTime,
                                                              LocalDateTime endDateTime) {
        LambdaQueryWrapper<WheelSpinRecord> query = new LambdaQueryWrapper<>();
        query.eq(WheelSpinRecord::getUserId, userId)
                .ge(WheelSpinRecord::getSpinTime, startDateTime)
                .le(WheelSpinRecord::getSpinTime, endDateTime)
                .eq(WheelSpinRecord::getDeleted, false);

        List<WheelSpinRecord> records = wheelSpinRecordMapper.selectList(query);

        return records.stream()
                .collect(Collectors.groupingBy(
                        record -> record.getSpinTime().getHour(),
                        Collectors.counting()
                ));
    }

    /**
     * 创建空的默契度结果
     */
    private CompatibilityResult createEmptyResult() {
        CompatibilityResult result = new CompatibilityResult();
        result.setTotalScore(0);
        result.setSpinFrequencyScore(0);
        result.setCategoryOverlapScore(0);
        result.setActiveTimeOverlapScore(0);
        result.setChatResponseScore(0);
        result.setDescription(getScoreDescription(0));
        return result;
    }
}
