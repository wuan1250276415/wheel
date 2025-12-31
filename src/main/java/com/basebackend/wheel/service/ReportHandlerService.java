package com.basebackend.wheel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basebackend.wheel.dto.ContentReportQueryDTO;
import com.basebackend.wheel.dto.ReportRequest;
import com.basebackend.wheel.dto.ReportResult;
import com.basebackend.wheel.entity.ContentReport;

/**
 * 举报处理服务接口
 * 
 * @author wheel-api
 * @since 2025-02-01
 */
public interface ReportHandlerService {

    /**
     * 提交举报
     *
     * @param request 举报请求
     * @return 举报结果
     */
    ReportResult submitReport(ReportRequest request);

    /**
     * 处理举报
     *
     * @param reportId     举报ID
     * @param handlerId    处理人ID
     * @param handleStatus 处理状态：1-已处理，2-已忽略
     * @param handleResult 处理结果
     * @param isValid      举报是否有效（用于更新举报人信誉）
     */
    void handleReport(Long reportId, Long handlerId, Integer handleStatus, String handleResult, boolean isValid);

    /**
     * 批量处理举报
     *
     * @param reportIds    举报ID列表
     * @param handlerId    处理人ID
     * @param handleStatus 处理状态
     * @param handleResult 处理结果
     * @param isValid      举报是否有效
     */
    void batchHandleReport(java.util.List<Long> reportIds, Long handlerId, Integer handleStatus, String handleResult, boolean isValid);

    /**
     * 获取举报列表（分页）
     *
     * @param query 查询条件
     * @return 举报列表
     */
    Page<ContentReport> getReportList(ContentReportQueryDTO query);

    /**
     * 检查用户是否已举报过该内容
     *
     * @param contentId  内容ID
     * @param reporterId 举报人ID
     * @return true-已举报过，false-未举报过
     */
    boolean hasReported(Long contentId, Long reporterId);

    /**
     * 统计内容被举报次数
     *
     * @param contentId 内容ID
     * @return 举报次数
     */
    int countReportsByContent(Long contentId);

    /**
     * 获取用户举报信誉分
     *
     * @param userId 用户ID
     * @return 信誉分（0-100）
     */
    int getUserReportCredibility(Long userId);

    /**
     * 根据内容举报次数更新审核优先级
     *
     * @param contentId 内容ID
     */
    void updateContentPriorityByReportCount(Long contentId);
}
