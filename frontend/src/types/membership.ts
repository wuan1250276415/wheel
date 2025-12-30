export interface MembershipPlan {
  id: number
  planName: string
  planKey: string
  tier: number
  tierName: string
  durationDays: number
  price: number
  originalPrice?: number
  benefits: Record<string, any>
  description: string
  isRecommended: boolean
  discount?: string
}

export interface MembershipStatus {
  userId: number
  tier: number
  tierName: string
  startTime?: string
  endTime?: string
  autoRenew: boolean
  isActive: boolean
  remainingDays?: number
}

export interface SubscribeRequest {
  planId: number
  paymentMethod: number
}

export interface PaymentResult {
  orderNo: string
  paymentStatus: number
  message: string
  prepayData?: string
}
