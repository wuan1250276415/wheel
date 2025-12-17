import request from './request'

// 创建邀请码
export function createInviteCode() {
  return request<{
    code: number
    message: string
    data: {
      inviteCode: string
      qrCode: string
      expiresAt: string
    }
  }>({
    url: '/couple/invite',
    method: 'POST'
  })
}

// 接受邀请
export function acceptInvite(data: { inviteCode: string }) {
  return request<{
    code: number
    message: string
    data: {
      coupleId: number
      partnerInfo: {
        nickname: string
        avatar: string
      }
    }
  }>({
    url: '/couple/accept',
    method: 'POST',
    data
  })
}

// 获取情侣关系信息
export function getCoupleInfo() {
  return request<{
    code: number
    message: string
    data: {
      id: number
      status: number
      inviteCode: string
      partnerInfo: {
        id: number
        nickname: string
        avatar: string
      } | null
      createdAt: string
    } | null
  }>({
    url: '/couple/info',
    method: 'GET'
  })
}

// 解除情侣关系
export function breakCouple() {
  return request<{
    code: number
    message: string
    data: any
  }>({
    url: '/couple/break',
    method: 'DELETE'
  })
}

// 获取情侣转盘记录
export function getCoupleSpinHistory(params: {
  page?: number
  pageSize?: number
}) {
  return request<{
    code: number
    message: string
    data: {
      list: Array<{
        id: number
        resultText: string
        spinTime: string
        partnerNickname: string
      }>
      total: number
    }
  }>({
    url: '/couple/history',
    method: 'GET',
    data: params
  })
}
