/**
 * 情侣报告API模块
 * 提供报告查询、纪念日管理、报告分享等功能
 */
import request from './request'
import type {
  CoupleReportVO,
  ReportQueryDTO,
  ReportPageResult,
  AnniversaryVO,
  AnniversaryCreateDTO,
  AnniversaryUpdateDTO,
  ShareImageVO
} from '@/types/report'

// ==================== 报告相关接口 ====================

/**
 * 获取当前报告
 * GET /api/report/current
 * @param type 报告类型: 1-周报, 2-月报, 3-年报
 * Requirements: 1.1, 1.2
 */
export function getCurrentReport(type: number = 1): Promise<CoupleReportVO> {
  return request<CoupleReportVO>({
    url: '/api/report/current',
    method: 'GET',
    params: { type }
  })
}

/**
 * 获取历史报告列表
 * GET /api/report/history
 * @param query 查询参数
 * Requirements: 7.1, 7.2, 7.3
 */
export function getReportHistory(query?: ReportQueryDTO): Promise<ReportPageResult> {
  return request<ReportPageResult>({
    url: '/api/report/history',
    method: 'GET',
    params: query
  })
}

/**
 * 获取报告详情
 * GET /api/report/{reportId}
 * @param reportId 报告ID
 * Requirements: 7.4
 */
export function getReportById(reportId: number): Promise<CoupleReportVO> {
  return request<CoupleReportVO>({
    url: `/api/report/${reportId}`,
    method: 'GET'
  })
}

// ==================== 分享相关接口 ====================

/**
 * 生成分享图片
 * POST /api/report/{reportId}/share
 * @param reportId 报告ID
 * @param theme 主题: default, blue, purple
 * Requirements: 6.1, 6.3
 */
export function generateShareImage(reportId: number, theme: string = 'default'): Promise<ShareImageVO> {
  return request<ShareImageVO>({
    url: `/api/report/${reportId}/share`,
    method: 'POST',
    params: { theme }
  })
}

// ==================== 纪念日相关接口 ====================

/**
 * 获取纪念日列表
 * GET /api/report/anniversary
 * Requirements: 5.2
 */
export function getAnniversaries(): Promise<AnniversaryVO[]> {
  return request<AnniversaryVO[]>({
    url: '/api/report/anniversary',
    method: 'GET'
  })
}

/**
 * 添加纪念日
 * POST /api/report/anniversary
 * @param data 纪念日创建参数
 * Requirements: 5.2
 */
export function addAnniversary(data: AnniversaryCreateDTO): Promise<AnniversaryVO> {
  return request<AnniversaryVO>({
    url: '/api/report/anniversary',
    method: 'POST',
    data
  })
}

/**
 * 更新纪念日
 * PUT /api/report/anniversary/{id}
 * @param id 纪念日ID
 * @param data 纪念日更新参数
 * Requirements: 5.2
 */
export function updateAnniversary(id: number, data: AnniversaryUpdateDTO): Promise<AnniversaryVO> {
  return request<AnniversaryVO>({
    url: `/api/report/anniversary/${id}`,
    method: 'PUT',
    data
  })
}

/**
 * 删除纪念日
 * DELETE /api/report/anniversary/{id}
 * @param id 纪念日ID
 * Requirements: 5.6
 */
export function deleteAnniversary(id: number): Promise<void> {
  return request<void>({
    url: `/api/report/anniversary/${id}`,
    method: 'DELETE'
  })
}
