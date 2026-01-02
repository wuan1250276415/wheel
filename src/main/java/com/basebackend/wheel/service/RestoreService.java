package com.basebackend.wheel.service;

import com.basebackend.wheel.dto.RestoreProgressVO;
import com.basebackend.wheel.dto.RestoreResultVO;
import com.basebackend.wheel.entity.RestoreRecord;
import org.springframework.web.multipart.MultipartFile;

/**
 * 数据恢复服务接口
 * 提供从备份文件恢复数据库的功能
 *
 * @author wheel-api
 * @since 2025-02-02
 */
public interface RestoreService {

    // ==================== 恢复执行 ====================

    /**
     * 从备份记录恢复数据库
     * 1. 验证备份文件完整性
     * 2. 创建恢复前备份
     * 3. 执行恢复操作
     * 4. 记录恢复日志
     *
     * @param backupId 备份记录ID
     * @return 恢复结果
     */
    RestoreResultVO restoreFromBackup(Long backupId);

    /**
     * 从上传的备份文件恢复数据库
     * 1. 保存上传文件
     * 2. 验证文件完整性
     * 3. 创建恢复前备份
     * 4. 执行恢复操作
     * 5. 记录恢复日志
     *
     * @param file 上传的备份文件
     * @return 恢复结果
     */
    RestoreResultVO restoreFromUpload(MultipartFile file);

    // ==================== 恢复进度 ====================

    /**
     * 获取恢复进度
     *
     * @param restoreId 恢复记录ID
     * @return 恢复进度VO
     */
    RestoreProgressVO getRestoreProgress(Long restoreId);

    /**
     * 检查是否有恢复正在进行
     *
     * @return 是否有恢复进行中
     */
    boolean isRestoreInProgress();

    // ==================== 恢复验证 ====================

    /**
     * 验证备份文件完整性
     * 检查文件是否存在、格式是否正确、是否可以解压/解密
     *
     * @param filePath 备份文件路径
     * @return 是否有效
     */
    boolean validateBackupFile(String filePath);

    /**
     * 验证备份记录对应的文件
     *
     * @param backupId 备份记录ID
     * @return 是否有效
     */
    boolean validateBackupById(Long backupId);

    // ==================== 恢复记录查询 ====================

    /**
     * 获取恢复记录
     *
     * @param restoreId 恢复记录ID
     * @return 恢复记录
     */
    RestoreRecord getRestoreById(Long restoreId);

    /**
     * 获取最近一次恢复记录
     *
     * @return 恢复记录
     */
    RestoreRecord getLastRestore();

    // ==================== 恢复回滚 ====================

    /**
     * 执行恢复回滚
     * 当恢复失败时，尝试从恢复前备份恢复到之前的状态
     *
     * @param restoreId 恢复记录ID
     * @return 回滚是否成功
     */
    boolean rollbackRestore(Long restoreId);
}
