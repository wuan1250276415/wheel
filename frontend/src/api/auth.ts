import request from './request'

// 登录接口
export function login(data: { phone: string; password: string }) {
  return request<{
    code: number
    message: string
    data: {
      accessToken: string
      refreshToken: string
      userInfo: {
        id: number
        nickname: string
        avatar: string
        phone: string
      }
    }
  }>({
    url: '/auth/login',
    method: 'POST',
    data
  })
}

// 注册接口
export function register(data: {
  phone: string
  password: string
  nickname: string
  avatar?: string
}) {
  return request<{
    code: number
    message: string
    data: any
  }>({
    url: '/auth/register',
    method: 'POST',
    data
  })
}

// 获取用户信息
export function getUserInfo() {
  return request<{
    code: number
    message: string
    data: {
      id: number
      nickname: string
      avatar: string
      phone: string
      createdAt: string
    }
  }>({
    url: '/auth/profile',
    method: 'GET'
  })
}

// 更新用户信息
export function updateProfile(data: {
  nickname?: string
  avatar?: string
}) {
  return request<{
    code: number
    message: string
    data: any
  }>({
    url: '/auth/profile',
    method: 'PUT',
    data
  })
}

// 刷新token
export function refreshToken(refreshToken: string) {
  return request<{
    code: number
    message: string
    data: {
      accessToken: string
      refreshToken: string
    }
  }>({
    url: '/auth/refresh',
    method: 'POST',
    data: { refreshToken }
  })
}
