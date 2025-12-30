package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.MomentComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 动态评论 Mapper
 */
@Mapper
public interface MomentCommentMapper extends BaseMapper<MomentComment> {
}
