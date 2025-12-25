import request from './request'
import type { PageResult } from '@/types/api'

// 转盘分类
export interface WheelCategory {
  id: number
  categoryName: string
  description?: string
  sortOrder: number
  iconUrl?: string
  themeColor?: string
  status: boolean
  isSystem: boolean
}

// 转盘内容
export interface WheelContent {
  id: number
  categoryId: number
  contentText: string
  weight: number
  status: number
  isSystem: boolean
}

// 转盘配置选项（组合 categories 和 contents）
export interface WheelOptions {
  categories: WheelCategory[]
  contents: WheelContent[]
}

// 转盘执行DTO
export interface WheelSpinDTO {
  categoryIds?: number[]
  radius?: number
  animationDuration?: number
}

// 转盘结果
export interface WheelSpinResultDTO {
  contentId: number
  resultText: string
  categoryId: number
  categoryName: string
  rotationAngle: number
  spinDuration: number
  isWinning: boolean
}

// 转盘历史记录
export interface WheelSpinRecord {
  id: number
  contentId: number
  resultText: string
  categoryId: number
  spinTime: string
}

// 用户统计
export interface UserStats {
  totalSpins: number
  todaySpins: number
  lastSpinTime?: string
}

// 获取转盘分类
export function getCategories() {
  return request<WheelCategory[]>({
    url: '/api/wheel/categories',
    method: 'GET'
  })
}

/**
 * 获取转盘内容
 * @param categoryIds 分类ID数组，必传，用于获取指定分类下的内容
 */
export function getContents(categoryIds: number[]) {
  return request<WheelContent[]>({
    url: '/api/wheel/contents',
    method: 'GET',
    params: { categoryIds }
  })
}

// 执行转盘
export function spin(data: WheelSpinDTO) {
  return request<WheelSpinResultDTO>({
    url: '/api/wheel/spin',
    method: 'POST',
    data
  })
}

// 获取转盘历史记录（使用pageNum/pageSize）
export function getHistory(pageNum: number = 1, pageSize: number = 20) {
  return request<PageResult<WheelSpinRecord>>({
    url: '/api/wheel/history',
    method: 'GET',
    params: { pageNum, pageSize }
  })
}

// 获取用户统计
export function getStats() {
  return request<UserStats>({
    url: '/api/wheel/stats',
    method: 'GET'
  })
}

// 保留旧的函数名以保持向后兼容
export const getWheelContents = getContents
export const spinWheel = spin
export const getSpinHistory = getHistory
