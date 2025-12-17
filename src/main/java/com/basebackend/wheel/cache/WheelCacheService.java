package com.basebackend.wheel.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 转盘缓存服务
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Slf4j
@Service
public class WheelCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // 缓存键前缀
    private static final String CACHE_PREFIX = "wheel:";
    private static final String CATEGORIES_KEY = CACHE_PREFIX + "categories";
    private static final String CONTENTS_KEY = CACHE_PREFIX + "contents:category:";
    private static final String USER_CONFIG_KEY = CACHE_PREFIX + "config:user:";
    private static final String SPIN_FREQUENCY_KEY = CACHE_PREFIX + "spin:frequency:user:";

    // 缓存过期时间（秒）
    private static final long CATEGORIES_TTL = 300; // 5分钟
    private static final long CONTENTS_TTL = 600; // 10分钟
    private static final long USER_CONFIG_TTL = 1800; // 30分钟
    private static final long SPIN_FREQUENCY_TTL = 86400; // 24小时

    /**
     * 获取转盘分类缓存
     */
    @Cacheable(value = "wheel:categories", key = "'categories'", unless = "#result == null")
    public <T> T getCategoriesCache(String cacheKey, TypeReference<T> typeRef) {
        try {
            Object cached = redisTemplate.opsForValue().get(CATEGORIES_KEY);
            if (cached != null) {
                return objectMapper.convertValue(cached, typeRef);
            }
        } catch (Exception e) {
            log.warn("获取分类缓存失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 设置转盘分类缓存
     */
    public <T> void setCategoriesCache(T categories) {
        try {
            redisTemplate.opsForValue().set(CATEGORIES_KEY, categories, CATEGORIES_TTL, TimeUnit.SECONDS);
            log.debug("设置分类缓存成功");
        } catch (Exception e) {
            log.warn("设置分类缓存失败: {}", e.getMessage());
        }
    }

    /**
     * 清空转盘分类缓存
     */
    @CacheEvict(value = "wheel:categories", key = "'categories'")
    public void clearCategoriesCache() {
        try {
            redisTemplate.delete(CATEGORIES_KEY);
            log.debug("清空分类缓存成功");
        } catch (Exception e) {
            log.warn("清空分类缓存失败: {}", e.getMessage());
        }
    }

    /**
     * 获取内容缓存
     */
    public <T> T getContentsCache(Long categoryId, TypeReference<T> typeRef) {
        try {
            String key = CONTENTS_KEY + categoryId;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return objectMapper.convertValue(cached, typeRef);
            }
        } catch (Exception e) {
            log.warn("获取内容缓存失败: categoryId={}, error={}", categoryId, e.getMessage());
        }
        return null;
    }

    /**
     * 设置内容缓存
     */
    public <T> void setContentsCache(Long categoryId, T contents) {
        try {
            String key = CONTENTS_KEY + categoryId;
            redisTemplate.opsForValue().set(key, contents, CONTENTS_TTL, TimeUnit.SECONDS);
            log.debug("设置内容缓存成功: categoryId={}", categoryId);
        } catch (Exception e) {
            log.warn("设置内容缓存失败: categoryId={}, error={}", categoryId, e.getMessage());
        }
    }

    /**
     * 清空内容缓存
     */
    public void clearContentsCache(Long categoryId) {
        try {
            String key = CONTENTS_KEY + categoryId;
            redisTemplate.delete(key);
            log.debug("清空内容缓存成功: categoryId={}", categoryId);
        } catch (Exception e) {
            log.warn("清空内容缓存失败: categoryId={}, error={}", categoryId, e.getMessage());
        }
    }

    /**
     * 获取用户配置缓存
     */
    public <T> T getUserConfigCache(Long userId, TypeReference<T> typeRef) {
        try {
            String key = USER_CONFIG_KEY + userId;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return objectMapper.convertValue(cached, typeRef);
            }
        } catch (Exception e) {
            log.warn("获取用户配置缓存失败: userId={}, error={}", userId, e.getMessage());
        }
        return null;
    }

    /**
     * 设置用户配置缓存
     */
    public <T> void setUserConfigCache(Long userId, T config) {
        try {
            String key = USER_CONFIG_KEY + userId;
            redisTemplate.opsForValue().set(key, config, USER_CONFIG_TTL, TimeUnit.SECONDS);
            log.debug("设置用户配置缓存成功: userId={}", userId);
        } catch (Exception e) {
            log.warn("设置用户配置缓存失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 检查转盘频率限制
     */
    public boolean checkSpinFrequency(Long userId) {
        try {
            String key = SPIN_FREQUENCY_KEY + userId;
            Object count = redisTemplate.opsForValue().get(key);
            if (count == null) {
                // 第一次使用，初始化计数器
                redisTemplate.opsForValue().set(key, 1, SPIN_FREQUENCY_TTL, TimeUnit.SECONDS);
                return true;
            }

            int currentCount = Integer.parseInt(count.toString());
            if (currentCount >= 50) { // 每日限制50次
                return false;
            }

            // 增加计数器
            redisTemplate.opsForValue().increment(key);
            return true;
        } catch (Exception e) {
            log.warn("检查转盘频率失败: userId={}, error={}", userId, e.getMessage());
            return true; // 出错时允许继续，避免影响用户体验
        }
    }

    /**
     * 获取转盘频率计数
     */
    public int getSpinFrequencyCount(Long userId) {
        try {
            String key = SPIN_FREQUENCY_KEY + userId;
            Object count = redisTemplate.opsForValue().get(key);
            return count != null ? Integer.parseInt(count.toString()) : 0;
        } catch (Exception e) {
            log.warn("获取转盘频率计数失败: userId={}, error={}", userId, e.getMessage());
            return 0;
        }
    }

    /**
     * 清空所有缓存
     */
    public void clearAllCache() {
        try {
            // 获取所有wheel相关的key
            var keys = redisTemplate.keys(CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            log.info("清空所有缓存成功");
        } catch (Exception e) {
            log.warn("清空所有缓存失败: {}", e.getMessage());
        }
    }
}
