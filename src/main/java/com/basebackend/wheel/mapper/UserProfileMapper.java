package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户画像Mapper
 *
 * @author wheel-api
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {

    /**
     * 根据用户ID查询画像
     *
     * @param userId 用户ID
     * @return 用户画像
     */
    UserProfile selectByUserId(@Param("userId") Long userId);

    /**
     * 查询过期的用户画像列表
     *
     * @param staleThreshold 过期阈值时间
     * @param limit 限制数量
     * @return 过期画像列表
     */
    List<UserProfile> selectStaleProfiles(
            @Param("staleThreshold") LocalDateTime staleThreshold,
            @Param("limit") Integer limit
    );

    /**
     * 批量更新画像过期状态
     *
     * @param staleThreshold 过期阈值时间
     * @return 更新数量
     */
    int batchMarkStale(@Param("staleThreshold") LocalDateTime staleThreshold);

    /**
     * 更新用户画像的活跃时间和转盘次数
     *
     * @param userId 用户ID
     * @param lastActiveTime 最后活跃时间
     * @return 更新数量
     */
    int updateActivityInfo(
            @Param("userId") Long userId,
            @Param("lastActiveTime") LocalDateTime lastActiveTime
    );

    /**
     * 统计用户在指定时间范围内各分类的转盘次数
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分类ID -> 转盘次数的映射列表
     */
    List<Map<String, Object>> selectCategorySpinStats(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计用户在指定时间范围内各小时的转盘次数
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 小时 -> 转盘次数的映射列表
     */
    List<Map<String, Object>> selectHourlySpinStats(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计用户在指定时间范围内的总转盘次数
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 转盘次数
     */
    Integer countSpinsInRange(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 根据人口统计信息查询相似用户的画像
     * 用于冷启动阶段的人口统计相似度推荐
     *
     * @param gender 性别（可选）
     * @param minAge 最小年龄（可选）
     * @param maxAge 最大年龄（可选）
     * @param city 城市（可选）
     * @param excludeUserId 排除的用户ID
     * @param limit 限制数量
     * @return 相似用户的画像列表
     */
    List<UserProfile> selectByDemographics(
            @Param("gender") Integer gender,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("city") String city,
            @Param("excludeUserId") Long excludeUserId,
            @Param("limit") Integer limit
    );
}
