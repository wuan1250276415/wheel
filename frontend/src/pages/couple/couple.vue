<template>
  <view class="container">
    <!-- 情侣关系状态 -->
    <view v-if="coupleInfo" class="couple-card">
      <view class="couple-avatar">
        <image
          v-if="coupleInfo.partnerAvatar"
          :src="coupleInfo.partnerAvatar"
          class="avatar"
          mode="aspectFill"
        />
        <view class="avatar-placeholder" v-else>
          <text>{{ coupleInfo.partnerNickname?.[0] || '伴' }}</text>
        </view>
      </view>

      <view class="couple-info">
        <text class="couple-name">{{ coupleInfo.partnerNickname }}</text>
        <text class="couple-status">已配对</text>
        <text class="couple-date">配对日期：{{ formatDate(coupleInfo.createdAt) }}</text>
        <text class="couple-spins">共同转盘：{{ coupleInfo.totalSpins || 0 }}次</text>
      </view>

      <button class="break-btn" @click="showBreakConfirm">解除关系</button>
    </view>

    <!-- 邀请情侣 -->
    <view v-else class="invite-section">
      <!-- 通过手机号邀请 -->
      <view class="invite-card">
        <view class="invite-icon"></view>
        <text class="invite-title">邀请你的另一半</text>
        <text class="invite-desc">输入对方手机号发送邀请</text>

        <view class="phone-input-group">
          <input
            v-model="partnerPhone"
            class="phone-input"
            type="number"
            placeholder="请输入对方手机号"
            maxlength="11"
          />
          <button 
            class="invite-btn" 
            @click="sendInvite" 
            :disabled="!isValidPhone || isInviting"
          >
            {{ isInviting ? '发送中...' : '发送邀请' }}
          </button>
        </view>
      </view>

      <!-- 邀请码输入 -->
      <view class="input-section">
        <view class="input-title">或输入TA的邀请码</view>
        <view class="input-group">
          <input
            v-model="inviteCode"
            class="code-input"
            placeholder="请输入邀请码"
            maxlength="8"
            @blur="validateCode"
          />
          <button 
            class="accept-btn" 
            @click="handleAcceptInvite" 
            :disabled="!inviteCode || isAccepting || !isCodeValid"
          >
            {{ isAccepting ? '接受中...' : '接受邀请' }}
          </button>
        </view>
        <text v-if="codeValidationMsg" class="validation-msg" :class="{ error: !isCodeValid }">
          {{ codeValidationMsg }}
        </text>
      </view>
    </view>

    <!-- 邀请码弹窗 -->
    <uni-popup ref="invitePopup" type="dialog">
      <uni-popup-dialog
        :show="showInvitePopup"
        title="邀请TA加入"
        @close="showInvitePopup = false"
      >
        <view class="invite-content">
          <view class="qr-code">
            <image :src="inviteData.qrCode" class="qr-image" mode="aspectFit" />
          </view>

          <view class="invite-code-display">
            <text class="code-label">邀请码</text>
            <text class="code-value">{{ inviteData.inviteCode }}</text>
            <button class="copy-btn" @click="copyInviteCode">复制</button>
          </view>

          <view class="expire-time">
            <text>有效期至：{{ formatDateTime(inviteData.expiresAt) }}</text>
          </view>
        </view>
      </uni-popup-dialog>
    </uni-popup>

    <!-- 情侣转盘记录 -->
    <view v-if="coupleInfo && coupleSpinHistory.length > 0" class="history-section">
      <view class="section-title">
        <text>情侣转盘记录</text>
      </view>

      <scroll-view class="history-list" scroll-y>
        <view
          class="history-item"
          v-for="item in coupleSpinHistory"
          :key="item.id"
        >
          <view class="history-content">
            <text class="history-text">{{ item.resultText }}</text>
            <text class="history-partner">与 {{ item.partnerNickname }}</text>
          </view>
          <text class="history-time">{{ formatTime(item.spinTime) }}</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import * as coupleApi from '@/api/couple'

const coupleInfo = ref<coupleApi.CoupleInfo | null>(null)
const inviteCode = ref('')
const partnerPhone = ref('')
const inviteData = ref<any>({})
const showInvitePopup = ref(false)
const coupleSpinHistory = ref<any[]>([])

// 加载状态
const isInviting = ref(false)
const isAccepting = ref(false)

// 邀请码验证状态
const isCodeValid = ref(true)
const codeValidationMsg = ref('')
const validationTimer = ref<any>(null)

// 计算属性：验证手机号格式
const isValidPhone = computed(() => {
  return /^1[3-9]\d{9}$/.test(partnerPhone.value)
})

// 页面加载
onMounted(() => {
  loadCoupleInfo()
})

// 加载情侣信息
async function loadCoupleInfo() {
  try {
    const res = await coupleApi.getCoupleInfo()

    if (res.code === 200 && res.data) {
      coupleInfo.value = res.data

      // 如果已配对，加载情侣转盘记录
      if (coupleInfo.value) {
        loadCoupleSpinHistory()
      }
    }
  } catch (error) {
    console.error('获取情侣信息失败:', error)
  }
}

// 通过手机号发送邀请
async function sendInvite() {
  if (!isValidPhone.value) {
    uni.showToast({
      title: '请输入正确的手机号',
      icon: 'none'
    })
    return
  }

  isInviting.value = true

  try {
    const res = await coupleApi.inviteCouple({
      partnerPhoneNumber: partnerPhone.value
    })

    if (res.code === 200 && res.data) {
      inviteData.value = res.data
      showInvitePopup.value = true
      
      uni.showToast({
        title: '邀请已发送',
        icon: 'success'
      })
    } else {
      uni.showToast({
        title: res.message || '邀请发送失败',
        icon: 'none'
      })
    }
  } catch (error: any) {
    uni.showToast({
      title: error.message || '邀请发送失败',
      icon: 'none'
    })
  } finally {
    isInviting.value = false
  }
}

// 验证邀请码
async function validateCode() {
  const code = inviteCode.value.trim()
  
  if (!code) {
    isCodeValid.value = true
    codeValidationMsg.value = ''
    return
  }

  // 清除之前的定时器
  if (validationTimer.value) {
    clearTimeout(validationTimer.value)
  }

  // 延迟验证，避免频繁请求
  validationTimer.value = setTimeout(async () => {
    try {
      const res = await coupleApi.validateInviteCode(code)
      
      if (res.code === 200) {
        isCodeValid.value = res.data === true
        codeValidationMsg.value = res.data ? '邀请码有效' : '邀请码无效或已过期'
      } else {
        isCodeValid.value = false
        codeValidationMsg.value = res.message || '验证失败'
      }
    } catch (error: any) {
      isCodeValid.value = false
      codeValidationMsg.value = error.message || '验证失败'
    }
  }, 500)
}

// 接受邀请
async function handleAcceptInvite() {
  const code = inviteCode.value.trim()
  
  if (!code) {
    uni.showToast({
      title: '请输入邀请码',
      icon: 'none'
    })
    return
  }

  isAccepting.value = true

  try {
    const res = await coupleApi.acceptInvite(code)

    if (res.code === 200) {
      uni.showToast({
        title: '配对成功',
        icon: 'success'
      })

      // 重新加载情侣信息
      await loadCoupleInfo()

      // 清空输入
      inviteCode.value = ''
      codeValidationMsg.value = ''
    } else {
      uni.showToast({
        title: res.message || '配对失败',
        icon: 'none'
      })
    }
  } catch (error: any) {
    uni.showToast({
      title: error.message || '配对失败',
      icon: 'none'
    })
  } finally {
    isAccepting.value = false
  }
}

// 复制邀请码
function copyInviteCode() {
  uni.setClipboardData({
    data: inviteData.value.inviteCode,
    success: () => {
      uni.showToast({
        title: '已复制',
        icon: 'success'
      })
    }
  })
}

// 显示解除关系确认
function showBreakConfirm() {
  uni.showModal({
    title: '提示',
    content: '确定要解除情侣关系吗？此操作不可恢复！',
    success: async (res) => {
      if (res.confirm) {
        await breakCouple()
      }
    }
  })
}

// 解除关系
async function breakCouple() {
  try {
    const res = await coupleApi.unbindCouple()

    if (res.code === 200) {
      uni.showToast({
        title: '已解除关系',
        icon: 'success'
      })

      // 清空情侣信息
      coupleInfo.value = null
      coupleSpinHistory.value = []
    } else {
      uni.showToast({
        title: res.message || '解除失败',
        icon: 'none'
      })
    }
  } catch (error: any) {
    uni.showToast({
      title: error.message || '解除失败',
      icon: 'none'
    })
  }
}

// 加载情侣转盘记录
async function loadCoupleSpinHistory() {
  try {
    const res = await coupleApi.getCoupleSpinHistory({ page: 1, pageSize: 20 })

    if (res.code === 200 && res.data) {
      coupleSpinHistory.value = res.data.list || []
    }
  } catch (error) {
    console.error('获取情侣转盘记录失败:', error)
  }
}

// 格式化日期
function formatDate(dateStr: string) {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 格式化日期时间
function formatDateTime(dateTimeStr: string) {
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 格式化时间
function formatTime(timeStr: string) {
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 60 * 1000) {
    return '刚刚'
  } else if (diff < 60 * 60 * 1000) {
    return `${Math.floor(diff / 60 / 1000)}分钟前`
  } else if (diff < 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / 60 / 60 / 1000)}小时前`
  } else {
    return date.toLocaleDateString()
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF0F5 0%, #FFFFFF 100%);
  padding: 20rpx;
}

.couple-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 40rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
  margin-bottom: 30rpx;
}

.couple-avatar {
  position: relative;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 4rpx solid #FF69B4;
}

.avatar-placeholder {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  font-weight: bold;
  color: #fff;
  border: 4rpx solid #FF69B4;
}

.couple-info {
  flex: 1;
  margin-left: 30rpx;
}

.couple-name {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}

.couple-status {
  display: block;
  font-size: 28rpx;
  color: #FF1493;
  margin-bottom: 5rpx;
}

.couple-date {
  display: block;
  font-size: 24rpx;
  color: #999;
}

.break-btn {
  width: 120rpx;
  height: 60rpx;
  background: #DC143C;
  color: white;
  border: none;
  border-radius: 30rpx;
  font-size: 26rpx;
}

.invite-section {
  margin-bottom: 30rpx;
}

.invite-card {
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  border-radius: 20rpx;
  padding: 60rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 8rpx 20rpx rgba(255, 105, 180, 0.3);
  margin-bottom: 30rpx;
}

.invite-icon {
  width: 120rpx;
  height: 120rpx;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  margin-bottom: 30rpx;
}

.invite-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 15rpx;
}

.invite-desc {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
  text-align: center;
  margin-bottom: 40rpx;
}

.create-invite-btn {
  width: 300rpx;
  height: 80rpx;
  background: #fff;
  color: #FF1493;
  border: none;
  border-radius: 40rpx;
  font-size: 30rpx;
  font-weight: bold;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.1);
}

.phone-input-group {
  width: 100%;
  display: flex;
  gap: 20rpx;
  margin-top: 20rpx;
}

.phone-input {
  flex: 1;
  height: 80rpx;
  padding: 0 30rpx;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 40rpx;
  font-size: 30rpx;
  color: #333;
}

.invite-btn {
  width: 200rpx;
  height: 80rpx;
  background: #fff;
  color: #FF1493;
  border: none;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: bold;
}

.invite-btn:disabled {
  opacity: 0.6;
}

.validation-msg {
  display: block;
  font-size: 24rpx;
  color: #32CD32;
  margin-top: 15rpx;
  padding-left: 20rpx;
}

.validation-msg.error {
  color: #DC143C;
}

.couple-spins {
  display: block;
  font-size: 24rpx;
  color: #FF69B4;
  margin-top: 5rpx;
}

.input-section {
  background: #fff;
  border-radius: 20rpx;
  padding: 40rpx;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}

.input-title {
  font-size: 30rpx;
  color: #666;
  margin-bottom: 20rpx;
}

.input-group {
  display: flex;
  gap: 20rpx;
}

.code-input {
  flex: 1;
  height: 80rpx;
  padding: 0 20rpx;
  background: #f5f5f5;
  border-radius: 40rpx;
  font-size: 30rpx;
  text-align: center;
  color: #333;
}

.accept-btn {
  width: 200rpx;
  height: 80rpx;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  color: white;
  border: none;
  border-radius: 40rpx;
  font-size: 30rpx;
  font-weight: bold;
}

.accept-btn:disabled {
  opacity: 0.6;
}

.invite-content {
  padding: 20rpx 0;
}

.qr-code {
  display: flex;
  justify-content: center;
  margin-bottom: 30rpx;
}

.qr-image {
  width: 300rpx;
  height: 300rpx;
  background: #f5f5f5;
  border-radius: 10rpx;
}

.invite-code-display {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20rpx;
}

.code-label {
  font-size: 28rpx;
  color: #666;
  margin-right: 20rpx;
}

.code-value {
  font-size: 48rpx;
  font-weight: bold;
  color: #FF1493;
  margin-right: 20rpx;
}

.copy-btn {
  padding: 10rpx 30rpx;
  background: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 30rpx;
  font-size: 26rpx;
}

.expire-time {
  text-align: center;
  font-size: 24rpx;
  color: #999;
}

.history-section {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.history-list {
  max-height: 500rpx;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  background: #FFF0F5;
  border-radius: 15rpx;
  margin-bottom: 15rpx;
}

.history-content {
  flex: 1;
  margin-right: 20rpx;
}

.history-text {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-partner {
  display: block;
  font-size: 24rpx;
  color: #999;
}

.history-time {
  font-size: 24rpx;
  color: #999;
  white-space: nowrap;
}
</style>
