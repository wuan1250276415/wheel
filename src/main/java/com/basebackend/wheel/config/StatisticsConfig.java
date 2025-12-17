package com.basebackend.wheel.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 自定义统计配置，禁用 StatisticsCache
 */
@Configuration
public class StatisticsConfig {

    /**
     * 禁用 StatisticsCache bean 的创建
     * 这将导致依赖 StatisticsCache 的其他 bean 也无法创建，
     * 但由于 StatisticsAutoConfiguration 没有正确的条件判断，
     * 这是最直接的解决方案
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public Object statisticsCache() {
        // 返回一个空对象，阻止 StatisticsCache 的创建
        return new Object();
    }
}
