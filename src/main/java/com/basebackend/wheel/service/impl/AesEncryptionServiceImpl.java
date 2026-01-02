package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.config.BackupProperties;
import com.basebackend.wheel.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * AES-256-GCM 加密服务实现
 * 提供备份文件的加密和解密功能
 *
 * <p>文件格式：
 * <pre>
 * [MAGIC_BYTES (8 bytes)] [IV_LENGTH (4 bytes)] [IV (12 bytes)] [ENCRYPTED_DATA]
 * </pre>
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AesEncryptionServiceImpl implements EncryptionService {

    private final BackupProperties backupProperties;

    /**
     * 加密算法
     */
    private static final String ALGORITHM = "AES/GCM/NoPadding";

    /**
     * 密钥算法
     */
    private static final String KEY_ALGORITHM = "AES";

    /**
     * GCM 认证标签长度（bits）
     */
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * IV 长度（bytes）- GCM 推荐使用 12 字节
     */
    private static final int IV_LENGTH = 12;

    /**
     * 密钥长度（bytes）- AES-256 需要 32 字节
     */
    private static final int KEY_LENGTH = 32;

    /**
     * 魔数，用于标识加密文件
     */
    private static final byte[] MAGIC_BYTES = "WHLBKUP\0".getBytes(StandardCharsets.UTF_8);

    /**
     * 缓冲区大小（8KB）
     */
    private static final int BUFFER_SIZE = 8192;

    /**
     * 安全随机数生成器
     */
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 缓存的密钥
     */
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        initializeKey();
    }

    /**
     * 初始化加密密钥
     */
    private void initializeKey() {
        String encryptionKey = backupProperties.getEncryptionKey();
        
        if (!StringUtils.hasText(encryptionKey)) {
            log.warn("备份加密密钥未配置，加密功能将不可用。请设置环境变量 BACKUP_ENCRYPTION_KEY");
            return;
        }

        try {
            // 使用 SHA-256 将任意长度的密钥转换为 32 字节
            byte[] keyBytes = deriveKey(encryptionKey);
            this.secretKey = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
            log.info("备份加密服务初始化成功");
        } catch (Exception e) {
            log.error("备份加密密钥初始化失败", e);
            this.secretKey = null;
        }
    }

    @Override
    public void encryptFile(String sourcePath, String targetPath) {
        validateAvailability();
        validatePaths(sourcePath, targetPath);

        Path source = Paths.get(sourcePath);
        Path target = Paths.get(targetPath);

        if (!Files.exists(source)) {
            throw new EncryptionException("源文件不存在: " + sourcePath);
        }

        try {
            // 确保目标目录存在
            Files.createDirectories(target.getParent());

            // 生成随机 IV
            byte[] iv = generateIv();

            // 初始化加密器
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

            try (InputStream in = new BufferedInputStream(Files.newInputStream(source), BUFFER_SIZE);
                 OutputStream out = new BufferedOutputStream(Files.newOutputStream(target), BUFFER_SIZE)) {

                // 写入文件头
                writeHeader(out, iv);

                // 加密文件内容
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    byte[] encrypted = cipher.update(buffer, 0, bytesRead);
                    if (encrypted != null) {
                        out.write(encrypted);
                    }
                }

                // 写入最终块（包含 GCM 认证标签）
                byte[] finalBlock = cipher.doFinal();
                if (finalBlock != null) {
                    out.write(finalBlock);
                }
            }

            log.info("文件加密成功: {} -> {}", sourcePath, targetPath);

        } catch (EncryptionException e) {
            throw e;
        } catch (Exception e) {
            // 清理可能创建的不完整文件
            try {
                Files.deleteIfExists(target);
            } catch (IOException ignored) {
            }
            throw new EncryptionException("文件加密失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void decryptFile(String sourcePath, String targetPath) {
        validateAvailability();
        validatePaths(sourcePath, targetPath);

        Path source = Paths.get(sourcePath);
        Path target = Paths.get(targetPath);

        if (!Files.exists(source)) {
            throw new EncryptionException("源文件不存在: " + sourcePath);
        }

        if (!isEncrypted(sourcePath)) {
            throw new EncryptionException("文件未加密或格式不正确: " + sourcePath);
        }

        try {
            // 确保目标目录存在
            Files.createDirectories(target.getParent());

            try (InputStream in = new BufferedInputStream(Files.newInputStream(source), BUFFER_SIZE);
                 OutputStream out = new BufferedOutputStream(Files.newOutputStream(target), BUFFER_SIZE)) {

                // 读取文件头并获取 IV
                byte[] iv = readHeader(in);

                // 初始化解密器
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

                // 读取剩余的加密数据
                byte[] encryptedData = in.readAllBytes();
                
                // 解密数据
                byte[] decrypted = cipher.doFinal(encryptedData);
                out.write(decrypted);
            }

            log.info("文件解密成功: {} -> {}", sourcePath, targetPath);

        } catch (EncryptionException e) {
            throw e;
        } catch (javax.crypto.AEADBadTagException e) {
            // 清理可能创建的不完整文件
            try {
                Files.deleteIfExists(target);
            } catch (IOException ignored) {
            }
            throw new EncryptionException("解密失败：密钥错误或文件已损坏", e);
        } catch (Exception e) {
            // 清理可能创建的不完整文件
            try {
                Files.deleteIfExists(target);
            } catch (IOException ignored) {
            }
            throw new EncryptionException("文件解密失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isEncrypted(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return false;
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return false;
        }

        try (InputStream in = Files.newInputStream(path)) {
            byte[] header = new byte[MAGIC_BYTES.length];
            int bytesRead = in.read(header);
            
            if (bytesRead != MAGIC_BYTES.length) {
                return false;
            }
            
            return Arrays.equals(header, MAGIC_BYTES);
        } catch (IOException e) {
            log.warn("检查文件加密状态失败: {}", filePath, e);
            return false;
        }
    }

    @Override
    public boolean isAvailable() {
        return secretKey != null && backupProperties.isEncryptionEnabled();
    }

    // ==================== 私有方法 ====================

    /**
     * 验证加密服务可用性
     */
    private void validateAvailability() {
        if (secretKey == null) {
            throw new EncryptionException("加密服务不可用：密钥未配置。请设置环境变量 BACKUP_ENCRYPTION_KEY");
        }
    }

    /**
     * 验证路径参数
     */
    private void validatePaths(String sourcePath, String targetPath) {
        if (!StringUtils.hasText(sourcePath)) {
            throw new EncryptionException("源文件路径不能为空");
        }
        if (!StringUtils.hasText(targetPath)) {
            throw new EncryptionException("目标文件路径不能为空");
        }
        if (sourcePath.equals(targetPath)) {
            throw new EncryptionException("源文件和目标文件路径不能相同");
        }
    }

    /**
     * 从密码派生密钥
     * 使用 SHA-256 将任意长度的密码转换为 32 字节的密钥
     */
    private byte[] deriveKey(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        
        // SHA-256 产生 32 字节的哈希，正好是 AES-256 需要的密钥长度
        if (hash.length != KEY_LENGTH) {
            throw new EncryptionException("密钥派生失败：哈希长度不正确");
        }
        
        return hash;
    }

    /**
     * 生成随机 IV
     */
    private byte[] generateIv() {
        byte[] iv = new byte[IV_LENGTH];
        secureRandom.nextBytes(iv);
        return iv;
    }

    /**
     * 写入文件头
     */
    private void writeHeader(OutputStream out, byte[] iv) throws IOException {
        // 写入魔数
        out.write(MAGIC_BYTES);
        
        // 写入 IV 长度（4 字节，大端序）
        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
        lengthBuffer.putInt(iv.length);
        out.write(lengthBuffer.array());
        
        // 写入 IV
        out.write(iv);
    }

    /**
     * 读取文件头并返回 IV
     */
    private byte[] readHeader(InputStream in) throws IOException {
        // 读取并验证魔数
        byte[] magic = new byte[MAGIC_BYTES.length];
        int bytesRead = in.read(magic);
        if (bytesRead != MAGIC_BYTES.length || !Arrays.equals(magic, MAGIC_BYTES)) {
            throw new EncryptionException("无效的加密文件格式");
        }
        
        // 读取 IV 长度
        byte[] lengthBytes = new byte[4];
        bytesRead = in.read(lengthBytes);
        if (bytesRead != 4) {
            throw new EncryptionException("无效的加密文件格式：无法读取 IV 长度");
        }
        int ivLength = ByteBuffer.wrap(lengthBytes).getInt();
        
        // 验证 IV 长度
        if (ivLength != IV_LENGTH) {
            throw new EncryptionException("无效的加密文件格式：IV 长度不正确");
        }
        
        // 读取 IV
        byte[] iv = new byte[ivLength];
        bytesRead = in.read(iv);
        if (bytesRead != ivLength) {
            throw new EncryptionException("无效的加密文件格式：无法读取完整的 IV");
        }
        
        return iv;
    }
}
