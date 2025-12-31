package com.basebackend.wheel.scheduler;

import com.basebackend.wheel.service.ViolationAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 违规预警定时任务
 * 
 * 定期检查违规内容数量，异常增加时发送预警通知
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Slf4j
@Component
public class ViolationAlertScheduler {

    @Autowired
    private ViolationAlertService violationAlertService;

    /**
     * 是否启用违规预警
     */
    @Value("${audit.alert.enabled:true}")
    private boolean alertEnabled;

    /**
     * 每15分钟检查一次违规内容数量
     */
    @Scheduled(fixedRateString = "${audit.alert.check-interval-ms:900000}")
    public void checkViolationAlert() {
        if (!alertEnabled) {
            log.debug("违规预警功能已禁用");
            return;
        }

        log.debug("开始执行违规预警检查...");
        try {
            violationAlertService.checkAndAlert();
            log.debug("违规预警检查完成");
        } catch (Exception e) {
            log.error("违规预警检查执行失败: {}", e.getMessage(), e);
        }
    }
}
