package com.basebackend.wheel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "push.jpush")
public class JPushProperties {

    private boolean enabled = false;
    private String appKey;
    private String masterSecret;
    private String title = "情侣转盘";
    private boolean apnsProduction = false;
}
