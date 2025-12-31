package com.basebackend.wheel.mapper;

import com.basebackend.wheel.entity.ContentAuditLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 内容审核日志Mapper
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Mapper
public interface ContentAuditLogMapper extends BaseMapper<ContentAuditLog> {

    /**
     * 根据内容ID查询审核日志
     *
     * @param contentId 内容ID
     * @return 审核日志列表
     */
    List<ContentAuditLog> selectByContentId(@Param("contentId") Long contentId);

    /**
     * 查询审核统计（按时间范围）
     */
    List<ContentAuditLog> selectAuditStatistics(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 查询审核员工作量
     */
    List<ContentAuditLog> selectAuditorWorkload(@Param("auditorId") Long auditorId, @Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 统计今日审核数据
     */
    List<ContentAuditLog> selectTodayAuditLogs();

    /**
     * 统计违规类型分布
     */
    List<Map<String, Object>> selectViolationDistribution(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 统计每日审核数量
     */
    List<Map<String, Object>> selectDailyAuditCount(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 统计审核员工作量汇总
     */
    List<Map<String, Object>> selectAuditorWorkloadSummary(@Param("startTime") String startTime, @Param("endTime") String endTime);

}
