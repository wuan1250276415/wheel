package com.basebackend.wheel.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * 多级缓存配置
 * 注意：Caffeine缓存已被注释掉，需要添加caffeine依赖后启用
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Slf4j
@Configuration
public class CacheConfig {

    // 注意：所有Caffeine缓存已被注释，需要添加caffeine依赖后启用
    // /**
    //  * L1缓存（Caffeine）- 进程内缓存
    //  * 用于缓存热点数据，如热门分类、用户基础信息等
    //  */
    // @Bean
    // @Primary
    // public Cache<String, Object> caffeineCache() {
    //     return Caffeine.newBuilder()
    //             .initialCapacity(100) // 初始容量
    //             .maximumSize(1000) // 最大容量
    //             .expireAfterWrite(5, TimeUnit.MINUTES) // 写后5分钟过期
    //             .expireAfterAccess(10, TimeUnit.MINUTES) // 访问后10分钟过期
    //             .recordStats() // 开启统计
    //             .build();
    // }

    // /**
    //  * 用户信息缓存
    //  */
    // @Bean
    // public Cache<Long, Object> userCache() {
    //     return Caffeine.newBuilder()
    //             .initialCapacity(50)
    //             .maximumSize(500)
    //             .expireAfterWrite(30, TimeUnit.MINUTES)
    //             .recordStats()
    //             .build();
    // }

    // /**
    //  * 转盘内容缓存
    //  */
    // @Bean
    // public Cache<Long, Object> contentCache() {
    //     return Caffeine.newBuilder()
    //             .initialCapacity(20)
    //             .maximumSize(200)
    //             .expireAfterWrite(10, TimeUnit.MINUTES)
    //             .recordStats()
    //             .build();
    // }

    // /**
    //  * 分类信息缓存
    //  */
    // @Bean
    // public Cache<String, Object> categoryCache() {
    //     return Caffeine.newBuilder()
    //             .initialCapacity(10)
    //             .maximumSize(100)
    //             .expireAfterWrite(5, TimeUnit.MINUTES)
    //             .recordStats()
    //             .build();
    // }
}
