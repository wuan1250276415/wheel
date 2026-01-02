package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.config.BackupProperties;
import com.basebackend.wheel.dto.*;
import com.basebackend.wheel.enums.AlertType;
import com.basebackend.wheel.enums.BackupHealthStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 备份服务基础验证测试
 * Checkpoint 6: 验证配置服务、加密服务、存储服务、监控服务的基础功能
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@DisplayName("备份服务基础验证测试")
class BackupServicesCheckpointTest {

    @Nested
    @DisplayName("BackupProperties 配置属性测试")
    class BackupPropertiesTest {

        private BackupProperties properties;

        @BeforeEach
        void setUp() {
            properties = new BackupProperties();
        }

        @Test
        @DisplayName("默认配置值应正确设置")
        void testDefaultValues() {
            assertTrue(properties.isEnabled());
            assertEquals("0 0 2 * * ?", properties.getDailyCron());
            assertEquals("0 0 3 ? * SUN", properties.getWeeklyCron());
            assertEquals("0 0 4 * * ?", properties.getCleanupCron());
            assertEquals("0 0 * * * ?", properties.getHealthCheckCron());
            assertEquals(30, properties.getRetentionDays());
            assertEquals(7, properties.getMinBackupCount());
            assertEquals("/data/backups", properties.getStoragePath());
            assertTrue(properties.isEncryptionEnabled());
            assertEquals(48, properties.getAlertThresholdHours());
            assertEquals(50, properties.getStorageWarningThresholdGb());
        }

        @Test
        @DisplayName("MySQL配置默认值应正确设置")
        void testMysqlConfigDefaults() {
            BackupProperties.MysqlConfig mysql = properties.getMysql();
            assertNotNull(mysql);
            assertEquals("mysqldump", mysql.getMysqldumpPath());
            assertEquals("mysql", mysql.getMysqlPath());
            assertEquals("localhost", mysql.getHost());
            assertEquals(3306, mysql.getPort());
        }

        @Test
        @DisplayName("重试配置默认值应正确设置")
        void testRetryConfigDefaults() {
            BackupProperties.RetryConfig retry = properties.getRetry();
            assertNotNull(retry);
            assertEquals(3, retry.getMaxRetries());
            assertEquals(1000, retry.getInitialDelayMs());
            assertEquals(2.0, retry.getMultiplier());
            assertEquals(30000, retry.getMaxDelayMs());
        }

        @Test
        @DisplayName("配置值应可修改")
        void testConfigModification() {
            properties.setRetentionDays(60);
            properties.setMinBackupCount(14);
            properties.setStoragePath("/custom/backups");

            assertEquals(60, properties.getRetentionDays());
            assertEquals(14, properties.getMinBackupCount());
            assertEquals("/custom/backups", properties.getStoragePath());
        }
    }

    @Nested
    @DisplayName("BackupConfigDTO 验证测试")
    class BackupConfigDTOTest {

        @Test
        @DisplayName("DTO应正确存储配置值")
        void testDTOValues() {
            BackupConfigDTO dto = new BackupConfigDTO();
            dto.setDailyCron("0 0 3 * * ?");
            dto.setWeeklyCron("0 0 4 ? * MON");
            dto.setRetentionDays(45);
            dto.setMinBackupCount(10);
            dto.setStoragePath("/test/backups");
            dto.setEncryptionEnabled(false);
            dto.setAlertThresholdHours(72);
            dto.setStorageWarningThresholdGb(100);

            assertEquals("0 0 3 * * ?", dto.getDailyCron());
            assertEquals("0 0 4 ? * MON", dto.getWeeklyCron());
            assertEquals(45, dto.getRetentionDays());
            assertEquals(10, dto.getMinBackupCount());
            assertEquals("/test/backups", dto.getStoragePath());
            assertFalse(dto.getEncryptionEnabled());
            assertEquals(72, dto.getAlertThresholdHours());
            assertEquals(100, dto.getStorageWarningThresholdGb());
        }
    }

    @Nested
    @DisplayName("BackupProgressVO 进度VO测试")
    class BackupProgressVOTest {

        @Test
        @DisplayName("初始进度应正确创建")
        void testInitialProgress() {
            Long backupId = 123L;
            BackupProgressVO progress = BackupProgressVO.initial(backupId);

            assertEquals(backupId, progress.getBackupId());
            assertEquals(0, progress.getProgress());
            assertEquals("准备中", progress.getStage());
            assertEquals(BackupProgressVO.STAGE_PREPARING, progress.getStageCode());
            assertFalse(progress.getCompleted());
            assertFalse(progress.getSuccess());
            assertNotNull(progress.getStartTimestamp());
        }

        @Test
        @DisplayName("完成进度应正确创建")
        void testCompletedProgress() {
            Long backupId = 456L;
            long elapsedMs = 5000;
            
            BackupProgressVO successProgress = BackupProgressVO.completed(backupId, true, elapsedMs, null);
            assertEquals(100, successProgress.getProgress());
            assertEquals("完成", successProgress.getStage());
            assertTrue(successProgress.getCompleted());
            assertTrue(successProgress.getSuccess());
            assertNull(successProgress.getErrorMessage());

            BackupProgressVO failedProgress = BackupProgressVO.completed(backupId, false, elapsedMs, "Test error");
            assertEquals(100, failedProgress.getProgress());
            assertEquals("失败", failedProgress.getStage());
            assertTrue(failedProgress.getCompleted());
            assertFalse(failedProgress.getSuccess());
            assertEquals("Test error", failedProgress.getErrorMessage());
        }

        @Test
        @DisplayName("阶段名称应正确返回")
        void testStageName() {
            assertEquals("准备中", BackupProgressVO.getStageName(BackupProgressVO.STAGE_PREPARING));
            assertEquals("导出中", BackupProgressVO.getStageName(BackupProgressVO.STAGE_EXPORTING));
            assertEquals("压缩中", BackupProgressVO.getStageName(BackupProgressVO.STAGE_COMPRESSING));
            assertEquals("加密中", BackupProgressVO.getStageName(BackupProgressVO.STAGE_ENCRYPTING));
            assertEquals("完成", BackupProgressVO.getStageName(BackupProgressVO.STAGE_COMPLETED));
            assertEquals("失败", BackupProgressVO.getStageName(BackupProgressVO.STAGE_FAILED));
            assertEquals("未知", BackupProgressVO.getStageName(99));
        }

        @Test
        @DisplayName("时长格式化应正确")
        void testFormatDuration() {
            assertEquals("0秒", BackupProgressVO.formatDuration(0));
            assertEquals("0秒", BackupProgressVO.formatDuration(-100));
            assertEquals("5秒", BackupProgressVO.formatDuration(5000));
            assertEquals("1分30秒", BackupProgressVO.formatDuration(90000));
            assertEquals("1小时30分0秒", BackupProgressVO.formatDuration(5400000));
        }

        @Test
        @DisplayName("字节格式化应正确")
        void testFormatBytes() {
            assertEquals("未知", BackupProgressVO.formatBytes(-1));
            assertEquals("0 B", BackupProgressVO.formatBytes(0));
            assertEquals("512 B", BackupProgressVO.formatBytes(512));
            assertEquals("1.00 KB", BackupProgressVO.formatBytes(1024));
            assertEquals("1.50 MB", BackupProgressVO.formatBytes(1572864));
            assertEquals("2.00 GB", BackupProgressVO.formatBytes(2147483648L));
        }
    }

    @Nested
    @DisplayName("AlertType 告警类型枚举测试")
    class AlertTypeTest {

        @Test
        @DisplayName("告警类型应正确定义")
        void testAlertTypes() {
            assertEquals(1, AlertType.BACKUP_FAILED.getCode());
            assertEquals("备份失败", AlertType.BACKUP_FAILED.getName());
            
            assertEquals(3, AlertType.NO_RECENT_BACKUP.getCode());
            assertEquals("无近期备份", AlertType.NO_RECENT_BACKUP.getName());
            
            assertEquals(4, AlertType.STORAGE_WARNING.getCode());
            assertEquals("存储空间警告", AlertType.STORAGE_WARNING.getName());
        }

        @Test
        @DisplayName("严重告警判断应正确")
        void testIsCritical() {
            assertTrue(AlertType.BACKUP_FAILED.isCritical());
            assertTrue(AlertType.STORAGE_CRITICAL.isCritical());
            assertTrue(AlertType.RESTORE_FAILED.isCritical());
            assertTrue(AlertType.INTEGRITY_ERROR.isCritical());
            
            assertFalse(AlertType.STORAGE_WARNING.isCritical());
            assertFalse(AlertType.NO_RECENT_BACKUP.isCritical());
        }

        @Test
        @DisplayName("存储相关告警判断应正确")
        void testIsStorageRelated() {
            assertTrue(AlertType.STORAGE_WARNING.isStorageRelated());
            assertTrue(AlertType.STORAGE_CRITICAL.isStorageRelated());
            
            assertFalse(AlertType.BACKUP_FAILED.isStorageRelated());
            assertFalse(AlertType.RESTORE_FAILED.isStorageRelated());
        }

        @Test
        @DisplayName("备份相关告警判断应正确")
        void testIsBackupRelated() {
            assertTrue(AlertType.BACKUP_FAILED.isBackupRelated());
            assertTrue(AlertType.BACKUP_TIMEOUT.isBackupRelated());
            assertTrue(AlertType.NO_RECENT_BACKUP.isBackupRelated());
            
            assertFalse(AlertType.RESTORE_FAILED.isBackupRelated());
            assertFalse(AlertType.STORAGE_WARNING.isBackupRelated());
        }

        @Test
        @DisplayName("fromCode应正确返回告警类型")
        void testFromCode() {
            assertEquals(AlertType.BACKUP_FAILED, AlertType.fromCode(1));
            assertEquals(AlertType.NO_RECENT_BACKUP, AlertType.fromCode(3));
            assertEquals(AlertType.STORAGE_WARNING, AlertType.fromCode(4));
            
            assertThrows(IllegalArgumentException.class, () -> AlertType.fromCode(999));
        }
    }

    @Nested
    @DisplayName("BackupHealthStatus 健康状态枚举测试")
    class BackupHealthStatusTest {

        @Test
        @DisplayName("健康状态应正确定义")
        void testHealthStatuses() {
            assertEquals(0, BackupHealthStatus.HEALTHY.getCode());
            assertEquals("健康", BackupHealthStatus.HEALTHY.getName());
            
            assertEquals(1, BackupHealthStatus.WARNING.getCode());
            assertEquals("警告", BackupHealthStatus.WARNING.getName());
            
            assertEquals(2, BackupHealthStatus.CRITICAL.getCode());
            assertEquals("危险", BackupHealthStatus.CRITICAL.getName());
        }

        @Test
        @DisplayName("健康状态判断方法应正确")
        void testStatusMethods() {
            assertTrue(BackupHealthStatus.HEALTHY.isHealthy());
            assertFalse(BackupHealthStatus.WARNING.isHealthy());
            assertFalse(BackupHealthStatus.CRITICAL.isHealthy());

            assertFalse(BackupHealthStatus.HEALTHY.needsAttention());
            assertTrue(BackupHealthStatus.WARNING.needsAttention());
            assertTrue(BackupHealthStatus.CRITICAL.needsAttention());

            assertFalse(BackupHealthStatus.HEALTHY.isCritical());
            assertFalse(BackupHealthStatus.WARNING.isCritical());
            assertTrue(BackupHealthStatus.CRITICAL.isCritical());
        }

        @Test
        @DisplayName("fromCode应正确返回健康状态")
        void testFromCode() {
            assertEquals(BackupHealthStatus.HEALTHY, BackupHealthStatus.fromCode(0));
            assertEquals(BackupHealthStatus.WARNING, BackupHealthStatus.fromCode(1));
            assertEquals(BackupHealthStatus.CRITICAL, BackupHealthStatus.fromCode(2));
            
            assertThrows(IllegalArgumentException.class, () -> BackupHealthStatus.fromCode(999));
        }
    }

    @Nested
    @DisplayName("AesEncryptionServiceImpl 加密服务测试")
    class AesEncryptionServiceTest {

        private AesEncryptionServiceImpl encryptionService;
        private BackupProperties backupProperties;
        private Path tempDir;

        @BeforeEach
        void setUp() throws IOException {
            backupProperties = new BackupProperties();
            backupProperties.setEncryptionKey("test-encryption-key-for-unit-test");
            backupProperties.setEncryptionEnabled(true);
            
            encryptionService = new AesEncryptionServiceImpl(backupProperties);
            encryptionService.init();
            
            tempDir = Files.createTempDirectory("backup-test");
        }

        @Test
        @DisplayName("加密服务应可用")
        void testServiceAvailable() {
            assertTrue(encryptionService.isAvailable());
        }

        @Test
        @DisplayName("无密钥时服务应不可用")
        void testServiceUnavailableWithoutKey() {
            BackupProperties propsWithoutKey = new BackupProperties();
            propsWithoutKey.setEncryptionKey(null);
            
            AesEncryptionServiceImpl serviceWithoutKey = new AesEncryptionServiceImpl(propsWithoutKey);
            serviceWithoutKey.init();
            
            assertFalse(serviceWithoutKey.isAvailable());
        }

        @Test
        @DisplayName("加密解密往返应保持数据一致")
        void testEncryptDecryptRoundTrip() throws IOException {
            // 创建测试文件
            Path sourceFile = tempDir.resolve("test-source.txt");
            String originalContent = "This is test content for encryption. 这是加密测试内容。";
            Files.writeString(sourceFile, originalContent);

            // 加密
            Path encryptedFile = tempDir.resolve("test-encrypted.enc");
            encryptionService.encryptFile(sourceFile.toString(), encryptedFile.toString());

            // 验证加密文件存在且已加密
            assertTrue(Files.exists(encryptedFile));
            assertTrue(encryptionService.isEncrypted(encryptedFile.toString()));
            assertFalse(encryptionService.isEncrypted(sourceFile.toString()));

            // 解密
            Path decryptedFile = tempDir.resolve("test-decrypted.txt");
            encryptionService.decryptFile(encryptedFile.toString(), decryptedFile.toString());

            // 验证解密后内容一致
            String decryptedContent = Files.readString(decryptedFile);
            assertEquals(originalContent, decryptedContent);

            // 清理
            Files.deleteIfExists(sourceFile);
            Files.deleteIfExists(encryptedFile);
            Files.deleteIfExists(decryptedFile);
            Files.deleteIfExists(tempDir);
        }

        @Test
        @DisplayName("加密不存在的文件应抛出异常")
        void testEncryptNonExistentFile() {
            assertThrows(Exception.class, () -> 
                encryptionService.encryptFile("/non/existent/file.txt", "/output/file.enc"));
        }

        @Test
        @DisplayName("解密非加密文件应抛出异常")
        void testDecryptNonEncryptedFile() throws IOException {
            Path plainFile = tempDir.resolve("plain.txt");
            Files.writeString(plainFile, "Plain text content");

            assertThrows(Exception.class, () ->
                encryptionService.decryptFile(plainFile.toString(), tempDir.resolve("output.txt").toString()));

            Files.deleteIfExists(plainFile);
            Files.deleteIfExists(tempDir);
        }
    }

    @Nested
    @DisplayName("Cron表达式验证测试")
    class CronExpressionValidationTest {

        @Test
        @DisplayName("有效的cron表达式应被接受")
        void testValidCronExpressions() {
            // 使用Spring的CronExpression验证
            String[] validCrons = {
                "0 0 2 * * ?",      // 每天凌晨2点
                "0 0 3 ? * SUN",    // 每周日凌晨3点
                "0 0 * * * ?",      // 每小时
                "0 30 4 * * ?",     // 每天凌晨4:30
                "0 0 0 1 * ?"       // 每月1号
            };

            for (String cron : validCrons) {
                assertDoesNotThrow(() -> 
                    org.springframework.scheduling.support.CronExpression.parse(cron),
                    "Cron expression should be valid: " + cron);
            }
        }

        @Test
        @DisplayName("无效的cron表达式应被拒绝")
        void testInvalidCronExpressions() {
            String[] invalidCrons = {
                "invalid",
                "0 0 25 * * ?",     // 无效的小时
                "0 60 * * * ?",     // 无效的分钟
                ""
            };

            for (String cron : invalidCrons) {
                assertThrows(Exception.class, () ->
                    org.springframework.scheduling.support.CronExpression.parse(cron),
                    "Cron expression should be invalid: " + cron);
            }
        }
    }

    @Nested
    @DisplayName("RestoreProgressVO 恢复进度VO测试")
    class RestoreProgressVOTest {

        @Test
        @DisplayName("初始进度应正确创建")
        void testInitialProgress() {
            Long restoreId = 123L;
            RestoreProgressVO progress = RestoreProgressVO.initial(restoreId);

            assertEquals(restoreId, progress.getRestoreId());
            assertEquals(0, progress.getProgress());
            assertEquals("验证中", progress.getStage());
            assertEquals(RestoreProgressVO.STAGE_VALIDATING, progress.getStageCode());
            assertFalse(progress.getCompleted());
            assertFalse(progress.getSuccess());
            assertNotNull(progress.getStartTimestamp());
        }

        @Test
        @DisplayName("完成进度应正确创建")
        void testCompletedProgress() {
            Long restoreId = 456L;
            long elapsedMs = 5000;
            
            RestoreProgressVO successProgress = RestoreProgressVO.completed(restoreId, true, elapsedMs, null);
            assertEquals(100, successProgress.getProgress());
            assertEquals("完成", successProgress.getStage());
            assertTrue(successProgress.getCompleted());
            assertTrue(successProgress.getSuccess());
            assertNull(successProgress.getErrorMessage());

            RestoreProgressVO failedProgress = RestoreProgressVO.completed(restoreId, false, elapsedMs, "Test error");
            assertEquals(100, failedProgress.getProgress());
            assertEquals("失败", failedProgress.getStage());
            assertTrue(failedProgress.getCompleted());
            assertFalse(failedProgress.getSuccess());
            assertEquals("Test error", failedProgress.getErrorMessage());
        }

        @Test
        @DisplayName("阶段名称应正确返回")
        void testStageName() {
            assertEquals("验证中", RestoreProgressVO.getStageName(RestoreProgressVO.STAGE_VALIDATING));
            assertEquals("解密中", RestoreProgressVO.getStageName(RestoreProgressVO.STAGE_DECRYPTING));
            assertEquals("解压中", RestoreProgressVO.getStageName(RestoreProgressVO.STAGE_DECOMPRESSING));
            assertEquals("恢复中", RestoreProgressVO.getStageName(RestoreProgressVO.STAGE_RESTORING));
            assertEquals("完成", RestoreProgressVO.getStageName(RestoreProgressVO.STAGE_COMPLETED));
            assertEquals("失败", RestoreProgressVO.getStageName(RestoreProgressVO.STAGE_FAILED));
            assertEquals("回滚中", RestoreProgressVO.getStageName(RestoreProgressVO.STAGE_ROLLING_BACK));
            assertEquals("未知", RestoreProgressVO.getStageName(99));
        }

        @Test
        @DisplayName("时长格式化应正确")
        void testFormatDuration() {
            assertEquals("0秒", RestoreProgressVO.formatDuration(0));
            assertEquals("0秒", RestoreProgressVO.formatDuration(-100));
            assertEquals("5秒", RestoreProgressVO.formatDuration(5000));
            assertEquals("1分30秒", RestoreProgressVO.formatDuration(90000));
            assertEquals("1小时30分0秒", RestoreProgressVO.formatDuration(5400000));
        }
    }

    @Nested
    @DisplayName("RestoreResultVO 恢复结果VO测试")
    class RestoreResultVOTest {

        @Test
        @DisplayName("成功结果应正确创建")
        void testSuccessResult() {
            Long restoreId = 123L;
            Long backupId = 456L;
            Long preRestoreBackupId = 789L;
            int sourceType = RestoreResultVO.SOURCE_TYPE_LOCAL;
            String sourceFilename = "backup_daily_20250202.sql.gz";
            long durationMs = 5000;

            RestoreResultVO result = RestoreResultVO.success(
                    restoreId, backupId, preRestoreBackupId, sourceType, sourceFilename, durationMs);

            assertEquals(restoreId, result.getRestoreId());
            assertEquals(backupId, result.getBackupId());
            assertEquals(preRestoreBackupId, result.getPreRestoreBackupId());
            assertEquals(sourceType, result.getSourceType());
            assertEquals("本地备份", result.getSourceTypeName());
            assertEquals(sourceFilename, result.getSourceFilename());
            assertTrue(result.getSuccess());
            assertEquals(RestoreResultVO.STATUS_SUCCESS, result.getStatus());
            assertEquals("成功", result.getStatusName());
            assertEquals(durationMs, result.getDurationMs());
            assertEquals("5秒", result.getDurationFormatted());
            assertEquals("数据库恢复成功", result.getMessage());
        }

        @Test
        @DisplayName("失败结果应正确创建")
        void testFailedResult() {
            Long restoreId = 123L;
            String errorMessage = "恢复失败：文件损坏";

            RestoreResultVO result = RestoreResultVO.failed(restoreId, errorMessage);

            assertEquals(restoreId, result.getRestoreId());
            assertFalse(result.getSuccess());
            assertEquals(RestoreResultVO.STATUS_FAILED, result.getStatus());
            assertEquals("失败", result.getStatusName());
            assertEquals(errorMessage, result.getErrorMessage());
            assertTrue(result.getMessage().contains(errorMessage));
        }

        @Test
        @DisplayName("进行中结果应正确创建")
        void testInProgressResult() {
            Long restoreId = 123L;
            Long backupId = 456L;
            int sourceType = RestoreResultVO.SOURCE_TYPE_UPLOAD;
            String sourceFilename = "uploaded_backup.sql.gz";

            RestoreResultVO result = RestoreResultVO.inProgress(restoreId, backupId, sourceType, sourceFilename);

            assertEquals(restoreId, result.getRestoreId());
            assertEquals(backupId, result.getBackupId());
            assertEquals(sourceType, result.getSourceType());
            assertEquals("上传文件", result.getSourceTypeName());
            assertEquals(sourceFilename, result.getSourceFilename());
            assertFalse(result.getSuccess());
            assertEquals(RestoreResultVO.STATUS_IN_PROGRESS, result.getStatus());
            assertEquals("进行中", result.getStatusName());
        }

        @Test
        @DisplayName("已回滚结果应正确创建")
        void testRolledBackResult() {
            Long restoreId = 123L;
            Long preRestoreBackupId = 789L;
            String errorMessage = "恢复失败，已回滚";

            RestoreResultVO result = RestoreResultVO.rolledBack(restoreId, preRestoreBackupId, errorMessage);

            assertEquals(restoreId, result.getRestoreId());
            assertEquals(preRestoreBackupId, result.getPreRestoreBackupId());
            assertFalse(result.getSuccess());
            assertEquals(RestoreResultVO.STATUS_ROLLED_BACK, result.getStatus());
            assertEquals("已回滚", result.getStatusName());
            assertEquals(errorMessage, result.getErrorMessage());
            assertTrue(result.getMessage().contains("回滚"));
        }

        @Test
        @DisplayName("来源类型名称应正确返回")
        void testSourceTypeName() {
            assertEquals("本地备份", RestoreResultVO.getSourceTypeName(RestoreResultVO.SOURCE_TYPE_LOCAL));
            assertEquals("上传文件", RestoreResultVO.getSourceTypeName(RestoreResultVO.SOURCE_TYPE_UPLOAD));
            assertEquals("未知", RestoreResultVO.getSourceTypeName(99));
        }

        @Test
        @DisplayName("时长格式化应正确")
        void testFormatDuration() {
            assertEquals("0秒", RestoreResultVO.formatDuration(0));
            assertEquals("0秒", RestoreResultVO.formatDuration(-100));
            assertEquals("5秒", RestoreResultVO.formatDuration(5000));
            assertEquals("1分30秒", RestoreResultVO.formatDuration(90000));
            assertEquals("1小时30分0秒", RestoreResultVO.formatDuration(5400000));
        }
    }

    @Nested
    @DisplayName("BackupResultVO 备份结果VO测试")
    class BackupResultVOTest {

        @Test
        @DisplayName("成功结果应正确创建")
        void testSuccessResult() {
            Long backupId = 123L;
            String filename = "backup_daily_20250202.sql.gz";
            int backupType = 1;
            long fileSize = 1024 * 1024;
            long durationMs = 5000;
            boolean encrypted = true;

            BackupResultVO result = BackupResultVO.success(
                    backupId, filename, backupType, fileSize, durationMs, encrypted);

            assertEquals(backupId, result.getBackupId());
            assertEquals(filename, result.getFilename());
            assertEquals(backupType, result.getBackupType());
            assertEquals(fileSize, result.getFileSize());
            assertEquals(durationMs, result.getDurationMs());
            assertEquals(encrypted, result.getIsEncrypted());
            assertEquals(1, result.getStatus()); // STATUS_SUCCESS
            assertNull(result.getErrorMessage());
        }

        @Test
        @DisplayName("失败结果应正确创建")
        void testFailedResult() {
            Long backupId = 123L;
            String errorMessage = "备份失败：磁盘空间不足";

            BackupResultVO result = BackupResultVO.failed(backupId, errorMessage);

            assertEquals(backupId, result.getBackupId());
            assertEquals(2, result.getStatus()); // STATUS_FAILED
            assertEquals(errorMessage, result.getErrorMessage());
        }
    }

    @Nested
    @DisplayName("BackupStatisticsVO 备份统计VO测试")
    class BackupStatisticsVOTest {

        @Test
        @DisplayName("空统计应正确创建")
        void testEmptyStatistics() {
            BackupStatisticsVO stats = BackupStatisticsVO.empty();

            assertNull(stats.getLastBackupTime());
            assertEquals(0, stats.getTotalBackupCount());
            assertEquals(0, stats.getSuccessCount());
            assertEquals(0, stats.getFailureCount());
            assertEquals(0.0, stats.getSuccessRate());
            assertEquals("0.00%", stats.getSuccessRateFormatted());
        }

        @Test
        @DisplayName("成功率计算应正确")
        void testSuccessRateCalculation() {
            assertEquals(0.0, BackupStatisticsVO.calculateSuccessRate(0, 0));
            assertEquals(100.0, BackupStatisticsVO.calculateSuccessRate(10, 10));
            assertEquals(50.0, BackupStatisticsVO.calculateSuccessRate(5, 10));
            assertEquals(75.0, BackupStatisticsVO.calculateSuccessRate(3, 4));
        }

        @Test
        @DisplayName("文件大小格式化应正确")
        void testFormatFileSize() {
            assertEquals("0 B", BackupStatisticsVO.formatFileSize(0));
            assertEquals("512.00 B", BackupStatisticsVO.formatFileSize(512));
            assertEquals("1.00 KB", BackupStatisticsVO.formatFileSize(1024));
            assertEquals("1.50 MB", BackupStatisticsVO.formatFileSize(1572864));
            assertEquals("2.00 GB", BackupStatisticsVO.formatFileSize(2147483648L));
        }
    }
}
