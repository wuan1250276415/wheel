import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { AdminInfo } from '@/types/api'
import { login as loginApi, logout as logoutApi } from '@/api/auth'
import { setToken, removeToken, setRefreshToken } from '@/utils/auth'

export const useAuthStore = defineStore('auth', () => {
  const adminInfo = ref<AdminInfo | null>(null)
  const permissions = ref<string[]>([])
  const roles = ref<string[]>([])

  /**
   * 登录
   */
  async function login(username: string, password: string) {
    const res = await loginApi(username, password)
    const { accessToken, refreshToken, adminInfo: info, permissions: perms, roles: roleList } = res.data

    setToken(accessToken)
    setRefreshToken(refreshToken)

    adminInfo.value = info
    permissions.value = perms
    roles.value = roleList

    return res
  }

  /**
   * 登出
   */
  async function logout() {
    try {
      await logoutApi()
    } catch (error) {
      console.error('登出失败:', error)
    } finally {
      adminInfo.value = null
      permissions.value = []
      roles.value = []
      removeToken()
    }
  }

  /**
   * 设置管理员信息
   */
  function setAdminInfo(info: AdminInfo) {
    adminInfo.value = info
    permissions.value = info.permissions
    roles.value = info.roles
  }

  /**
   * 清除信息
   */
  function clearInfo() {
    adminInfo.value = null
    permissions.value = []
    roles.value = []
    removeToken()
  }

  return {
    adminInfo,
    permissions,
    roles,
    login,
    logout,
    setAdminInfo,
    clearInfo
  }
})
