package com.basebackend.wheel.scheduler;

import com.basebackend.wheel.service.BlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 黑名单定时调度器
 * 负责定时检查并解除过期的黑名单
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BlacklistScheduler {

    private final BlacklistService blacklistService;

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * 重试间隔（毫秒）
     */
    private static final long RETRY_DELAY_MS = 1000;

    /**
     * 每分钟检查一次过期的黑名单
     * Cron表达式: 0 * * * * ? = 每分钟的第0秒执行
     */
    @Scheduled(cron = "0 * * * * ?")
    public void releaseExpiredBlacklist() {
        log.debug("开始检查过期黑名单...");
        try {
            int count = executeWithRetry();
            if (count > 0) {
                log.info("自动解除过期黑名单完成: count={}", count);
            }
        } catch (Exception e) {
            log.error("自动解除过期黑名单失败（已重试{}次）", MAX_RETRY_ATTEMPTS, e);
        }
    }

    /**
     * 带重试机制的过期黑名单解除执行方法
     * 最多重试3次，每次间隔1秒
     *
     * @return 解除的黑名单数量
     */
    private int executeWithRetry() {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                return blacklistService.releaseExpiredBlacklist();
            } catch (Exception e) {
                lastException = e;
                log.warn("解除过期黑名单失败: 尝试次数={}/{}, 错误={}", 
                        attempt, MAX_RETRY_ATTEMPTS, e.getMessage());
                
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
        throw new RuntimeException("解除过期黑名单失败，已重试" + MAX_RETRY_ATTEMPTS + "次", lastException);
    }

    /**
     * 手动触发过期黑名单解除（供管理接口调用）
     *
     * @return 解除的黑名单数量
     */
    public int triggerReleaseExpired() {
        log.info("手动触发解除过期黑名单");
        try {
            int count = executeWithRetry();
            log.info("手动触发解除过期黑名单完成: count={}", count);
            return count;
        } catch (Exception e) {
            log.error("手动触发解除过期黑名单失败", e);
            throw e;
        }
    }
}
