package com.basebackend.wheel.mapper;

import com.basebackend.wheel.entity.ContentAuditLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 内容审核日志Mapper
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Mapper
public interface ContentAuditLogMapper extends BaseMapper<ContentAuditLog> {

    /**
     * 根据内容ID查询审核日志
     *
     * @param contentId 内容ID
     * @return 审核日志列表
     */
    List<ContentAuditLog> selectByContentId(@Param("contentId") Long contentId);

    /**
     * 查询待审核的内容
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 待审核内容列表
     */
    List<ContentAuditLog> selectPendingAudits(
            @Param("pageNum") Integer pageNum,
            @Param("pageSize") Integer pageSize
    );

    /**
     * 统计待审核数量
     *
     * @return 待审核数量
     */
    int countPendingAudits();
}
