package com.basebackend.wheel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 转盘配置属性
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Data
@Component
@ConfigurationProperties(prefix = "wheel")
public class WheelProperties {

    private Jwt jwt = new Jwt();

    private Wheel wheel = new Wheel();

    private Cache cache = new Cache();

    @Data
    public static class Jwt {
        private String secret;
        private long accessTokenValidityInSeconds = 1800;
        private long refreshTokenValidityInSeconds = 604800;
    }

    @Data
    public static class Wheel {
        private int maxDailySpins = 50;
        private long minSpinInterval = 1000;
        private double defaultWeight = 1.0;
        private int maxOptionsPerSpin = 12;
        private int animationDuration = 3000;
    }

    @Data
    public static class Cache {
        private boolean enabled = true;
        private int caffeineMaxSize = 1000;
        private int caffeineExpireAfterWrite = 300; // 秒
        private int redisExpireAfterWrite = 1800; // 秒
    }
}
