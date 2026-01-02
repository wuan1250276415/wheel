package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basebackend.wheel.entity.RestoreRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 恢复记录 Mapper
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Mapper
public interface RestoreRecordMapper extends BaseMapper<RestoreRecord> {

    /**
     * 分页查询恢复记录
     *
     * @param page       分页参数
     * @param sourceType 来源类型
     * @param status     状态
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 分页结果
     */
    IPage<RestoreRecord> selectRestorePage(
            Page<RestoreRecord> page,
            @Param("sourceType") Integer sourceType,
            @Param("status") Integer status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 根据备份ID查询恢复记录
     *
     * @param backupId 备份ID
     * @return 恢复记录列表
     */
    List<RestoreRecord> selectByBackupId(@Param("backupId") Long backupId);

    /**
     * 查询进行中的恢复
     *
     * @return 进行中的恢复记录
     */
    RestoreRecord selectInProgressRestore();

    /**
     * 查询最近一次恢复记录
     *
     * @return 恢复记录
     */
    RestoreRecord selectLastRestore();

    /**
     * 更新恢复状态
     *
     * @param id           恢复ID
     * @param status       状态
     * @param durationMs   耗时
     * @param errorMessage 错误信息
     * @return 更新数量
     */
    int updateRestoreStatus(
            @Param("id") Long id,
            @Param("status") Integer status,
            @Param("durationMs") Long durationMs,
            @Param("errorMessage") String errorMessage
    );

    /**
     * 更新恢复前备份ID
     *
     * @param id                  恢复ID
     * @param preRestoreBackupId  恢复前备份ID
     * @return 更新数量
     */
    int updatePreRestoreBackupId(
            @Param("id") Long id,
            @Param("preRestoreBackupId") Long preRestoreBackupId
    );

    /**
     * 统计恢复记录数量
     *
     * @return 恢复记录数量
     */
    int countTotalRestores();

    /**
     * 统计成功恢复数量
     *
     * @return 成功恢复数量
     */
    int countSuccessRestores();

    /**
     * 查询指定数量的最新恢复记录
     *
     * @param limit 数量限制
     * @return 恢复记录列表
     */
    List<RestoreRecord> selectLatestRestores(@Param("limit") int limit);
}
