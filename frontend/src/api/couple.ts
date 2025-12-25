import request from './request'

// 邀请DTO
export interface CoupleInviteDTO {
  partnerPhoneNumber: string
}

// 邀请结果
export interface InviteResult {
  inviteCode: string
  partnerPhoneNumber: string
  inviteMessage?: string
  status: string
}

// 接受结果
export interface AcceptResult {
  relationshipId: number
  partnerNickname: string
  status: string
  confirmedAt: string
}

// 情侣状态
export interface CoupleStatus {
  status: number // 0-无关系，1-待确认，2-已确认，3-已解除
  statusText?: string
  partnerUserId?: number
  partnerNickname?: string
  inviteCode?: string
  createdAt?: string
}

// 情侣信息
export interface CoupleInfo {
  userId1: number
  nickname1: string
  avatarUrl1?: string
  userId2: number
  nickname2: string
  avatarUrl2?: string
  status: string
  confirmedAt?: string
  createdAt: string
}

/**
 * 邀请情侣
 * POST /api/couple/invite
 * Requirements: 4.1
 */
export function inviteCouple(data: CoupleInviteDTO): Promise<InviteResult> {
  return request<InviteResult>({
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
export function acceptInvite(inviteCode: string): Promise<AcceptResult> {
  return request<AcceptResult>({
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
export function getCoupleStatus(): Promise<CoupleStatus> {
  return request<CoupleStatus>({
    url: '/api/couple/status',
    method: 'GET'
  })
}

/**
 * 获取情侣信息
 * GET /api/couple/info
 * Requirements: 4.4
 */
export function getCoupleInfo(): Promise<CoupleInfo | null> {
  return request<CoupleInfo | null>({
    url: '/api/couple/info',
    method: 'GET'
  })
}

/**
 * 解除情侣关系
 * DELETE /api/couple/unbind
 * Requirements: 4.5
 */
export function unbindCouple(): Promise<void> {
  return request<void>({
    url: '/api/couple/unbind',
    method: 'DELETE'
  })
}

/**
 * 验证邀请码
 * GET /api/couple/validate-invite-code
 * Requirements: 4.6
 */
export function validateInviteCode(inviteCode: string): Promise<boolean> {
  return request<boolean>({
    url: '/api/couple/validate-invite-code',
    method: 'GET',
    params: { inviteCode }
  })
}

// 获取情侣转盘记录 (保留原有功能)
export function getCoupleSpinHistory(params: {
  page?: number
  pageSize?: number
}): Promise<{
  list: Array<{
    id: number
    resultText: string
    spinTime: string
    partnerNickname: string
  }>
  total: number
}> {
  return request({
    url: '/api/couple/history',
    method: 'GET',
    params
  })
}
