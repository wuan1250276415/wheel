package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.CoupleMoment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 情侣动态 Mapper
 */
@Mapper
public interface CoupleMomentMapper extends BaseMapper<CoupleMoment> {

    /**
     * 增加点赞数
     *
     * @param momentId 动态ID
     * @param delta 增量（可为负数）
     * @return 影响行数
     */
    int updateLikeCount(@Param("momentId") Long momentId, @Param("delta") Integer delta);

    /**
     * 增加评论数
     *
     * @param momentId 动态ID
     * @param delta 增量（可为负数）
     * @return 影响行数
     */
    int updateCommentCount(@Param("momentId") Long momentId, @Param("delta") Integer delta);
}
