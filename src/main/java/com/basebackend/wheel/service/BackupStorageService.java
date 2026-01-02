package com.basebackend.wheel.service;

import org.springframework.core.io.Resource;

/**
 * 备份存储服务接口
 * 负责备份文件的存储、获取、删除和存储使用量统计
 *
 * @author wheel-api
 * @since 2025-02-02
 */
public interface BackupStorageService {

    /**
     * 保存备份文件
     * 将源文件移动或复制到备份存储目录
     *
     * @param sourcePath 源文件路径
     * @param filename   目标文件名
     * @return 保存后的完整文件路径
     * @throws StorageException 存储失败时抛出
     */
    String saveBackupFile(String sourcePath, String filename);

    /**
     * 获取备份文件完整路径
     *
     * @param filename 文件名
     * @return 完整文件路径
     */
    String getBackupFilePath(String filename);

    /**
     * 删除备份文件
     *
     * @param filename 文件名
     * @throws StorageException 删除失败时抛出
     */
    void deleteBackupFile(String filename);

    /**
     * 获取备份文件下载资源
     *
     * @param filename 文件名
     * @return 文件资源
     * @throws StorageException 文件不存在或读取失败时抛出
     */
    Resource getBackupFileResource(String filename);

    /**
     * 检查备份文件是否存在
     *
     * @param filename 文件名
     * @return 是否存在
     */
    boolean backupFileExists(String filename);

    /**
     * 获取备份文件大小
     *
     * @param filename 文件名
     * @return 文件大小（字节），文件不存在返回 -1
     */
    long getBackupFileSize(String filename);

    /**
     * 获取存储使用情况
     *
     * @return 存储使用情况
     */
    StorageUsage getStorageUsage();

    /**
     * 确保存储目录存在
     * 如果目录不存在则创建
     *
     * @throws StorageException 创建目录失败时抛出
     */
    void ensureStorageDirectoryExists();

    /**
     * 检查存储目录是否可写
     *
     * @return 是否可写
     */
    boolean isStorageDirectoryWritable();

    /**
     * 存储使用情况
     */
    interface StorageUsage {
        /**
         * 获取已使用空间（字节）
         */
        long getUsedBytes();

        /**
         * 获取总空间（字节）
         */
        long getTotalBytes();

        /**
         * 获取可用空间（字节）
         */
        long getAvailableBytes();

        /**
         * 获取备份文件数量
         */
        int getFileCount();

        /**
         * 获取使用率（0-100）
         */
        double getUsagePercent();
    }

    /**
     * 存储异常
     */
    class StorageException extends RuntimeException {
        public StorageException(String message) {
            super(message);
        }

        public StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
