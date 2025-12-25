<template>
  <view class="container">
    <!-- 加载中 -->
    <view v-if="loading" class="loading-state">
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 已确认关系 -->
    <view v-else-if="status?.status === 2" class="couple-card">
      <view class="couple-avatar">
        <image
          v-if="partnerInfo?.avatar"
          :src="partnerInfo?.avatar"
          class="avatar"
          mode="aspectFill"
        />
        <view class="avatar-placeholder" v-else>
          <text>{{ partnerInfo?.nickname?.[0] || '伴' }}</text>
        </view>
      </view>

      <view class="couple-info">
        <text class="couple-name">{{ partnerInfo?.nickname || '另一半' }}</text>
        <text class="couple-status">已配对</text>
        <text class="couple-date">配对日期：{{ formatDate(coupleInfo?.createdAt || '') }}</text>
        <text class="couple-spins">共同转盘：{{ coupleSpinHistory.length }}次</text>
      </view>

      <button class="break-btn" @click="showBreakConfirm">解除关系</button>
    </view>

    <!-- 待确认（已发出邀请） -->
    <view v-else-if="status?.status === 1" class="pending-section">
      <view class="invite-card pending-card">
        <view class="invite-icon pending-icon">
          <text class="icon-text">⏳</text>
        </view>
        <text class="invite-title">等待对方确认</text>
        <text class="invite-desc">已向 {{ status.partnerNickname || '对方' }} 发送邀请</text>
        
        <view v-if="status.inviteCode" class="invite-code-display small">
          <text class="code-label">邀请码</text>
          <text class="code-value">{{ status.inviteCode }}</text>
          <button class="copy-btn" @click="copyText(status.inviteCode || '')">复制</button>
        </view>

        <button class="cancel-btn" @click="showCancelInviteConfirm">取消邀请</button>
      </view>
    </view>

    <!-- 无关系/已解除 (显示邀请/接受) -->
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

    <!-- 邀请成功弹窗 -->
    <uni-popup ref="invitePopup" type="dialog">
      <uni-popup-dialog
        :show="showInvitePopup"
        title="邀请已发送"
        @close="closeInvitePopup"
        confirmText="知道了"
        @confirm="closeInvitePopup"
      >
        <view class="invite-content">
          <view class="invite-code-display">
            <text class="code-label">邀请码</text>
            <text class="code-value">{{ inviteResult?.inviteCode }}</text>
            <button class="copy-btn" @click="copyText(inviteResult?.inviteCode || '')">复制</button>
          </view>
          <view class="expire-time">
             <text>请通知对方使用此邀请码接受邀请</text>
          </view>
        </view>
      </uni-popup-dialog>
    </uni-popup>

    <!-- 情侣转盘记录 (仅已确认状态显示) -->
    <view v-if="status?.status === 2 && coupleSpinHistory.length > 0" class="history-section">
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
import { 
  inviteCouple, 
  acceptInvite, 
  getCoupleStatus, 
  getCoupleInfo, 
  unbindCouple, 
  validateInviteCode, 
  getCoupleSpinHistory 
} from '@/api/couple'
import type { 
  CoupleStatus, 
  CoupleInfo, 
  InviteResult 
} from '@/api/couple'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 状态数据
const loading = ref(true)
const status = ref<CoupleStatus | null>(null)
const coupleInfo = ref<CoupleInfo | null>(null)
const coupleSpinHistory = ref<any[]>([])

// 邀请/接受 表单数据
const partnerPhone = ref('')
const inviteCode = ref('')
const inviteResult = ref<InviteResult | null>(null)
const showInvitePopup = ref(false)

// 操作状态
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

// 计算属性：伴侣信息 (用于已确认状态)
const partnerInfo = computed(() => {
  if (!coupleInfo.value) return null
  
  const currentUserId = userStore.userInfo?.userId ?? userStore.userInfo?.id
  // 如果没有 currentUserId，默认无法区分，暂且返回对方信息（假设逻辑）
  // 实际上需要确保 userStore 已初始化
  if (!currentUserId) return { 
     nickname: coupleInfo.value.nickname2, 
     avatar: coupleInfo.value.avatarUrl2 
  }

  const isUser1 = coupleInfo.value.userId1 === currentUserId
  return {
    nickname: isUser1 ? coupleInfo.value.nickname2 : coupleInfo.value.nickname1,
    avatar: isUser1 ? coupleInfo.value.avatarUrl2 : coupleInfo.value.avatarUrl1
  }
})

// 页面加载
onMounted(() => {
  userStore.init()
  refreshStatus()
})

// 刷新状态 (核心状态机)
async function refreshStatus() {
  loading.value = true
  try {
    const res = await getCoupleStatus()
    status.value = res
    
    // 如果是已确认状态，加载详细信息和历史记录
    if (res.status === 2) {
      await Promise.all([
        loadCoupleInfo(),
        loadCoupleSpinHistory()
      ])
    } else {
      // 非确认状态，清空详细信息
      coupleInfo.value = null
      coupleSpinHistory.value = []
    }
  } catch (error) {
    console.error('获取状态失败:', error)
    // 失败时不强制弹窗，因为可能是网络波动，让用户留在界面上
  } finally {
    loading.value = false
  }
}

// 加载情侣详细信息
async function loadCoupleInfo() {
  try {
    const res = await getCoupleInfo()
    coupleInfo.value = res
  } catch (error) {
    console.error('获取情侣信息失败:', error)
  }
}

// 加载历史记录
async function loadCoupleSpinHistory() {
  try {
    const res = await getCoupleSpinHistory({ page: 1, pageSize: 20 })
    coupleSpinHistory.value = res.list || []
  } catch (error) {
    console.error('获取记录失败:', error)
  }
}

// 发送邀请
async function sendInvite() {
  if (!isValidPhone.value) return

  isInviting.value = true
  try {
    const res = await inviteCouple({
      partnerPhoneNumber: partnerPhone.value
    })
    inviteResult.value = res
    showInvitePopup.value = true
    
    // 邀请成功后，通常状态会变为 Pending (1)
    // 刷新一下状态
    await refreshStatus()
  } catch (error: any) {
    uni.showToast({
      title: error.message || '邀请失败',
      icon: 'none'
    })
  } finally {
    isInviting.value = false
  }
}

// 关闭邀请弹窗
function closeInvitePopup() {
  showInvitePopup.value = false
}

// 验证邀请码
function validateCode() {
  const code = inviteCode.value.trim()
  if (!code) {
    isCodeValid.value = true
    codeValidationMsg.value = ''
    return
  }
  if (validationTimer.value) clearTimeout(validationTimer.value)

  validationTimer.value = setTimeout(async () => {
    try {
      const res = await validateInviteCode(code)
      isCodeValid.value = res
      codeValidationMsg.value = res ? '邀请码有效' : '邀请码无效或已过期'
    } catch (error: any) {
      isCodeValid.value = false
      codeValidationMsg.value = error.message || '验证失败'
    }
  }, 500)
}

// 接受邀请
async function handleAcceptInvite() {
  const code = inviteCode.value.trim()
  if (!code) return

  isAccepting.value = true
  try {
    await acceptInvite(code)
    uni.showToast({ title: '配对成功', icon: 'success' })
    inviteCode.value = '' // 清空输入
    await refreshStatus()
  } catch (error: any) {
    uni.showToast({
      title: error.message || '配对失败',
      icon: 'none'
    })
  } finally {
    isAccepting.value = false
  }
}

// 复制文本
function copyText(text: string) {
  if (!text) return
  uni.setClipboardData({
    data: text,
    success: () => uni.showToast({ title: '已复制', icon: 'success' })
  })
}

// 显示解除关系确认
function showBreakConfirm() {
  uni.showModal({
    title: '解除关系',
    content: '确定要解除情侣关系吗？此操作不可恢复。',
    confirmColor: '#DC143C',
    success: async (res) => {
      if (res.confirm) {
        await executeUnbind()
      }
    }
  })
}

// 显示取消邀请确认 (Pending状态)
function showCancelInviteConfirm() {
  uni.showModal({
    title: '取消邀请',
    content: '确定要取消当前的邀请吗？',
    success: async (res) => {
      if (res.confirm) {
        await executeUnbind()
      }
    }
  })
}

// 执行解除绑定 (API同一个接口)
async function executeUnbind() {
  try {
    await unbindCouple()
    uni.showToast({ title: '操作成功', icon: 'success' })
    await refreshStatus() // 回到初始状态
  } catch (error: any) {
    uni.showToast({
      title: error.message || '操作失败',
      icon: 'none'
    })
  }
}

// 格式化日期
function formatDate(dateStr: string) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 格式化时间 (相对时间)
function formatTime(timeStr: string) {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 60 * 1000) return '刚刚'
  if (diff < 60 * 60 * 1000) return `${Math.floor(diff / 60 / 1000)}分钟前`
  if (diff < 24 * 60 * 60 * 1000) return `${Math.floor(diff / 60 / 60 / 1000)}小时前`
  return `${date.getMonth() + 1}-${date.getDate()}`
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF0F5 0%, #FFFFFF 100%);
  padding: 20rpx;
}

.loading-state {
  display: flex;
  justify-content: center;
  padding-top: 200rpx;
}
.loading-text {
  color: #999;
  font-size: 28rpx;
}

/* 共同卡片样式 */
.couple-card, .invite-card, .input-section {
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
  margin-bottom: 30rpx;
}

/* 已配对卡片 */
.couple-card {
  padding: 40rpx;
  display: flex;
  align-items: center;
}

.couple-avatar {
  position: relative;
  margin-right: 30rpx;
}

.avatar, .avatar-placeholder {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 4rpx solid #FF69B4;
}

.avatar-placeholder {
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  font-weight: bold;
  color: #fff;
}

.couple-info {
  flex: 1;
}

.couple-name {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 5rpx;
}

.couple-status {
  display: inline-block;
  font-size: 24rpx;
  color: #fff;
  background: #FF69B4;
  padding: 2rpx 12rpx;
  border-radius: 20rpx;
  margin-bottom: 8rpx;
}

.couple-date, .couple-spins {
  display: block;
  font-size: 24rpx;
  color: #999;
  line-height: 1.5;
}

.break-btn {
  background: #f5f5f5;
  color: #999;
  font-size: 24rpx;
  padding: 10rpx 20rpx;
  border-radius: 30rpx;
  margin-left: 20rpx;
  line-height: 1.5;
}

.break-btn::after { border: none; }

/* 邀请区域 */
.invite-card {
  padding: 60rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  color: #fff;
  box-shadow: 0 8rpx 20rpx rgba(255, 105, 180, 0.3);
}

/* Pending 状态特殊样式 */
.pending-card {
  background: #fff;
  color: #333;
  border: 4rpx dashed #FF69B4;
}

.invite-icon {
  width: 100rpx;
  height: 100rpx;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  margin-bottom: 30rpx;
}
.pending-icon {
  background: #FFF0F5;
  display: flex;
  align-items: center;
  justify-content: center;
}
.icon-text { font-size: 40rpx; }

.invite-title {
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 10rpx;
}

.invite-desc {
  font-size: 28rpx;
  opacity: 0.9;
  margin-bottom: 40rpx;
  text-align: center;
}
.pending-card .invite-desc { opacity: 0.6; }

.phone-input-group {
  width: 100%;
  display: flex;
  gap: 20rpx;
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
  line-height: 80rpx;
  background: #fff;
  color: #FF1493;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: bold;
}
.invite-btn::after { border: none; }
.invite-btn:disabled { opacity: 0.7; }

.cancel-btn {
  background: #f5f5f5;
  color: #999;
  font-size: 26rpx;
  padding: 10rpx 40rpx;
  border-radius: 30rpx;
  margin-top: 30rpx;
}
.cancel-btn::after { border: none; }

/* 邀请码输入区 */
.input-section {
  padding: 40rpx;
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
  padding: 0 30rpx;
  background: #f5f5f5;
  border-radius: 40rpx;
  font-size: 30rpx;
  color: #333;
}

.accept-btn {
  width: 200rpx;
  height: 80rpx;
  line-height: 80rpx;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  color: white;
  border-radius: 40rpx;
  font-size: 30rpx;
  font-weight: bold;
}
.accept-btn::after { border: none; }
.accept-btn:disabled { opacity: 0.6; background: #ccc; }

.validation-msg {
  display: block;
  font-size: 24rpx;
  color: #32CD32;
  margin-top: 15rpx;
  padding-left: 20rpx;
}
.validation-msg.error { color: #DC143C; }

/* 弹窗内容 */
.invite-content {
  padding: 20rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.invite-code-display {
  background: #F8F8F8;
  padding: 20rpx 40rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}
.pending-section .invite-code-display.small {
  padding: 10rpx 30rpx;
  margin-bottom: 10rpx;
}

.code-label {
  font-size: 28rpx;
  color: #666;
  margin-right: 20rpx;
}

.code-value {
  font-size: 40rpx;
  font-weight: bold;
  color: #FF1493;
  margin-right: 20rpx;
}

.copy-btn {
  font-size: 24rpx;
  padding: 4rpx 20rpx;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 20rpx;
  line-height: 1.5;
}
.copy-btn::after { border: none; }

.expire-time {
  font-size: 24rpx;
  color: #999;
  text-align: center;
}

/* 历史记录 */
.history-section {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
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
  margin-bottom: 4rpx;
}

.history-partner {
  display: block;
  font-size: 22rpx;
  color: #999;
}

.history-time {
  font-size: 22rpx;
  color: #bbb;
}
</style>
