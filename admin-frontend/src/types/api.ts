// 通用响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 分页响应
export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 管理员信息
export interface AdminInfo {
  id: number
  username: string
  realName: string
  phone?: string
  email?: string
  avatarUrl?: string
  status: number
  lastLoginTime?: string
  permissions: string[]
  roles: string[]
}

// 登录响应
export interface LoginResponse {
  accessToken: string
  refreshToken: string
  adminInfo: AdminInfo
  permissions: string[]
  roles: string[]
}

// 会员套餐
export interface MembershipPlan {
  id: number
  planName: string
  planKey: string
  tier: number
  tierName: string
  durationDays: number
  price: number
  originalPrice?: number
  benefits: string
  description?: string
  isRecommended: boolean
  status: number
  sortOrder: number
  createTime: string
  updateTime: string
}

// 用户会员
export interface UserMembership {
  id: number
  userId: number
  userNickname?: string
  nickname?: string
  planId: number
  planName: string
  tier: number
  tierName: string
  startTime: string
  endTime: string
  remainingDays: number
  autoRenew: boolean
  status: number
  orderId?: number
  createTime: string
  updateTime: string
}

// 会员套餐查询
export interface MembershipPlanQueryDTO {
  tier?: number
  status?: number
  keyword?: string
  current?: number
  size?: number
}

// 会员套餐创建/编辑
export interface MembershipPlanCreateDTO {
  id?: number
  planName: string
  planKey: string
  tier: number
  durationDays: number
  price: number
  originalPrice: number
  benefits: string
  description?: string
  isRecommended: boolean
  sortOrder: number
}

// 支付订单
export interface PaymentOrder {
  id: number
  orderNo: string
  userId: number
  userNickname?: string
  nickname?: string
  planId: number
  planName: string
  amount: number
  orderAmount: number
  paidAmount?: number
  paymentMethod: number
  paymentMethodName?: string
  paymentStatus: number
  paymentStatusName?: string
  tradeNo?: string
  transactionId?: string
  paidAt?: string
  paymentTime?: string
  refundedAt?: string
  refundTime?: string
  refundAmount?: number
  refundReason?: string
  callbackData?: string
  remark?: string
  createTime: string
  updateTime: string
}

// 用户会员查询
export interface UserMembershipQueryDTO {
  userId?: number
  tier?: number
  status?: number
  dateRange: string[]
  current?: number
  size?: number
}

// 支付订单查询
export interface PaymentOrderQueryDTO {
  orderNo?: string
  userId?: number
  paymentStatus?: number
  dateRange: string[]
  current?: number
  size?: number
}

// 订单统计
export interface OrderStatistics {
  todayOrders: number
  todayIncome: number
  monthIncome: number
  refundRate: number
}

// 审核内容
export interface WheelContent {
  id: number
  categoryId: number
  contentText: string
  weight: number
  createUserId: number
  auditStatus: number
  auditorId?: number
  auditedAt?: string
  auditComment?: string
  auditPriority: number
  tags?: string
  isSystem: boolean
  status: number
  createTime: string
}

// 审核历史
export interface AuditHistory {
  id: number
  contentId: number
  contentText: string
  submitUserId: number
  submitUserName?: string
  auditStatus: number
  auditStatusName: string
  auditorId: number
  auditorName?: string
  auditedAt: string
  auditComment?: string
  aiAuditResult?: Record<string, any>
  auditType: number
  auditTypeName: string
  processTime?: number
  auditSource?: string
  createTime: string
}

// 审核统计
export interface AuditStatistics {
  todayAuditCount: number
  approveRate: number
  avgProcessTime: number
  pendingCount: number
  auditorStats?: AuditorWorkload[]
  trendStats?: AuditTrendItem[]
}

export interface AuditorWorkload {
  auditorId: number
  auditorName?: string
  totalCount: number
  approveCount: number
  rejectCount: number
  approvalRate: number
  avgProcessTime: number
}

export interface AuditTrendItem {
  date: string
  totalCount: number
  approveCount: number
  rejectCount: number
  approveRate: number
}

// 审核队列内容
export interface AuditContent {
  id: number
  title: string
  description: string
  categoryId: number
  priority: number
  tags: string[]
  submitUser: string
  submitTime: string
  aiAuditResult?: number
  aiAuditReason?: string
}

// 审核队列查询
export interface AuditContentQueryDTO {
  priority?: number
  categoryId?: number
  dateRange: string[]
  current?: number
  size?: number
}

// 待审核统计
export interface PendingCount {
  svipPending: number
  vipPending: number
  normalPending: number
  totalPending: number
}

// 审核历史查询
export interface AuditHistoryQueryDTO {
  auditorId?: number
  auditStatus?: number
  dateRange: string[]
  current?: number
  size?: number
}
