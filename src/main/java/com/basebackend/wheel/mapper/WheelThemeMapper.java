package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.WheelTheme;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 转盘主题Mapper
 */
@Mapper
public interface WheelThemeMapper extends BaseMapper<WheelTheme> {

    /**
     * 查询所有启用的主题
     * @return 主题列表
     */
    List<WheelTheme> selectEnabledThemes();

    /**
     * 根据主题key查询主题
     * @param themeKey 主题key
     * @return 主题信息
     */
    WheelTheme selectByThemeKey(@Param("themeKey") String themeKey);

    /**
     * 查询默认主题
     * @return 默认主题
     */
    WheelTheme selectDefaultTheme();
}
