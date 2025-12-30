package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.TicketReply;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单回复Mapper
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Mapper
public interface TicketReplyMapper extends BaseMapper<TicketReply> {
}
