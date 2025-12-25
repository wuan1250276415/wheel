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
    userId: number
    id?: number
    username: string
    nickname?: string
    avatar?: string
    phone?: string
    gender?: number
    deptId?: number
    userType?: number
    status?: number
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
  userId?: number
  id?: number
  username: string
  nickname?: string
  avatar?: string
  phone?: string
  createdAt?: string
  gender?: number
  deptId?: number
  userType?: number
  status?: number
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

export interface SyncUserRequest {
  userId: number
  username?: string
  nickname?: string
  phone?: string
  avatar?: string
  gender?: number
  deptId?: number
  userType?: number
  status?: number
}

// 同步用户信息到wheel-api
export function syncWheelUser(data: SyncUserRequest) {
  return request<void>({
    url: '/api/auth/sync-user',
    method: 'POST',
    data
  })
}

// 登出
export function logout(): void {
  uni.removeStorageSync('token')
  uni.removeStorageSync('refreshToken')
  uni.removeStorageSync('userInfo')
}
