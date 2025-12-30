/**
 * 动态可见性枚举
 */
export enum MomentVisibility {
  /** 仅情侣可见 */
  COUPLE_ONLY = 0,
  /** 公开 */
  PUBLIC = 1
}

/**
 * 动态数据结构
 */
export interface Moment {
  /** 动态ID */
  id: string | number
  /** 情侣关系ID */
  coupleId: number
  /** 发布者用户ID */
  userId: number
  /** 发布者昵称 */
  nickname: string
  /** 发布者头像 */
  avatarUrl: string
  /** 动态文本内容 */
  content: string
  /** 图片URL列表 */
  images: string[]
  /** 可见性 */
  visibility: MomentVisibility
  /** 点赞数 */
  likeCount: number
  /** 评论数 */
  commentCount: number
  /** 当前用户是否已点赞 */
  isLiked: boolean
  /** 创建时间 */
  createTime: string
}

/**
 * 创建动态请求
 */
export interface MomentCreateRequest {
  /** 动态文本内容 */
  content: string
  /** 图片URL列表（最多9张） */
  images?: string[]
  /** 可见性 */
  visibility?: MomentVisibility
}

/**
 * 用户简要信息
 */
export interface UserBrief {
  /** 用户ID */
  id: number
  /** 用户昵称 */
  nickname: string
  /** 用户头像 */
  avatarUrl: string
}

/**
 * 评论数据结构
 */
export interface Comment {
  /** 评论ID */
  id: string | number
  /** 动态ID */
  momentId: string | number
  /** 父评论ID */
  parentCommentId?: string | number
  /** 回复的目标用户ID */
  replyToUserId?: number
  /** 回复的目标用户昵称 */
  replyToNickname?: string
  /** 评论用户ID */
  userId: number
  /** 评论用户昵称 */
  nickname: string
  /** 评论用户头像 */
  avatarUrl: string
  /** 评论内容 */
  content: string
  /** 被@提及的用户列表 */
  mentionedUsers?: UserBrief[]
  /** 创建时间 */
  createTime: string
  /** 回复数量 */
  replyCount?: number
}

/**
 * 创建评论请求
 */
export interface CommentCreateRequest {
  /** 评论内容 */
  content: string
  /** 父评论ID（回复时使用） */
  parentCommentId?: number
  /** 回复的目标用户ID */
  replyToUserId?: number
  /** 回复的目标用户昵称 */
  replyToNickname?: string
  /** 被@提及的用户ID列表 */
  mentionedUserIds?: number[]
}

/**
 * 分页响应
 */
export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}
