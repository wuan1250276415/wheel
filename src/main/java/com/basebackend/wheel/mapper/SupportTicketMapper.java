package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.SupportTicket;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单Mapper
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Mapper
public interface SupportTicketMapper extends BaseMapper<SupportTicket> {
}
