package com.basebackend.wheel.mapper;

import com.basebackend.wheel.entity.UserBehaviorStats;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户行为统计Mapper
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Mapper
public interface UserBehaviorStatsMapper extends BaseMapper<UserBehaviorStats> {

    /**
     * 查询用户指定日期的统计数据
     *
     * @param userId 用户ID
     * @param date 日期
     * @return 统计数据
     */
    UserBehaviorStats selectByUserIdAndDate(
            @Param("userId") Long userId,
            @Param("date") LocalDate date
    );

    /**
     * 插入或更新用户行为统计
     *
     * @param stats 统计数据
     * @return 影响行数
     */
    int upsert(UserBehaviorStats stats);

    /**
     * 查询用户近期的统计数据
     *
     * @param userId 用户ID
     * @param days 天数
     * @return 统计数据列表
     */
    List<UserBehaviorStats> selectRecentByUserId(
            @Param("userId") Long userId,
            @Param("days") Integer days
    );
}
