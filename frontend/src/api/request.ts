/**
 * HTTP请求封装
 */
const BASE_URL = 'http://localhost:8080/api'

// 请求拦截器
uni.addInterceptor('request', {
  invoke(args: any) {
    // 添加token
    const token = uni.getStorageSync('token')
    if (token && args.url !== `${BASE_URL}/auth/login`) {
      args.header = {
        ...args.header,
        'Authorization': `Bearer ${token}`
      }
    }

    // 添加请求ID
    args.header = {
      ...args.header,
      'X-Request-ID': Date.now().toString()
    }

    console.log('发起请求:', args.url, args)
  },
  success(args: any) {
    console.log('请求成功:', args)
  },
  fail(err: any) {
    console.error('请求失败:', err)
  },
  complete(res: any) {
    console.log('请求完成:', res)
  }
})

// 响应拦截器
uni.addInterceptor('response', {
  success(args: any) {
    const { statusCode, data } = args

    if (statusCode === 200) {
      if (data.code === 200) {
        return data
      } else {
        // 业务错误
        uni.showToast({
          title: data.message || '请求失败',
          icon: 'none'
        })
        return Promise.reject(new Error(data.message || '请求失败'))
      }
    } else if (statusCode === 401) {
      // token失效，清除登录信息并跳转登录页
      uni.removeStorageSync('token')
      uni.removeStorageSync('refreshToken')
      uni.reLaunch({
        url: '/pages/index/index'
      })
      return Promise.reject(new Error('登录已失效'))
    } else {
      uni.showToast({
        title: '网络错误',
        icon: 'none'
      })
      return Promise.reject(new Error(`网络错误: ${statusCode}`))
    }
  },
  fail(err: any) {
    uni.showToast({
      title: '网络连接失败',
      icon: 'none'
    })
    return Promise.reject(err)
  }
})

interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
  data?: any
  header?: Record<string, string>
}

export function request<T = any>(options: RequestOptions): Promise<T> {
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}${options.url}`,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        ...options.header
      },
      success: (res) => {
        resolve(res.data as T)
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

export default request
