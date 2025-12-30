import { useAuthStore } from '@/stores/auth'

/**
 * 检查是否有指定权限
 */
export function hasPermission(permission: string | string[]): boolean {
  const authStore = useAuthStore()
  const permissions = authStore.permissions

  if (!permissions || permissions.length === 0) {
    return false
  }

  if (Array.isArray(permission)) {
    return permission.some(p => permissions.includes(p))
  }

  return permissions.includes(permission)
}

/**
 * 检查是否有指定角色
 */
export function hasRole(role: string | string[]): boolean {
  const authStore = useAuthStore()
  const roles = authStore.roles

  if (!roles || roles.length === 0) {
    return false
  }

  if (Array.isArray(role)) {
    return role.some(r => roles.includes(r))
  }

  return roles.includes(role)
}
