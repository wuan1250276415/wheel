import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authApi from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>('')
  const refreshToken = ref<string>('')
  const userInfo = ref<{
    id: number
    nickname: string
    avatar: string
    phone: string
  } | null>(null)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const hasCouple = computed(() => {
    return userInfo.value?.coupleId !== null && userInfo.value?.coupleId !== undefined
  })

  // 初始化 - 从本地存储恢复状态
  function init() {
    const storedToken = uni.getStorageSync('token')
    const storedRefreshToken = uni.getStorageSync('refreshToken')
    const storedUserInfo = uni.getStorageSync('userInfo')

    if (storedToken) {
      token.value = storedToken
    }
    if (storedRefreshToken) {
      refreshToken.value = storedRefreshToken
    }
    if (storedUserInfo) {
      userInfo.value = storedUserInfo
    }
  }

  // 登录
  async function login(phone: string, password: string) {
    try {
      const res = await authApi.login({ phone, password })

      if (res.code === 200) {
        token.value = res.data.accessToken
        refreshToken.value = res.data.refreshToken
        userInfo.value = res.data.userInfo

        // 保存到本地存储
        uni.setStorageSync('token', res.data.accessToken)
        uni.setStorageSync('refreshToken', res.data.refreshToken)
        uni.setStorageSync('userInfo', res.data.userInfo)

        return { success: true }
      } else {
        return { success: false, message: res.message }
      }
    } catch (error: any) {
      return { success: false, message: error.message || '登录失败' }
    }
  }

  // 注册
  async function register(data: {
    phone: string
    password: string
    nickname: string
    avatar?: string
  }) {
    try {
      const res = await authApi.register(data)

      if (res.code === 200) {
        return { success: true }
      } else {
        return { success: false, message: res.message }
      }
    } catch (error: any) {
      return { success: false, message: error.message || '注册失败' }
    }
  }

  // 获取用户信息
  async function fetchUserInfo() {
    try {
      const res = await authApi.getUserInfo()

      if (res.code === 200) {
        userInfo.value = res.data
        uni.setStorageSync('userInfo', res.data)
        return { success: true }
      } else {
        return { success: false, message: res.message }
      }
    } catch (error: any) {
      return { success: false, message: error.message || '获取用户信息失败' }
    }
  }

  // 更新用户信息
  async function updateProfile(data: {
    nickname?: string
    avatar?: string
  }) {
    try {
      const res = await authApi.updateProfile(data)

      if (res.code === 200) {
        // 重新获取用户信息
        await fetchUserInfo()
        return { success: true }
      } else {
        return { success: false, message: res.message }
      }
    } catch (error: any) {
      return { success: false, message: error.message || '更新失败' }
    }
  }

  // 登出
  function logout() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null

    uni.removeStorageSync('token')
    uni.removeStorageSync('refreshToken')
    uni.removeStorageSync('userInfo')
  }

  // 刷新token
  async function refreshAccessToken() {
    try {
      if (!refreshToken.value) {
        throw new Error('No refresh token')
      }

      const res = await authApi.refreshToken(refreshToken.value)

      if (res.code === 200) {
        token.value = res.data.accessToken
        refreshToken.value = res.data.refreshToken

        uni.setStorageSync('token', res.data.accessToken)
        uni.setStorageSync('refreshToken', res.data.refreshToken)

        return { success: true }
      } else {
        throw new Error(res.message)
      }
    } catch (error) {
      // 刷新失败，清除登录信息
      logout()
      return { success: false }
    }
  }

  return {
    token,
    refreshToken,
    userInfo,
    isLoggedIn,
    hasCouple,
    init,
    login,
    register,
    fetchUserInfo,
    updateProfile,
    logout,
    refreshAccessToken
  }
})
