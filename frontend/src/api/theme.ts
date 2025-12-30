import request from './request'

export interface ThemeVO {
  id: number
  themeName: string
  themeKey: string
  description: string
  themeConfig: string
  previewUrl: string
  price: number
  isDefault: boolean
  owned: boolean
  active: boolean
}

/**
 * 获取所有主题列表
 */
export function getAllThemes() {
  return request<ThemeVO[]>({
    url: '/theme/list',
    method: 'GET'
  })
}

/**
 * 获取当前使用的主题
 */
export function getCurrentTheme() {
  return request<ThemeVO>({
    url: '/theme/current',
    method: 'GET'
  })
}

/**
 * 购买主题
 */
export function purchaseTheme(themeId: number) {
  return request({
    url: `/theme/purchase/${themeId}`,
    method: 'POST'
  })
}

/**
 * 应用主题
 */
export function applyTheme(themeId: number) {
  return request({
    url: `/theme/apply/${themeId}`,
    method: 'POST'
  })
}

/**
 * 获取拥有的主题ID列表
 */
export function getOwnedThemes() {
  return request<number[]>({
    url: '/theme/owned',
    method: 'GET'
  })
}
