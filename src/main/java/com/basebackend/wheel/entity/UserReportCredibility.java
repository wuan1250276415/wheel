package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户举报信誉表
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_report_credibility")
public class UserReportCredibility extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 信誉分（0-100）
     */
    @TableField("credibility_score")
    private Integer credibilityScore;

    /**
     * 总举报数
     */
    @TableField("total_reports")
    private Integer totalReports;

    /**
     * 有效举报数
     */
    @TableField("valid_reports")
    private Integer validReports;

    /**
     * 无效举报数
     */
    @TableField("invalid_reports")
    private Integer invalidReports;

    /**
     * 最后举报时间
     */
    @TableField("last_report_at")
    private LocalDateTime lastReportAt;

    /**
     * 默认信誉分
     */
    public static final int DEFAULT_CREDIBILITY_SCORE = 100;

    /**
     * 最大信誉分
     */
    public static final int MAX_CREDIBILITY_SCORE = 100;

    /**
     * 最小信誉分
     */
    public static final int MIN_CREDIBILITY_SCORE = 0;

    /**
     * 低信誉阈值（默认30分以下为低信誉用户）
     */
    public static final int LOW_CREDIBILITY_THRESHOLD = 30;

    /**
     * 判断是否为低信誉用户
     */
    public boolean isLowCredibility() {
        return this.credibilityScore != null && this.credibilityScore < LOW_CREDIBILITY_THRESHOLD;
    }

    /**
     * 计算有效举报率
     */
    public double getValidReportRate() {
        if (this.totalReports == null || this.totalReports == 0) {
            return 0.0;
        }
        return (double) (this.validReports != null ? this.validReports : 0) / this.totalReports;
    }

    /**
     * 增加有效举报
     */
    public void incrementValidReport() {
        this.totalReports = (this.totalReports != null ? this.totalReports : 0) + 1;
        this.validReports = (this.validReports != null ? this.validReports : 0) + 1;
        this.lastReportAt = LocalDateTime.now();
    }

    /**
     * 增加无效举报
     */
    public void incrementInvalidReport() {
        this.totalReports = (this.totalReports != null ? this.totalReports : 0) + 1;
        this.invalidReports = (this.invalidReports != null ? this.invalidReports : 0) + 1;
        this.lastReportAt = LocalDateTime.now();
    }

    /**
     * 调整信誉分（确保在有效范围内）
     */
    public void adjustCredibilityScore(int delta) {
        int newScore = (this.credibilityScore != null ? this.credibilityScore : DEFAULT_CREDIBILITY_SCORE) + delta;
        this.credibilityScore = Math.max(MIN_CREDIBILITY_SCORE, Math.min(MAX_CREDIBILITY_SCORE, newScore));
    }
}
