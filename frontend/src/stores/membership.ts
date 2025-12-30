import { defineStore } from 'pinia'
import type { MembershipPlan, MembershipStatus } from '@/types/membership'
import { membershipAPI } from '@/api/membership'

export const useMembershipStore = defineStore('membership', {
  state: () => ({
    plans: [] as MembershipPlan[],
    currentStatus: null as MembershipStatus | null,
    loading: false,
    error: null as string | null
  }),

  getters: {
    isVIP: (state) => state.currentStatus?.tier === 1,
    isSVIP: (state) => state.currentStatus?.tier === 2,
    hasActiveMembership: (state) => state.currentStatus?.isActive === true,
    vipPlans: (state) => state.plans.filter(p => p.tier === 1),
    svipPlans: (state) => state.plans.filter(p => p.tier === 2)
  },

  actions: {
    async fetchPlans() {
      this.loading = true
      this.error = null
      try {
        this.plans = await membershipAPI.getAllPlans()
      } catch (error: any) {
        this.error = error.message || '获取套餐列表失败'
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchStatus() {
      this.loading = true
      this.error = null
      try {
        this.currentStatus = await membershipAPI.getMembershipStatus()
      } catch (error: any) {
        this.error = error.message || '获取会员状态失败'
        throw error
      } finally {
        this.loading = false
      }
    },

    async subscribe(planId: number, paymentMethod: number) {
      this.loading = true
      this.error = null
      try {
        const result = await membershipAPI.subscribe({ planId, paymentMethod })
        await this.fetchStatus()
        return result
      } catch (error: any) {
        this.error = error.message || '订阅失败'
        throw error
      } finally {
        this.loading = false
      }
    },

    async updateAutoRenew(autoRenew: boolean) {
      try {
        await membershipAPI.updateAutoRenew(autoRenew)
        if (this.currentStatus) {
          this.currentStatus.autoRenew = autoRenew
        }
      } catch (error: any) {
        this.error = error.message || '更新自动续费失败'
        throw error
      }
    }
  }
})
