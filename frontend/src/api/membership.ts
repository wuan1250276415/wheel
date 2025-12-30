import type { MembershipPlan, MembershipStatus, SubscribeRequest, PaymentResult } from '@/types/membership'
import http from '@/api/request'

export const membershipAPI = {
  getAllPlans(): Promise<MembershipPlan[]> {
    return http.get('/api/membership/plans')
  },

  getMembershipStatus(): Promise<MembershipStatus> {
    return http.get('/api/membership/status')
  },

  subscribe(data: SubscribeRequest): Promise<PaymentResult> {
    return http.post('/api/membership/subscribe', data)
  },

  updateAutoRenew(autoRenew: boolean): Promise<void> {
    return http.put(`/api/membership/auto-renew?autoRenew=${autoRenew}`)
  }
}
