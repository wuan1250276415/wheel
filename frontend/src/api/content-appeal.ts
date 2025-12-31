/**
 * 内容申诉API模块
 * 提供内容审核申诉功能
 */
import request from './request'

/**
 * 申诉状态枚举
 */
export enum AppealStatus {
  /** 待处理 */
  PENDING = 0,
  /** 申诉通过 */
  APPROVED = 1,
  /** 申诉驳回 */
  REJECTED = 2
}

/**
 * 内容申诉请求
 */
export interface ContentAppealRequest {
  /** 内容ID */
  contentId: number
  /** 申诉理由 */
  appealReason: string
  /** 补充说明 */
  additionalInfo?: string
  /** 证据截图URL列表 */
  evidenceUrls?: string[]
}

/**
 * 内容申诉结果
 */
export interface ContentAppealResult {
  /** 申诉ID */
  appealId: number
  /** 是否成功 */
  success: boolean
  /** 消息 */
  message: string
}

/**
 * 申诉状态信息
 */
export interface AppealStatusInfo {
  /** 申诉ID */
  appealId: number
  /** 内容ID */
  contentId: number
  /** 申诉状态 */
  status: number
  /** 状态文本 */
  statusText: string
  /** 申诉理由 */
  appealReason: string
  /** 处理结果 */
  handleResult?: string
  /** 申诉时间 */
  appealTime: string
  /** 处理时间 */
  handleTime?: string
}

/**
 * 提交内容申诉
 * POST /api/content/appeal
 * Requirements: 6.7
 */
export function submitContentAppeal(data: ContentAppealRequest): Promise<ContentAppealResult> {
  return request<ContentAppealResult>({
    url: '/api/content/appeal',
    method: 'POST',
    data
  })
}

/**
 * 获取申诉状态
 * GET /api/content/appeal/status
 * Requirements: 6.7
 */
export function getAppealStatus(contentId: number): Promise<AppealStatusInfo | null> {
  return request<AppealStatusInfo | null>({
    url: '/api/content/appeal/status',
    method: 'GET',
    params: { contentId }
  })
}

/**
 * 检查是否可以申诉
 * GET /api/content/appeal/check
 * Requirements: 6.7
 */
export function checkCanAppeal(contentId: number): Promise<{
  canAppeal: boolean
  reason?: string
}> {
  return request({
    url: '/api/content/appeal/check',
    method: 'GET',
    params: { contentId }
  })
}

/**
 * 获取申诉状态文本
 */
export function getAppealStatusText(status: number): string {
  switch (status) {
    case AppealStatus.PENDING:
      return '待处理'
    case AppealStatus.APPROVED:
      return '申诉通过'
    case AppealStatus.REJECTED:
      return '申诉驳回'
    default:
      return '未知状态'
  }
}
