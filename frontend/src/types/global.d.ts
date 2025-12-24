/**
 * 全局类型定义
 */

// Vite 环境变量类型
interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

// UniApp 扩展类型
declare module '@dcloudio/uni-app' {
  interface Uni {
    addInterceptor(type: string, options: any): void
  }

  interface RequestOptions {
    url: string
    method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
    data?: any
    header?: Record<string, string>
  }

  interface CanvasContext {
    clearRect(x: number, y: number, width: number, height: number): void
    beginPath(): void
    moveTo(x: number, y: number): void
    lineTo(x: number, y: number): void
    arc(x: number, y: number, radius: number, startAngle: number, endAngle: number, counterclockwise?: boolean): void
    closePath(): void
    fill(): void
    stroke(): void
    fillStyle: string
    strokeStyle: string
    lineWidth: number
    save(): void
    restore(): void
    translate(x: number, y: number): void
    rotate(angle: number): void
    scale(scaleX: number, scaleY: number): void
    textAlign: string
    textBaseline: string
    font: string
    fillText(text: string, x: number, y: number): void
    measureText(text: string): { width: number }
  }
}

// 响应数据类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 用户信息
export interface UserInfo {
  id: number
  nickname: string
  avatar: string
  phone: string
  coupleId?: number
  createdAt: string
}

// 转盘内容
export interface WheelContent {
  id: number
  contentText: string
  categoryId: number
  weight: number
}

// 转盘配置
export interface WheelConfig {
  id: number
  userId: number
  radius: number
  categories: number[]
  theme: string
  animationDuration: number
}

// 转盘结果
export interface SpinResult {
  id: number
  contentText: string
  categoryId: number
}

// 转盘历史记录
export interface SpinHistory {
  id: number
  resultText: string
  spinTime: string
  categoryName: string
}

// 情侣关系信息
export interface CoupleInfo {
  id: number
  status: number
  inviteCode: string
  partnerInfo: {
    id: number
    nickname: string
    avatar: string
  } | null
  createdAt: string
}

// 用户统计数据
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

// 系统概览
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

// 转盘趋势数据
export interface SpinTrend {
  date: string
  spinCount: number
  userCount: number
  coupleCount: number
}

// 热门内容
export interface PopularContent {
  contentId: number
  contentText: string
  categoryName: string
  spinCount: number
  winRate: number
}

// 分类统计
export interface CategoryStatistics {
  categoryId: number
  categoryName: string
  contentCount: number
  totalSpins: number
  avgWeight: number
  uniqueUsers: number
}

// 情侣统计数据
export interface CoupleStatistics {
  totalCouples: number
  activeCouples: number
  avgCoupleAge: number
  formationRate: number
  mostActiveCoupleSpins: number
  coupleRetentionRate7d: number
  coupleRetentionRate30d: number
}

// 分页数据
export interface PaginationData<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
  hasMore: boolean
}
