/**
 * 黑名单API模块
 * 提供黑名单检查和申诉功能
 */
import request from './request'

/**
 * 黑名单类型枚举
 */
export enum BlacklistType {
  /** 用户封禁 */
  USER = 1,
  /** IP封禁 */
  IP = 2,
  /** 设备封禁 */
  DEVICE = 3
}

/**
 * 黑名单检查结果
 */
export interface BlacklistCheckResult {
  /** 是否被封禁 */
  blocked: boolean
  /** 黑名单ID */
  blacklistId?: number
  /** 封禁类型 */
  type?: number
  /** 封禁原因 */
  reason?: string
  /** 过期时间 */
  expireAt?: string
  /** 是否可以申诉 */
  canAppeal?: boolean
}

/**
 * 黑名单申诉请求
 */
export interface BlacklistAppealRequest {
  /** 黑名单ID */
  blacklistId?: number
  /** 申诉理由 */
  appealReason: string
  /** 联系方式 */
  contactInfo?: string
}

/**
 * 黑名单申诉结果
 */
export interface BlacklistAppealResult {
  /** 是否成功 */
  success: boolean
  /** 消息 */
  message: string
}

/**
 * 检查当前用户是否被封禁
 * GET /api/blacklist/check
 * Requirements: 4.1, 4.2, 4.3, 4.8
 */
export function checkBlacklist(): Promise<BlacklistCheckResult> {
  return request<BlacklistCheckResult>({
    url: '/api/blacklist/check',
    method: 'GET'
  })
}

/**
 * 提交黑名单申诉
 * POST /api/blacklist/appeal
 * Requirements: 4.7
 */
export function submitBlacklistAppeal(data: BlacklistAppealRequest): Promise<BlacklistAppealResult> {
  return request<BlacklistAppealResult>({
    url: '/api/blacklist/appeal',
    method: 'POST',
    data
  })
}

/**
 * 获取申诉状态
 * GET /api/blacklist/appeal/status
 * Requirements: 4.7
 */
export function getAppealStatus(blacklistId: number): Promise<{
  status: number
  statusText: string
  handleResult?: string
}> {
  return request({
    url: '/api/blacklist/appeal/status',
    method: 'GET',
    params: { blacklistId }
  })
}

/**
 * 跳转到封禁页面
 */
export function navigateToBlockedPage(blockInfo: BlacklistCheckResult) {
  const params = new URLSearchParams()
  if (blockInfo.blacklistId) params.append('id', String(blockInfo.blacklistId))
  if (blockInfo.type) params.append('type', String(blockInfo.type))
  if (blockInfo.reason) params.append('reason', encodeURIComponent(blockInfo.reason))
  if (blockInfo.expireAt) params.append('expireAt', encodeURIComponent(blockInfo.expireAt))
  if (blockInfo.canAppeal !== undefined) params.append('canAppeal', String(blockInfo.canAppeal))
  
  uni.redirectTo({
    url: `/pages/blocked/blocked?${params.toString()}`
  })
}
