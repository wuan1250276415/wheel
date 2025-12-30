package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.SupportStaff;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 客服人员Mapper
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Mapper
public interface SupportStaffMapper extends BaseMapper<SupportStaff> {

    /**
     * 查询可用的VIP专属客服
     *
     * @return 客服人员
     */
    SupportStaff selectAvailableVipStaff();

    /**
     * 查询可用的普通客服
     *
     * @return 客服人员
     */
    SupportStaff selectAvailableNormalStaff();

    /**
     * 增加客服当前工单数
     *
     * @param staffId 客服ID
     * @return 影响行数
     */
    int incrementCurrentTickets(@Param("staffId") Long staffId);

    /**
     * 减少客服当前工单数
     *
     * @param staffId 客服ID
     * @return 影响行数
     */
    int decrementCurrentTickets(@Param("staffId") Long staffId);
}
