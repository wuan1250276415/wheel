package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.entity.ContentAuditLog;
import com.basebackend.wheel.mapper.ContentAuditLogMapper;
import com.basebackend.wheel.service.ViolationAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 违规预警服务实现
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Slf4j
@Service
public class ViolationAlertServiceImpl implements ViolationAlertService {

    private static final String ALERT_CACHE_KEY = "audit:violation:alert:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ContentAuditLogMapper auditLogMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 违规数量增长阈值倍数（默认2倍）
     */
    @Value("${audit.alert.threshold-multiplier:2.0}")
    private double thresholdMultiplier;

    /**
     * 预警检查时间窗口（小时）
     */
    @Value("${audit.alert.check-window-hours:1}")
    private int checkWindowHours;

    /**
     * 历史统计天数
     */
    @Value("${audit.alert.history-days:7}")
    private int historyDays;

    /**
     * 预警冷却时间（分钟）
     */
    @Value("${audit.alert.cooldown-minutes:30}")
    private int cooldownMinutes;

    @Override
    public void checkAndAlert() {
        try {
            // 检查是否在冷却期
            String cooldownKey = ALERT_CACHE_KEY + "cooldown";
            if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
                log.debug("违规预警在冷却期内，跳过检查");
                return;
            }

            // 获取当前时间段违规数量
            long currentViolations = getViolationCountInHours(checkWindowHours);
            
            // 获取历史平均值
            double avgViolationsPerHour = getAverageViolationPerHour(historyDays);
            long expectedViolations = (long) (avgViolationsPerHour * checkWindowHours);
            
            // 计算阈值
            long threshold = (long) (expectedViolations * thresholdMultiplier);
            
            // 如果阈值太低，设置最小阈值
            if (threshold < 5) {
                threshold = 5;
            }

            log.debug("违规预警检查: currentViolations={}, expectedViolations={}, threshold={}", 
                    currentViolations, expectedViolations, threshold);

            // 检查是否超过阈值
            if (currentViolations > threshold) {
                String message = String.format(
                        "违规内容数量异常增加！过去%d小时内检测到%d条违规内容，超过预期阈值%d条（历史平均%.1f条/小时）",
                        checkWindowHours, currentViolations, threshold, avgViolationsPerHour);
                
                sendAlertNotification("VIOLATION_SPIKE", currentViolations, threshold, message);
                
                // 设置冷却期
                redisTemplate.opsForValue().set(cooldownKey, "1", cooldownMinutes, TimeUnit.MINUTES);
            }

            // 检查拒绝率是否异常
            checkRejectionRateAlert();

        } catch (Exception e) {
            log.error("违规预警检查失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 检查拒绝率是否异常
     */
    private void checkRejectionRateAlert() {
        try {
            String cooldownKey = ALERT_CACHE_KEY + "rejection_rate_cooldown";
            if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
                return;
            }

            // 获取今日审核数据
            List<ContentAuditLog> todayLogs = auditLogMapper.selectTodayAuditLogs();
            if (todayLogs.size() < 10) {
                // 样本太少，不进行检查
                return;
            }

            long rejectedCount = todayLogs.stream()
                    .filter(log -> log.getAuditStatus() == 2)
                    .count();
            double rejectionRate = (double) rejectedCount / todayLogs.size() * 100;

            // 如果拒绝率超过50%，发送预警
            if (rejectionRate > 50) {
                String message = String.format(
                        "今日内容拒绝率异常！当前拒绝率%.1f%%（%d/%d），请关注内容质量或审核标准",
                        rejectionRate, rejectedCount, todayLogs.size());
                
                sendAlertNotification("HIGH_REJECTION_RATE", (long) rejectionRate, 50, message);
                
                // 设置冷却期
                redisTemplate.opsForValue().set(cooldownKey, "1", cooldownMinutes * 2, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            log.error("拒绝率预警检查失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendAlertNotification(String alertType, long currentCount, long threshold, String message) {
        log.warn("【违规预警】类型: {}, 当前值: {}, 阈值: {}, 消息: {}", 
                alertType, currentCount, threshold, message);
        
        // 记录预警日志到Redis
        String alertKey = ALERT_CACHE_KEY + "history:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String alertData = String.format("{\"type\":\"%s\",\"current\":%d,\"threshold\":%d,\"message\":\"%s\",\"time\":\"%s\"}", 
                alertType, currentCount, threshold, message, LocalDateTime.now().format(DATE_FORMATTER));
        redisTemplate.opsForValue().set(alertKey, alertData, 7, TimeUnit.DAYS);

        // TODO: 集成实际的通知渠道（如：邮件、短信、企业微信等）
        // 这里可以调用JPush或其他推送服务发送通知给管理员
    }

    @Override
    public long getViolationCountInHours(int hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        String startTimeStr = startTime.format(DATE_FORMATTER);
        
        List<ContentAuditLog> logs = auditLogMapper.selectAuditStatistics(startTimeStr, null);
        return logs.stream()
                .filter(log -> log.getAuditStatus() == 2)
                .count();
    }

    @Override
    public double getAverageViolationPerHour(int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        String startTimeStr = startTime.format(DATE_FORMATTER);
        
        List<ContentAuditLog> logs = auditLogMapper.selectAuditStatistics(startTimeStr, null);
        long totalViolations = logs.stream()
                .filter(log -> log.getAuditStatus() == 2)
                .count();
        
        // 计算总小时数
        int totalHours = days * 24;
        
        return totalHours > 0 ? (double) totalViolations / totalHours : 0;
    }
}
