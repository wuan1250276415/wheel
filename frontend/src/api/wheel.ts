import request from './request'

// 获取转盘配置
export function getWheelConfig() {
  return request<{
    code: number
    message: string
    data: {
      id: number
      userId: number
      radius: number
      categories: number[]
      theme: string
      animationDuration: number
    }
  }>({
    url: '/wheel/config',
    method: 'GET'
  })
}

// 保存转盘配置
export function saveWheelConfig(data: {
  radius?: number
  categories?: number[]
  theme?: string
  animationDuration?: number
}) {
  return request<{
    code: number
    message: string
    data: any
  }>({
    url: '/wheel/config',
    method: 'POST',
    data
  })
}

// 获取可用的转盘内容
export function getWheelContents(categoryId?: number) {
  return request<{
    code: number
    message: string
    data: Array<{
      id: number
      contentText: string
      categoryId: number
      weight: number
    }>
  }>({
    url: '/wheel/contents',
    method: 'GET',
    data: { categoryId }
  })
}

// 执行转盘
export function spinWheel(data: {
  categoryIds?: number[]
  config?: any
}) {
  return request<{
    code: number
    message: string
    data: {
      result: {
        id: number
        contentText: string
        categoryId: number
      }
      angle: number
      duration: number
      recordId: number
    }
  }>({
    url: '/wheel/spin',
    method: 'POST',
    data
  })
}

// 获取转盘历史记录
export function getSpinHistory(params: {
  page?: number
  pageSize?: number
}) {
  return request<{
    code: number
    message: string
    data: {
      list: Array<{
        id: number
        resultText: string
        spinTime: string
        categoryName: string
      }>
      total: number
      page: number
      pageSize: number
    }
  }>({
    url: '/wheel/history',
    method: 'GET',
    data: params
  })
}
