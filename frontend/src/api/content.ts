import request from './request'

// 内容提交DTO
export interface ContentSubmitDTO {
  contentText: string
  categoryId: number
  weight?: number
  timeSensitive?: boolean
  startTime?: string
  endTime?: string
}

// 内容实体
export interface WheelContent {
  id: string | number
  contentText: string
  categoryId: number
  categoryName?: string
  weight: number
  status: number // 0-待审核, 1-已通过, 2-已拒绝
  createdAt: string
  updatedAt: string
}

// 审核状态
export interface AuditStatus {
  status: number
  reason?: string
  auditTime?: string
}

// 分页响应
export interface PageResponse<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

// 提交结果
export interface SubmitResult {
  id: number
  contentText: string
  status: number
}

/**
 * 提交内容
 * POST /api/content/submit
 * Requirements: 3.1
 */
export function submitContent(data: ContentSubmitDTO): Promise<SubmitResult> {
  return request<SubmitResult>({
    url: '/api/content/submit',
    method: 'POST',
    data
  })
}

/**
 * 获取我的内容列表
 * GET /api/content/my
 * Requirements: 3.2
 */
export function getMyContents(pageNum: number = 1, pageSize: number = 10): Promise<PageResponse<WheelContent>> {
  return request<PageResponse<WheelContent>>({
    url: '/api/content/my',
    method: 'GET',
    params: { pageNum, pageSize }
  })
}

/**
 * 更新内容
 * PUT /api/content/{contentId}
 * Requirements: 3.3
 */
export function updateContent(contentId: number, data: ContentSubmitDTO): Promise<void> {
  return request<void>({
    url: `/api/content/${contentId}`,
    method: 'PUT',
    data
  })
}

/**
 * 删除内容
 * DELETE /api/content/{contentId}
 * Requirements: 3.4
 */
export function deleteContent(contentId: number): Promise<void> {
  return request<void>({
    url: `/api/content/${contentId}`,
    method: 'DELETE'
  })
}

/**
 * 举报内容
 * POST /api/content/report
 * Requirements: 3.5
 */
export function reportContent(contentId: number, reason: number, description?: string): Promise<void> {
  return request<void>({
    url: '/api/content/report',
    method: 'POST',
    data: { contentId, reason, description }
  })
}

/**
 * 获取审核状态
 * GET /api/content/{contentId}/audit-status
 * Requirements: 3.6
 */
export function getAuditStatus(contentId: number): Promise<AuditStatus> {
  return request<AuditStatus>({
    url: `/api/content/${contentId}/audit-status`,
    method: 'GET'
  })
}

/**
 * 搜索内容
 * GET /api/content/search
 * Requirements: 3.7
 */
export function searchContents(
  keyword?: string,
  categoryId?: number,
  pageNum: number = 1,
  pageSize: number = 10
): Promise<PageResponse<WheelContent>> {
  return request<PageResponse<WheelContent>>({
    url: '/api/content/search',
    method: 'GET',
    params: {
      keyword,
      categoryId,
      pageNum,
      pageSize
    }
  })
}
