/**
 * 情侣报告相关类型定义
 */

/**
 * 报告类型枚举
 */
export enum ReportType {
  /** 周报 */
  WEEKLY = 1,
  /** 月报 */
  MONTHLY = 2,
  /** 年报 */
  YEARLY = 3
}

/**
 * 纪念日类型枚举
 */
export enum AnniversaryType {
  /** 恋爱纪念日 */
  RELATIONSHIP = 1,
  /** 生日 */
  BIRTHDAY = 2,
  /** 自定义 */
  CUSTOM = 3
}

/**
 * 纪念日视图对象
 */
export interface AnniversaryVO {
  /** 纪念日ID */
  id: number
  /** 纪念日类型: 1-恋爱纪念日, 2-生日, 3-自定义 */
  anniversaryType: number
  /** 纪念日类型名称 */
  anniversaryTypeName: string
  /** 纪念日日期 (YYYY-MM-DD) */
  anniversaryDate: string
  /** 纪念日名称 */
  name: string
  /** 提前提醒天数 */
  remindDays: number
  /** 距离纪念日还有多少天 */
  daysUntil: number
}

/**
 * 报告数据结构
 */
export interface ReportData {
  /** 认识天数 */
  daysTogether: number
  /** 总转盘次数 */
  totalSpins: number
  /** 用户1转盘次数 */
  user1Spins: number
  /** 用户2转盘次数 */
  user2Spins: number
  /** 最常用分类 */
  favoriteCategory: string
  /** 最常用分类次数 */
  favoriteCategoryCount: number
  /** 活跃时间分布(小时->次数) */
  activeTimeDistribution: Record<number, number>
  /** 聊天消息数 */
  chatMessageCount: number
  /** 动态数量 */
  momentCount: number
  /** 默契度评分 */
  compatibilityScore: number
  /** 默契度描述 */
  compatibilityDesc: string
  /** 即将到来的纪念日 */
  upcomingAnniversaries: AnniversaryVO[]
}

/**
 * 情侣报告视图对象
 */
export interface CoupleReportVO {
  /** 报告ID */
  id: number
  /** 报告类型: 1-周报, 2-月报, 3-年报 */
  reportType: number
  /** 报告类型名称 */
  reportTypeName: string
  /** 统计开始日期 (YYYY-MM-DD) */
  startDate: string
  /** 统计结束日期 (YYYY-MM-DD) */
  endDate: string
  /** 报告数据 */
  reportData: ReportData
  /** 默契度评分(0-100) */
  compatibilityScore: number
  /** 生成时间 (ISO datetime) */
  generatedAt: string
}

/**
 * 报告查询参数
 */
export interface ReportQueryDTO {
  /** 报告类型: 1-周报, 2-月报, 3-年报 */
  reportType?: number
  /** 查询开始日期 (YYYY-MM-DD) */
  startDate?: string
  /** 查询结束日期 (YYYY-MM-DD) */
  endDate?: string
  /** 页码，默认1 */
  pageNum?: number
  /** 每页大小，默认10 */
  pageSize?: number
}

/**
 * 纪念日创建参数
 */
export interface AnniversaryCreateDTO {
  /** 纪念日类型: 1-恋爱纪念日, 2-生日, 3-自定义 */
  anniversaryType: number
  /** 纪念日日期 (YYYY-MM-DD) */
  anniversaryDate: string
  /** 纪念日名称 */
  name: string
  /** 提前提醒天数，默认7天 */
  remindDays?: number
}

/**
 * 纪念日更新参数
 */
export interface AnniversaryUpdateDTO {
  /** 纪念日类型: 1-恋爱纪念日, 2-生日, 3-自定义 */
  anniversaryType?: number
  /** 纪念日日期 (YYYY-MM-DD) */
  anniversaryDate?: string
  /** 纪念日名称 */
  name?: string
  /** 提前提醒天数 */
  remindDays?: number
}

/**
 * 分享图片结果
 */
export interface ShareImageVO {
  /** Base64编码的图片数据 */
  imageBase64: string
  /** 使用的主题 */
  theme: string
}

/**
 * 报告分页响应
 */
export interface ReportPageResult {
  /** 当前页码 */
  current: number
  /** 每页大小 */
  size: number
  /** 总记录数 */
  total: number
  /** 总页数 */
  pages: number
  /** 报告列表 */
  records: CoupleReportVO[]
}
