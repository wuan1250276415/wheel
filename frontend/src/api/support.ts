import request from './request'

// ==================== 类型定义 ====================

/**
 * 工单状态
 */
export enum TicketStatus {
  PENDING = 1,
  IN_PROGRESS = 2,
  AWAITING_REPLY = 3,
  RESOLVED = 4,
  CLOSED = 5
}

/**
 * 工单优先级
 */
export enum TicketPriority {
  LOW = 1,
  MEDIUM = 2,
  HIGH = 3,
  URGENT = 4
}

/**
 * 工单创建DTO
 */
export interface TicketCreateDTO {
  title: string
  description: string
  category: string
}

/**
 * 工单VO
 */
export interface TicketVO {
  id: number
  ticketNo: string
  title: string
  description: string
  category: string
  priority: TicketPriority
  status: TicketStatus
  assignedStaffName?: string
  isVipDedicated?: boolean
  rating?: number
  createTime: string
  resolvedAt?: string
  unreadCount: number
}

/**
 * 回复VO
 */
export interface ReplyVO {
  id: number
  isStaff: boolean
  senderName: string
  content: string
  createTime: string
}

/**
 * FAQ VO
 */
export interface FaqVO {
  id: number
  category: string
  question: string
  answer: string
  isHelpful: boolean
}

// ==================== API 函数 ====================

/**
 * 创建工单
 * POST /api/support/tickets
 */
export function createTicket(data: TicketCreateDTO) {
  return request<TicketVO>({
    url: '/api/support/tickets',
    method: 'POST',
    data
  })
}

/**
 * 获取我的工单列表
 * GET /api/support/tickets
 */
export function getMyTickets() {
  return request<TicketVO[]>({
    url: '/api/support/tickets',
    method: 'GET'
  })
}

/**
 * 获取工单详情
 * GET /api/support/tickets/{id}
 */
export function getTicketDetail(id: number) {
  return request<TicketVO>({
    url: `/api/support/tickets/${id}`,
    method: 'GET'
  })
}

/**
 * 获取工单回复列表
 * GET /api/support/tickets/{id}/replies
 */
export function getTicketReplies(id: number) {
  return request<ReplyVO[]>({
    url: `/api/support/tickets/${id}/replies`,
    method: 'GET'
  })
}

/**
 * 回复工单
 * POST /api/support/tickets/{id}/reply
 */
export function replyTicket(id: number, content: string) {
  return request<ReplyVO>({
    url: `/api/support/tickets/${id}/reply`,
    method: 'POST',
    params: {
      content
    }
  })
}

/**
 * 评价工单
 * POST /api/support/tickets/{id}/rating
 */
export function rateTicket(id: number, rating: number, comment?: string) {
  return request<void>({
    url: `/api/support/tickets/${id}/rating`,
    method: 'POST',
    params: {
      rating,
      comment
    }
  })
}

/**
 * 获取FAQ列表
 * GET /api/support/faq
 */
export function getFaqList(category?: string) {
  return request<FaqVO[]>({
    url: '/api/support/faq',
    method: 'GET',
    params: {
      category
    }
  })
}

/**
 * 标记FAQ有帮助
 * POST /api/support/faq/{id}/helpful
 */
export function markFaqHelpful(id: number) {
  return request<void>({
    url: `/api/support/faq/${id}/helpful`,
    method: 'POST'
  })
}
