package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.UserReportCredibility;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户举报信誉 Mapper
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Mapper
public interface UserReportCredibilityMapper extends BaseMapper<UserReportCredibility> {

    /**
     * 根据用户ID查询信誉记录
     *
     * @param userId 用户ID
     * @return 信誉记录
     */
    UserReportCredibility selectByUserId(@Param("userId") Long userId);

    /**
     * 查询低信誉用户列表
     *
     * @param threshold 信誉阈值
     * @return 低信誉用户列表
     */
    List<UserReportCredibility> selectLowCredibilityUsers(@Param("threshold") Integer threshold);

    /**
     * 增加有效举报计数并调整信誉分
     *
     * @param userId    用户ID
     * @param increment 信誉分增量
     * @return 更新数量
     */
    int incrementValidReport(@Param("userId") Long userId, @Param("increment") Integer increment);

    /**
     * 增加无效举报计数并调整信誉分
     *
     * @param userId    用户ID
     * @param decrement 信誉分减量（正数）
     * @return 更新数量
     */
    int incrementInvalidReport(@Param("userId") Long userId, @Param("decrement") Integer decrement);

    /**
     * 初始化用户信誉记录
     *
     * @param userId 用户ID
     * @return 插入数量
     */
    int initUserCredibility(@Param("userId") Long userId);

    /**
     * 统计各信誉等级用户数量
     *
     * @return 信誉等级统计
     */
    List<java.util.Map<String, Object>> countByCredibilityLevel();

    /**
     * 查询举报活跃用户（按举报数排序）
     *
     * @param limit 限制数量
     * @return 活跃用户列表
     */
    List<UserReportCredibility> selectActiveReporters(@Param("limit") Integer limit);
}
