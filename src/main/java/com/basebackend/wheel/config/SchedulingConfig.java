package com.basebackend.wheel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务配置类
 * 启用Spring的定时任务功能
 *
 * @author wheel-api
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // 启用定时任务调度
}
