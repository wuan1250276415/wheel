package com.basebackend.wheel.scheduler;

import com.basebackend.wheel.enums.ReportType;
import com.basebackend.wheel.service.CoupleReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 情侣报告定时调度器
 * 负责定时生成周报、月报、年报
 *
 * @author wheel-api
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportScheduler {

    private final CoupleReportService coupleReportService;

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * 重试间隔（毫秒）
     */
    private static final long RETRY_DELAY_MS = 1000;

    /**
     * 每周一8:00生成周报
     * Cron表达式: 秒 分 时 日 月 周
     * 0 0 8 ? * MON = 每周一8:00:00
     */
    @Scheduled(cron = "0 0 8 ? * MON")
    public void generateWeeklyReports() {
        log.info("开始执行周报定时任务");
        try {
            executeWithRetry(ReportType.WEEKLY);
            log.info("周报定时任务执行完成");
        } catch (Exception e) {
            log.error("周报定时任务执行失败（已重试{}次）", MAX_RETRY_ATTEMPTS, e);
        }
    }

    /**
     * 每月1日8:00生成月报
     * Cron表达式: 0 0 8 1 * ? = 每月1日8:00:00
     */
    @Scheduled(cron = "0 0 8 1 * ?")
    public void generateMonthlyReports() {
        log.info("开始执行月报定时任务");
        try {
            executeWithRetry(ReportType.MONTHLY);
            log.info("月报定时任务执行完成");
        } catch (Exception e) {
            log.error("月报定时任务执行失败（已重试{}次）", MAX_RETRY_ATTEMPTS, e);
        }
    }

    /**
     * 每年1月1日8:00生成年报
     * Cron表达式: 0 0 8 1 1 ? = 每年1月1日8:00:00
     */
    @Scheduled(cron = "0 0 8 1 1 ?")
    public void generateYearlyReports() {
        log.info("开始执行年报定时任务");
        try {
            executeWithRetry(ReportType.YEARLY);
            log.info("年报定时任务执行完成");
        } catch (Exception e) {
            log.error("年报定时任务执行失败（已重试{}次）", MAX_RETRY_ATTEMPTS, e);
        }
    }

    /**
     * 带重试机制的报告生成执行方法
     * 最多重试3次，每次间隔1秒
     *
     * @param type 报告类型
     */
    public void executeWithRetry(ReportType type) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                log.info("执行报告生成: type={}, 尝试次数={}/{}", type, attempt, MAX_RETRY_ATTEMPTS);
                coupleReportService.generateScheduledReports(type);
                return; // 成功则直接返回
            } catch (Exception e) {
                lastException = e;
                log.warn("报告生成失败: type={}, 尝试次数={}/{}, 错误={}", 
                        type, attempt, MAX_RETRY_ATTEMPTS, e.getMessage());
                
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试被中断", ie);
                    }
                }
            }
        }
        
        // 所有重试都失败，抛出最后一个异常
        throw new RuntimeException("报告生成失败，已重试" + MAX_RETRY_ATTEMPTS + "次", lastException);
    }

    /**
     * 手动触发报告生成（供管理接口调用）
     *
     * @param type 报告类型
     */
    public void triggerReportGeneration(ReportType type) {
        log.info("手动触发报告生成: type={}", type);
        try {
            executeWithRetry(type);
            log.info("手动触发报告生成完成: type={}", type);
        } catch (Exception e) {
            log.error("手动触发报告生成失败: type={}", type, e);
            throw e;
        }
    }
}
