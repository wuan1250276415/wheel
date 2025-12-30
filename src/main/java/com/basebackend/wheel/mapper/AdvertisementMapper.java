package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.Advertisement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 广告Mapper
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Mapper
public interface AdvertisementMapper extends BaseMapper<Advertisement> {

    /**
     * 根据广告位查询有效广告
     *
     * @param placementKey 广告位标识
     * @param limit        限制数量
     * @return 广告列表
     */
    List<Advertisement> selectActiveAdsByPlacement(@Param("placementKey") String placementKey,
                                                    @Param("limit") Integer limit);
}
