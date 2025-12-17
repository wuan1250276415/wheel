package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.WheelSpinRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface WheelSpinRecordMapper extends BaseMapper<WheelSpinRecord> {
    /**
     * 查询用户的历史记录
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 记录列表
     */
    List<WheelSpinRecord> selectByUserId(
            @Param("userId") Long userId,
            @Param("pageNum") Integer pageNum,
            @Param("pageSize") Integer pageSize
    );

    /**
     * 统计用户今日转盘次数
     *
     * @param userId 用户ID
     * @param date   日期
     * @return 转盘次数
     */
    int countTodayByUserId(
            @Param("userId") Long userId,
            @Param("date") LocalDateTime date
    );


}