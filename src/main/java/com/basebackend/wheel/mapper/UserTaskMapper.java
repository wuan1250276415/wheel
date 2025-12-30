package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.UserTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface UserTaskMapper extends BaseMapper<UserTask> {

    List<UserTask> selectByUserIdAndPeriod(
            @Param("userId") Long userId,
            @Param("periodType") Integer periodType,
            @Param("periodStart") LocalDate periodStart
    );

    int updateProgress(
            @Param("id") Long id,
            @Param("currentCount") Integer currentCount,
            @Param("status") Integer status
    );

    List<UserTask> selectExpiredTasks(@Param("currentDate") LocalDate currentDate);
}
