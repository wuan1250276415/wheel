package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basebackend.wheel.dto.ContentReportQueryDTO;
import com.basebackend.wheel.entity.ContentReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ContentReportMapper extends BaseMapper<ContentReport> {

    /**
     * 根据内容ID查询举报
     *
     * @param contentId 内容ID
     * @return 举报列表
     */
    List<ContentReport> selectByContentId(@Param("contentId") Long contentId);

    /**
     * 检查用户是否已举报过该内容
     *
     * @param contentId  内容ID
     * @param reporterId 举报人ID
     * @return 举报数量
     */
    Integer checkDuplicateReport(@Param("contentId") Long contentId, @Param("reporterId") Long reporterId);

    /**
     * 统计内容被举报次数
     *
     * @param contentId 内容ID
     * @return 举报次数
     */
    Integer countByContentId(@Param("contentId") Long contentId);

    /**
     * 更新内容举报优先级
     *
     * @param contentId 内容ID
     * @param priority  优先级
     * @return 更新数量
     */
    int updatePriorityByContentId(@Param("contentId") Long contentId, @Param("priority") Integer priority);

    /**
     * 分页查询举报列表
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 举报列表
     */
    Page<ContentReport> selectReportPage(Page<ContentReport> page, @Param("query") ContentReportQueryDTO query);

    /**
     * 批量更新处理状态
     *
     * @param reportIds    举报ID列表
     * @param handleStatus 处理状态
     * @param handlerId    处理人ID
     * @param handleResult 处理结果
     * @return 更新数量
     */
    int batchUpdateStatus(@Param("reportIds") List<Long> reportIds,
                          @Param("handleStatus") Integer handleStatus,
                          @Param("handlerId") Long handlerId,
                          @Param("handleResult") String handleResult);

    /**
     * 根据ID更新处理状态
     *
     * @param reportId     举报ID
     * @param handleStatus 处理状态
     * @param handlerId    处理人ID
     * @param handleResult 处理结果
     * @return 更新数量
     */
    int updateStatusById(@Param("reportId") Long reportId,
                         @Param("handleStatus") Integer handleStatus,
                         @Param("handlerId") Long handlerId,
                         @Param("handleResult") String handleResult);
}