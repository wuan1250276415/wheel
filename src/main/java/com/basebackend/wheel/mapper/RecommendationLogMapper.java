package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.RecommendationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 推荐记录Mapper
 *
 * @author wheel-api
 */
@Mapper
public interface RecommendationLogMapper extends BaseMapper<RecommendationLog> {

    /**
     * 查询用户最近的推荐记录
     *
     * @param userId 用户ID
     * @param since 起始时间
     * @return 推荐记录列表
     */
    List<RecommendationLog> selectRecentByUserId(
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since
    );

    /**
     * 查询用户在指定时间内已看过的内容ID列表
     *
     * @param userId 用户ID
     * @param since 起始时间
     * @return 内容ID列表
     */
    List<Long> selectSeenContentIds(
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since
    );

    /**
     * 计算内容的点击率
     *
     * @param contentId 内容ID
     * @param minImpressions 最小曝光数
     * @return 点击率（0-1之间）
     */
    Double calculateCTR(
            @Param("contentId") Long contentId,
            @Param("minImpressions") Integer minImpressions
    );

    /**
     * 更新点击状态
     *
     * @param userId 用户ID
     * @param contentId 内容ID
     * @param sessionId 会话ID
     * @param clickTime 点击时间
     * @return 更新数量
     */
    int updateClickStatus(
            @Param("userId") Long userId,
            @Param("contentId") Long contentId,
            @Param("sessionId") String sessionId,
            @Param("clickTime") LocalDateTime clickTime
    );

    /**
     * 删除指定时间之前的记录
     *
     * @param before 截止时间
     * @return 删除数量
     */
    int deleteOldLogs(@Param("before") LocalDateTime before);

    /**
     * 统计内容的曝光次数
     *
     * @param contentId 内容ID
     * @return 曝光次数
     */
    Long countImpressions(@Param("contentId") Long contentId);

    /**
     * 统计内容的点击次数
     *
     * @param contentId 内容ID
     * @return 点击次数
     */
    Long countClicks(@Param("contentId") Long contentId);
}
