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
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 内容列表
     */
    List<WheelContent> selectByCreateUserId(
            @Param("userId") Long userId,
            @Param("pageNum") Integer pageNum,
            @Param("pageSize") Integer pageSize
    );

    /**
     * 统计用户创建的内容数量
     *
     * @param userId 用户ID
     * @return 内容数量
     */
    int countByCreateUserId(@Param("userId") Long userId);
}