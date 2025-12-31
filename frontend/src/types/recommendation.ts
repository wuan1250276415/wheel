/**
 * AI 推荐模块相关类型定义
 */

/**
 * 反馈类型枚举
 */
export enum FeedbackType {
  /** 点击 */
  CLICK = 'CLICK',
  /** 忽略 */
  IGNORE = 'IGNORE',
  /** 喜欢 */
  LIKE = 'LIKE',
  /** 不喜欢 */
  DISLIKE = 'DISLIKE'
}

/**
 * 推荐结果VO
 */
export interface RecommendationVO {
  /** 内容ID */
  contentId: number
  /** 内容文本 */
  contentText: string
  /** 分类ID */
  categoryId: number
  /** 分类名称 */
  categoryName: string
  /** 推荐分数 */
  score: number
  /** 推荐原因 */
  reason: string
}

/**
 * 分类排名VO
 */
export interface CategoryRankVO {
  /** 分类ID */
  categoryId: number
  /** 分类名称 */
  categoryName: string
  /** 分类图标URL */
  iconUrl?: string
  /** 排名位置 */
  rank: number
  /** 偏好权重 */
  weight: number
}

/**
 * 情侣推荐VO
 */
export interface CoupleRecommendationVO {
  /** 分类ID */
  categoryId: number
  /** 分类名称 */
  categoryName: string
  /** 推荐内容列表 */
  contents: RecommendationVO[]
  /** 匹配原因 */
  matchReason: string
  /** 匹配度分数 */
  compatibilityScore: number
}

/**
 * 首页推荐VO
 */
export interface HomepageRecommendationVO {
  /** 个性化分类排名 */
  categoryRanking: CategoryRankVO[]
  /** 为你推荐（Top 5） */
  recommendedForYou: RecommendationVO[]
  /** 情侣精选（可选，仅情侣用户显示） */
  couplePicks?: RecommendationVO[]
  /** 刷新时间 */
  refreshTime: string
}

/**
 * 推荐反馈DTO
 */
export interface RecommendationFeedbackDTO {
  /** 内容ID */
  contentId: number
  /** 反馈类型：CLICK, IGNORE, LIKE, DISLIKE */
  feedbackType: FeedbackType | 'CLICK' | 'IGNORE' | 'LIKE' | 'DISLIKE'
  /** 会话ID */
  sessionId?: string
}

/**
 * 偏好调查DTO
 */
export interface PreferenceSurveyDTO {
  /** 选择的分类ID列表 */
  selectedCategoryIds?: number[]
  /** 是否跳过调查 */
  skipped: boolean
}

/**
 * 推荐分页响应
 */
export interface RecommendationPageResult {
  /** 当前页码 */
  current: number
  /** 每页大小 */
  size: number
  /** 总记录数 */
  total: number
  /** 总页数 */
  pages: number
  /** 推荐列表 */
  records: RecommendationVO[]
}
