/**
 * HTTP请求封装
 * Gateway URL: http://192.168.66.126:8280/wheel-api
 * 用户认证服务URL: http://192.168.66.126:8280/basebackend-user-api
 */

// Gateway基础URL（用于wheel-api相关接口）
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://192.168.66.126:8280/wheel-api'

// Gateway根URL（用于用户认证等其他服务）
const GATEWAY_ROOT_URL = import.meta.env.VITE_GATEWAY_URL || 'http://192.168.66.126:8280'

// 登录相关的URL路径（这些路径使用GATEWAY_ROOT_URL而不是BASE_URL）
const AUTH_PATHS = ['/basebackend-user-api/api/user/auth']

// 默认请求超时时间（毫秒）
const DEFAULT_TIMEOUT = 30000

// 错误消息常量
const ERROR_MESSAGES = {
  NETWORK_ERROR: '网络连接失败，请检查网络设置',
  TIMEOUT_ERROR: '网络连接超时，请重试',
  SERVER_ERROR: '服务器错误，请稍后重试',
  AUTH_EXPIRED: '登录已失效，请重新登录',
  FORBIDDEN: '没有访问权限',
  NOT_FOUND: '请求的资源不存在',
  REQUEST_FAILED: '请求失败',
  REQUEST_CANCELLED: '请求已取消'
}

import type { Result } from '@/types/api'
import { ErrorCode } from '@/types/api'

/**
 * 错误类型枚举
 */
export enum RequestErrorType {
  NETWORK = 'NETWORK',
  TIMEOUT = 'TIMEOUT',
  AUTH = 'AUTH',
  SERVER = 'SERVER',
  BUSINESS = 'BUSINESS',
  CANCELLED = 'CANCELLED'
}

/**
 * 自定义请求错误类
 */
export class RequestError extends Error {
  type: RequestErrorType
  statusCode?: number
  code?: number // 业务错误码
  originalError?: any
  canRetry: boolean

  constructor(
    message: string,
    type: RequestErrorType,
    statusCode?: number,
    code?: number,
    originalError?: any,
    canRetry: boolean = false
  ) {
    super(message)
    this.name = 'RequestError'
    this.type = type
    this.statusCode = statusCode
    this.code = code
    this.originalError = originalError
    this.canRetry = canRetry
  }
}

/**
 * 判断是否为认证相关的请求
 */
function isAuthRequest(url: string): boolean {
  return AUTH_PATHS.some(path => url.includes(path))
}

/**
 * 获取完整的请求URL
 * 登录接口使用 GATEWAY_ROOT_URL + /basebackend-user-api/...
 * 其他接口使用 BASE_URL (wheel-api)
 */
export function getFullUrl(url: string): string {
  // 如果URL包含basebackend-user-api前缀，使用Gateway根URL
  if (url.startsWith('/basebackend-user-api')) {
    return `${GATEWAY_ROOT_URL}${url}`
  }
  // 其他接口使用wheel-api基础URL
  return `${BASE_URL}${url}`
}

/**
 * 获取存储的Token
 */
export function getToken(): string {
  return uni.getStorageSync('token') || ''
}

/**
 * 清除所有认证信息
 */
export function clearAuthInfo(): void {
  uni.removeStorageSync('token')
  uni.removeStorageSync('refreshToken')
  uni.removeStorageSync('userInfo')
}

/**
 * 重定向到登录页面
 */
export function redirectToLogin(): void {
  clearAuthInfo()
  uni.reLaunch({
    url: '/pages/login/login'
  })
}

/**
 * 显示错误消息
 * @param message 错误消息
 * @param duration 显示时长（毫秒）
 */
export function showErrorMessage(message: string, duration: number = 2000): void {
  uni.showToast({
    title: message,
    icon: 'none',
    duration
  })
}

/**
 * 显示带重试选项的错误提示
 * @param message 错误消息
 * @param onRetry 重试回调
 */
export function showRetryableError(message: string, onRetry?: () => void): void {
  if (onRetry) {
    uni.showModal({
      title: '请求失败',
      content: message,
      confirmText: '重试',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) {
          onRetry()
        }
      }
    })
  } else {
    showErrorMessage(message)
  }
}

// 请求拦截器
uni.addInterceptor('request', {
  invoke(args: UniApp.RequestOptions) {
    const token = getToken()
    const url = args.url || ''

    // 非登录请求且有token时，添加Authorization header
    if (token && !isAuthRequest(url)) {
      args.header = {
        ...args.header,
        'Authorization': `Bearer ${token}`
      }
    }

    // 确保Content-Type为application/json
    args.header = {
      'Content-Type': 'application/json',
      ...args.header,
      'X-Request-ID': Date.now().toString()
    }
  },
  success(_args: UniApp.RequestSuccessCallbackResult) {
    // 请求成功回调 - 具体处理在handleResponse中
  },
  fail(err: any) {
    console.error('请求拦截器捕获错误:', err)
  },
  complete(_res: any) {
    // 请求完成回调
  }
})

interface RequestConfig {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: any
  params?: Record<string, any>
  header?: Record<string, string>
  timeout?: number
  showError?: boolean // 是否自动显示错误消息，默认true
  retryable?: boolean // 是否允许重试，默认false
}

interface ApiResponse<T = any> {
  code?: number
  message?: string
  data?: T
}

/**
 * 判断是否为网络错误
 */
function isNetworkError(err: any): boolean {
  const errMsg = err?.errMsg || err?.message || ''
  return (
    errMsg.includes('request:fail') ||
    errMsg.includes('network') ||
    errMsg.includes('ERR_NETWORK') ||
    errMsg.includes('ERR_CONNECTION') ||
    errMsg.includes('net::') ||
    errMsg.includes('Failed to fetch')
  )
}

/**
 * 判断是否为超时错误
 */
function isTimeoutError(err: any): boolean {
  const errMsg = err?.errMsg || err?.message || ''
  return (
    errMsg.includes('timeout') ||
    errMsg.includes('TIMEOUT') ||
    errMsg.includes('ERR_TIMED_OUT')
  )
}

/**
 * 判断是否为请求取消
 */
function isRequestCancelled(err: any): boolean {
  const errMsg = err?.errMsg || err?.message || ''
  return (
    errMsg.includes('abort') ||
    errMsg.includes('cancel') ||
    errMsg.includes('ERR_CANCELED')
  )
}

/**
 * 处理响应数据
 */
function handleResponse<T>(res: UniApp.RequestSuccessCallbackResult, showError: boolean = true): T {
  const { statusCode, data } = res
  const resData = data as Result<T>

  // 1. HTTP 状态码 200，但业务可能成功也可能失败
  if (statusCode === 200) {
    // 业务成功: code = 200 or 0 (兼容)
    if (resData.code === ErrorCode.SUCCESS || resData.code === 0) {
      return resData.data
    }

    // 业务错误逻辑
    const errorCode = resData.code
    const errorMsg = resData.message || ERROR_MESSAGES.REQUEST_FAILED

    // 特殊处理 Token 过期 (2001, 2002, etc)
    if ([ErrorCode.TOKEN_EXPIRED, ErrorCode.TOKEN_INVALID, ErrorCode.TOKEN_MISSING, ErrorCode.TOKEN_BLACKLISTED].includes(errorCode)) {
      redirectToLogin()
      throw new RequestError(errorMsg, RequestErrorType.AUTH, statusCode, errorCode)
    }

    // 其他业务错误
    if (showError) {
      showErrorMessage(errorMsg)
    }
    throw new RequestError(errorMsg, RequestErrorType.BUSINESS, statusCode, errorCode)
  }

  // 2. HTTP 状态码非 200 (网关或框架层面的错误)
  else if (statusCode === 401) {
    redirectToLogin()
    throw new RequestError(ERROR_MESSAGES.AUTH_EXPIRED, RequestErrorType.AUTH, statusCode)
  } else if (statusCode === 403) {
    if (showError) showErrorMessage(ERROR_MESSAGES.FORBIDDEN)
    throw new RequestError(ERROR_MESSAGES.FORBIDDEN, RequestErrorType.AUTH, statusCode)
  } else if (statusCode === 404) {
    if (showError) showErrorMessage(ERROR_MESSAGES.NOT_FOUND)
    throw new RequestError(ERROR_MESSAGES.NOT_FOUND, RequestErrorType.SERVER, statusCode)
  } else if (statusCode >= 500) {
    const errorMsg = (resData as any)?.message || ERROR_MESSAGES.SERVER_ERROR
    if (showError) showErrorMessage(errorMsg)
    throw new RequestError(errorMsg, RequestErrorType.SERVER, statusCode, undefined, null, true)
  } else {
    const errorMsg = (resData as any)?.message || `${ERROR_MESSAGES.REQUEST_FAILED}: ${statusCode}`
    if (showError) showErrorMessage(errorMsg)
    throw new RequestError(errorMsg, RequestErrorType.SERVER, statusCode)
  }
}

/**
 * 处理请求失败（网络层错误）
 */
function handleRequestError(err: any, showError: boolean = true): never {
  console.error('请求错误详情:', err)

  // 请求被取消 - 静默处理
  if (isRequestCancelled(err)) {
    throw new RequestError(ERROR_MESSAGES.REQUEST_CANCELLED, RequestErrorType.CANCELLED, undefined, err, false)
  }

  // 超时错误 - 允许重试
  if (isTimeoutError(err)) {
    if (showError) {
      showErrorMessage(ERROR_MESSAGES.TIMEOUT_ERROR)
    }
    throw new RequestError(ERROR_MESSAGES.TIMEOUT_ERROR, RequestErrorType.TIMEOUT, undefined, err, true)
  }

  // 网络错误 - 允许重试
  if (isNetworkError(err)) {
    if (showError) {
      showErrorMessage(ERROR_MESSAGES.NETWORK_ERROR)
    }
    throw new RequestError(ERROR_MESSAGES.NETWORK_ERROR, RequestErrorType.NETWORK, undefined, err, true)
  }

  // 其他未知错误
  const errorMsg = err?.errMsg || err?.message || ERROR_MESSAGES.NETWORK_ERROR
  if (showError) {
    showErrorMessage(errorMsg)
  }
  throw new RequestError(errorMsg, RequestErrorType.NETWORK, undefined, err, true)
}

/**
 * 构建带查询参数的URL
 */
function buildUrlWithParams(url: string, params?: Record<string, any>): string {
  if (!params || Object.keys(params).length === 0) {
    return url
  }

  const queryString = Object.entries(params)
    .filter(([_, value]) => value !== undefined && value !== null)
    .map(([key, value]) => {
      if (Array.isArray(value)) {
        return value.map(v => `${encodeURIComponent(key)}=${encodeURIComponent(v)}`).join('&')
      }
      return `${encodeURIComponent(key)}=${encodeURIComponent(value)}`
    })
    .join('&')

  return queryString ? `${url}?${queryString}` : url
}

/**
 * 统一请求方法
 */
export function request<T = any>(options: RequestConfig): Promise<T> {
  const showError = options.showError !== false

  return new Promise((resolve, reject) => {
    const token = getToken()
    const url = options.url || ''

    // 构建完整URL（包含查询参数）
    const urlWithParams = buildUrlWithParams(options.url, options.params)
    const fullUrl = getFullUrl(urlWithParams)

    // 构建请求头
    const header: Record<string, string> = {
      'Content-Type': 'application/json',
      'X-Request-ID': Date.now().toString(),
      ...options.header
    }

    // 非登录请求且有token时，添加Authorization header
    if (token && !isAuthRequest(url)) {
      header['Authorization'] = `Bearer ${token}`
    }

    uni.request({
      url: fullUrl,
      method: options.method || 'GET',
      data: options.data,
      header,
      timeout: options.timeout || DEFAULT_TIMEOUT,
      success: (res) => {
        try {
          const result = handleResponse<T>(res, showError)
          resolve(result)
        } catch (error) {
          reject(error)
        }
      },
      fail: (err) => {
        try {
          handleRequestError(err, showError)
        } catch (error) {
          reject(error)
        }
      }
    })
  })
}

/**
 * 带重试功能的请求方法
 * @param options 请求配置
 * @param maxRetries 最大重试次数，默认2次
 * @param retryDelay 重试延迟（毫秒），默认1000ms
 */
export async function requestWithRetry<T = any>(
  options: RequestConfig,
  maxRetries: number = 2,
  retryDelay: number = 1000
): Promise<T> {
  let lastError: RequestError | null = null

  for (let attempt = 0; attempt <= maxRetries; attempt++) {
    try {
      // 第一次尝试显示错误，重试时不显示
      const result = await request<T>({
        ...options,
        showError: attempt === maxRetries // 只在最后一次尝试时显示错误
      })
      return result
    } catch (error) {
      lastError = error as RequestError

      // 如果错误不可重试，直接抛出
      if (!(error instanceof RequestError) || !error.canRetry) {
        throw error
      }

      // 如果还有重试机会，等待后重试
      if (attempt < maxRetries) {
        console.log(`请求失败，${retryDelay}ms后进行第${attempt + 2}次尝试...`)
        await new Promise(resolve => setTimeout(resolve, retryDelay))
      }
    }
  }

  // 所有重试都失败了
  throw lastError
}

/**
 * 带重试确认的请求方法
 * 当请求失败且可重试时，会弹出确认框让用户选择是否重试
 */
export function requestWithRetryConfirm<T = any>(options: RequestConfig): Promise<T> {
  return new Promise((resolve, reject) => {
    const executeRequest = () => {
      request<T>({ ...options, showError: false })
        .then(resolve)
        .catch((error: RequestError) => {
          if (error.canRetry) {
            showRetryableError(error.message, executeRequest)
          } else {
            showErrorMessage(error.message)
            reject(error)
          }
        })
    }

    executeRequest()
  })
}

export default request
