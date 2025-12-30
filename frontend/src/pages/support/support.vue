<template>
  <view class="support-page">
    <!-- SVIP专属横幅 -->
    <view class="vip-banner" v-if="hasDedicatedSupport">
      <view class="banner-icon">👑</view>
      <view class="banner-content">
        <text class="banner-title">SVIP专属客服</text>
        <text class="banner-desc">您的工单将获得最高优先级处理</text>
      </view>
    </view>

    <!-- FAQ常见问题 -->
    <view class="faq-section">
      <view class="section-header">
        <text class="section-title">常见问题</text>
      </view>
      <view class="faq-list">
        <view
          v-for="faq in faqs"
          :key="faq.id"
          class="faq-item"
          @tap="toggleFaq(faq.id)"
        >
          <view class="faq-question">
            <text>{{ faq.question }}</text>
            <text class="expand-icon">{{ expandedFaqs.includes(faq.id) ? '▲' : '▼' }}</text>
          </view>
          <view v-if="expandedFaqs.includes(faq.id)" class="faq-answer">
            <text>{{ faq.answer }}</text>
            <view class="faq-helpful">
              <text>此回答有帮助吗？</text>
              <button
                class="helpful-btn"
                :class="{ active: faq.isHelpful }"
                @tap.stop="markHelpful(faq.id)"
              >
                👍 有帮助
              </button>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 我的工单列表 -->
    <view class="tickets-section">
      <view class="section-header">
        <text class="section-title">我的工单</text>
        <button class="create-btn" @tap="showCreateModal = true">
          + 创建工单
        </button>
      </view>

      <view v-if="!hasVipAccess" class="vip-tip">
        <text>创建工单需要VIP会员权限</text>
      </view>

      <view v-else-if="tickets.length === 0" class="empty-tickets">
        <text>暂无工单</text>
      </view>

      <view v-else class="tickets-list">
        <view
          v-for="ticket in tickets"
          :key="ticket.id"
          class="ticket-card"
          @tap="viewTicketDetail(ticket.id)"
        >
          <view class="ticket-header">
            <view class="ticket-no">{{ ticket.ticketNo }}</view>
            <view class="ticket-status" :class="`status-${ticket.status}`">
              {{ getStatusText(ticket.status) }}
            </view>
          </view>
          <view class="ticket-title">{{ ticket.title }}</view>
          <view class="ticket-meta">
            <view class="ticket-priority" :class="`priority-${ticket.priority}`">
              {{ getPriorityText(ticket.priority) }}
            </view>
            <view class="ticket-staff" v-if="ticket.assignedStaffName">
              <text v-if="ticket.isVipDedicated" class="vip-tag">👑</text>
              <text>{{ ticket.assignedStaffName }}</text>
            </view>
            <view class="ticket-time">{{ formatTime(ticket.createTime) }}</view>
          </view>
          <view v-if="ticket.unreadCount > 0" class="unread-badge">
            {{ ticket.unreadCount }} 条新回复
          </view>
        </view>
      </view>
    </view>

    <!-- 创建工单弹窗 -->
    <view class="create-modal" v-if="showCreateModal" @tap="closeCreateModal">
      <view class="modal-content" @tap.stop>
        <view class="modal-title">创建工单</view>
        <view class="form-group">
          <text class="form-label">工单标题</text>
          <input
            v-model="newTicket.title"
            class="form-input"
            placeholder="请简要描述您的问题"
            maxlength="50"
          />
        </view>
        <view class="form-group">
          <text class="form-label">问题分类</text>
          <picker :range="categories" @change="onCategoryChange">
            <view class="picker-view">
              {{ newTicket.category || '请选择分类' }}
            </view>
          </picker>
        </view>
        <view class="form-group">
          <text class="form-label">问题描述</text>
          <textarea
            v-model="newTicket.description"
            class="form-textarea"
            placeholder="请详细描述您遇到的问题"
            maxlength="500"
          />
        </view>
        <view class="modal-actions">
          <button class="cancel-btn" @tap="closeCreateModal">取消</button>
          <button class="submit-btn" @tap="submitTicket" :loading="submitting">提交</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useMembershipStore } from '@/stores/membership'
import * as supportApi from '@/api/support'
import type { TicketVO, FaqVO, TicketStatus, TicketPriority } from '@/api/support'

const userStore = useUserStore()
const membershipStore = useMembershipStore()

const faqs = ref<FaqVO[]>([])
const tickets = ref<TicketVO[]>([])
const expandedFaqs = ref<number[]>([])
const showCreateModal = ref(false)
const submitting = ref(false)

const newTicket = ref({
  title: '',
  category: '',
  description: ''
})

const categories = ['账号问题', '支付问题', '功能问题', '其他问题']

const hasVipAccess = computed(() => {
  return membershipStore.hasActiveMembership
})

const hasDedicatedSupport = computed(() => {
  return membershipStore.currentStatus?.tier === 2
})

onMounted(async () => {
  await Promise.all([
    loadFaqs(),
    loadTickets(),
    membershipStore.fetchStatus()
  ])
})

const loadFaqs = async () => {
  try {
    faqs.value = await supportApi.getFaqList()
  } catch (error: any) {
    console.error('加载FAQ失败:', error)
  }
}

const loadTickets = async () => {
  if (!hasVipAccess.value) return
  try {
    tickets.value = await supportApi.getMyTickets()
  } catch (error: any) {
    console.error('加载工单失败:', error)
  }
}

const toggleFaq = (id: number) => {
  const index = expandedFaqs.value.indexOf(id)
  if (index > -1) {
    expandedFaqs.value.splice(index, 1)
  } else {
    expandedFaqs.value.push(id)
  }
}

const markHelpful = async (id: number) => {
  try {
    await supportApi.markFaqHelpful(id)
    const faq = faqs.value.find(f => f.id === id)
    if (faq) {
      faq.isHelpful = true
    }
    uni.showToast({ title: '感谢您的反馈', icon: 'success' })
  } catch (error: any) {
    console.error('标记失败:', error)
  }
}

const onCategoryChange = (e: any) => {
  newTicket.value.category = categories[e.detail.value]
}

const closeCreateModal = () => {
  showCreateModal.value = false
  newTicket.value = {
    title: '',
    category: '',
    description: ''
  }
}

const submitTicket = async () => {
  if (!newTicket.value.title.trim()) {
    uni.showToast({ title: '请输入工单标题', icon: 'none' })
    return
  }
  if (!newTicket.value.category) {
    uni.showToast({ title: '请选择问题分类', icon: 'none' })
    return
  }
  if (!newTicket.value.description.trim()) {
    uni.showToast({ title: '请输入问题描述', icon: 'none' })
    return
  }

  submitting.value = true
  try {
    await supportApi.createTicket(newTicket.value)
    uni.showToast({ title: '工单创建成功', icon: 'success' })
    closeCreateModal()
    await loadTickets()
  } catch (error: any) {
    uni.showToast({ title: error.message || '创建失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

const viewTicketDetail = (id: number) => {
  uni.navigateTo({
    url: `/pages/support/ticket-detail?id=${id}`
  })
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

const formatTime = (timeStr: string): string => {
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))

  if (hours < 1) return '刚刚'
  if (hours < 24) return `${hours}小时前`

  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}天前`

  return date.toLocaleDateString('zh-CN')
}
</script>

<style lang="scss" scoped>
.support-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 40rpx;
}

.vip-banner {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8e53 100%);
  margin: 30rpx;
  padding: 30rpx;
  border-radius: 20rpx;
  color: white;
}

.banner-icon {
  font-size: 48rpx;
}

.banner-content {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.banner-title {
  font-size: 32rpx;
  font-weight: bold;
}

.banner-desc {
  font-size: 24rpx;
  opacity: 0.9;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
}

.create-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 16rpx 32rpx;
  border-radius: 50rpx;
  font-size: 28rpx;
}

.faq-section {
  margin-bottom: 40rpx;
}

.faq-list {
  padding: 0 30rpx;
}

.faq-item {
  background: white;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
}

.faq-question {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  font-size: 28rpx;
  font-weight: 500;
}

.expand-icon {
  font-size: 24rpx;
  color: #999;
}

.faq-answer {
  padding: 0 30rpx 30rpx 30rpx;
  border-top: 1rpx solid #f0f0f0;
  padding-top: 20rpx;
  color: #666;
  line-height: 1.6;
}

.faq-helpful {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx dashed #e0e0e0;
  font-size: 24rpx;
}

.helpful-btn {
  padding: 12rpx 24rpx;
  background: #f5f5f5;
  border: none;
  border-radius: 50rpx;
  font-size: 24rpx;
  color: #666;

  &.active {
    background: #e3f2fd;
    color: #1976d2;
  }
}

.tickets-section {
  margin-bottom: 40rpx;
}

.vip-tip {
  text-align: center;
  padding: 60rpx 30rpx;
  color: #999;
}

.empty-tickets {
  text-align: center;
  padding: 80rpx 30rpx;
  color: #999;
}

.tickets-list {
  padding: 0 30rpx;
}

.ticket-card {
  position: relative;
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.ticket-header {
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

.ticket-title {
  font-size: 30rpx;
  font-weight: 500;
  margin-bottom: 16rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ticket-meta {
  display: flex;
  align-items: center;
  gap: 20rpx;
  font-size: 24rpx;
  color: #999;
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

.ticket-staff {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.vip-tag {
  font-size: 20rpx;
}

.unread-badge {
  position: absolute;
  top: 30rpx;
  right: 30rpx;
  background: #ff6b6b;
  color: white;
  padding: 6rpx 16rpx;
  border-radius: 50rpx;
  font-size: 22rpx;
}

.create-modal {
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

.modal-content {
  width: 100%;
  background: white;
  border-radius: 40rpx 40rpx 0 0;
  padding: 40rpx;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-title {
  font-size: 36rpx;
  font-weight: bold;
  text-align: center;
  margin-bottom: 40rpx;
}

.form-group {
  margin-bottom: 30rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  margin-bottom: 16rpx;
  color: #333;
}

.form-input,
.picker-view {
  width: 100%;
  padding: 24rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  font-size: 28rpx;
  border: none;
}

.picker-view {
  color: #999;
}

.form-textarea {
  width: 100%;
  min-height: 200rpx;
  padding: 24rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  font-size: 28rpx;
  border: none;
}

.modal-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 40rpx;
}

.cancel-btn,
.submit-btn {
  flex: 1;
  padding: 28rpx 0;
  border-radius: 50rpx;
  font-size: 32rpx;
  border: none;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
}

.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}
</style>
