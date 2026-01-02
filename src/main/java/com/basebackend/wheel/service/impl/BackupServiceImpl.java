package com.basebackend.wheel.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basebackend.wheel.config.BackupProperties;
import com.basebackend.wheel.dto.*;
import com.basebackend.wheel.entity.BackupRecord;
import com.basebackend.wheel.enums.AlertType;
import com.basebackend.wheel.mapper.BackupRecordMapper;
import com.basebackend.wheel.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

/**
 * 备份服务实现
 * 提供数据库备份的核心功能，包括执行备份、查询、删除和清理
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BackupServiceImpl implements BackupService {

    private final BackupRecordMapper backupRecordMapper;
    private final BackupConfigService backupConfigService;
    private final BackupStorageService backupStorageService;
    private final BackupMonitorService backupMonitorService;
    private final EncryptionService encryptionService;
    private final BackupProperties backupProperties;
    private final StringRedisTemplate redisTemplate;

    // ==================== Redis Key 常量 ====================

    private static final String BACKUP_LOCK_KEY = "backup:lock:executing";
    private static final int BACKUP_LOCK_TIMEOUT_MINUTES = 60;

    // ==================== 文件名格式 ====================

    private static final DateTimeFormatter FILENAME_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final String BACKUP_FILE_PREFIX = "backup_";
    private static final String BACKUP_FILE_EXTENSION = ".sql.gz";
    private static final String ENCRYPTED_FILE_EXTENSION = ".enc";

    // ==================== 备份执行 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BackupRecord executeBackup(int backupType, String description) {
        // 检查并发控制
        if (!acquireBackupLock()) {
            throw new IllegalStateException("已有备份任务正在执行，请稍后重试");
        }

        BackupRecord record = null;
        long startTime = System.currentTimeMillis();

        try {
            // 创建备份记录
            record = createBackupRecord(backupType, description);
            backupRecordMapper.insert(record);

            // 记录备份开始
            backupMonitorService.recordBackupStart(record.getId());
            backupMonitorService.updateBackupProgress(record.getId(), 5, BackupProgressVO.STAGE_PREPARING);

            // 执行备份（带重试）
            executeBackupWithRetry(record);

            // 更新备份记录
            long fileSize = backupStorageService.getBackupFileSize(record.getFilename());
            long durationMs = System.currentTimeMillis() - startTime;

            record.setFileSize(fileSize);
            record.setDurationMs(durationMs);
            record.setStatus(BackupRecord.STATUS_SUCCESS);
            backupRecordMapper.updateBackupStatus(
                    record.getId(),
                    BackupRecord.STATUS_SUCCESS,
                    fileSize,
                    durationMs,
                    null
            );

            // 记录备份完成
            backupMonitorService.recordBackupComplete(record.getId(), true);

            log.info("备份执行成功: id={}, filename={}, size={}, duration={}ms",
                    record.getId(), record.getFilename(), fileSize, durationMs);

            return record;

        } catch (Exception e) {
            log.error("备份执行失败", e);

            // 更新备份记录为失败
            if (record != null && record.getId() != null) {
                long durationMs = System.currentTimeMillis() - startTime;
                backupRecordMapper.updateBackupStatus(
                        record.getId(),
                        BackupRecord.STATUS_FAILED,
                        null,
                        durationMs,
                        e.getMessage()
                );
                backupMonitorService.recordBackupComplete(record.getId(), false, e.getMessage());
            }

            throw new RuntimeException("备份执行失败: " + e.getMessage(), e);

        } finally {
            releaseBackupLock();
        }
    }

    @Override
    public BackupResultVO triggerManualBackup(ManualBackupDTO dto) {
        String description = dto != null ? dto.getDescription() : null;

        try {
            BackupRecord record = executeBackup(BACKUP_TYPE_MANUAL, description);
            return BackupResultVO.success(
                    record.getId(),
                    record.getFilename(),
                    record.getBackupType(),
                    record.getFileSize() != null ? record.getFileSize() : 0,
                    record.getDurationMs() != null ? record.getDurationMs() : 0,
                    record.isEncryptedFile()
            );
        } catch (IllegalStateException e) {
            // 并发控制异常
            return BackupResultVO.failed(null, e.getMessage());
        } catch (Exception e) {
            return BackupResultVO.failed(null, e.getMessage());
        }
    }

    @Override
    public BackupRecord executeDailyBackup() {
        return executeBackup(BACKUP_TYPE_DAILY, "每日自动备份");
    }

    @Override
    public BackupRecord executeWeeklyBackup() {
        return executeBackup(BACKUP_TYPE_WEEKLY, "每周自动备份");
    }

    // ==================== 备份查询 ====================

    @Override
    public IPage<BackupRecordVO> listBackups(BackupQueryDTO query) {
        Page<BackupRecord> page = new Page<>(
                query.getPageNum() != null ? query.getPageNum() : 1,
                query.getPageSize() != null ? query.getPageSize() : 10
        );

        IPage<BackupRecord> recordPage = backupRecordMapper.selectBackupPage(
                page,
                query.getBackupType(),
                query.getStatus(),
                query.getStartTime(),
                query.getEndTime()
        );

        // 转换为 VO
        Page<BackupRecordVO> voPage = new Page<>(recordPage.getCurrent(), recordPage.getSize(), recordPage.getTotal());
        voPage.setRecords(
                recordPage.getRecords().stream()
                        .map(BackupRecordVO::fromEntity)
                        .collect(Collectors.toList())
        );

        return voPage;
    }

    @Override
    public BackupRecordVO getBackupById(Long backupId) {
        BackupRecord record = getBackupEntityById(backupId);
        return BackupRecordVO.fromEntity(record);
    }

    @Override
    public BackupRecord getBackupEntityById(Long backupId) {
        if (backupId == null) {
            return null;
        }
        return backupRecordMapper.selectById(backupId);
    }

    @Override
    public BackupRecord getLastSuccessBackup() {
        return backupRecordMapper.selectLastSuccessBackup();
    }

    // ==================== 备份管理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBackup(Long backupId) {
        BackupRecord record = backupRecordMapper.selectById(backupId);
        if (record == null) {
            throw new IllegalArgumentException("备份记录不存在: " + backupId);
        }

        // 删除物理文件
        if (StringUtils.hasText(record.getFilename())) {
            try {
                backupStorageService.deleteBackupFile(record.getFilename());
            } catch (Exception e) {
                log.warn("删除备份文件失败: {}", record.getFilename(), e);
            }
        }

        // 删除数据库记录（逻辑删除）
        backupRecordMapper.deleteById(backupId);

        log.info("备份删除成功: id={}, filename={}", backupId, record.getFilename());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanupExpiredBackups() {
        int retentionDays = backupConfigService.getRetentionDays();
        int minBackupCount = backupConfigService.getMinBackupCount();

        // 获取过期时间点
        LocalDateTime expireTime = LocalDateTime.now().minusDays(retentionDays);

        // 获取所有过期备份
        List<BackupRecord> expiredBackups = backupRecordMapper.selectBackupsBeforeTime(expireTime);

        if (expiredBackups.isEmpty()) {
            log.info("没有过期备份需要清理");
            return 0;
        }

        // 获取当前总备份数
        int totalBackups = backupRecordMapper.countTotalBackups();

        // 计算可以删除的数量（保证最少保留数量）
        int canDeleteCount = Math.max(0, totalBackups - minBackupCount);
        int toDeleteCount = Math.min(expiredBackups.size(), canDeleteCount);

        if (toDeleteCount <= 0) {
            log.info("需要保留最少{}个备份，跳过清理", minBackupCount);
            return 0;
        }

        // 删除过期备份（按时间排序，先删除最旧的）
        int deletedCount = 0;
        for (int i = 0; i < toDeleteCount; i++) {
            BackupRecord record = expiredBackups.get(i);
            try {
                deleteBackup(record.getId());
                deletedCount++;
            } catch (Exception e) {
                log.error("清理备份失败: id={}", record.getId(), e);
            }
        }

        log.info("过期备份清理完成: 清理{}个，保留{}个", deletedCount, totalBackups - deletedCount);
        return deletedCount;
    }

    // ==================== 备份进度 ====================

    @Override
    public BackupProgressVO getBackupProgress(Long backupId) {
        return backupMonitorService.getBackupProgress(backupId);
    }

    @Override
    public boolean isBackupInProgress() {
        return backupMonitorService.isBackupInProgress();
    }

    // ==================== 备份统计 ====================

    @Override
    public BackupStatisticsVO getBackupStatistics() {
        // 获取统计数据
        int totalCount = backupRecordMapper.countTotalBackups();
        int successCount = backupRecordMapper.countSuccessBackups();
        int failureCount = backupRecordMapper.countFailedBackups();
        Long totalStorageUsed = backupRecordMapper.sumTotalStorageUsed();

        // 获取时间段统计
        int todayBackupCount = backupRecordMapper.countTodayBackups();
        int weekBackupCount = backupRecordMapper.countWeekBackups();
        int monthBackupCount = backupRecordMapper.countMonthBackups();

        if (totalCount == 0) {
            BackupStatisticsVO emptyStats = BackupStatisticsVO.empty();
            emptyStats.setTodayBackupCount(0);
            emptyStats.setWeekBackupCount(0);
            emptyStats.setMonthBackupCount(0);
            return emptyStats;
        }

        // 获取最后一次成功备份
        BackupRecord lastBackup = backupRecordMapper.selectLastSuccessBackup();

        // 计算成功率
        double successRate = BackupStatisticsVO.calculateSuccessRate(successCount, totalCount);

        // 获取存储使用情况
        BackupStorageService.StorageUsage storageUsage = backupStorageService.getStorageUsage();

        // 获取健康状态
        BackupMonitorService.HealthCheckResult healthResult = backupMonitorService.getHealthCheckResult();

        return BackupStatisticsVO.builder()
                .lastBackupTime(lastBackup != null ? lastBackup.getCreateTime() : null)
                .lastBackupSize(lastBackup != null ? lastBackup.getFileSize() : null)
                .lastBackupSizeFormatted(lastBackup != null && lastBackup.getFileSize() != null
                        ? BackupStatisticsVO.formatFileSize(lastBackup.getFileSize()) : null)
                .totalBackupCount(totalCount)
                .successCount(successCount)
                .failureCount(failureCount)
                .successRate(successRate)
                .successRateFormatted(String.format("%.2f%%", successRate))
                .totalStorageUsed(totalStorageUsed != null ? totalStorageUsed : 0L)
                .totalStorageUsedFormatted(BackupStatisticsVO.formatFileSize(totalStorageUsed != null ? totalStorageUsed : 0L))
                .availableStorage(storageUsage.getAvailableBytes())
                .availableStorageFormatted(BackupStatisticsVO.formatFileSize(storageUsage.getAvailableBytes()))
                .storageUsagePercent(storageUsage.getUsagePercent())
                .healthStatus(healthResult.getStatus().getName())
                .healthStatusCode(healthResult.getStatus().getCode())
                .healthMessage(healthResult.getMessage())
                .hoursSinceLastBackup(healthResult.getHoursSinceLastBackup())
                .backupInProgress(healthResult.hasBackupInProgress())
                .restoreInProgress(healthResult.hasRestoreInProgress())
                .todayBackupCount(todayBackupCount)
                .weekBackupCount(weekBackupCount)
                .monthBackupCount(monthBackupCount)
                .build();
    }

    // ==================== 私有方法 ====================

    /**
     * 创建备份记录
     */
    private BackupRecord createBackupRecord(int backupType, String description) {
        String filename = generateBackupFilename(backupType);
        boolean encryptionEnabled = backupConfigService.isEncryptionEnabled() && encryptionService.isAvailable();

        BackupRecord record = new BackupRecord();
        record.setBackupType(backupType);
        record.setFilename(filename);
        record.setFilePath(backupStorageService.getBackupFilePath(filename));
        record.setIsEncrypted(encryptionEnabled ? BackupRecord.ENCRYPTED_YES : BackupRecord.ENCRYPTED_NO);
        record.setStatus(BackupRecord.STATUS_IN_PROGRESS);
        record.setDescription(description);

        return record;
    }

    /**
     * 生成备份文件名
     */
    private String generateBackupFilename(int backupType) {
        String typePrefix;
        switch (backupType) {
            case BACKUP_TYPE_DAILY:
                typePrefix = "daily";
                break;
            case BACKUP_TYPE_WEEKLY:
                typePrefix = "weekly";
                break;
            case BACKUP_TYPE_MANUAL:
                typePrefix = "manual";
                break;
            default:
                typePrefix = "unknown";
        }

        String timestamp = LocalDateTime.now().format(FILENAME_DATE_FORMAT);
        String filename = BACKUP_FILE_PREFIX + typePrefix + "_" + timestamp + BACKUP_FILE_EXTENSION;

        // 如果启用加密，添加加密扩展名
        if (backupConfigService.isEncryptionEnabled() && encryptionService.isAvailable()) {
            filename += ENCRYPTED_FILE_EXTENSION;
        }

        return filename;
    }

    /**
     * 执行备份（带重试机制）
     */
    private String executeBackupWithRetry(BackupRecord record) throws Exception {
        BackupProperties.RetryConfig retryConfig = backupProperties.getRetry();
        int maxRetries = retryConfig.getMaxRetries();
        long delayMs = retryConfig.getInitialDelayMs();
        double multiplier = retryConfig.getMultiplier();
        long maxDelayMs = retryConfig.getMaxDelayMs();

        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("执行备份，尝试次数: {}/{}", attempt, maxRetries);
                return doExecuteBackup(record);
            } catch (Exception e) {
                lastException = e;
                log.warn("备份执行失败，尝试次数: {}/{}, 错误: {}", attempt, maxRetries, e.getMessage());

                if (attempt < maxRetries) {
                    // 指数退避
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("备份被中断", ie);
                    }
                    delayMs = Math.min((long) (delayMs * multiplier), maxDelayMs);
                }
            }
        }

        // 所有重试都失败，发送告警
        backupMonitorService.sendAlert(AlertType.BACKUP_FAILED,
                "备份执行失败，已重试" + maxRetries + "次",
                lastException != null ? lastException.getMessage() : "未知错误");

        throw lastException != null ? lastException : new RuntimeException("备份执行失败");
    }

    /**
     * 实际执行备份
     */
    private String doExecuteBackup(BackupRecord record) throws Exception {
        // 确保存储目录存在
        backupStorageService.ensureStorageDirectoryExists();

        // 创建临时文件
        Path tempDir = Files.createTempDirectory("backup_");
        Path tempSqlFile = tempDir.resolve("dump.sql");
        Path tempGzFile = tempDir.resolve("dump.sql.gz");

        try {
            // 阶段1: 执行 mysqldump
            backupMonitorService.updateBackupProgress(record.getId(), 10, BackupProgressVO.STAGE_EXPORTING);
            executeMysqldump(tempSqlFile.toString());

            // 阶段2: 压缩文件
            backupMonitorService.updateBackupProgress(record.getId(), 50, BackupProgressVO.STAGE_COMPRESSING);
            compressFile(tempSqlFile.toString(), tempGzFile.toString());

            // 删除原始 SQL 文件
            Files.deleteIfExists(tempSqlFile);

            String finalFilePath;
            boolean encryptionEnabled = backupConfigService.isEncryptionEnabled() && encryptionService.isAvailable();

            if (encryptionEnabled) {
                // 阶段3: 加密文件
                backupMonitorService.updateBackupProgress(record.getId(), 70, BackupProgressVO.STAGE_ENCRYPTING);
                Path tempEncFile = tempDir.resolve("dump.sql.gz.enc");
                encryptionService.encryptFile(tempGzFile.toString(), tempEncFile.toString());

                // 删除压缩文件
                Files.deleteIfExists(tempGzFile);

                // 保存到存储目录
                finalFilePath = backupStorageService.saveBackupFile(tempEncFile.toString(), record.getFilename());
            } else {
                // 直接保存压缩文件
                finalFilePath = backupStorageService.saveBackupFile(tempGzFile.toString(), record.getFilename());
            }

            // 阶段4: 完成
            backupMonitorService.updateBackupProgress(record.getId(), 100, BackupProgressVO.STAGE_COMPLETED);

            return finalFilePath;

        } finally {
            // 清理临时目录
            try {
                Files.deleteIfExists(tempSqlFile);
                Files.deleteIfExists(tempGzFile);
                Files.deleteIfExists(tempDir);
            } catch (Exception e) {
                log.warn("清理临时文件失败", e);
            }
        }
    }

    /**
     * 执行 mysqldump 命令
     */
    private void executeMysqldump(String outputPath) throws Exception {
        BackupProperties.MysqlConfig mysqlConfig = backupProperties.getMysql();

        // 构建 mysqldump 命令
        ProcessBuilder pb = new ProcessBuilder(
                mysqlConfig.getMysqldumpPath(),
                "-h", mysqlConfig.getHost(),
                "-P", String.valueOf(mysqlConfig.getPort()),
                "-u", mysqlConfig.getUsername(),
                "--password=" + mysqlConfig.getPassword(),
                mysqlConfig.getDatabase()
        );

        // 添加额外参数
        if (StringUtils.hasText(mysqlConfig.getExtraArgs())) {
            String[] extraArgs = mysqlConfig.getExtraArgs().split("\\s+");
            for (String arg : extraArgs) {
                if (StringUtils.hasText(arg)) {
                    pb.command().add(arg);
                }
            }
        }

        // 重定向输出到文件
        pb.redirectOutput(new File(outputPath));
        pb.redirectErrorStream(false);

        log.info("执行 mysqldump: database={}", mysqlConfig.getDatabase());

        Process process = pb.start();

        // 读取错误输出
        StringBuilder errorOutput = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            String error = errorOutput.toString();
            log.error("mysqldump 执行失败: exitCode={}, error={}", exitCode, error);
            throw new RuntimeException("mysqldump 执行失败: " + error);
        }

        log.info("mysqldump 执行成功: output={}", outputPath);
    }

    /**
     * 压缩文件
     */
    private void compressFile(String sourcePath, String targetPath) throws IOException {
        try (FileInputStream fis = new FileInputStream(sourcePath);
             FileOutputStream fos = new FileOutputStream(targetPath);
             GZIPOutputStream gzos = new GZIPOutputStream(fos)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                gzos.write(buffer, 0, bytesRead);
            }
        }

        log.info("文件压缩成功: {} -> {}", sourcePath, targetPath);
    }

    /**
     * 获取备份锁
     */
    private boolean acquireBackupLock() {
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
                BACKUP_LOCK_KEY,
                String.valueOf(System.currentTimeMillis()),
                BACKUP_LOCK_TIMEOUT_MINUTES,
                TimeUnit.MINUTES
        );
        return Boolean.TRUE.equals(acquired);
    }

    /**
     * 释放备份锁
     */
    private void releaseBackupLock() {
        redisTemplate.delete(BACKUP_LOCK_KEY);
    }
}
