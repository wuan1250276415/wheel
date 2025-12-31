package com.basebackend.wheel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 敏感词检测结果
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveWordResult {

    /**
     * 是否包含敏感词
     */
    private boolean hasSensitiveWord;

    /**
     * 匹配到的敏感词列表
     */
    @Builder.Default
    private List<MatchedWord> matchedWords = new ArrayList<>();

    /**
     * 风险评分（0-100）
     */
    private double riskScore;

    /**
     * 检测耗时（毫秒）
     */
    private long detectTimeMs;

    /**
     * 创建空结果（无敏感词）
     */
    public static SensitiveWordResult empty(long detectTimeMs) {
        return SensitiveWordResult.builder()
                .hasSensitiveWord(false)
                .matchedWords(new ArrayList<>())
                .riskScore(0.0)
                .detectTimeMs(detectTimeMs)
                .build();
    }

    /**
     * 添加匹配的敏感词
     */
    public void addMatchedWord(MatchedWord word) {
        if (this.matchedWords == null) {
            this.matchedWords = new ArrayList<>();
        }
        this.matchedWords.add(word);
        this.hasSensitiveWord = true;
    }

    /**
     * 计算风险评分
     * 基于匹配到的敏感词数量和风险等级计算
     */
    public void calculateRiskScore() {
        if (matchedWords == null || matchedWords.isEmpty()) {
            this.riskScore = 0.0;
            return;
        }

        double totalScore = 0.0;
        for (MatchedWord word : matchedWords) {
            // 根据风险等级计算分数：低=10, 中=25, 高=50
            int levelScore = switch (word.getLevel()) {
                case 1 -> 10;
                case 2 -> 25;
                case 3 -> 50;
                default -> 15;
            };
            totalScore += levelScore;
        }

        // 限制最大分数为100
        this.riskScore = Math.min(100.0, totalScore);
    }
}
