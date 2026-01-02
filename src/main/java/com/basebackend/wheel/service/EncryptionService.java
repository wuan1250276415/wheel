package com.basebackend.wheel.service;

/**
 * 加密服务接口
 * 提供备份文件的加密和解密功能
 *
 * @author wheel-api
 * @since 2025-02-02
 */
public interface EncryptionService {

    /**
     * 加密文件
     * 使用 AES-256-GCM 算法加密源文件并保存到目标路径
     *
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径（加密后）
     * @throws EncryptionException 加密失败时抛出
     */
    void encryptFile(String sourcePath, String targetPath);

    /**
     * 解密文件
     * 使用 AES-256-GCM 算法解密源文件并保存到目标路径
     *
     * @param sourcePath 源文件路径（加密的）
     * @param targetPath 目标文件路径（解密后）
     * @throws EncryptionException 解密失败时抛出
     */
    void decryptFile(String sourcePath, String targetPath);

    /**
     * 检查文件是否已加密
     * 通过检查文件头部的魔数来判断
     *
     * @param filePath 文件路径
     * @return 是否已加密
     */
    boolean isEncrypted(String filePath);

    /**
     * 检查加密服务是否可用
     * 验证加密密钥是否已配置
     *
     * @return 是否可用
     */
    boolean isAvailable();

    /**
     * 加密异常
     */
    class EncryptionException extends RuntimeException {
        public EncryptionException(String message) {
            super(message);
        }

        public EncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
