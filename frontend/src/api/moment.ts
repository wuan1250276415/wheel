import request from './request'
import type {
  Moment,
  MomentCreateRequest,
  Comment,
  CommentCreateRequest,
  PageResponse,
  UserBrief
} from '@/types/moment'

/**
 * 创建动态
 */
export const createMoment = (data: MomentCreateRequest) => {
  return request<number>({
    url: '/api/moments',
    method: 'POST',
    data
  })
}

/**
 * 获取动态详情
 */
export const getMomentDetail = (momentId: string | number) => {
  return request<Moment>({
    url: `/api/moments/${momentId}`,
    method: 'GET'
  })
}

/**
 * 获取动态时间线
 */
export const getMomentTimeline = (page: number = 1, pageSize: number = 10) => {
  return request<PageResponse<Moment>>({
    url: '/api/moments/timeline',
    method: 'GET',
    params: { page, pageSize }
  })
}

/**
 * 更新动态
 */
export const updateMoment = (momentId: string | number, data: MomentCreateRequest) => {
  return request<void>({
    url: `/api/moments/${momentId}`,
    method: 'PUT',
    data
  })
}

/**
 * 删除动态
 */
export const deleteMoment = (momentId: string | number) => {
  return request<void>({
    url: `/api/moments/${momentId}`,
    method: 'DELETE'
  })
}

/**
 * 点赞动态
 */
export const likeMoment = (momentId: string | number) => {
  return request<void>({
    url: `/api/moments/${momentId}/like`,
    method: 'POST'
  })
}

/**
 * 取消点赞
 */
export const unlikeMoment = (momentId: string | number) => {
  return request<void>({
    url: `/api/moments/${momentId}/like`,
    method: 'DELETE'
  })
}

/**
 * 添加评论
 */
export const addComment = (momentId: string | number, data: CommentCreateRequest) => {
  return request<number>({
    url: `/api/moments/${momentId}/comments`,
    method: 'POST',
    data
  })
}

/**
 * 获取评论列表
 */
export const getComments = (momentId: string | number, page: number = 1, pageSize: number = 20) => {
  return request<PageResponse<Comment>>({
    url: `/api/moments/${momentId}/comments`,
    method: 'GET',
    params: { page, pageSize }
  })
}

/**
 * 获取评论回复列表
 */
export const getCommentReplies = (commentId: string | number, page: number = 1, pageSize: number = 20) => {
  return request<PageResponse<Comment>>({
    url: `/api/moments/comments/${commentId}/replies`,
    method: 'GET',
    params: { page, pageSize }
  })
}

/**
 * 删除评论
 */
export const deleteComment = (commentId: string | number) => {
  return request<void>({
    url: `/api/moments/comments/${commentId}`,
    method: 'DELETE'
  })
}

/**
 * 获取可提及用户列表
 */
export const getMentionableUsers = () => {
  return request<UserBrief[]>({
    url: '/api/moments/mentionable-users',
    method: 'GET'
  })
}
