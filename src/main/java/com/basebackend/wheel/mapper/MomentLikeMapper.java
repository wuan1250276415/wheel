package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.MomentLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 动态点赞 Mapper
 */
@Mapper
public interface MomentLikeMapper extends BaseMapper<MomentLike> {
}
