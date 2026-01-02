package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basebackend.wheel.entity.BackupRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 备份记录 Mapper
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Mapper
public interface BackupRecordMapper extends BaseMapper<BackupRecord> {

    /**
     * 分页查询备份记录
     *
     * @param page       分页参数
     * @param backupType 备份类型
     * @param status     状态
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 分页结果
     */
    IPage<BackupRecord> selectBackupPage(
            Page<BackupRecord> page,
            @Param("backupType") Integer backupType,
            @Param("status") Integer status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询最近一次成功的备份
     *
     * @return 备份记录
     */
    BackupRecord selectLastSuccessBackup();

    /**
     * 查询指定时间之前的备份记录
     *
     * @param beforeTime 时间点
     * @return 备份记录列表
     */
    List<BackupRecord> selectBackupsBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 统计成功备份数量
     *
     * @return 成功备份数量
     */
    int countSuccessBackups();

    /**
     * 统计失败备份数量
     *
     * @return 失败备份数量
     */
    int countFailedBackups();

    /**
     * 统计总备份数量
     *
     * @return 总备份数量
     */
    int countTotalBackups();

    /**
     * 计算总存储使用量
     *
     * @return 总存储使用量（字节）
     */
    Long sumTotalStorageUsed();

    /**
     * 查询备份统计信息
     *
     * @return 统计信息Map
     */
    Map<String, Object> selectBackupStatistics();

    /**
     * 查询指定数量的最新备份（按创建时间降序）
     *
     * @param limit 数量限制
     * @return 备份记录列表
     */
    List<BackupRecord> selectLatestBackups(@Param("limit") int limit);

    /**
     * 查询进行中的备份
     *
     * @return 进行中的备份记录
     */
    BackupRecord selectInProgressBackup();

    /**
     * 更新备份状态
     *
     * @param id           备份ID
     * @param status       状态
     * @param fileSize     文件大小
     * @param durationMs   耗时
     * @param errorMessage 错误信息
     * @return 更新数量
     */
    int updateBackupStatus(
            @Param("id") Long id,
            @Param("status") Integer status,
            @Param("fileSize") Long fileSize,
            @Param("durationMs") Long durationMs,
            @Param("errorMessage") String errorMessage
    );

    /**
     * 批量删除备份记录（逻辑删除）
     *
     * @param ids 备份ID列表
     * @return 删除数量
     */
    int batchDeleteByIds(@Param("ids") List<Long> ids);

    /**
     * 统计今日备份数量
     *
     * @return 今日备份数量
     */
    int countTodayBackups();

    /**
     * 统计本周备份数量
     *
     * @return 本周备份数量
     */
    int countWeekBackups();

    /**
     * 统计本月备份数量
     *
     * @return 本月备份数量
     */
    int countMonthBackups();

    /**
     * 统计指定时间范围内的成功备份数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 成功备份数量
     */
    int countSuccessBackupsInRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内的失败备份数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 失败备份数量
     */
    int countFailedBackupsInRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
