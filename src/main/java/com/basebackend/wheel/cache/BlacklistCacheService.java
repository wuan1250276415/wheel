package com.basebackend.wheel.cache;

import com.basebackend.wheel.dto.BlacklistCheckResult;
import com.basebackend.wheel.entity.Blacklist;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 黑名单缓存服务
 * 使用Redis缓存黑名单数据，提高检查效率
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Slf4j
@Service
public class BlacklistCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 缓存键前缀
    private static final String CACHE_PREFIX = "blacklist:";
    private static final String USER_KEY = CACHE_PREFIX + "user:";
    private static final String IP_KEY = CACHE_PREFIX + "ip:";
    private static final String DEVICE_KEY = CACHE_PREFIX + "device:";

    // 默认缓存过期时间（秒）- 5分钟
    private static final long DEFAULT_TTL = 300;

    // 未封禁标记的缓存时间（秒）- 1分钟（较短，避免新增黑名单后延迟生效）
    private static final long NOT_BLOCKED_TTL = 60;

    // ==================== 用户黑名单缓存 ====================

    /**
     * 获取用户黑名单缓存
     *
     * @param userId 用户ID
     * @return 缓存的检查结果，null表示缓存未命中
     */
    public BlacklistCheckResult getUserBlacklistCache(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            String key = USER_KEY + userId;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof BlacklistCheckResult) {
                log.debug("用户黑名单缓存命中: userId={}", userId);
                return (BlacklistCheckResult) cached;
            }
        } catch (Exception e) {
            log.warn("获取用户黑名单缓存失败: userId={}, error={}", userId, e.getMessage());
        }
        return null;
    }

    /**
     * 设置用户黑名单缓存
     *
     * @param userId 用户ID
     * @param result 检查结果
     */
    public void setUserBlacklistCache(Long userId, BlacklistCheckResult result) {
        if (userId == null || result == null) {
            return;
        }
        try {
            String key = USER_KEY + userId;
            long ttl = calculateTTL(result);
            redisTemplate.opsForValue().set(key, result, ttl, TimeUnit.SECONDS);
            log.debug("设置用户黑名单缓存: userId={}, blocked={}, ttl={}s", userId, result.isBlocked(), ttl);
        } catch (Exception e) {
            log.warn("设置用户黑名单缓存失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 清除用户黑名单缓存
     *
     * @param userId 用户ID
     */
    public void clearUserBlacklistCache(Long userId) {
        if (userId == null) {
            return;
        }
        try {
            String key = USER_KEY + userId;
            redisTemplate.delete(key);
            log.debug("清除用户黑名单缓存: userId={}", userId);
        } catch (Exception e) {
            log.warn("清除用户黑名单缓存失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    // ==================== IP黑名单缓存 ====================

    /**
     * 获取IP黑名单缓存
     *
     * @param ip IP地址
     * @return 缓存的检查结果，null表示缓存未命中
     */
    public BlacklistCheckResult getIpBlacklistCache(String ip) {
        if (ip == null || ip.isEmpty()) {
            return null;
        }
        try {
            String key = IP_KEY + ip;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof BlacklistCheckResult) {
                log.debug("IP黑名单缓存命中: ip={}", ip);
                return (BlacklistCheckResult) cached;
            }
        } catch (Exception e) {
            log.warn("获取IP黑名单缓存失败: ip={}, error={}", ip, e.getMessage());
        }
        return null;
    }

    /**
     * 设置IP黑名单缓存
     *
     * @param ip     IP地址
     * @param result 检查结果
     */
    public void setIpBlacklistCache(String ip, BlacklistCheckResult result) {
        if (ip == null || ip.isEmpty() || result == null) {
            return;
        }
        try {
            String key = IP_KEY + ip;
            long ttl = calculateTTL(result);
            redisTemplate.opsForValue().set(key, result, ttl, TimeUnit.SECONDS);
            log.debug("设置IP黑名单缓存: ip={}, blocked={}, ttl={}s", ip, result.isBlocked(), ttl);
        } catch (Exception e) {
            log.warn("设置IP黑名单缓存失败: ip={}, error={}", ip, e.getMessage());
        }
    }

    /**
     * 清除IP黑名单缓存
     *
     * @param ip IP地址
     */
    public void clearIpBlacklistCache(String ip) {
        if (ip == null || ip.isEmpty()) {
            return;
        }
        try {
            String key = IP_KEY + ip;
            redisTemplate.delete(key);
            log.debug("清除IP黑名单缓存: ip={}", ip);
        } catch (Exception e) {
            log.warn("清除IP黑名单缓存失败: ip={}, error={}", ip, e.getMessage());
        }
    }

    // ==================== 设备黑名单缓存 ====================

    /**
     * 获取设备黑名单缓存
     *
     * @param deviceId 设备ID
     * @return 缓存的检查结果，null表示缓存未命中
     */
    public BlacklistCheckResult getDeviceBlacklistCache(String deviceId) {
        if (deviceId == null || deviceId.isEmpty()) {
            return null;
        }
        try {
            String key = DEVICE_KEY + deviceId;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof BlacklistCheckResult) {
                log.debug("设备黑名单缓存命中: deviceId={}", deviceId);
                return (BlacklistCheckResult) cached;
            }
        } catch (Exception e) {
            log.warn("获取设备黑名单缓存失败: deviceId={}, error={}", deviceId, e.getMessage());
        }
        return null;
    }

    /**
     * 设置设备黑名单缓存
     *
     * @param deviceId 设备ID
     * @param result   检查结果
     */
    public void setDeviceBlacklistCache(String deviceId, BlacklistCheckResult result) {
        if (deviceId == null || deviceId.isEmpty() || result == null) {
            return;
        }
        try {
            String key = DEVICE_KEY + deviceId;
            long ttl = calculateTTL(result);
            redisTemplate.opsForValue().set(key, result, ttl, TimeUnit.SECONDS);
            log.debug("设置设备黑名单缓存: deviceId={}, blocked={}, ttl={}s", deviceId, result.isBlocked(), ttl);
        } catch (Exception e) {
            log.warn("设置设备黑名单缓存失败: deviceId={}, error={}", deviceId, e.getMessage());
        }
    }

    /**
     * 清除设备黑名单缓存
     *
     * @param deviceId 设备ID
     */
    public void clearDeviceBlacklistCache(String deviceId) {
        if (deviceId == null || deviceId.isEmpty()) {
            return;
        }
        try {
            String key = DEVICE_KEY + deviceId;
            redisTemplate.delete(key);
            log.debug("清除设备黑名单缓存: deviceId={}", deviceId);
        } catch (Exception e) {
            log.warn("清除设备黑名单缓存失败: deviceId={}, error={}", deviceId, e.getMessage());
        }
    }

    // ==================== 通用方法 ====================

    /**
     * 根据黑名单实体清除对应的缓存
     *
     * @param blacklist 黑名单实体
     */
    public void clearCacheByBlacklist(Blacklist blacklist) {
        if (blacklist == null) {
            return;
        }
        
        switch (blacklist.getType()) {
            case Blacklist.TYPE_USER:
                clearUserBlacklistCache(Long.parseLong(blacklist.getTargetId()));
                break;
            case Blacklist.TYPE_IP:
                clearIpBlacklistCache(blacklist.getTargetId());
                break;
            case Blacklist.TYPE_DEVICE:
                clearDeviceBlacklistCache(blacklist.getTargetId());
                break;
            default:
                log.warn("未知的黑名单类型: type={}", blacklist.getType());
        }
    }

    /**
     * 清除所有黑名单缓存
     */
    public void clearAllBlacklistCache() {
        try {
            Set<String> keys = scanKeys(CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("清除所有黑名单缓存: count={}", keys.size());
            }
        } catch (Exception e) {
            log.warn("清除所有黑名单缓存失败: error={}", e.getMessage());
        }
    }

    /**
     * 计算缓存TTL
     * 如果是封禁状态且有过期时间，则TTL为到过期时间的剩余秒数
     * 否则使用默认TTL
     *
     * @param result 检查结果
     * @return TTL（秒）
     */
    private long calculateTTL(BlacklistCheckResult result) {
        if (!result.isBlocked()) {
            // 未封禁的缓存时间较短
            return NOT_BLOCKED_TTL;
        }
        
        if (result.getExpireAt() == null) {
            // 永久封禁，使用默认TTL
            return DEFAULT_TTL;
        }
        
        // 计算到过期时间的剩余秒数
        long remainingSeconds = Duration.between(LocalDateTime.now(), result.getExpireAt()).getSeconds();
        
        if (remainingSeconds <= 0) {
            // 已过期，使用较短的TTL
            return NOT_BLOCKED_TTL;
        }
        
        // 取剩余时间和默认TTL的较小值
        return Math.min(remainingSeconds, DEFAULT_TTL);
    }

    /**
     * 扫描匹配的键
     *
     * @param pattern 匹配模式
     * @return 匹配的键集合
     */
    private Set<String> scanKeys(String pattern) {
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(1000).build();
        Set<String> keys = new HashSet<>();
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            try (var cursor = connection.scan(options)) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            }
            return null;
        });
        return keys;
    }
}
