package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.UserAchievement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserAchievementMapper extends BaseMapper<UserAchievement> {
    List<UserAchievement> selectByUserId(@Param("userId") Long userId);

    UserAchievement selectByUserIdAndAchievementId(
        @Param("userId") Long userId,
        @Param("achievementId") Long achievementId
    );
}
