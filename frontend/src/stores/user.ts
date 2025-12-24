import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authApi from '@/api/auth'
import type { UserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>('')
  const refreshToken = ref<string>('')
  const userInfo = ref<UserInfo | null>(null)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const hasCouple = computed(() => {
    return (userInfo.value as any)?.coupleId !== null && (userInfo.value as any)?.coupleId !== undefined
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

  // 登录 - 使用username和password
  async function login(username: string, password: string) {
    try {
      const res = await authApi.login({ username, password })

      // 存储token和用户信息
      token.value = res.accessToken
      if (res.refreshToken) {
        refreshToken.value = res.refreshToken
      }
      userInfo.value = res.userInfo

      // 保存到本地存储
      uni.setStorageSync('token', res.accessToken)
      if (res.refreshToken) {
        uni.setStorageSync('refreshToken', res.refreshToken)
      }
      uni.setStorageSync('userInfo', res.userInfo)

      return { success: true }
    } catch (error: any) {
      return { success: false, message: error.message || '登录失败' }
    }
  }

  // 注册
  async function register(data: {
    username: string
    password: string
    nickname?: string
    avatar?: string
  }) {
    try {
      await authApi.register(data)
      return { success: true }
    } catch (error: any) {
      return { success: false, message: error.message || '注册失败' }
    }
  }

  // 获取用户信息
  async function fetchUserInfo() {
    try {
      const res = await authApi.getUserInfo()
      userInfo.value = res
      uni.setStorageSync('userInfo', res)
      return { success: true }
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
      await authApi.updateProfile(data)
      // 重新获取用户信息
      await fetchUserInfo()
      return { success: true }
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

      token.value = res.accessToken
      refreshToken.value = res.refreshToken

      uni.setStorageSync('token', res.accessToken)
      uni.setStorageSync('refreshToken', res.refreshToken)

      return { success: true }
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
