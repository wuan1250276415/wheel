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
     * @param contents 可选内容列表
     * @param currentTime 当前时间（用于时间敏感过滤）
     * @return 选中的内容
     */
    public WheelContent selectByWeight(List<WheelContent> contents, LocalDateTime currentTime) {
        if (contents == null || contents.isEmpty()) {
            throw new IllegalArgumentException("内容列表不能为空");
        }

        // 1. 时间敏感过滤
        List<WheelContent> availableContents = filterByTime(contents, currentTime);
        if (availableContents.isEmpty()) {
            log.warn("没有符合时间条件的可用内容，使用原始列表");
            availableContents = contents;
        }

        // 2. 计算权重总和
        double totalWeight = availableContents.stream()
                .mapToDouble(WheelContent::getWeight)
                .sum();

        if (totalWeight <= 0) {
            log.warn("权重总和无效，使用默认权重");
            return availableContents.get(0);
        }

        // 3. 加权随机选择
        double randomValue = random.nextDouble() * totalWeight;
        double cumulativeWeight = 0.0;

        for (WheelContent content : availableContents) {
            cumulativeWeight += content.getWeight();
            if (randomValue <= cumulativeWeight) {
                log.debug("权重选择结果: contentId={}, weight={}, randomValue={}",
                        content.getId(), content.getWeight(), randomValue);
                return content;
            }
        }

        // 兜底返回第一个
        log.warn("权重选择兜底返回第一个内容");
        return availableContents.get(0);
    }

    /**
     * 时间敏感内容过滤
     *
     * @param contents 内容列表
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
     * @param content 内容
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
     * @param totalOptions 总选项数
     * @param currentRotation 当前旋转角度
     * @return 目标旋转角度
     */
    public double calculateRotationAngle(WheelContent selectedContent, int totalOptions, double currentRotation) {
        if (totalOptions <= 0) {
            throw new IllegalArgumentException("选项总数必须大于0");
        }

        // 计算每个选项占据的角度
        double anglePerOption = 360.0 / totalOptions;

        // 随机选择该内容在转盘上的位置
        int randomIndex = random.nextInt(totalOptions);
        double baseAngle = anglePerOption * randomIndex + anglePerOption / 2;

        // 计算目标角度（确保顺时针旋转）
        double spins = 5 + random.nextInt(3); // 随机转5-7圈
        double targetAngle = currentRotation + spins * 360 + (360 - baseAngle);

        log.debug("旋转角度计算: selectedContentId={}, totalOptions={}, baseAngle={}, targetAngle={}",
                selectedContent.getId(), totalOptions, baseAngle, targetAngle);

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
        return content.getWeight() > 1.5;
    }

    /**
     * 获取转盘结果的置信度
     *
     * @param content 选中的内容
     * @param allContents 所有可选内容
     * @return 置信度（0-1之间）
     */
    public double getResultConfidence(WheelContent content, List<WheelContent> allContents) {
        if (allContents == null || allContents.isEmpty()) {
            return 1.0;
        }

        double totalWeight = allContents.stream()
                .mapToDouble(WheelContent::getWeight)
                .sum();

        double contentWeight = content.getWeight();

        // 置信度 = 内容权重 / 总权重
        return Math.min(1.0, contentWeight / totalWeight);
    }

    /**
     * 生成转盘种子（用于复现结果）
     *
     * @param userId 用户ID
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
     * @param seed 随机种子
     * @param selectedContent 选中的内容
     * @param allContents 所有可选内容
     * @param requestTime 请求时间
     * @return 是否一致
     */
    public boolean verifyResultConsistency(long seed, WheelContent selectedContent,
                                          List<WheelContent> allContents,
                                          LocalDateTime requestTime) {
        try {
            // 使用相同种子重新生成结果
            Random verifyRandom = new Random(seed);
            List<WheelContent> availableContents = filterByTime(allContents, requestTime);

            if (availableContents.isEmpty()) {
                availableContents = allContents;
            }

            double totalWeight = availableContents.stream()
                    .mapToDouble(WheelContent::getWeight)
                    .sum();

            double randomValue = verifyRandom.nextDouble() * totalWeight;
            double cumulativeWeight = 0.0;

            for (WheelContent content : availableContents) {
                cumulativeWeight += content.getWeight();
                if (randomValue <= cumulativeWeight) {
                    boolean consistent = content.getId().equals(selectedContent.getId());
                    log.debug("结果一致性验证: expectedId={}, actualId={}, consistent={}",
                            content.getId(), selectedContent.getId(), consistent);
                    return consistent;
                }
            }

            return false;
        } catch (Exception e) {
            log.error("结果一致性验证失败: {}", e.getMessage(), e);
            return false;
        }
    }
}
