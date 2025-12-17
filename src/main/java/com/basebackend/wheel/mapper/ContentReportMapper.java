package com.basebackend.wheel.mapper;

import com.basebackend.wheel.entity.ContentReport;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 内容举报Mapper
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Mapper
public interface ContentReportMapper extends BaseMapper<ContentReport> {

    /**
     * 查询待处理的举报
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 举报列表
     */
    List<ContentReport> selectPendingReports(
            @Param("pageNum") Integer pageNum,
            @Param("pageSize") Integer pageSize
    );

    /**
     * 统计待处理举报数量
     *
     * @return 待处理举报数量
     */
    int countPendingReports();

    /**
     * 根据内容ID查询举报
     *
     * @param contentId 内容ID
     * @return 举报列表
     */
    List<ContentReport> selectByContentId(@Param("contentId") Long contentId);

    /**
     * 统计用户的举报次数
     *
     * @param userId 用户ID
     * @return 举报次数
     */
    int countByReporterUserId(@Param("userId") Long userId);
}
