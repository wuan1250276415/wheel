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

    /**
     * 统计内容的转盘次数
     *
     * @param contentId 内容ID
     * @return 转盘次数
     */
    int countByContentId(@Param("contentId") Long contentId);

    /**
     * 统计内容在指定时间范围内的转盘次数
     *
     * @param contentId 内容ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 转盘次数
     */
    int countByContentIdInRange(
            @Param("contentId") Long contentId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 批量统计内容的转盘次数
     *
     * @param contentIds 内容ID列表
     * @return 内容ID和转盘次数的映射列表
     */
    List<java.util.Map<String, Object>> batchCountByContentIds(@Param("contentIds") List<Long> contentIds);

    /**
     * 获取内容最近一次被转到的时间
     *
     * @param contentId 内容ID
     * @return 最近转盘时间
     */
    LocalDateTime getLastSpinTimeByContentId(@Param("contentId") Long contentId);
}