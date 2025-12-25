package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.WheelConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WheelConfigMapper extends BaseMapper {
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