package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.Blacklist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 黑名单 Mapper
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Mapper
public interface BlacklistMapper extends BaseMapper<Blacklist> {

    /**
     * 根据类型和目标ID查询有效的黑名单记录
     *
     * @param type     类型
     * @param targetId 目标ID
     * @return 黑名单记录
     */
    Blacklist selectActiveByTypeAndTarget(@Param("type") Integer type, @Param("targetId") String targetId);

    /**
     * 查询用户的有效黑名单记录
     *
     * @param userId 用户ID
     * @return 黑名单记录
     */
    Blacklist selectActiveByUserId(@Param("userId") Long userId);

    /**
     * 查询IP的有效黑名单记录
     *
     * @param ip IP地址
     * @return 黑名单记录
     */
    Blacklist selectActiveByIp(@Param("ip") String ip);

    /**
     * 查询设备的有效黑名单记录
     *
     * @param deviceId 设备ID
     * @return 黑名单记录
     */
    Blacklist selectActiveByDeviceId(@Param("deviceId") String deviceId);

    /**
     * 查询已过期的黑名单记录
     *
     * @param now 当前时间
     * @return 过期的黑名单列表
     */
    List<Blacklist> selectExpired(@Param("now") LocalDateTime now);

    /**
     * 批量更新过期黑名单状态为已解除
     *
     * @param now 当前时间
     * @return 更新数量
     */
    int batchReleaseExpired(@Param("now") LocalDateTime now);

    /**
     * 查询待处理的申诉列表
     *
     * @return 申诉列表
     */
    List<Blacklist> selectPendingAppeals();

    /**
     * 统计各类型黑名单数量
     *
     * @return 类型统计
     */
    List<java.util.Map<String, Object>> countByType();

    /**
     * 查询指定时间范围内新增的黑名单
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 黑名单列表
     */
    List<Blacklist> selectByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                       @Param("endTime") LocalDateTime endTime);
}
