package com.basebackend.wheel.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 性能优化配置
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Slf4j
@Configuration
@EnableAsync
public class PerformanceConfig {

    /**
     * 异步任务执行器
     */
    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // 核心线程数
        executor.setMaxPoolSize(20); // 最大线程数
        executor.setQueueCapacity(100); // 队列容量
        executor.setKeepAliveSeconds(60); // 线程空闲时间
        executor.setThreadNamePrefix("Wheel-Async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    // 注意：Caffeine缓存已被注释掉，需要添加caffeine依赖后启用
    // /**
    //  * 热点数据缓存
    //  */
    // @Bean
    // public Cache<String, Object> hotDataCache() {
    //     return Caffeine.newBuilder()
    //             .initialCapacity(50)
    //             .maximumSize(500)
    //             .expireAfterWrite(10, TimeUnit.MINUTES)
    //             .recordStats()
    //             .build();
    // }

    // /**
    //  * 查询结果缓存
    //  */
    // @Bean
    // public Cache<String, Object> queryResultCache() {
    //     return Caffeine.newBuilder()
    //             .initialCapacity(100)
    //             .maximumSize(1000)
    //             .expireAfterWrite(5, TimeUnit.MINUTES)
    //             .recordStats()
    //             .build();
    // }

    // /**
    //  * 统计信息缓存
    //  */
    // @Bean
    // public Cache<String, Object> statsCache() {
    //     return Caffeine.newBuilder()
    //             .initialCapacity(20)
    //             .maximumSize(200)
    //             .expireAfterWrite(15, TimeUnit.MINUTES)
    //             .recordStats()
    //             .build();
    // }
}
