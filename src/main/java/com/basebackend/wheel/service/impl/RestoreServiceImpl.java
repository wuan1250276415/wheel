package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.config.BackupProperties;
import com.basebackend.wheel.dto.RestoreProgressVO;
import com.basebackend.wheel.dto.RestoreResultVO;
import com.basebackend.wheel.entity.BackupRecord;
import com.basebackend.wheel.entity.RestoreRecord;
import com.basebackend.wheel.enums.AlertType;
import com.basebackend.wheel.mapper.RestoreRecordMapper;
import com.basebackend.wheel.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

/**
 * 数据恢复服务实现
 * 提供从备份文件恢复数据库的功能
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RestoreServiceImpl implements RestoreService {

    private final RestoreRecordMapper restoreRecordMapper;
    private final BackupService backupService;
    private final BackupStorageService backupStorageService;
    private final BackupMonitorService backupMonitorService;
    private final EncryptionService encryptionService;
    private final BackupProperties backupProperties;
    private final StringRedisTemplate redisTemplate;

    // ==================== Redis Key 常量 ====================

    private static final String RESTORE_LOCK_KEY = "backup:lock:restoring";
    private static final int RESTORE_LOCK_TIMEOUT_MINUTES = 120;

    // ==================== 恢复执行 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestoreResultVO restoreFromBackup(Long backupId) {
        // 参数验证
        if (backupId == null) {
            return RestoreResultVO.failed(null, "备份ID不能为空");
        }

        // 获取备份记录
        BackupRecord backupRecord = backupService.getBackupEntityById(backupId);
        if (backupRecord == null) {
            return RestoreResultVO.failed(null, "备份记录不存在: " + backupId);
        }

        // 检查备份状态
        if (!backupRecord.isSuccess()) {
            return RestoreResultVO.failed(null, "备份状态异常，无法恢复");
        }

        // 检查备份文件是否存在
        String backupFilePath = backupStorageService.getBackupFilePath(backupRecord.getFilename());
        if (!backupStorageService.backupFileExists(backupRecord.getFilename())) {
            return RestoreResultVO.failed(null, "备份文件不存在: " + backupRecord.getFilename());
        }

        // 执行恢复
        return doRestore(
                backupId,
                backupFilePath,
                backupRecord.getFilename(),
                RestoreRecord.SOURCE_TYPE_LOCAL,
                backupRecord.isEncryptedFile()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestoreResultVO restoreFromUpload(MultipartFile file) {
        // 参数验证
        if (file == null || file.isEmpty()) {
            return RestoreResultVO.failed(null, "上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            return RestoreResultVO.failed(null, "文件名不能为空");
        }

        // 验证文件扩展名
        if (!isValidBackupFilename(originalFilename)) {
            return RestoreResultVO.failed(null, "无效的备份文件格式，支持 .sql, .sql.gz, .sql.gz.enc");
        }

        Path tempFile = null;
        try {
            // 保存上传文件到临时目录
            tempFile = Files.createTempFile("restore_upload_", "_" + originalFilename);
            file.transferTo(tempFile.toFile());

            // 检查是否加密
            boolean isEncrypted = encryptionService.isEncrypted(tempFile.toString()) ||
                    originalFilename.endsWith(".enc");

            // 执行恢复
            return doRestore(
                    null,
                    tempFile.toString(),
                    originalFilename,
                    RestoreRecord.SOURCE_TYPE_UPLOAD,
                    isEncrypted
            );

        } catch (IOException e) {
            log.error("保存上传文件失败", e);
            return RestoreResultVO.failed(null, "保存上传文件失败: " + e.getMessage());
        } finally {
            // 清理临时文件
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    log.warn("清理临时文件失败: {}", tempFile, e);
                }
            }
        }
    }

    // ==================== 恢复进度 ====================

    @Override
    public RestoreProgressVO getRestoreProgress(Long restoreId) {
        if (restoreId == null) {
            return null;
        }

        // 从监控服务获取进度（使用 Redis 存储）
        // 由于 BackupMonitorService 使用 BackupProgressVO，我们需要转换
        Long currentRestoreId = backupMonitorService.getCurrentRestoreId();
        if (currentRestoreId != null && currentRestoreId.equals(restoreId)) {
            // 恢复正在进行中
            RestoreRecord record = restoreRecordMapper.selectById(restoreId);
            if (record != null) {
                return RestoreProgressVO.builder()
                        .restoreId(restoreId)
                        .backupId(record.getBackupId())
                        .preRestoreBackupId(record.getPreRestoreBackupId())
                        .progress(50) // 进行中默认50%
                        .stage(RestoreProgressVO.STAGE_NAME_RESTORING)
                        .stageCode(RestoreProgressVO.STAGE_RESTORING)
                        .completed(false)
                        .success(false)
                        .build();
            }
        }

        // 查询数据库记录
        RestoreRecord record = restoreRecordMapper.selectById(restoreId);
        if (record == null) {
            return null;
        }

        // 根据状态返回进度
        if (record.isInProgress()) {
            return RestoreProgressVO.inProgress(
                    restoreId,
                    50,
                    RestoreProgressVO.STAGE_RESTORING,
                    record.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            );
        } else {
            return RestoreProgressVO.completed(
                    restoreId,
                    record.isSuccess(),
                    record.getDurationMs() != null ? record.getDurationMs() : 0,
                    record.getErrorMessage()
            );
        }
    }

    @Override
    public boolean isRestoreInProgress() {
        return backupMonitorService.isRestoreInProgress();
    }

    // ==================== 恢复验证 ====================

    @Override
    public boolean validateBackupFile(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return false;
        }

        Path path = Path.of(filePath);
        if (!Files.exists(path)) {
            log.warn("备份文件不存在: {}", filePath);
            return false;
        }

        try {
            // 检查文件大小
            long fileSize = Files.size(path);
            if (fileSize == 0) {
                log.warn("备份文件为空: {}", filePath);
                return false;
            }

            // 如果是加密文件，验证加密格式
            if (encryptionService.isEncrypted(filePath)) {
                // 加密文件格式验证已在 isEncrypted 中完成
                return true;
            }

            // 如果是 gzip 文件，尝试读取头部验证
            if (filePath.endsWith(".gz")) {
                return validateGzipFile(filePath);
            }

            // 如果是 SQL 文件，检查是否包含有效的 SQL 内容
            if (filePath.endsWith(".sql")) {
                return validateSqlFile(filePath);
            }

            return true;

        } catch (Exception e) {
            log.error("验证备份文件失败: {}", filePath, e);
            return false;
        }
    }

    @Override
    public boolean validateBackupById(Long backupId) {
        if (backupId == null) {
            return false;
        }

        BackupRecord record = backupService.getBackupEntityById(backupId);
        if (record == null) {
            return false;
        }

        String filePath = backupStorageService.getBackupFilePath(record.getFilename());
        return validateBackupFile(filePath);
    }

    // ==================== 恢复记录查询 ====================

    @Override
    public RestoreRecord getRestoreById(Long restoreId) {
        if (restoreId == null) {
            return null;
        }
        return restoreRecordMapper.selectById(restoreId);
    }

    @Override
    public RestoreRecord getLastRestore() {
        return restoreRecordMapper.selectLastRestore();
    }

    // ==================== 恢复回滚 ====================

    @Override
    public boolean rollbackRestore(Long restoreId) {
        if (restoreId == null) {
            return false;
        }

        RestoreRecord record = restoreRecordMapper.selectById(restoreId);
        if (record == null) {
            log.warn("恢复记录不存在: {}", restoreId);
            return false;
        }

        // 检查是否有恢复前备份
        if (record.getPreRestoreBackupId() == null) {
            log.warn("没有恢复前备份，无法回滚: restoreId={}", restoreId);
            return false;
        }

        // 获取恢复前备份
        BackupRecord preRestoreBackup = backupService.getBackupEntityById(record.getPreRestoreBackupId());
        if (preRestoreBackup == null) {
            log.error("恢复前备份记录不存在: backupId={}", record.getPreRestoreBackupId());
            return false;
        }

        // 检查备份文件是否存在
        String backupFilePath = backupStorageService.getBackupFilePath(preRestoreBackup.getFilename());
        if (!backupStorageService.backupFileExists(preRestoreBackup.getFilename())) {
            log.error("恢复前备份文件不存在: {}", preRestoreBackup.getFilename());
            return false;
        }

        try {
            log.info("开始回滚恢复: restoreId={}, preRestoreBackupId={}", restoreId, record.getPreRestoreBackupId());

            // 更新恢复进度
            backupMonitorService.updateRestoreProgress(restoreId, 10, RestoreProgressVO.STAGE_NAME_ROLLING_BACK);

            // 执行回滚（从恢复前备份恢复）
            Path tempDir = Files.createTempDirectory("rollback_");
            try {
                String sqlFilePath = prepareRestoreFile(
                        backupFilePath,
                        tempDir,
                        preRestoreBackup.isEncryptedFile()
                );

                // 执行 mysql 恢复
                executeMysqlRestore(sqlFilePath);

                // 更新恢复记录状态为已回滚
                restoreRecordMapper.updateRestoreStatus(
                        restoreId,
                        RestoreRecord.STATUS_ROLLED_BACK,
                        record.getDurationMs(),
                        "恢复失败后已回滚到恢复前状态"
                );

                log.info("回滚成功: restoreId={}", restoreId);
                return true;

            } finally {
                // 清理临时目录
                cleanupTempDirectory(tempDir);
            }

        } catch (Exception e) {
            log.error("回滚失败: restoreId={}", restoreId, e);
            return false;
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 执行恢复操作
     */
    private RestoreResultVO doRestore(Long backupId, String filePath, String filename,
                                       int sourceType, boolean isEncrypted) {
        // 检查并发控制
        if (!acquireRestoreLock()) {
            return RestoreResultVO.failed(null, "已有恢复任务正在执行，请稍后重试");
        }

        RestoreRecord record = null;
        long startTime = System.currentTimeMillis();

        try {
            // 验证备份文件
            if (!validateBackupFile(filePath)) {
                return RestoreResultVO.failed(null, "备份文件验证失败");
            }

            // 创建恢复记录
            record = createRestoreRecord(backupId, sourceType, filename);
            restoreRecordMapper.insert(record);

            // 记录恢复开始
            backupMonitorService.recordRestoreStart(record.getId());
            backupMonitorService.updateRestoreProgress(record.getId(), 5, RestoreProgressVO.STAGE_NAME_VALIDATING);

            // 创建恢复前备份
            backupMonitorService.updateRestoreProgress(record.getId(), 10, "创建恢复前备份");
            BackupRecord preRestoreBackup = createPreRestoreBackup();
            if (preRestoreBackup != null) {
                record.setPreRestoreBackupId(preRestoreBackup.getId());
                restoreRecordMapper.updatePreRestoreBackupId(record.getId(), preRestoreBackup.getId());
            }

            // 准备恢复文件（解密、解压）
            backupMonitorService.updateRestoreProgress(record.getId(), 30, 
                    isEncrypted ? RestoreProgressVO.STAGE_NAME_DECRYPTING : RestoreProgressVO.STAGE_NAME_DECOMPRESSING);
            
            Path tempDir = Files.createTempDirectory("restore_");
            try {
                String sqlFilePath = prepareRestoreFile(filePath, tempDir, isEncrypted);

                // 执行恢复
                backupMonitorService.updateRestoreProgress(record.getId(), 50, RestoreProgressVO.STAGE_NAME_RESTORING);
                executeMysqlRestore(sqlFilePath);

                // 更新恢复记录
                long durationMs = System.currentTimeMillis() - startTime;
                record.setDurationMs(durationMs);
                record.setStatus(RestoreRecord.STATUS_SUCCESS);
                restoreRecordMapper.updateRestoreStatus(
                        record.getId(),
                        RestoreRecord.STATUS_SUCCESS,
                        durationMs,
                        null
                );

                // 记录恢复完成
                backupMonitorService.recordRestoreComplete(record.getId(), true, null);

                log.info("数据库恢复成功: restoreId={}, duration={}ms", record.getId(), durationMs);

                return RestoreResultVO.success(
                        record.getId(),
                        backupId,
                        record.getPreRestoreBackupId(),
                        sourceType,
                        filename,
                        durationMs
                );

            } finally {
                // 清理临时目录
                cleanupTempDirectory(tempDir);
            }

        } catch (Exception e) {
            log.error("数据库恢复失败", e);

            long durationMs = System.currentTimeMillis() - startTime;

            // 更新恢复记录为失败
            if (record != null && record.getId() != null) {
                restoreRecordMapper.updateRestoreStatus(
                        record.getId(),
                        RestoreRecord.STATUS_FAILED,
                        durationMs,
                        e.getMessage()
                );
                backupMonitorService.recordRestoreComplete(record.getId(), false, e.getMessage());

                // 尝试回滚
                if (record.getPreRestoreBackupId() != null) {
                    log.info("尝试回滚到恢复前状态...");
                    boolean rollbackSuccess = rollbackRestore(record.getId());
                    if (rollbackSuccess) {
                        return RestoreResultVO.rolledBack(record.getId(), record.getPreRestoreBackupId(), e.getMessage());
                    }
                }
            }

            return RestoreResultVO.failed(record != null ? record.getId() : null, e.getMessage());

        } finally {
            releaseRestoreLock();
        }
    }

    /**
     * 创建恢复记录
     */
    private RestoreRecord createRestoreRecord(Long backupId, int sourceType, String filename) {
        RestoreRecord record = new RestoreRecord();
        record.setBackupId(backupId);
        record.setSourceType(sourceType);
        record.setSourceFilename(filename);
        record.setStatus(RestoreRecord.STATUS_IN_PROGRESS);
        return record;
    }

    /**
     * 创建恢复前备份
     */
    private BackupRecord createPreRestoreBackup() {
        try {
            log.info("创建恢复前备份...");
            return backupService.executeBackup(BackupService.BACKUP_TYPE_MANUAL, "恢复前自动备份");
        } catch (Exception e) {
            log.warn("创建恢复前备份失败，继续执行恢复", e);
            // 发送告警但不阻止恢复
            backupMonitorService.sendAlert(AlertType.BACKUP_FAILED, "恢复前备份创建失败", e.getMessage());
            return null;
        }
    }

    /**
     * 准备恢复文件（解密、解压）
     */
    private String prepareRestoreFile(String sourcePath, Path tempDir, boolean isEncrypted) throws Exception {
        String currentPath = sourcePath;

        // 如果是加密文件，先解密
        if (isEncrypted) {
            Path decryptedPath = tempDir.resolve("decrypted.sql.gz");
            encryptionService.decryptFile(currentPath, decryptedPath.toString());
            currentPath = decryptedPath.toString();
        }

        // 如果是 gzip 文件，解压
        if (currentPath.endsWith(".gz")) {
            Path decompressedPath = tempDir.resolve("restore.sql");
            decompressFile(currentPath, decompressedPath.toString());
            currentPath = decompressedPath.toString();
        }

        return currentPath;
    }

    /**
     * 解压 gzip 文件
     */
    private void decompressFile(String sourcePath, String targetPath) throws IOException {
        try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(sourcePath));
             FileOutputStream fos = new FileOutputStream(targetPath)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = gzis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }

        log.info("文件解压成功: {} -> {}", sourcePath, targetPath);
    }

    /**
     * 执行 mysql 恢复命令
     */
    private void executeMysqlRestore(String sqlFilePath) throws Exception {
        BackupProperties.MysqlConfig mysqlConfig = backupProperties.getMysql();

        // 构建 mysql 命令
        ProcessBuilder pb = new ProcessBuilder(
                mysqlConfig.getMysqlPath(),
                "-h", mysqlConfig.getHost(),
                "-P", String.valueOf(mysqlConfig.getPort()),
                "-u", mysqlConfig.getUsername(),
                "--password=" + mysqlConfig.getPassword(),
                mysqlConfig.getDatabase()
        );

        // 重定向输入从 SQL 文件
        pb.redirectInput(new File(sqlFilePath));
        pb.redirectErrorStream(false);

        log.info("执行 mysql 恢复: database={}", mysqlConfig.getDatabase());

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
            log.error("mysql 恢复执行失败: exitCode={}, error={}", exitCode, error);
            throw new RuntimeException("mysql 恢复执行失败: " + error);
        }

        log.info("mysql 恢复执行成功");
    }

    /**
     * 验证 gzip 文件
     */
    private boolean validateGzipFile(String filePath) {
        try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(filePath))) {
            // 尝试读取一些数据来验证 gzip 格式
            byte[] buffer = new byte[1024];
            gzis.read(buffer);
            return true;
        } catch (Exception e) {
            log.warn("无效的 gzip 文件: {}", filePath, e);
            return false;
        }
    }

    /**
     * 验证 SQL 文件
     */
    private boolean validateSqlFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // 读取前几行检查是否包含 SQL 内容
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null && lineCount < 10) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("--") && !line.startsWith("/*")) {
                    // 检查是否包含常见的 SQL 关键字
                    String upperLine = line.toUpperCase();
                    if (upperLine.contains("CREATE") || upperLine.contains("INSERT") ||
                            upperLine.contains("DROP") || upperLine.contains("ALTER") ||
                            upperLine.contains("USE") || upperLine.contains("SET")) {
                        return true;
                    }
                }
                lineCount++;
            }
            return false;
        } catch (Exception e) {
            log.warn("验证 SQL 文件失败: {}", filePath, e);
            return false;
        }
    }

    /**
     * 验证备份文件名
     */
    private boolean isValidBackupFilename(String filename) {
        if (!StringUtils.hasText(filename)) {
            return false;
        }
        String lowerFilename = filename.toLowerCase();
        return lowerFilename.endsWith(".sql") ||
                lowerFilename.endsWith(".sql.gz") ||
                lowerFilename.endsWith(".sql.gz.enc");
    }

    /**
     * 清理临时目录
     */
    private void cleanupTempDirectory(Path tempDir) {
        if (tempDir == null) {
            return;
        }
        try {
            Files.walk(tempDir)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log.warn("删除临时文件失败: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            log.warn("清理临时目录失败: {}", tempDir, e);
        }
    }

    /**
     * 获取恢复锁
     */
    private boolean acquireRestoreLock() {
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
                RESTORE_LOCK_KEY,
                String.valueOf(System.currentTimeMillis()),
                RESTORE_LOCK_TIMEOUT_MINUTES,
                TimeUnit.MINUTES
        );
        return Boolean.TRUE.equals(acquired);
    }

    /**
     * 释放恢复锁
     */
    private void releaseRestoreLock() {
        redisTemplate.delete(RESTORE_LOCK_KEY);
    }
}
