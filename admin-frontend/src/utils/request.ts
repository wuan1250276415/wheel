import axios, { AxiosInstance, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from './auth'
import router from '@/router'

// Gateway 基础地址
const GATEWAY_BASE_URL = 'http://192.168.66.126:8280'

// 本地管理后台请求实例
const request: AxiosInstance = axios.create({
  baseURL: `${GATEWAY_BASE_URL}/wheel-api/api`,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 用户服务请求实例（通过 gateway）
export const userApiRequest: AxiosInstance = axios.create({
  baseURL: `${GATEWAY_BASE_URL}/basebackend-user-api`,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 系统服务请求实例（通过 gateway）
export const systemApiRequest: AxiosInstance = axios.create({
  baseURL: `${GATEWAY_BASE_URL}/basebackend-system-api`,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 通用请求拦截器配置
const setupRequestInterceptor = (instance: AxiosInstance) => {
  instance.interceptors.request.use(
    (config) => {
      const token = getToken()
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
      return config
    },
    (error) => {
      return Promise.reject(error)
    }
  )
}

// 通用响应拦截器配置
const setupResponseInterceptor = (instance: AxiosInstance) => {
  instance.interceptors.response.use(
    (response: AxiosResponse) => {
      const res = response.data

      if (res.code !== undefined && res.code !== 200) {
        ElMessage.error(res.msg || res.message || '请求失败')

        if (res.code === 401) {
          removeToken()
          router.push('/login')
        }

        return Promise.reject(new Error(res.msg || res.message || '请求失败'))
      }

      return res
    },
    async (error) => {
      if (error.response) {
        const { status, data } = error.response

        if (status === 401) {
          ElMessage.error('登录已过期，请重新登录')
          removeToken()
          router.push('/login')
        } else if (status === 403) {
          ElMessage.error('权限不足，无法访问')
        } else if (status === 404) {
          ElMessage.error('请求的资源不存在')
        } else if (status === 500) {
          ElMessage.error(data.msg || data.message || '服务器错误')
        } else {
          ElMessage.error(data.msg || data.message || '请求失败')
        }
      } else if (error.request) {
        ElMessage.error('网络错误，请检查网络连接')
      } else {
        ElMessage.error('请求配置错误')
      }

      return Promise.reject(error)
    }
  )
}

// 为所有实例配置拦截器
setupRequestInterceptor(request)
setupResponseInterceptor(request)

setupRequestInterceptor(userApiRequest)
setupResponseInterceptor(userApiRequest)

setupRequestInterceptor(systemApiRequest)
setupResponseInterceptor(systemApiRequest)

export default request
