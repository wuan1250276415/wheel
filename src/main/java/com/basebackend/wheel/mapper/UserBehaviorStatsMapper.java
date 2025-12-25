package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.UserBehaviorStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

@Mapper
public interface UserBehaviorStatsMapper extends BaseMapper<UserBehaviorStats> {
    /**
     * 查询用户指定日期的统计数据
     *
     * @param userId 用户ID
     * @param date   日期
     * @return 统计数据
     */
    UserBehaviorStats selectByUserIdAndDate(
            @Param("userId") Long userId,
            @Param("date") LocalDate date
    );
}