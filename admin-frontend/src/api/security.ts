import request from '@/utils/request'
import type { ApiResponse, PageResponse } from '@/types/api'
import type {
  SensitiveWord,
  SensitiveWordQueryDTO,
  SensitiveWordCreateDTO,
  Blacklist,
  BlacklistQueryDTO,
  BlacklistCreateDTO,
  ContentReport,
  ContentReportQueryDTO,
  AuditConfig,
  ReportStatistics
} from '@/types/security'

// ==================== 敏感词管理 ====================

/**
 * 获取敏感词列表
 */
export function getSensitiveWordList(params: SensitiveWordQueryDTO) {
  return request.get<any, ApiResponse<PageResponse<SensitiveWord>>>('/admin/security/sensitive-words', { params })
}

/**
 * 添加敏感词
 */
export function addSensitiveWord(data: SensitiveWordCreateDTO) {
  return request.post<any, ApiResponse<void>>('/admin/security/sensitive-words', data)
}

/**
 * 更新敏感词
 */
export function updateSensitiveWord(id: number, data: SensitiveWordCreateDTO) {
  return request.put<any, ApiResponse<void>>(`/admin/security/sensitive-words/${id}`, data)
}

/**
 * 删除敏感词
 */
export function deleteSensitiveWord(id: number) {
  return request.delete<any, ApiResponse<void>>(`/admin/security/sensitive-words/${id}`)
}

/**
 * 批量删除敏感词
 */
export function batchDeleteSensitiveWords(ids: number[]) {
  return request.delete<any, ApiResponse<void>>('/admin/security/sensitive-words/batch', { data: { ids } })
}

/**
 * 刷新敏感词缓存
 */
export function refreshSensitiveWordCache() {
  return request.post<any, ApiResponse<void>>('/admin/security/sensitive-words/refresh-cache')
}

// ==================== 黑名单管理 ====================

/**
 * 获取黑名单列表
 */
export function getBlacklistList(params: BlacklistQueryDTO) {
  return request.get<any, ApiResponse<PageResponse<Blacklist>>>('/admin/security/blacklist', { params })
}

/**
 * 添加黑名单
 */
export function addBlacklist(data: BlacklistCreateDTO) {
  return request.post<any, ApiResponse<void>>('/admin/security/blacklist', data)
}

/**
 * 解除黑名单
 */
export function removeBlacklist(id: number) {
  return request.post<any, ApiResponse<void>>(`/admin/security/blacklist/${id}/remove`)
}

/**
 * 获取申诉列表
 */
export function getBlacklistAppeals(params: { status?: number; current?: number; size?: number }) {
  return request.get<any, ApiResponse<PageResponse<Blacklist>>>('/admin/security/blacklist/appeals', { params })
}

/**
 * 处理申诉
 */
export function handleBlacklistAppeal(id: number, data: { approved: boolean; reason?: string }) {
  return request.post<any, ApiResponse<void>>(`/admin/security/blacklist/${id}/appeal/handle`, data)
}

// ==================== 举报管理 ====================

/**
 * 获取举报列表
 */
export function getReportList(params: ContentReportQueryDTO) {
  return request.get<any, ApiResponse<PageResponse<ContentReport>>>('/admin/security/reports', { params })
}

/**
 * 处理举报
 */
export function handleReport(id: number, data: { status: number; handleResult?: string }) {
  return request.post<any, ApiResponse<void>>(`/admin/security/reports/${id}/handle`, data)
}

/**
 * 获取举报统计
 */
export function getReportStatistics(params?: { startDate?: string; endDate?: string }) {
  return request.get<any, ApiResponse<ReportStatistics>>('/admin/security/reports/statistics', { params })
}

// ==================== 审核配置 ====================

/**
 * 获取审核配置列表
 */
export function getAuditConfigList() {
  return request.get<any, ApiResponse<AuditConfig[]>>('/admin/security/audit-config')
}

/**
 * 更新审核配置
 */
export function updateAuditConfig(id: number, data: { configValue: string }) {
  return request.put<any, ApiResponse<void>>(`/admin/security/audit-config/${id}`, data)
}

/**
 * 批量更新审核配置
 */
export function batchUpdateAuditConfig(configs: { configKey: string; configValue: string }[]) {
  return request.put<any, ApiResponse<void>>('/admin/security/audit-config/batch', { configs })
}

/**
 * 导出审核报表
 */
export function exportAuditReport(params: {
  format: string
  content: string[]
  startDate?: string
  endDate?: string
}) {
  return request.post('/admin/audit/export', params, {
    responseType: 'blob'
  }).then((res: any) => {
    const blob = new Blob([res], {
      type: params.format === 'excel'
        ? 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        : 'application/pdf'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `审核报表_${params.startDate || 'all'}.${params.format === 'excel' ? 'xlsx' : 'pdf'}`
    link.click()
    window.URL.revokeObjectURL(url)
  })
}
