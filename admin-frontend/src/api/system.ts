import { userApiRequest, systemApiRequest } from '@/utils/request'
import type { ApiResponse, PageResult } from '@/types/api'

// ==================== 用户管理 ====================

export interface UserDTO {
  id: number
  username: string
  nickname: string
  email?: string
  phone?: string
  gender?: number
  deptId?: number
  status: number
  createTime: string
  updateTime: string
}

export interface UserQueryDTO {
  current?: number
  size?: number
  username?: string
  nickname?: string
  phone?: string
  deptId?: number
  status?: number
}

export interface UserCreateDTO {
  username: string
  password: string
  nickname: string
  email?: string
  phone?: string
  gender?: number
  deptId?: number
  roleIds?: number[]
}

/**
 * 分页查询用户
 */
export function queryUsers(params: UserQueryDTO) {
  return userApiRequest.get<any, ApiResponse<PageResult<UserDTO>>>('/api/user', { params })
}

/**
 * 创建用户
 */
export function createUser(data: UserCreateDTO) {
  return userApiRequest.post<any, ApiResponse<number>>('/api/user', data)
}

/**
 * 更新用户
 */
export function updateUser(id: number, data: Partial<UserCreateDTO>) {
  return userApiRequest.put<any, ApiResponse<void>>(`/api/user/${id}`, data)
}

/**
 * 删除用户
 */
export function deleteUser(id: number) {
  return userApiRequest.delete<any, ApiResponse<void>>(`/api/user/${id}`)
}

// ==================== 角色管理 ====================

export interface RoleDTO {
  id?: number
  roleName: string
  roleKey: string
  roleSort?: number
  dataScope?: number
  status?: number
  menuIds?: number[]
}

/**
 * 查询角色列表
 */
export function queryRoles(params?: { current?: number; size?: number }) {
  return userApiRequest.get<any, ApiResponse<PageResult<RoleDTO>>>('/api/user/roles', { params })
}

/**
 * 创建角色
 */
export function createRole(data: RoleDTO) {
  return userApiRequest.post<any, ApiResponse<number>>('/api/user/roles', data)
}

/**
 * 更新角色
 */
export function updateRole(id: number, data: RoleDTO) {
  return userApiRequest.put<any, ApiResponse<void>>(`/api/user/roles/${id}`, data)
}

/**
 * 删除角色
 */
export function deleteRole(id: number) {
  return userApiRequest.delete<any, ApiResponse<void>>(`/api/user/roles/${id}`)
}

// ==================== 部门管理 ====================

export interface DeptDTO {
  id?: number
  deptName: string
  parentId?: number
  orderNum?: number
  leader?: string
  status?: number
  children?: DeptDTO[]
}

/**
 * 获取部门树
 */
export function getDeptTree() {
  return systemApiRequest.get<any, ApiResponse<DeptDTO[]>>('/api/system/depts/tree')
}

/**
 * 创建部门
 */
export function createDept(data: DeptDTO) {
  return systemApiRequest.post<any, ApiResponse<number>>('/api/system/depts', data)
}

/**
 * 更新部门
 */
export function updateDept(id: number, data: DeptDTO) {
  return systemApiRequest.put<any, ApiResponse<void>>(`/api/system/depts/${id}`, data)
}

/**
 * 删除部门
 */
export function deleteDept(id: number) {
  return systemApiRequest.delete<any, ApiResponse<void>>(`/api/system/depts/${id}`)
}

// ==================== 应用管理 ====================

export interface ApplicationDTO {
  id?: number
  appName: string
  appCode: string
  appType: string
  appIcon?: string
  appUrl?: string
  status: number
}

/**
 * 查询应用列表
 */
export function queryApplications(params?: { current?: number; size?: number }) {
  return systemApiRequest.get<any, ApiResponse<PageResult<ApplicationDTO>>>('/api/system/application', { params })
}

/**
 * 创建应用
 */
export function createApplication(data: ApplicationDTO) {
  return systemApiRequest.post<any, ApiResponse<number>>('/api/system/application', data)
}

/**
 * 更新应用
 */
export function updateApplication(id: number, data: ApplicationDTO) {
  return systemApiRequest.put<any, ApiResponse<void>>(`/api/system/application/${id}`, data)
}

/**
 * 删除应用
 */
export function deleteApplication(id: number) {
  return systemApiRequest.delete<any, ApiResponse<void>>(`/api/system/application/${id}`)
}

// ==================== 资源菜单管理 ====================

export interface ApplicationResourceDTO {
  id?: number
  appId: number
  resourceName: string
  parentId?: number
  resourceType: string // M-目录, C-菜单, F-按钮
  path?: string
  component?: string
  perms?: string
  icon?: string
  visible?: number
  children?: ApplicationResourceDTO[]
}

/**
 * 获取应用资源树
 */
export function getApplicationResourceTree(appId: number) {
  return systemApiRequest.get<any, ApiResponse<ApplicationResourceDTO[]>>(`/api/system/application/resource/tree`, {
    params: { appId }
  })
}

/**
 * 创建资源
 */
export function createApplicationResource(data: ApplicationResourceDTO) {
  return systemApiRequest.post<any, ApiResponse<number>>('/api/system/application/resource', data)
}

/**
 * 更新资源
 */
export function updateApplicationResource(data: ApplicationResourceDTO) {
  return systemApiRequest.put<any, ApiResponse<void>>('/api/system/application/resource', data)
}

/**
 * 删除资源
 */
export function deleteApplicationResource(id: number) {
  return systemApiRequest.delete<any, ApiResponse<void>>(`/api/system/application/resource/${id}`)
}

// ==================== 字典管理 ====================

export interface DictDataDTO {
  id?: number
  dictType: string
  dictLabel: string
  dictValue: string
  cssClass?: string
  listClass?: string
  status?: number
  sortOrder?: number
}

/**
 * 根据字典类型获取字典数据
 */
export function getDictDataByType(dictType: string) {
  return systemApiRequest.get<any, ApiResponse<DictDataDTO[]>>(`/api/system/dicts/${dictType}`)
}

/**
 * 创建字典数据
 */
export function createDictData(data: DictDataDTO) {
  return systemApiRequest.post<any, ApiResponse<number>>('/api/system/dicts', data)
}

/**
 * 更新字典数据
 */
export function updateDictData(id: number, data: DictDataDTO) {
  return systemApiRequest.put<any, ApiResponse<void>>(`/api/system/dicts/${id}`, data)
}

/**
 * 删除字典数据
 */
export function deleteDictData(id: number) {
  return systemApiRequest.delete<any, ApiResponse<void>>(`/api/system/dicts/${id}`)
}
