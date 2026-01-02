package com.basebackend.wheel.scheduler;

import com.basebackend.wheel.config.BackupProperties;
import com.basebackend.wheel.entity.BackupRecord;
import com.basebackend.wheel.enums.AlertType;
import com.basebackend.wheel.event.BackupConfigChangedEvent;
import com.basebackend.wheel.service.BackupConfigService;
import com.basebackend.wheel.service.BackupMonitorService;
import com.basebackend.wheel.service.BackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 备份调度器
 * 负责定时执行备份任务、清理过期备份和健康检查
 * 支持运行时动态更新 cron 表达式
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BackupScheduler implements InitializingBean, DisposableBean {

    private final BackupService backupService;
    private final BackupConfigService backupConfigService;
    private final BackupMonitorService backupMonitorService;
    private final BackupProperties backupProperties;

    /**
     * 任务调度器
     */
    private TaskScheduler taskScheduler;

    /**
     * 已调度的任务映射
     */
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    /**
     * 当前使用的 cron 表达式映射
     */
    private final Map<String, String> currentCronExpressions = new ConcurrentHashMap<>();

    // ==================== 任务名称常量 ====================

    public static final String TASK_DAILY_BACKUP = "dailyBackup";
    public static final String TASK_WEEKLY_BACKUP = "weeklyBackup";
    public static final String TASK_CLEANUP = "cleanup";
    public static final String TASK_HEALTH_CHECK = "healthCheck";

    // ==================== 初始化和销毁 ====================

    @Override
    public void afterPropertiesSet() {
        if (!backupProperties.isEnabled()) {
            log.info("备份功能已禁用，跳过调度器初始化");
            return;
        }

        // 初始化任务调度器
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("backup-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.initialize();
        this.taskScheduler = scheduler;

        // 调度所有任务
        scheduleAllTasks();

        log.info("备份调度器初始化完成");
    }

    @Override
    public void destroy() {
        // 取消所有任务
        cancelAllTasks();

        // 关闭调度器
        if (taskScheduler instanceof ThreadPoolTaskScheduler) {
            ((ThreadPoolTaskScheduler) taskScheduler).shutdown();
        }

        log.info("备份调度器已关闭");
    }

    // ==================== 任务调度方法 ====================

    /**
     * 调度所有任务
     */
    public void scheduleAllTasks() {
        scheduleDailyBackup();
        scheduleWeeklyBackup();
        scheduleCleanup();
        scheduleHealthCheck();
    }

    /**
     * 调度每日备份任务
     */
    public void scheduleDailyBackup() {
        String cronExpression = backupConfigService.getDailyCron();
        scheduleTask(TASK_DAILY_BACKUP, cronExpression, this::executeDailyBackup);
    }

    /**
     * 调度每周备份任务
     */
    public void scheduleWeeklyBackup() {
        String cronExpression = backupConfigService.getWeeklyCron();
        scheduleTask(TASK_WEEKLY_BACKUP, cronExpression, this::executeWeeklyBackup);
    }

    /**
     * 调度清理任务
     */
    public void scheduleCleanup() {
        String cronExpression = backupConfigService.getCleanupCron();
        scheduleTask(TASK_CLEANUP, cronExpression, this::executeCleanup);
    }

    /**
     * 调度健康检查任务
     */
    public void scheduleHealthCheck() {
        String cronExpression = backupConfigService.getHealthCheckCron();
        scheduleTask(TASK_HEALTH_CHECK, cronExpression, this::executeHealthCheck);
    }

    /**
     * 调度单个任务
     *
     * @param taskName       任务名称
     * @param cronExpression cron 表达式
     * @param task           任务执行逻辑
     */
    private void scheduleTask(String taskName, String cronExpression, Runnable task) {
        if (taskScheduler == null) {
            log.warn("任务调度器未初始化，跳过任务调度: {}", taskName);
            return;
        }

        // 取消已存在的任务
        cancelTask(taskName);

        try {
            // 验证 cron 表达式
            if (!backupConfigService.isValidCronExpression(cronExpression)) {
                log.error("无效的 cron 表达式: taskName={}, cron={}", taskName, cronExpression);
                return;
            }

            // 调度新任务
            CronTrigger trigger = new CronTrigger(cronExpression);
            ScheduledFuture<?> future = taskScheduler.schedule(task, trigger);

            scheduledTasks.put(taskName, future);
            currentCronExpressions.put(taskName, cronExpression);

            log.info("任务调度成功: taskName={}, cron={}", taskName, cronExpression);

        } catch (Exception e) {
            log.error("任务调度失败: taskName={}, cron={}", taskName, cronExpression, e);
        }
    }

    /**
     * 取消单个任务
     *
     * @param taskName 任务名称
     */
    public void cancelTask(String taskName) {
        ScheduledFuture<?> future = scheduledTasks.remove(taskName);
        if (future != null) {
            future.cancel(false);
            currentCronExpressions.remove(taskName);
            log.info("任务已取消: {}", taskName);
        }
    }

    /**
     * 取消所有任务
     */
    public void cancelAllTasks() {
        scheduledTasks.forEach((name, future) -> {
            if (future != null) {
                future.cancel(false);
            }
        });
        scheduledTasks.clear();
        currentCronExpressions.clear();
        log.info("所有任务已取消");
    }

    // ==================== 动态配置更新 ====================

    /**
     * 刷新所有任务的调度配置
     * 当配置更新时调用此方法
     */
    public void refreshSchedule() {
        if (!backupProperties.isEnabled()) {
            log.info("备份功能已禁用，跳过调度刷新");
            return;
        }

        log.info("开始刷新备份调度配置");

        // 检查并更新每日备份
        refreshTaskIfChanged(TASK_DAILY_BACKUP, backupConfigService.getDailyCron(), this::executeDailyBackup);

        // 检查并更新每周备份
        refreshTaskIfChanged(TASK_WEEKLY_BACKUP, backupConfigService.getWeeklyCron(), this::executeWeeklyBackup);

        // 检查并更新清理任务
        refreshTaskIfChanged(TASK_CLEANUP, backupConfigService.getCleanupCron(), this::executeCleanup);

        // 检查并更新健康检查
        refreshTaskIfChanged(TASK_HEALTH_CHECK, backupConfigService.getHealthCheckCron(), this::executeHealthCheck);

        log.info("备份调度配置刷新完成");
    }

    /**
     * 监听备份配置变更事件
     * 当配置更新时自动刷新调度
     *
     * @param event 配置变更事件
     */
    @EventListener
    public void onBackupConfigChanged(BackupConfigChangedEvent event) {
        log.info("收到备份配置变更事件: configKey={}, isBatchUpdate={}",
                event.getConfigKey(), event.isBatchUpdate());

        // 只有 cron 相关的配置变更才需要刷新调度
        if (event.isCronRelated()) {
            refreshSchedule();
        }
    }

    /**
     * 如果 cron 表达式发生变化，则刷新任务
     *
     * @param taskName          任务名称
     * @param newCronExpression 新的 cron 表达式
     * @param task              任务执行逻辑
     */
    private void refreshTaskIfChanged(String taskName, String newCronExpression, Runnable task) {
        String currentCron = currentCronExpressions.get(taskName);

        if (currentCron == null || !currentCron.equals(newCronExpression)) {
            log.info("检测到 cron 表达式变化: taskName={}, old={}, new={}",
                    taskName, currentCron, newCronExpression);
            scheduleTask(taskName, newCronExpression, task);
        }
    }

    /**
     * 更新单个任务的 cron 表达式
     *
     * @param taskName          任务名称
     * @param newCronExpression 新的 cron 表达式
     */
    public void updateTaskCron(String taskName, String newCronExpression) {
        if (!backupConfigService.isValidCronExpression(newCronExpression)) {
            throw new IllegalArgumentException("无效的 cron 表达式: " + newCronExpression);
        }

        Runnable task;
        switch (taskName) {
            case TASK_DAILY_BACKUP:
                task = this::executeDailyBackup;
                break;
            case TASK_WEEKLY_BACKUP:
                task = this::executeWeeklyBackup;
                break;
            case TASK_CLEANUP:
                task = this::executeCleanup;
                break;
            case TASK_HEALTH_CHECK:
                task = this::executeHealthCheck;
                break;
            default:
                throw new IllegalArgumentException("未知的任务名称: " + taskName);
        }

        scheduleTask(taskName, newCronExpression, task);
    }

    // ==================== 任务执行方法 ====================

    /**
     * 执行每日备份
     */
    public void executeDailyBackup() {
        log.info("开始执行每日备份定时任务");
        try {
            BackupRecord record = backupService.executeDailyBackup();
            log.info("每日备份定时任务执行成功: id={}, filename={}, size={}",
                    record.getId(), record.getFilename(), record.getFileSize());
        } catch (IllegalStateException e) {
            // 并发控制异常，可能有其他备份正在执行
            log.warn("每日备份跳过执行: {}", e.getMessage());
        } catch (Exception e) {
            log.error("每日备份定时任务执行失败", e);
            // 发送告警
            backupMonitorService.sendAlert(AlertType.BACKUP_FAILED,
                    "每日备份执行失败",
                    e.getMessage());
        }
    }

    /**
     * 执行每周备份
     */
    public void executeWeeklyBackup() {
        log.info("开始执行每周备份定时任务");
        try {
            BackupRecord record = backupService.executeWeeklyBackup();
            log.info("每周备份定时任务执行成功: id={}, filename={}, size={}",
                    record.getId(), record.getFilename(), record.getFileSize());
        } catch (IllegalStateException e) {
            // 并发控制异常，可能有其他备份正在执行
            log.warn("每周备份跳过执行: {}", e.getMessage());
        } catch (Exception e) {
            log.error("每周备份定时任务执行失败", e);
            // 发送告警
            backupMonitorService.sendAlert(AlertType.BACKUP_FAILED,
                    "每周备份执行失败",
                    e.getMessage());
        }
    }

    /**
     * 执行清理任务
     */
    public void executeCleanup() {
        log.info("开始执行备份清理定时任务");
        try {
            int cleanedCount = backupService.cleanupExpiredBackups();
            log.info("备份清理定时任务执行成功: 清理了{}个过期备份", cleanedCount);
        } catch (Exception e) {
            log.error("备份清理定时任务执行失败", e);
            // 发送告警
            backupMonitorService.sendAlert(AlertType.CLEANUP_FAILED,
                    "备份清理执行失败",
                    e.getMessage());
        }
    }

    /**
     * 执行健康检查
     */
    public void executeHealthCheck() {
        log.debug("开始执行备份健康检查定时任务");
        try {
            backupMonitorService.performHealthCheck();
            log.debug("备份健康检查定时任务执行成功");
        } catch (Exception e) {
            log.error("备份健康检查定时任务执行失败", e);
        }
    }

    // ==================== 状态查询方法 ====================

    /**
     * 获取任务是否正在运行
     *
     * @param taskName 任务名称
     * @return 是否正在运行
     */
    public boolean isTaskScheduled(String taskName) {
        ScheduledFuture<?> future = scheduledTasks.get(taskName);
        return future != null && !future.isCancelled() && !future.isDone();
    }

    /**
     * 获取任务当前的 cron 表达式
     *
     * @param taskName 任务名称
     * @return cron 表达式
     */
    public String getTaskCronExpression(String taskName) {
        return currentCronExpressions.get(taskName);
    }

    /**
     * 获取所有任务的状态
     *
     * @return 任务状态映射
     */
    public Map<String, TaskStatus> getAllTaskStatus() {
        Map<String, TaskStatus> statusMap = new ConcurrentHashMap<>();

        for (String taskName : new String[]{TASK_DAILY_BACKUP, TASK_WEEKLY_BACKUP, TASK_CLEANUP, TASK_HEALTH_CHECK}) {
            ScheduledFuture<?> future = scheduledTasks.get(taskName);
            String cronExpression = currentCronExpressions.get(taskName);

            TaskStatus status = new TaskStatus();
            status.setTaskName(taskName);
            status.setCronExpression(cronExpression);
            status.setScheduled(future != null && !future.isCancelled());
            status.setCancelled(future != null && future.isCancelled());
            status.setDone(future != null && future.isDone());

            statusMap.put(taskName, status);
        }

        return statusMap;
    }

    /**
     * 任务状态
     */
    @lombok.Data
    public static class TaskStatus {
        private String taskName;
        private String cronExpression;
        private boolean scheduled;
        private boolean cancelled;
        private boolean done;
    }

    // ==================== 手动触发方法 ====================

    /**
     * 手动触发每日备份
     */
    public void triggerDailyBackup() {
        log.info("手动触发每日备份");
        executeDailyBackup();
    }

    /**
     * 手动触发每周备份
     */
    public void triggerWeeklyBackup() {
        log.info("手动触发每周备份");
        executeWeeklyBackup();
    }

    /**
     * 手动触发清理任务
     */
    public void triggerCleanup() {
        log.info("手动触发备份清理");
        executeCleanup();
    }

    /**
     * 手动触发健康检查
     */
    public void triggerHealthCheck() {
        log.info("手动触发健康检查");
        executeHealthCheck();
    }
}
