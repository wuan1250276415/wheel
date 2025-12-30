import request from './request'

// ==================== 类型定义 ====================

/**
 * 用户统计数据
 */
export interface UserStatistics {
  totalSpins: number
  todaySpins: number
  weekSpins: number
  monthSpins: number
  favoriteCategory: string
  avgSpinDuration: number
  consecutiveDays: number
  totalCoupleSpins: number
  uniqueContentsSeen: number
  lastSpinTime: string | null
}

/**
 * 系统概览数据（管理员）
 */
export interface SystemOverview {
  totalUsers: number
  totalCouples: number
  totalSpins: number
  totalContents: number
  todayNewUsers: number
  todayNewCouples: number
  todaySpins: number
  activeUsers24h: number
  avgSpinsPerUser: number
  coupleFormationRate: number
}

/**
 * 转盘使用趋势
 */
export interface SpinTrend {
  date: string
  spinCount: number
  userCount: number
  coupleCount: number
}

/**
 * 热门内容
 */
export interface PopularContent {
  contentId: number
  contentText: string
  categoryName: string
  spinCount: number
  winRate: number
}

/**
 * 分类统计
 */
export interface CategoryStatistics {
  categoryId: number
  categoryName: string
  contentCount: number
  totalSpins: number
  avgWeight: number
  uniqueUsers: number
}

/**
 * 情侣关系统计
 */
export interface CoupleStatistics {
  totalCouples: number
  activeCouples: number
  avgCoupleAge: number
  formationRate: number
  mostActiveCoupleSpins: number
  coupleRetentionRate7d: number
  coupleRetentionRate30d: number
}

// ==================== API 函数 ====================

/**
 * 获取用户统计
 * GET /api/statistics/user
 * Requirements: 5.1
 */
export function getUserStatistics() {
  return request<UserStatistics>({
    url: '/api/statistics/user',
    method: 'GET'
  })
}

/**
 * 获取系统概览（管理员）
 * GET /api/statistics/overview
 * Requirements: 5.2
 */
export function getSystemOverview() {
  return request<SystemOverview>({
    url: '/api/statistics/overview',
    method: 'GET'
  })
}

/**
 * 获取转盘使用趋势
 * GET /api/statistics/spin-trend
 * Requirements: 5.3
 * @param startDate 开始日期 (YYYY-MM-DD格式)
 * @param endDate 结束日期 (YYYY-MM-DD格式)
 */
export function getSpinTrend(startDate: string, endDate: string) {
  return request<SpinTrend[]>({
    url: '/api/statistics/spin-trend',
    method: 'GET',
    params: {
      startDate,
      endDate
    }
  })
}

/**
 * 获取热门内容
 * GET /api/statistics/popular-content
 * Requirements: 5.4
 * @param limit 返回数量限制，默认10
 */
export function getPopularContent(limit: number = 10) {
  return request<PopularContent[]>({
    url: '/api/statistics/popular-content',
    method: 'GET',
    params: {
      limit
    }
  })
}

/**
 * 获取分类统计
 * GET /api/statistics/category
 * Requirements: 5.5
 */
export function getCategoryStatistics() {
  return request<CategoryStatistics[]>({
    url: '/api/statistics/category',
    method: 'GET'
  })
}

/**
 * 获取情侣关系统计
 * GET /api/statistics/couple
 * Requirements: 5.6
 */
export function getCoupleStatistics() {
  return request<CoupleStatistics>({
    url: '/api/statistics/couple',
    method: 'GET'
  })
}

// ==================== 导出功能 ====================

/**
 * 导出类型
 */
export type ExportType = 'user' | 'trend' | 'popular' | 'category' | 'all'

/**
 * 导出参数
 */
export interface ExportParams {
  type: ExportType
  startDate?: string
  endDate?: string
  limit?: number
}

/**
 * 导出统计数据为Excel
 * GET /api/statistics/export
 * @param params 导出参数
 */
export function exportStatistics(params: ExportParams) {
  const { type, startDate, endDate, limit } = params

  // 构建查询参数
  const queryParams = new URLSearchParams({ type })
  if (startDate) queryParams.append('startDate', startDate)
  if (endDate) queryParams.append('endDate', endDate)
  if (limit) queryParams.append('limit', limit.toString())

  // 获取token
  const token = uni.getStorageSync('token')

  // 构建完整URL
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  const url = `${baseUrl}/api/statistics/export?${queryParams.toString()}`

  // 触发下载
  return new Promise<void>((resolve, reject) => {
    uni.downloadFile({
      url,
      header: {
        'Authorization': `Bearer ${token}`
      },
      success: (res) => {
        if (res.statusCode === 200) {
          const filePath = res.tempFilePath
          // 保存文件
          uni.saveFile({
            tempFilePath: filePath,
            success: (saveRes) => {
              uni.showToast({
                title: '导出成功',
                icon: 'success'
              })
              resolve()
            },
            fail: (err) => {
              console.error('保存文件失败:', err)
              reject(new Error('保存文件失败'))
            }
          })
        } else {
          reject(new Error('下载失败'))
        }
      },
      fail: (err) => {
        console.error('下载文件失败:', err)
        reject(err)
      }
    })
  })
}
