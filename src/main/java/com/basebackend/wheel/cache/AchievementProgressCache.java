package com.basebackend.wheel.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AchievementProgressCache {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_PREFIX = "achievement:progress:";
    private static final long EXPIRE_TIME = 7 * 24 * 60 * 60;

    public Long increment(Long userId, String eventType) {
        String key = buildKey(userId, eventType);
        Long value = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, EXPIRE_TIME, TimeUnit.SECONDS);
        return value != null ? value : 0L;
    }

    public Long get(Long userId, String eventType) {
        String key = buildKey(userId, eventType);
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof Long) {
            return (Long) value;
        }
        return 0L;
    }

    public void set(Long userId, String eventType, Long value) {
        String key = buildKey(userId, eventType);
        redisTemplate.opsForValue().set(key, value, EXPIRE_TIME, TimeUnit.SECONDS);
    }

    public void delete(Long userId, String eventType) {
        String key = buildKey(userId, eventType);
        redisTemplate.delete(key);
    }

    private String buildKey(Long userId, String eventType) {
        return KEY_PREFIX + userId + ":" + eventType;
    }
}
