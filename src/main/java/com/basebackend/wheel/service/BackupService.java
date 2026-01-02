package com.basebackend.wheel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.basebackend.wheel.dto.*;
import com.basebackend.wheel.entity.BackupRecord;

/**
 * 备份服务接口
 * 提供数据库备份的核心功能
 *
 * @author wheel-api
 * @since 2025-02-02
 */
public interface BackupService {

    // ==================== 备份执行 ====================

    /**
     * 执行数据库备份
     *
     * @param backupType  备份类型: 1-每日 2-每周 3-手动
     * @param description 备份描述（可选）
     * @return 备份记录
     */
    BackupRecord executeBackup(int backupType, String description);

    /**
     * 触发手动备份
     *
     * @param dto 手动备份DTO
     * @return 备份结果
     */
    BackupResultVO triggerManualBackup(ManualBackupDTO dto);

    /**
     * 执行每日备份
     *
     * @return 备份记录
     */
    BackupRecord executeDailyBackup();

    /**
     * 执行每周备份
     *
     * @return 备份记录
     */
    BackupRecord executeWeeklyBackup();

    // ==================== 备份查询 ====================

    /**
     * 分页查询备份列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<BackupRecordVO> listBackups(BackupQueryDTO query);

    /**
     * 获取备份详情
     *
     * @param backupId 备份ID
     * @return 备份记录VO
     */
    BackupRecordVO getBackupById(Long backupId);

    /**
     * 获取备份实体
     *
     * @param backupId 备份ID
     * @return 备份记录实体
     */
    BackupRecord getBackupEntityById(Long backupId);

    /**
     * 获取最近一次成功的备份
     *
     * @return 备份记录
     */
    BackupRecord getLastSuccessBackup();

    // ==================== 备份管理 ====================

    /**
     * 删除备份
     *
     * @param backupId 备份ID
     */
    void deleteBackup(Long backupId);

    /**
     * 清理过期备份
     * 删除超过保留期限的备份，但保留最少数量的备份
     *
     * @return 清理的备份数量
     */
    int cleanupExpiredBackups();

    // ==================== 备份进度 ====================

    /**
     * 获取备份进度
     *
     * @param backupId 备份ID
     * @return 备份进度VO
     */
    BackupProgressVO getBackupProgress(Long backupId);

    /**
     * 检查是否有备份正在进行
     *
     * @return 是否有备份进行中
     */
    boolean isBackupInProgress();

    // ==================== 备份统计 ====================

    /**
     * 获取备份统计信息
     *
     * @return 备份统计VO
     */
    BackupStatisticsVO getBackupStatistics();

    // ==================== 备份类型常量 ====================

    /**
     * 每日备份
     */
    int BACKUP_TYPE_DAILY = 1;

    /**
     * 每周备份
     */
    int BACKUP_TYPE_WEEKLY = 2;

    /**
     * 手动备份
     */
    int BACKUP_TYPE_MANUAL = 3;

    // ==================== 备份状态常量 ====================

    /**
     * 进行中
     */
    int STATUS_IN_PROGRESS = 0;

    /**
     * 成功
     */
    int STATUS_SUCCESS = 1;

    /**
     * 失败
     */
    int STATUS_FAILED = 2;
}
