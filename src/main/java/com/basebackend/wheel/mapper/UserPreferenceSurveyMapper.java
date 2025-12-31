package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.UserPreferenceSurvey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户偏好调查Mapper
 *
 * @author wheel-api
 */
@Mapper
public interface UserPreferenceSurveyMapper extends BaseMapper<UserPreferenceSurvey> {

    /**
     * 根据用户ID查询偏好调查
     *
     * @param userId 用户ID
     * @return 偏好调查
     */
    UserPreferenceSurvey selectByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否已完成调查
     *
     * @param userId 用户ID
     * @return 是否存在记录
     */
    boolean existsByUserId(@Param("userId") Long userId);
}
