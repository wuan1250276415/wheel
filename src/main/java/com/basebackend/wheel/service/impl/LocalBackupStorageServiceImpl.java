package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.service.BackupConfigService;
import com.basebackend.wheel.service.BackupStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 本地文件系统备份存储服务实现
 * 将备份文件存储在本地文件系统中
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalBackupStorageServiceImpl implements BackupStorageService {

    private final BackupConfigService backupConfigService;

    @PostConstruct
    public void init() {
        try {
            ensureStorageDirectoryExists();
            log.info("备份存储服务初始化成功，存储路径: {}", getStoragePath());
        } catch (Exception e) {
            log.error("备份存储服务初始化失败", e);
        }
    }

    @Override
    public String saveBackupFile(String sourcePath, String filename) {
        validateFilename(filename);
        
        Path source = Paths.get(sourcePath);
        if (!Files.exists(source)) {
            throw new StorageException("源文件不存在: " + sourcePath);
        }

        Path targetDir = Paths.get(getStoragePath());
        Path target = targetDir.resolve(filename);

        try {
            // 确保目标目录存在
            ensureStorageDirectoryExists();

            // 移动文件到存储目录
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("备份文件保存成功: {} -> {}", sourcePath, target);
            return target.toString();
            
        } catch (IOException e) {
            throw new StorageException("保存备份文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getBackupFilePath(String filename) {
        validateFilename(filename);
        return Paths.get(getStoragePath(), filename).toString();
    }

    @Override
    public void deleteBackupFile(String filename) {
        validateFilename(filename);
        
        Path filePath = Paths.get(getStoragePath(), filename);
        
        if (!Files.exists(filePath)) {
            log.warn("要删除的备份文件不存在: {}", filename);
            return;
        }

        try {
            Files.delete(filePath);
            log.info("备份文件删除成功: {}", filename);
        } catch (IOException e) {
            throw new StorageException("删除备份文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Resource getBackupFileResource(String filename) {
        validateFilename(filename);
        
        Path filePath = Paths.get(getStoragePath(), filename);
        
        if (!Files.exists(filePath)) {
            throw new StorageException("备份文件不存在: " + filename);
        }

        if (!Files.isReadable(filePath)) {
            throw new StorageException("备份文件不可读: " + filename);
        }

        return new FileSystemResource(filePath);
    }

    @Override
    public boolean backupFileExists(String filename) {
        if (!StringUtils.hasText(filename)) {
            return false;
        }
        return Files.exists(Paths.get(getStoragePath(), filename));
    }

    @Override
    public long getBackupFileSize(String filename) {
        if (!StringUtils.hasText(filename)) {
            return -1;
        }
        
        Path filePath = Paths.get(getStoragePath(), filename);
        if (!Files.exists(filePath)) {
            return -1;
        }

        try {
            return Files.size(filePath);
        } catch (IOException e) {
            log.warn("获取文件大小失败: {}", filename, e);
            return -1;
        }
    }

    @Override
    public StorageUsage getStorageUsage() {
        Path storagePath = Paths.get(getStoragePath());
        
        // 计算已使用空间和文件数量
        AtomicLong usedBytes = new AtomicLong(0);
        AtomicInteger fileCount = new AtomicInteger(0);

        if (Files.exists(storagePath)) {
            try {
                Files.walkFileTree(storagePath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        usedBytes.addAndGet(attrs.size());
                        fileCount.incrementAndGet();
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        log.warn("访问文件失败: {}", file, exc);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                log.error("遍历存储目录失败", e);
            }
        }

        // 获取磁盘空间信息
        long totalBytes = 0;
        long availableBytes = 0;
        
        try {
            FileStore fileStore = Files.getFileStore(storagePath);
            totalBytes = fileStore.getTotalSpace();
            availableBytes = fileStore.getUsableSpace();
        } catch (IOException e) {
            log.warn("获取磁盘空间信息失败", e);
        }

        final long finalUsedBytes = usedBytes.get();
        final int finalFileCount = fileCount.get();
        final long finalTotalBytes = totalBytes;
        final long finalAvailableBytes = availableBytes;

        return new StorageUsage() {
            @Override
            public long getUsedBytes() {
                return finalUsedBytes;
            }

            @Override
            public long getTotalBytes() {
                return finalTotalBytes;
            }

            @Override
            public long getAvailableBytes() {
                return finalAvailableBytes;
            }

            @Override
            public int getFileCount() {
                return finalFileCount;
            }

            @Override
            public double getUsagePercent() {
                if (finalTotalBytes == 0) {
                    return 0;
                }
                return (double) (finalTotalBytes - finalAvailableBytes) / finalTotalBytes * 100;
            }
        };
    }

    @Override
    public void ensureStorageDirectoryExists() {
        Path storagePath = Paths.get(getStoragePath());
        
        if (!Files.exists(storagePath)) {
            try {
                Files.createDirectories(storagePath);
                log.info("创建备份存储目录: {}", storagePath);
            } catch (IOException e) {
                throw new StorageException("创建备份存储目录失败: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean isStorageDirectoryWritable() {
        Path storagePath = Paths.get(getStoragePath());
        
        if (!Files.exists(storagePath)) {
            return false;
        }
        
        return Files.isWritable(storagePath);
    }

    // ==================== 私有方法 ====================

    /**
     * 获取存储路径
     */
    private String getStoragePath() {
        return backupConfigService.getStoragePath();
    }

    /**
     * 验证文件名
     */
    private void validateFilename(String filename) {
        if (!StringUtils.hasText(filename)) {
            throw new StorageException("文件名不能为空");
        }
        
        // 防止路径遍历攻击
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new StorageException("无效的文件名: " + filename);
        }
    }
}
