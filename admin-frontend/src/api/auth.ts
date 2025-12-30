import userApiRequest from '@/utils/request'
import type { ApiResponse, LoginResponse, AdminInfo } from '@/types/api'

/**
 * 管理员登录
 */
export function login(username: string, password: string) {
  return userApiRequest.post<any, ApiResponse<LoginResponse>>('/api/user/auth/login', {
    username,
    password
  })
}

/**
 * 刷新Token
 */
export function refreshToken(refreshToken: string) {
  return userApiRequest.post<any, ApiResponse<LoginResponse>>('/api/user/auth/refresh', null, {
    params: { refreshToken }
  })
}

/**
 * 管理员登出
 */
export function logout() {
  return userApiRequest.post<any, ApiResponse<void>>('/api/user/auth/logout')
}

/**
 * 获取当前管理员信息
 */
export function getCurrentAdmin() {
  return userApiRequest.get<any, ApiResponse<AdminInfo>>('/api/user/auth/current')
}
