import request from './request'

// 登录请求参数
export interface LoginRequest {
  username: string
  password: string
}

// 登录响应数据
export interface LoginResponse {
  accessToken: string
  refreshToken?: string
  userInfo: {
    id: number
    username: string
    nickname?: string
    avatar?: string
    phone?: string
  }
}

// 登录接口
export function login(data: LoginRequest): Promise<LoginResponse> {
  return request<LoginResponse>({
    url: '/basebackend-user-api/api/user/auth/login',
    method: 'POST',
    data
  })
}

// 用户信息类型
export interface UserInfo {
  id: number
  username: string
  nickname?: string
  avatar?: string
  phone?: string
  createdAt?: string
}

// 注册接口
export function register(data: {
  username: string
  password: string
  nickname?: string
  avatar?: string
}) {
  return request<any>({
    url: '/basebackend-user-api/api/user/auth/register',
    method: 'POST',
    data
  })
}

// 获取用户信息
export function getUserInfo(): Promise<UserInfo> {
  return request<UserInfo>({
    url: '/basebackend-user-api/api/user/auth/profile',
    method: 'GET'
  })
}

// 更新用户信息
export function updateProfile(data: {
  nickname?: string
  avatar?: string
}) {
  return request<any>({
    url: '/basebackend-user-api/api/user/auth/profile',
    method: 'PUT',
    data
  })
}

// 刷新token
export function refreshToken(refreshTokenValue: string) {
  return request<{
    accessToken: string
    refreshToken: string
  }>({
    url: '/basebackend-user-api/api/user/auth/refresh',
    method: 'POST',
    data: { refreshToken: refreshTokenValue }
  })
}

// 登出
export function logout(): void {
  uni.removeStorageSync('token')
  uni.removeStorageSync('refreshToken')
  uni.removeStorageSync('userInfo')
}
