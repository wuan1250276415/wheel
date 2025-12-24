package com.basebackend.wheel.engine;

import com.basebackend.wheel.entity.WheelContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

/**
 * 转盘核心算法引擎
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Slf4j
@Component
public class WheelSpinEngine {

    private final Random random = new Random();

    /**
     * 基于权重的随机选择算法
     *
     * @param contents    可选内容列表
     * @param currentTime 当前时间（用于时间敏感过滤）
     * @return 选中的内容
     */
    public WheelContent selectByWeight(List<WheelContent> contents, LocalDateTime currentTime) {
        return selectByWeightInternal(contents, currentTime, random);
    }

    /**
     * 基于权重的随机选择算法（可复现）
     *
     * @param contents    可选内容列表
     * @param currentTime 当前时间（用于时间敏感过滤）
     * @param seed        随机种子
     * @return 选中的内容
     */
    public WheelContent selectByWeight(List<WheelContent> contents, LocalDateTime currentTime, long seed) {
        return selectByWeightInternal(contents, currentTime, new Random(seed));
    }

    /**
     * 时间敏感内容过滤
     *
     * @param contents    内容列表
     * @param currentTime 当前时间
     * @return 过滤后的内容列表
     */
    private List<WheelContent> filterByTime(List<WheelContent> contents, LocalDateTime currentTime) {
        return contents.stream()
                .filter(content -> isContentAvailableAtTime(content, currentTime))
                .toList();
    }

    /**
     * 检查内容在指定时间是否可用
     *
     * @param content     内容
     * @param currentTime 当前时间
     * @return 是否可用
     */
    private boolean isContentAvailableAtTime(WheelContent content, LocalDateTime currentTime) {
        // TODO: 从content的tags字段中解析时间规则
        // 目前简化处理，所有内容都可用
        // 实际实现可以包括：
        // - 工作日/周末限定
        // - 特定时间段限定
        // - 节假日限定
        return true;
    }

    /**
     * 计算转盘旋转角度
     *
     * @param selectedContent 选中的内容
     * @param allContents     所有可选内容列表
     * @param currentRotation 当前旋转角度
     * @return 目标旋转角度
     */
    public double calculateRotationAngle(WheelContent selectedContent, List<WheelContent> allContents,
            double currentRotation) {
        if (allContents == null || allContents.isEmpty()) {
            throw new IllegalArgumentException("内容列表不能为空");
        }

        // 1. 计算总权重
        double totalWeight = allContents.stream()
                .mapToDouble(c -> c.getWeight() != null ? c.getWeight() : 1.0)
                .sum();

        // 2. 找到选中项的索引并计算它之前的累积角度
        double cumulativeAngle = 0;
        double selectedSliceAngle = 0;
        boolean found = false;

        for (WheelContent content : allContents) {
            double sliceAngle = ((content.getWeight() != null ? content.getWeight() : 1.0) / totalWeight) * 360.0;
            if (content.getId().equals(selectedContent.getId())) {
                selectedSliceAngle = sliceAngle;
                found = true;
                break;
            }
            cumulativeAngle += sliceAngle;
        }

        if (!found) {
            log.warn("未在列表中找到选中内容: id={}", selectedContent.getId());
            // 兜底：假设在第一个
            selectedSliceAngle = ((allContents.get(0).getWeight() != null ? allContents.get(0).getWeight() : 1.0)
                    / totalWeight) * 360.0;
        }

        // 3. 计算选中项中心对应的基础角度 (从起始位置顺时针计算)
        double baseAngle = cumulativeAngle + selectedSliceAngle / 2.0;

        // 4. 计算目标角度
        // 指针在顶部 (12点方向，对应 -90度)
        // 转盘绘制时第一个扇形从 -90度开始顺时针绘制
        // 如果转盘旋转了 R 度，则原本在 A 度的点现在在 A + R 度
        // 我们希望 A + R = -90 (或者 A + R = 270)
        // 这里的 A 是相对于起始位置的角度，即 baseAngle - 90
        // 所以 (baseAngle - 90) + R = -90 => R = -baseAngle
        // 为了顺时针旋转，R = 360 - baseAngle

        double spins = 6 + random.nextInt(3); // 随机转 6-8 圈，增加仪式感
        double targetAngle = currentRotation + spins * 360 + (360.0 - baseAngle % 360.0);

        log.debug("旋转角度计算: selectedContentId={}, baseAngle={}, targetAngle={}",
                selectedContent.getId(), baseAngle, targetAngle);

        return targetAngle;
    }

    /**
     * 判断是否是"中奖"结果
     *
     * @param content 选中的内容
     * @return 是否中奖
     */
    public boolean isWinningResult(WheelContent content) {
        // 简单的中奖判断：权重大于1.5的选项算中奖
        return getSafeWeight(content) > 1.5;
    }

    /**
     * 获取转盘结果的置信度
     *
     * @param content     选中的内容
     * @param allContents 所有可选内容
     * @return 置信度（0-1之间）
     */
    public double getResultConfidence(WheelContent content, List<WheelContent> allContents) {
        if (allContents == null || allContents.isEmpty()) {
            return 1.0;
        }

        double totalWeight = allContents.stream()
                .mapToDouble(this::getSafeWeight)
                .sum();

        double contentWeight = getSafeWeight(content);

        // 置信度 = 内容权重 / 总权重
        return Math.min(1.0, contentWeight / totalWeight);
    }

    /**
     * 生成转盘种子（用于复现结果）
     *
     * @param userId      用户ID
     * @param requestTime 请求时间
     * @param categoryIds 分类ID列表
     * @return 随机种子
     */
    public long generateSpinSeed(Long userId, LocalDateTime requestTime, List<Long> categoryIds) {
        long seed = userId.hashCode();
        seed = seed * 31 + requestTime.hashCode();
        if (categoryIds != null) {
            for (Long categoryId : categoryIds) {
                seed = seed * 31 + categoryId.hashCode();
            }
        }
        return Math.abs(seed);
    }

    /**
     * 验证转盘结果的一致性
     *
     * @param seed            随机种子
     * @param selectedContent 选中的内容
     * @param allContents     所有可选内容
     * @param requestTime     请求时间
     * @return 是否一致
     */
    public boolean verifyResultConsistency(long seed, WheelContent selectedContent,
            List<WheelContent> allContents,
            LocalDateTime requestTime) {
        try {
            WheelContent expected = selectByWeightInternal(allContents, requestTime, new Random(seed));
            boolean consistent = expected.getId().equals(selectedContent.getId());
            log.debug("结果一致性验证: expectedId={}, actualId={}, consistent={}",
                    expected.getId(), selectedContent.getId(), consistent);
            return consistent;
        } catch (Exception e) {
            log.error("结果一致性验证失败: {}", e.getMessage(), e);
            return false;
        }
    }

    private WheelContent selectByWeightInternal(List<WheelContent> contents, LocalDateTime currentTime, Random random) {
        if (contents == null || contents.isEmpty()) {
            throw new IllegalArgumentException("内容列表不能为空");
        }

        List<WheelContent> availableContents = filterByTime(contents, currentTime);
        if (availableContents.isEmpty()) {
            log.warn("没有符合时间条件的可用内容，使用原始列表");
            availableContents = contents;
        }

        double totalWeight = availableContents.stream()
                .mapToDouble(this::getSafeWeight)
                .sum();

        if (totalWeight <= 0) {
            log.warn("权重总和无效，使用默认权重");
            return availableContents.get(0);
        }

        double randomValue = random.nextDouble() * totalWeight;
        double cumulativeWeight = 0.0;

        for (WheelContent content : availableContents) {
            double weight = getSafeWeight(content);
            cumulativeWeight += weight;
            if (randomValue <= cumulativeWeight) {
                log.debug("权重选择结果: contentId={}, weight={}, randomValue={}",
                        content.getId(), weight, randomValue);
                return content;
            }
        }

        log.warn("权重选择兜底返回第一个内容");
        return availableContents.get(0);
    }

    private double getSafeWeight(WheelContent content) {
        if (content == null || content.getWeight() == null) {
            return 1.0;
        }
        return content.getWeight();
    }
}
