<template>
  <view class="ticket-detail-page">
    <!-- 工单信息卡片 -->
    <view class="ticket-info" v-if="ticket">
      <view class="info-header">
        <view class="ticket-no">{{ ticket.ticketNo }}</view>
        <view class="ticket-status" :class="`status-${ticket.status}`">
          {{ getStatusText(ticket.status) }}
        </view>
      </view>
      <view class="info-title">{{ ticket.title }}</view>
      <view class="info-meta">
        <view class="meta-item">
          <text class="meta-label">优先级：</text>
          <text class="ticket-priority" :class="`priority-${ticket.priority}`">
            {{ getPriorityText(ticket.priority) }}
          </text>
        </view>
        <view class="meta-item" v-if="ticket.assignedStaffName">
          <text class="meta-label">客服：</text>
          <text>
            <text v-if="ticket.isVipDedicated" class="vip-tag">👑 </text>
            {{ ticket.assignedStaffName }}
          </text>
        </view>
        <view class="meta-item">
          <text class="meta-label">创建时间：</text>
          <text>{{ formatDateTime(ticket.createTime) }}</text>
        </view>
        <view class="meta-item" v-if="ticket.resolvedAt">
          <text class="meta-label">解决时间：</text>
          <text>{{ formatDateTime(ticket.resolvedAt) }}</text>
        </view>
      </view>
      <view class="info-description">
        <text class="desc-label">问题描述：</text>
        <text class="desc-content">{{ ticket.description }}</text>
      </view>
    </view>

    <!-- VIP专属横幅 -->
    <view class="vip-banner" v-if="ticket?.isVipDedicated">
      <text>👑 VIP专属客服为您服务</text>
    </view>

    <!-- 对话列表 -->
    <view class="replies-section">
      <view v-if="replies.length === 0" class="empty-replies">
        <text>暂无回复</text>
      </view>
      <view v-else class="replies-list" :style="{ paddingBottom: replyBoxHeight + 'px' }">
        <view
          v-for="reply in replies"
          :key="reply.id"
          class="reply-item"
          :class="{ staff: reply.isStaff }"
        >
          <view class="reply-sender">{{ reply.senderName }}</view>
          <view class="reply-bubble">
            <text>{{ reply.content }}</text>
          </view>
          <view class="reply-time">{{ formatDateTime(reply.createTime) }}</view>
        </view>
      </view>
    </view>

    <!-- 满意度评价 -->
    <view class="rating-section" v-if="showRating">
      <view class="rating-card">
        <view class="rating-title">工单已解决，请为本次服务评分</view>
        <view class="rating-stars">
          <text
            v-for="star in 5"
            :key="star"
            class="star"
            :class="{ active: star <= rating }"
            @tap="setRating(star)"
          >
            ★
          </text>
        </view>
        <view class="rating-comment">
          <textarea
            v-model="ratingComment"
            placeholder="请输入评价内容（可选）"
            maxlength="200"
          />
        </view>
        <button class="submit-rating-btn" @tap="submitRating" :loading="submittingRating">
          提交评价
        </button>
      </view>
    </view>

    <!-- 回复输入框 -->
    <view class="reply-box" v-if="canReply" ref="replyBoxRef">
      <view class="input-wrapper">
        <textarea
          v-model="replyContent"
          placeholder="输入您的回复内容..."
          :auto-height="true"
          :maxlength="500"
          class="reply-input"
        />
        <button class="send-btn" @tap="sendReply" :loading="sending">发送</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import * as supportApi from '@/api/support'
import type { TicketVO, ReplyVO, TicketStatus, TicketPriority } from '@/api/support'

const ticket = ref<TicketVO | null>(null)
const replies = ref<ReplyVO[]>([])
const replyContent = ref('')
const sending = ref(false)
const rating = ref(0)
const ratingComment = ref('')
const submittingRating = ref(false)
const replyBoxHeight = ref(100)

let ticketId: number

onMounted(async () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1] as any
  const options = currentPage.options || {}
  ticketId = Number(options.id)

  if (!ticketId) {
    uni.showToast({ title: '工单ID无效', icon: 'none' })
    setTimeout(() => uni.navigateBack(), 1500)
    return
  }

  await loadTicketDetail()
  await loadReplies()

  // 定时刷新
  startAutoRefresh()
})

let refreshTimer: number | null = null

const startAutoRefresh = () => {
  refreshTimer = setInterval(() => {
    loadReplies()
  }, 30000) as unknown as number
}

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})

const loadTicketDetail = async () => {
  try {
    ticket.value = await supportApi.getTicketDetail(ticketId)
  } catch (error: any) {
    uni.showToast({ title: error.message || '加载失败', icon: 'none' })
  }
}

const loadReplies = async () => {
  try {
    replies.value = await supportApi.getTicketReplies(ticketId)
  } catch (error: any) {
    console.error('加载回复失败:', error)
  }
}

const canReply = computed(() => {
  return ticket.value && ticket.value.status !== 5 // 已关闭
})

const showRating = computed(() => {
  return ticket.value && ticket.value.status === 4 && !ticket.value.rating // 已解决且未评价
})

const sendReply = async () => {
  if (!replyContent.value.trim()) {
    uni.showToast({ title: '请输入回复内容', icon: 'none' })
    return
  }

  sending.value = true
  try {
    await supportApi.replyTicket(ticketId, replyContent.value.trim())
    replyContent.value = ''
    await loadReplies()
    await loadTicketDetail()
    uni.showToast({ title: '回复成功', icon: 'success' })
  } catch (error: any) {
    uni.showToast({ title: error.message || '回复失败', icon: 'none' })
  } finally {
    sending.value = false
  }
}

const setRating = (star: number) => {
  rating.value = star
}

const submitRating = async () => {
  if (rating.value === 0) {
    uni.showToast({ title: '请选择评分', icon: 'none' })
    return
  }

  submittingRating.value = true
  try {
    await supportApi.rateTicket(ticketId, rating.value, ratingComment.value)
    uni.showToast({ title: '评价成功', icon: 'success' })
    await loadTicketDetail()
  } catch (error: any) {
    uni.showToast({ title: error.message || '评价失败', icon: 'none' })
  } finally {
    submittingRating.value = false
  }
}

const getStatusText = (status: TicketStatus): string => {
  const statusMap = {
    1: '待处理',
    2: '处理中',
    3: '待回复',
    4: '已解决',
    5: '已关闭'
  }
  return statusMap[status] || '未知'
}

const getPriorityText = (priority: TicketPriority): string => {
  const priorityMap = {
    1: '低',
    2: '中',
    3: '高',
    4: '紧急'
  }
  return priorityMap[priority] || ''
}

const formatDateTime = (timeStr: string): string => {
  const date = new Date(timeStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}
</script>

<style lang="scss" scoped>
.ticket-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 200rpx;
}

.ticket-info {
  background: white;
  margin: 30rpx;
  padding: 30rpx;
  border-radius: 20rpx;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.ticket-no {
  font-size: 24rpx;
  color: #999;
}

.ticket-status {
  padding: 8rpx 20rpx;
  border-radius: 50rpx;
  font-size: 24rpx;

  &.status-1 { background: #fff3e0; color: #f57c00; }
  &.status-2 { background: #e3f2fd; color: #1976d2; }
  &.status-3 { background: #fce4ec; color: #c2185b; }
  &.status-4 { background: #e8f5e9; color: #388e3c; }
  &.status-5 { background: #f5f5f5; color: #999; }
}

.info-title {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
}

.info-meta {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-bottom: 20rpx;
  font-size: 26rpx;
}

.meta-item {
  display: flex;
  align-items: center;
}

.meta-label {
  color: #999;
  margin-right: 8rpx;
}

.ticket-priority {
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
  font-size: 22rpx;

  &.priority-1 { background: #e0e0e0; color: #666; }
  &.priority-2 { background: #fff3e0; color: #f57c00; }
  &.priority-3 { background: #ffe0b2; color: #e65100; }
  &.priority-4 { background: #ffcdd2; color: #c62828; }
}

.vip-tag {
  font-size: 20rpx;
}

.info-description {
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f0f0;
}

.desc-label {
  display: block;
  font-size: 24rpx;
  color: #999;
  margin-bottom: 12rpx;
}

.desc-content {
  font-size: 28rpx;
  line-height: 1.6;
  color: #333;
}

.vip-banner {
  margin: 0 30rpx 20rpx 30rpx;
  padding: 20rpx 30rpx;
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8e53 100%);
  color: white;
  border-radius: 12rpx;
  text-align: center;
  font-size: 26rpx;
}

.replies-section {
  padding: 0 30rpx;
}

.empty-replies {
  text-align: center;
  padding: 60rpx 0;
  color: #999;
}

.replies-list {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.reply-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;

  &.staff {
    align-items: flex-end;

    .reply-bubble {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
    }
  }
}

.reply-sender {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 12rpx;
}

.reply-bubble {
  max-width: 70%;
  padding: 24rpx;
  background: white;
  border-radius: 16rpx;
  font-size: 28rpx;
  line-height: 1.5;
  word-wrap: break-word;
}

.reply-time {
  font-size: 22rpx;
  color: #bbb;
  margin-top: 8rpx;
}

.rating-section {
  margin: 30rpx;
}

.rating-card {
  background: white;
  padding: 40rpx;
  border-radius: 20rpx;
  text-align: center;
}

.rating-title {
  font-size: 30rpx;
  font-weight: bold;
  margin-bottom: 30rpx;
}

.rating-stars {
  display: flex;
  justify-content: center;
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.star {
  font-size: 60rpx;
  color: #e0e0e0;

  &.active {
    color: #ffc107;
  }
}

.rating-comment {
  margin-bottom: 30rpx;

  textarea {
    width: 100%;
    min-height: 150rpx;
    padding: 20rpx;
    background: #f5f5f5;
    border-radius: 12rpx;
    font-size: 28rpx;
    border: none;
  }
}

.submit-rating-btn {
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 28rpx 0;
  border-radius: 50rpx;
  font-size: 32rpx;
}

.reply-box {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  border-top: 1rpx solid #e0e0e0;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}

.input-wrapper {
  display: flex;
  gap: 20rpx;
  align-items: flex-end;
}

.reply-input {
  flex: 1;
  min-height: 80rpx;
  max-height: 200rpx;
  padding: 20rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  font-size: 28rpx;
  border: none;
}

.send-btn {
  flex-shrink: 0;
  padding: 20rpx 40rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12rpx;
  font-size: 28rpx;
}
</style>
