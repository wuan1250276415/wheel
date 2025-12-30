package com.basebackend.wheel.config;

import cn.jpush.api.JPushClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PushConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "push.jpush", name = "enabled", havingValue = "true")
    public JPushClient jPushClient(JPushProperties properties) {
        return new JPushClient(properties.getMasterSecret(), properties.getAppKey());
    }
}
