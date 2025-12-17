package com.basebackend.wheel.mapper;

import com.basebackend.wheel.entity.WheelConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 转盘配置Mapper
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Mapper
public interface WheelConfigMapper extends BaseMapper<WheelConfig> {

    /**
     * 根据用户ID查询转盘配置
     *
     * @param userId 用户ID
     * @return 配置信息
     */
    WheelConfig selectByUserId(@Param("userId") Long userId);

    /**
     * 插入或更新用户转盘配置
     *
     * @param wheelConfig 转盘配置
     * @return 影响行数
     */
    int upsert(WheelConfig wheelConfig);
}
