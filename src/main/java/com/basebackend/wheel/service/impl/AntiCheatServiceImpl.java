package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.mapper.WheelSpinRecordMapper;
import com.basebackend.wheel.service.AntiCheatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * 防作弊服务实现
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Slf4j
@Service
public class AntiCheatServiceImpl implements AntiCheatService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private WheelSpinRecordMapper spinRecordMapper;

    @Value("${wheel.wheel.max-daily-spins:50}")
    private int maxDailySpins;

    @Value("${wheel.wheel.min-spin-interval:1000}")
    private long minSpinInterval;

    private static final String FREQUENCY_KEY = "wheel:frequency:user:";
    private static final String IP_KEY = "wheel:ip:user:";
    private static final String DEVICE_KEY = "wheel:device:user:";
    private static final String ANOMALY_KEY = "wheel:anomaly:user:";
    private static final String RISK_KEY = "wheel:risk:user:";

    @Override
    public ValidationResult validateSpinFrequency(Long userId, String ipAddress, String deviceId) {
        try {
            // 1. 检查每日转盘次数限制
            String frequencyKey = FREQUENCY_KEY + userId;
            Object dailyCount = redisTemplate.opsForValue().get(frequencyKey);

            int currentCount = dailyCount != null ? Integer.parseInt(dailyCount.toString()) : 0;

            if (currentCount >= maxDailySpins) {
                log.warn("用户今日转盘次数已达上限: userId={}, count={}, max={}",
                        userId, currentCount, maxDailySpins);
                return ValidationResult.fail("今日转盘次数已达上限");
            }

            // 2. 检查转盘间隔
            String intervalKey = "wheel:lastspin:user:" + userId;
            Object lastSpinTime = redisTemplate.opsForValue().get(intervalKey);

            if (lastSpinTime != null) {
                LocalDateTime lastTime = LocalDateTime.parse(lastSpinTime.toString());
                long secondsBetween = ChronoUnit.SECONDS.between(lastTime, LocalDateTime.now());

                if (secondsBetween < minSpinInterval / 1000) {
                    log.warn("用户转盘间隔过短: userId={}, interval={}ms, min={}ms",
                            userId, secondsBetween * 1000, minSpinInterval);
                    return ValidationResult.warn("转盘间隔过短，请稍后再试");
                }
            }

            // 3. 更新计数器
            redisTemplate.opsForValue().set(frequencyKey, currentCount + 1, 24, TimeUnit.HOURS);
            redisTemplate.opsForValue().set(intervalKey, LocalDateTime.now().toString(), 1, TimeUnit.HOURS);

            return ValidationResult.success();
        } catch (Exception e) {
            log.error("验证转盘频率失败: userId={}, error={}", userId, e.getMessage(), e);
            return ValidationResult.fail("系统繁忙，请稍后再试");
        }
    }

    @Override
    public ValidationResult validateIpAddress(Long userId, String ipAddress) {
        try {
            if (ipAddress == null || ipAddress.isEmpty()) {
                return ValidationResult.warn("IP地址为空");
            }

            // 检查同一IP是否有多个用户使用
            String ipUserKey = "wheel:ipusers:" + ipAddress;
            Object userList = redisTemplate.opsForValue().get(ipUserKey);

            if (userList != null) {
                String[] users = userList.toString().split(",");
                if (users.length > 3) {
                    log.warn("同一IP用户数过多: ip={}, userCount={}", ipAddress, users.length);
                    return ValidationResult.warn("同一IP用户数过多");
                }
            }

            // 记录用户与IP的关联
            String currentUserId = userId.toString();
            if (userList == null) {
                redisTemplate.opsForValue().set(ipUserKey, currentUserId, 24, TimeUnit.HOURS);
            } else {
                String existingUsers = userList.toString();
                if (!existingUsers.contains(currentUserId)) {
                    redisTemplate.opsForValue().set(ipUserKey, existingUsers + "," + currentUserId, 24, TimeUnit.HOURS);
                }
            }

            return ValidationResult.success();
        } catch (Exception e) {
            log.error("验证IP地址失败: userId={}, error={}", userId, e.getMessage(), e);
            return ValidationResult.fail("系统繁忙，请稍后再试");
        }
    }

    @Override
    public ValidationResult checkDeviceAnomaly(Long userId, String deviceId) {
        try {
            if (deviceId == null || deviceId.isEmpty()) {
                return ValidationResult.warn("设备ID为空");
            }

            // 检查设备是否被多个用户使用
            String deviceUserKey = "wheel:deviceusers:" + deviceId;
            Object userList = redisTemplate.opsForValue().get(deviceUserKey);

            if (userList != null) {
                String[] users = userList.toString().split(",");
                if (users.length > 2) {
                    log.warn("同一设备用户数过多: deviceId={}, userCount={}", deviceId, users.length);
                    return ValidationResult.warn("同一设备用户数过多");
                }
            }

            // 记录设备与用户的关联
            String currentUserId = userId.toString();
            if (userList == null) {
                redisTemplate.opsForValue().set(deviceUserKey, currentUserId, 24, TimeUnit.HOURS);
            } else {
                String existingUsers = userList.toString();
                if (!existingUsers.contains(currentUserId)) {
                    redisTemplate.opsForValue().set(deviceUserKey, existingUsers + "," + currentUserId, 24, TimeUnit.HOURS);
                }
            }

            return ValidationResult.success();
        } catch (Exception e) {
            log.error("检查设备异常失败: userId={}, error={}", userId, e.getMessage(), e);
            return ValidationResult.fail("系统繁忙，请稍后再试");
        }
    }

    @Override
    public ValidationResult validateSpinResult(Long userId, Long contentId, LocalDateTime spinTime, String ipAddress) {
        try {
            // 检查近期异常记录
//            var anomalyRecords = spinRecordMapper.selectAnomalyRecordsByUserId(userId, 7);
//            if (anomalyRecords.size() > 10) {
//                log.warn("用户近期异常记录过多: userId={}, anomalyCount={}", userId, anomalyRecords.size());
//                return ValidationResult.warn("近期异常记录较多，请注意使用规范");
//            }

            // 检查IP地址变化频率
            String ipKey = IP_KEY + userId;
            Object lastIp = redisTemplate.opsForValue().get(ipKey);

            if (lastIp != null && !lastIp.toString().equals(ipAddress)) {
                // IP发生变化
                Object changeCount = redisTemplate.opsForValue().get("wheel:ipchange:user:" + userId);
                int currentCount = changeCount != null ? Integer.parseInt(changeCount.toString()) : 0;

                if (currentCount > 5) {
                    log.warn("用户IP变化过于频繁: userId={}, changeCount={}", userId, currentCount);
                    return ValidationResult.warn("IP地址变化频繁");
                }

                redisTemplate.opsForValue().set("wheel:ipchange:user:" + userId, currentCount + 1, 24, TimeUnit.HOURS);
            }

            redisTemplate.opsForValue().set(ipKey, ipAddress, 24, TimeUnit.HOURS);

            return ValidationResult.success();
        } catch (Exception e) {
            log.error("验证转盘结果失败: userId={}, error={}", userId, e.getMessage(), e);
            return ValidationResult.fail("系统繁忙，请稍后再试");
        }
    }

    @Override
    public boolean recordAnomaly(Long userId, String anomalyType, String details) {
        try {
            String anomalyKey = ANOMALY_KEY + userId + ":" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(anomalyKey, details, 30, TimeUnit.DAYS);

            // 更新风险等级
            updateRiskLevel(userId);

            log.info("记录异常行为: userId={}, type={}, details={}", userId, anomalyType, details);
            return true;
        } catch (Exception e) {
            log.error("记录异常行为失败: userId={}, error={}", userId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public int getUserRiskLevel(Long userId) {
        try {
            String riskKey = RISK_KEY + userId;
            Object riskLevel = redisTemplate.opsForValue().get(riskKey);
            return riskLevel != null ? Integer.parseInt(riskLevel.toString()) : 0;
        } catch (Exception e) {
            log.error("获取用户风险等级失败: userId={}, error={}", userId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 更新用户风险等级
     */
    private void updateRiskLevel(Long userId) {
        try {
            // 计算异常记录数量
            int anomalyCount = countKeys(ANOMALY_KEY + userId + ":*");

            int riskLevel = 0;
            if (anomalyCount > 20) {
                riskLevel = 2; // 高风险
            } else if (anomalyCount > 10) {
                riskLevel = 1; // 中风险
            } else {
                riskLevel = 0; // 低风险
            }

            String riskKey = RISK_KEY + userId;
            redisTemplate.opsForValue().set(riskKey, riskLevel, 7, TimeUnit.DAYS);

            log.debug("更新用户风险等级: userId={}, anomalyCount={}, riskLevel={}",
                    userId, anomalyCount, riskLevel);
        } catch (Exception e) {
            log.error("更新用户风险等级失败: userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    private int countKeys(String pattern) {
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(1000).build();
        Integer count = redisTemplate.execute((RedisCallback<Integer>) connection -> {
            int total = 0;
            try (Cursor<byte[]> cursor = connection.scan(options)) {
                while (cursor.hasNext()) {
                    cursor.next();
                    total++;
                }
            }
            return total;
        });
        return count != null ? count : 0;
    }
}
