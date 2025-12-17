import request from './request'

// 获取用户统计
export function getUserStatistics() {
  return request<{
    code: number
    message: string
    data: {
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
  }>({
    url: '/statistics/user',
    method: 'GET'
  })
}

// 获取系统概览（管理员）
export function getSystemOverview() {
  return request<{
    code: number
    message: string
    data: {
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
  }>({
    url: '/statistics/overview',
    method: 'GET'
  })
}

// 获取转盘使用趋势
export function getSpinTrend(params: {
  startDate: string
  endDate: string
}) {
  return request<{
    code: number
    message: string
    data: Array<{
      date: string
      spinCount: number
      userCount: number
      coupleCount: number
    }>
  }>({
    url: '/statistics/spin-trend',
    method: 'GET',
    data: params
  })
}

// 获取热门内容
export function getPopularContent(params: {
  limit?: number
}) {
  return request<{
    code: number
    message: string
    data: Array<{
      contentId: number
      contentText: string
      categoryName: string
      spinCount: number
      winRate: number
    }>
  }>({
    url: '/statistics/popular-content',
    method: 'GET',
    data: params
  })
}

// 获取分类统计
export function getCategoryStatistics() {
  return request<{
    code: number
    message: string
    data: Array<{
      categoryId: number
      categoryName: string
      contentCount: number
      totalSpins: number
      avgWeight: number
      uniqueUsers: number
    }>
  }>({
    url: '/statistics/category',
    method: 'GET'
  })
}

// 获取情侣关系统计
export function getCoupleStatistics() {
  return request<{
    code: number
    message: string
    data: {
      totalCouples: number
      activeCouples: number
      avgCoupleAge: number
      formationRate: number
      mostActiveCoupleSpins: number
      coupleRetentionRate7d: number
      coupleRetentionRate30d: number
    }
  }>({
    url: '/statistics/couple',
    method: 'GET'
  })
}
