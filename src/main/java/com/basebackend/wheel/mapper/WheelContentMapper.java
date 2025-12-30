package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.WheelContent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WheelContentMapper extends BaseMapper<WheelContent> {
    /**
     * 根据分类ID和审核状态查询内容
     *
     * @param categoryId  分类ID
     * @param auditStatus 审核状态
     * @return 内容列表
     */
    List<WheelContent> selectByCategoryIdAndAuditStatus(
            @Param("categoryId") Long categoryId,
            @Param("auditStatus") Integer auditStatus
    );

    /**
     * 查询用户创建的内容列表
     *
     * @param userId   用户ID
     * @param offset   偏移量
     * @param pageSize 每页大小
     * @return 内容列表
     */
    List<WheelContent> selectByCreateUserId(
            @Param("userId") Long userId,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );

    /**
     * 统计用户创建的内容数量
     *
     * @param userId 用户ID
     * @return 内容数量
     */
    int countByCreateUserId(@Param("userId") Long userId);

    List<WheelContent> selectByCategoryIdsAndAuditStatus(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("auditStatus") Integer auditStatus
    );

    /**
     * 查询审核队列（按优先级降序，创建时间升序）
     *
     * @param offset   偏移量
     * @param limit    限制数量
     * @return 待审核内容列表
     */
    List<WheelContent> selectAuditQueue(
            @Param("offset") Integer offset,
            @Param("limit") Integer limit
    );

    /**
     * 统计指定审核状态和优先级的内容数量
     *
     * @param auditStatus   审核状态
     * @param auditPriority 审核优先级
     * @return 内容数量
     */
    Integer countByAuditStatusAndPriority(
            @Param("auditStatus") Integer auditStatus,
            @Param("auditPriority") Integer auditPriority
    );
}