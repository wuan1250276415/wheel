<template>
  <view class="membership-page">
    <view class="status-section" v-if="membershipStore.currentStatus">
      <view class="status-card">
        <view class="tier-badge" :class="`tier-${membershipStore.currentStatus.tier}`">
          {{ membershipStore.currentStatus.tierName }}
        </view>
        <view class="status-info" v-if="membershipStore.hasActiveMembership">
          <text class="expire-text">到期时间：{{ formatDate(membershipStore.currentStatus.endTime) }}</text>
          <text class="days-text">剩余 {{ membershipStore.currentStatus.remainingDays }} 天</text>
        </view>
        <view class="auto-renew" v-if="membershipStore.hasActiveMembership">
          <text>自动续费</text>
          <switch
            :checked="membershipStore.currentStatus.autoRenew"
            @change="handleAutoRenewChange"
            color="#ff6b6b"
          />
        </view>
      </view>
    </view>

    <view class="plans-section">
      <view class="section-title">选择会员套餐</view>

      <view class="tier-group">
        <view class="tier-label">VIP会员</view>
        <view class="plans-grid">
          <view
            v-for="plan in membershipStore.vipPlans"
            :key="plan.id"
            class="plan-card"
            :class="{ recommended: plan.isRecommended }"
            @tap="selectPlan(plan)"
          >
            <view class="plan-name">{{ plan.planName }}</view>
            <view class="plan-price">
              <text class="currency">¥</text>
              <text class="amount">{{ plan.price }}</text>
            </view>
            <view class="original-price" v-if="plan.originalPrice">
              <text>¥{{ plan.originalPrice }}</text>
            </view>
            <view class="discount-badge" v-if="plan.discount">{{ plan.discount }}</view>
            <view class="recommend-badge" v-if="plan.isRecommended">推荐</view>
          </view>
        </view>
      </view>

      <view class="tier-group">
        <view class="tier-label">SVIP会员</view>
        <view class="plans-grid">
          <view
            v-for="plan in membershipStore.svipPlans"
            :key="plan.id"
            class="plan-card svip"
            :class="{ recommended: plan.isRecommended }"
            @tap="selectPlan(plan)"
          >
            <view class="plan-name">{{ plan.planName }}</view>
            <view class="plan-price">
              <text class="currency">¥</text>
              <text class="amount">{{ plan.price }}</text>
            </view>
            <view class="original-price" v-if="plan.originalPrice">
              <text>¥{{ plan.originalPrice }}</text>
            </view>
            <view class="discount-badge" v-if="plan.discount">{{ plan.discount }}</view>
            <view class="recommend-badge" v-if="plan.isRecommended">推荐</view>
          </view>
        </view>
      </view>
    </view>

    <view class="payment-modal" v-if="showPayment" @tap="closePayment">
      <view class="payment-content" @tap.stop>
        <view class="modal-title">选择支付方式</view>
        <view class="selected-plan" v-if="selectedPlan">
          <text>{{ selectedPlan.planName }}</text>
          <text class="price">¥{{ selectedPlan.price }}</text>
        </view>
        <view class="payment-methods">
          <view
            class="method-item"
            :class="{ active: paymentMethod === 2 }"
            @tap="paymentMethod = 2"
          >
            <text>支付宝</text>
          </view>
          <view
            class="method-item"
            :class="{ active: paymentMethod === 1 }"
            @tap="paymentMethod = 1"
          >
            <text>微信支付</text>
          </view>
        </view>
        <button class="pay-btn" @tap="handlePay" :loading="paying">确认支付</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useMembershipStore } from '@/stores/membership'
import type { MembershipPlan } from '@/types/membership'

const membershipStore = useMembershipStore()

const showPayment = ref(false)
const selectedPlan = ref<MembershipPlan | null>(null)
const paymentMethod = ref(2)
const paying = ref(false)

onMounted(async () => {
  await Promise.all([
    membershipStore.fetchPlans(),
    membershipStore.fetchStatus()
  ])
})

const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const selectPlan = (plan: MembershipPlan) => {
  selectedPlan.value = plan
  showPayment.value = true
}

const closePayment = () => {
  showPayment.value = false
  selectedPlan.value = null
}

const handlePay = async () => {
  if (!selectedPlan.value) return

  paying.value = true
  try {
    await membershipStore.subscribe(selectedPlan.value.id, paymentMethod.value)
    uni.showToast({ title: '订阅成功', icon: 'success' })
    closePayment()
  } catch (error: any) {
    uni.showToast({ title: error.message || '订阅失败', icon: 'none' })
  } finally {
    paying.value = false
  }
}

const handleAutoRenewChange = async (e: any) => {
  try {
    await membershipStore.updateAutoRenew(e.detail.value)
    uni.showToast({ title: '设置成功', icon: 'success' })
  } catch (error: any) {
    uni.showToast({ title: '设置失败', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.membership-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 40rpx;
}

.status-section {
  padding: 30rpx;
}

.status-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20rpx;
  padding: 40rpx;
  color: white;
}

.tier-badge {
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 20rpx;

  &.tier-1 { color: #ffd700; }
  &.tier-2 { color: #ff6b6b; }
}

.status-info {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  margin-bottom: 20rpx;
}

.auto-renew {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid rgba(255,255,255,0.3);
}

.plans-section {
  padding: 0 30rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 30rpx;
}

.tier-group {
  margin-bottom: 40rpx;
}

.tier-label {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 20rpx;
}

.plans-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
}

.plan-card {
  position: relative;
  background: white;
  border-radius: 16rpx;
  padding: 30rpx 20rpx;
  text-align: center;
  border: 2rpx solid #e0e0e0;

  &.recommended {
    border-color: #667eea;
  }

  &.svip {
    background: linear-gradient(135deg, #fff5f5 0%, #ffe5e5 100%);
  }
}

.plan-name {
  font-size: 24rpx;
  margin-bottom: 10rpx;
}

.plan-price {
  font-size: 40rpx;
  font-weight: bold;
  color: #ff6b6b;
  margin: 10rpx 0;

  .currency { font-size: 24rpx; }
}

.original-price {
  font-size: 20rpx;
  color: #999;
  text-decoration: line-through;
}

.discount-badge,
.recommend-badge {
  position: absolute;
  top: 0;
  right: 0;
  background: #ff6b6b;
  color: white;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 0 16rpx 0 16rpx;
}

.recommend-badge {
  background: #667eea;
}

.payment-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: flex-end;
  z-index: 999;
}

.payment-content {
  width: 100%;
  background: white;
  border-radius: 40rpx 40rpx 0 0;
  padding: 40rpx;
}

.modal-title {
  font-size: 32rpx;
  font-weight: bold;
  text-align: center;
  margin-bottom: 30rpx;
}

.selected-plan {
  display: flex;
  justify-content: space-between;
  padding: 20rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  margin-bottom: 30rpx;

  .price {
    color: #ff6b6b;
    font-weight: bold;
  }
}

.payment-methods {
  display: flex;
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.method-item {
  flex: 1;
  padding: 30rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 12rpx;
  text-align: center;

  &.active {
    border-color: #667eea;
    background: #f0f4ff;
    color: #667eea;
  }
}

.pay-btn {
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 50rpx;
  padding: 28rpx 0;
  font-size: 32rpx;
}
</style>
