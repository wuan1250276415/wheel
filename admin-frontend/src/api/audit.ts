import request from '@/utils/request'
import type {
  ApiResponse,
  PageResponse,
  AuditContent,
  AuditHistory,
  AuditStatistics,
  AuditContentQueryDTO,
  AuditHistoryQueryDTO,
  PendingCount
} from '@/types/api'

/**
 * 获取审核队列
 */
export function getAuditQueue(params: AuditContentQueryDTO) {
  return request.get<any, ApiResponse<PageResponse<AuditContent>>>('/admin/audit/queue', { params })
}

/**
 * 获取待审核数量
 */
export function getPendingCount() {
  return request.get<any, ApiResponse<PendingCount>>('/admin/audit/pending-count')
}

/**
 * 通过审核
 */
export function approveContent(contentId: number, auditComment?: string) {
  return request.post<any, ApiResponse<void>>(`/admin/audit/approve/${contentId}`, { auditComment })
}

/**
 * 拒绝审核
 */
export function rejectContent(contentId: number, auditComment?: string) {
  return request.post<any, ApiResponse<void>>(`/admin/audit/reject/${contentId}`, { auditComment })
}

/**
 * 批量通过审核
 */
export function batchApproveContent(data: { contentIds: number[]; auditComment?: string }) {
  return request.post<any, ApiResponse<void>>('/admin/audit/batch-approve', data)
}

/**
 * 批量拒绝审核
 */
export function batchRejectContent(data: { contentIds: number[]; auditComment?: string }) {
  return request.post<any, ApiResponse<void>>('/admin/audit/batch-reject', data)
}

/**
 * 查询审核历史
 */
export function getAuditHistory(params: AuditHistoryQueryDTO) {
  return request.get<any, ApiResponse<PageResponse<AuditHistory>>>('/admin/audit/history', { params })
}

/**
 * 审核统计
 */
export function getAuditStatistics(params?: { startDate?: string; endDate?: string }) {
  return request.get<any, ApiResponse<AuditStatistics>>('/admin/audit/statistics', { params })
}
