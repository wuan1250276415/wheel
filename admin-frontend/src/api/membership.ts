import request from '@/utils/request'
import type {
  ApiResponse,
  PageResponse,
  MembershipPlan,
  MembershipPlanQueryDTO,
  MembershipPlanCreateDTO,
  UserMembership,
  UserMembershipQueryDTO,
  PaymentOrder,
  PaymentOrderQueryDTO,
  OrderStatistics
} from '@/types/api'

/**
 * 分页查询套餐列表
 */
export function queryPlans(params: MembershipPlanQueryDTO) {
  return request.get<any, ApiResponse<PageResponse<MembershipPlan>>>('/admin/membership/plans', { params })
}

/**
 * 获取套餐详情
 */
export function getPlanDetail(id: number | string) {
  return request.get<any, ApiResponse<MembershipPlan>>(`/admin/membership/plans/${id}`)
}

/**
 * 创建套餐
 */
export function createPlan(data: MembershipPlanCreateDTO) {
  return request.post<any, ApiResponse<number>>('/admin/membership/plans', data)
}

/**
 * 更新套餐
 */
export function updatePlan(id: number | string, data: MembershipPlanCreateDTO) {
  return request.put<any, ApiResponse<void>>(`/admin/membership/plans/${id}`, data)
}

/**
 * 删除套餐
 */
export function deletePlan(id: number | string) {
  return request.delete<any, ApiResponse<void>>(`/admin/membership/plans/${id}`)
}

/**
 * 更新套餐状态
 */
export function updatePlanStatus(id: number | string, status: number) {
  return request.put<any, ApiResponse<void>>(`/admin/membership/plans/${id}/status`, null, {
    params: { status }
  })
}

/**
 * 查询用户会员列表
 */
export function queryUserMemberships(params: UserMembershipQueryDTO) {
  return request.get<any, ApiResponse<PageResponse<UserMembership>>>('/admin/membership/users', { params })
}

/**
 * 获取用户会员历史记录
 */
export function getUserMembershipHistory(userId: number | string) {
  return request.get<any, ApiResponse<any[]>>(`/admin/membership/users/${userId}/history`)
}

/**
 * 延期用户会员
 */
export function extendUserMembership(userId: number | string, data: { days: number; reason: string }) {
  return request.post<any, ApiResponse<void>>(`/admin/membership/users/${userId}/extend`, data)
}

/**
 * 升级用户会员
 */
export function upgradeUserMembership(userId: number | string, data: { targetTier: number; durationDays: number; reason: string }) {
  return request.post<any, ApiResponse<void>>(`/admin/membership/users/${userId}/upgrade`, data)
}

/**
 * 取消用户会员
 */
export function cancelUserMembership(userId: number | string) {
  return request.post<any, ApiResponse<void>>(`/admin/membership/users/${userId}/cancel`)
}

/**
 * 更新自动续费状态
 */
export function updateAutoRenew(userId: number | string, autoRenew: boolean) {
  return request.put<any, ApiResponse<void>>(`/admin/membership/users/${userId}/auto-renew`, { autoRenew })
}

/**
 * 查询支付订单
 */
export function queryOrders(params: PaymentOrderQueryDTO) {
  return request.get<any, ApiResponse<PageResponse<PaymentOrder>>>('/admin/membership/orders', { params })
}

/**
 * 获取订单详情
 */
export function getOrderDetail(id: number | string) {
  return request.get<any, ApiResponse<PaymentOrder>>(`/admin/membership/orders/${id}`)
}

/**
 * 订单退款
 */
export function refundOrder(id: number | string, data: { refundAmount: number; refundReason: string }) {
  return request.post<any, ApiResponse<void>>(`/admin/membership/orders/${id}/refund`, data)
}

/**
 * 更新订单备注
 */
export function updateOrderRemark(id: number | string, remark: string) {
  return request.put<any, ApiResponse<void>>(`/admin/membership/orders/${id}/remark`, { remark })
}

/**
 * 获取订单统计数据
 */
export function getOrderStatistics(params: any) {
  return request.get<any, ApiResponse<OrderStatistics>>('/admin/membership/orders/statistics', { params })
}
