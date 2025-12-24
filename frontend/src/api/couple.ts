import request from './request'

// 邀请DTO
export interface CoupleInviteDTO {
  partnerPhoneNumber: string
}

// 邀请结果
export interface InviteResult {
  inviteCode: string
  expiresAt: string
}

// 接受结果
export interface AcceptResult {
  coupleId: number
  partnerInfo: {
    id: number
    nickname: string
    avatar?: string
  }
}

// 情侣状态
export interface CoupleStatus {
  hasCouple: boolean
  status: number // 0-待确认, 1-已确认, 2-已解除
  inviteCode?: string
}

// 情侣信息
export interface CoupleInfo {
  id: number
  partnerId: number
  partnerNickname: string
  partnerAvatar?: string
  createdAt: string
  totalSpins: number
}

// API响应类型
interface ApiResponse<T> {
  code?: number
  message?: string
  data?: T
}

/**
 * 邀请情侣
 * POST /api/couple/invite
 * Requirements: 4.1
 */
export function inviteCouple(data: CoupleInviteDTO): Promise<ApiResponse<InviteResult>> {
  return request<ApiResponse<InviteResult>>({
    url: '/api/couple/invite',
    method: 'POST',
    data
  })
}

/**
 * 接受邀请
 * POST /api/couple/accept
 * Requirements: 4.2
 */
export function acceptInvite(inviteCode: string): Promise<ApiResponse<AcceptResult>> {
  return request<ApiResponse<AcceptResult>>({
    url: '/api/couple/accept',
    method: 'POST',
    data: { inviteCode }
  })
}

/**
 * 获取情侣状态
 * GET /api/couple/status
 * Requirements: 4.3
 */
export function getCoupleStatus(): Promise<ApiResponse<CoupleStatus>> {
  return request<ApiResponse<CoupleStatus>>({
    url: '/api/couple/status',
    method: 'GET'
  })
}

/**
 * 获取情侣信息
 * GET /api/couple/info
 * Requirements: 4.4
 */
export function getCoupleInfo(): Promise<ApiResponse<CoupleInfo>> {
  return request<ApiResponse<CoupleInfo>>({
    url: '/api/couple/info',
    method: 'GET'
  })
}

/**
 * 解除情侣关系
 * DELETE /api/couple/unbind
 * Requirements: 4.5
 */
export function unbindCouple(): Promise<ApiResponse<void>> {
  return request<ApiResponse<void>>({
    url: '/api/couple/unbind',
    method: 'DELETE'
  })
}

/**
 * 验证邀请码
 * GET /api/couple/validate-invite-code
 * Requirements: 4.6
 */
export function validateInviteCode(inviteCode: string): Promise<ApiResponse<boolean>> {
  return request<ApiResponse<boolean>>({
    url: '/api/couple/validate-invite-code',
    method: 'GET',
    data: { inviteCode }
  })
}

// 获取情侣转盘记录 (保留原有功能)
export function getCoupleSpinHistory(params: {
  page?: number
  pageSize?: number
}): Promise<ApiResponse<{
  list: Array<{
    id: number
    resultText: string
    spinTime: string
    partnerNickname: string
  }>
  total: number
}>> {
  return request({
    url: '/api/couple/history',
    method: 'GET',
    data: params
  })
}
