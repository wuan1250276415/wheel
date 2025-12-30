package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.UserTheme;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户主题Mapper
 */
@Mapper
public interface UserThemeMapper extends BaseMapper<UserTheme> {

    /**
     * 查询用户拥有的所有主题
     * @param userId 用户ID
     * @return 用户主题列表
     */
    List<UserTheme> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户当前使用的主题
     * @param userId 用户ID
     * @return 当前使用的主题
     */
    UserTheme selectActiveTheme(@Param("userId") Long userId);

    /**
     * 取消用户所有主题的激活状态
     * @param userId 用户ID
     * @return 影响行数
     */
    int deactivateAllThemes(@Param("userId") Long userId);

    /**
     * 检查用户是否拥有某个主题
     * @param userId 用户ID
     * @param themeId 主题ID
     * @return 是否拥有
     */
    UserTheme selectByUserIdAndThemeId(@Param("userId") Long userId, @Param("themeId") Long themeId);
}
