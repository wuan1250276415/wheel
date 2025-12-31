<template>
  <view class="container">
    <!-- 顶部标签栏 -->
    <view class="tab-bar">
      <view
        class="tab-item"
        :class="{ active: activeTab === 'all' }"
        @click="switchTab('all')"
      >
        <text>全部</text>
        <text class="tab-count">{{ contentStore.myContents.length }}</text>
      </view>
      <view
        class="tab-item"
        :class="{ active: activeTab === 'pending' }"
        @click="switchTab('pending')"
      >
        <text>待审核</text>
        <text class="tab-count pending">{{ contentStore.pendingContents.length }}</text>
      </view>
      <view
        class="tab-item"
        :class="{ active: activeTab === 'approved' }"
        @click="switchTab('approved')"
      >
        <text>已通过</text>
        <text class="tab-count approved">{{ contentStore.approvedContents.length }}</text>
      </view>
      <view
        class="tab-item"
        :class="{ active: activeTab === 'rejected' }"
        @click="switchTab('rejected')"
      >
        <text>已拒绝</text>
        <text class="tab-count rejected">{{ contentStore.rejectedContents.length }}</text>
      </view>
    </view>

    <!-- 内容列表 -->
    <scroll-view
      class="content-list"
      scroll-y
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 空状态 -->
      <view v-if="filteredContents.length === 0 && !contentStore.loading" class="empty-state">
        <text class="empty-icon">📭</text>
        <text class="empty-text">暂无内容</text>
        <text class="empty-desc">点击下方按钮添加新内容</text>
      </view>

      <!-- 内容卡片 -->
      <view
        v-for="item in filteredContents"
        :key="item.id"
        class="content-card"
      >
        <view class="card-header">
          <view class="category-tag">{{ item.categoryName || '未分类' }}</view>
          <view class="status-tag" :class="getStatusClass(item.status)">
            {{ getStatusText(item.status) }}
          </view>
        </view>

        <view class="card-body">
          <text class="content-text">{{ item.contentText }}</text>
        </view>

        <view class="card-footer">
          <text class="time-text">{{ formatTime(item.createdAt) }}</text>
          <view class="action-btns">
            <!-- 已拒绝的内容可以申诉 -->
            <button
              v-if="item.status === 2"
              class="action-btn appeal-btn"
              @click="openAppealDialog(item)"
            >
              申诉
            </button>
            <!-- 只有待审核和已拒绝的内容可以编辑 -->
            <button
              v-if="item.status !== 1"
              class="action-btn edit-btn"
              @click="openEditModal(item)"
            >
              编辑
            </button>
            <button
              class="action-btn delete-btn"
              @click="confirmDelete(item)"
            >
              删除
            </button>
          </view>
        </view>
      </view>

      <!-- 加载更多 -->
      <view v-if="contentStore.loading" class="loading-more">
        <text>加载中...</text>
      </view>
      <view v-else-if="!contentStore.hasMore && filteredContents.length > 0" class="no-more">
        <text>没有更多内容了</text>
      </view>
    </scroll-view>

    <!-- 添加按钮 -->
    <view class="add-btn" @click="openAddModal">
      <text class="add-icon">+</text>
    </view>

    <!-- 添加/编辑弹窗 -->
    <view v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <view class="modal-content">
        <view class="modal-header">
          <text class="modal-title">{{ isEditing ? '编辑内容' : '添加内容' }}</text>
          <text class="modal-close" @click="closeModal">×</text>
        </view>

        <view class="modal-body">
          <!-- 内容输入 -->
          <view class="form-item">
            <text class="form-label">内容文本 <text class="required">*</text></text>
            <textarea
              v-model="formData.contentText"
              class="form-textarea"
              placeholder="请输入内容文本"
              maxlength="200"
              :auto-height="true"
            />
            <text class="char-count">{{ formData.contentText.length }}/200</text>
          </view>

          <!-- 分类选择 -->
          <view class="form-item">
            <text class="form-label">选择分类 <text class="required">*</text></text>
            <picker
              mode="selector"
              :range="categories"
              range-key="name"
              :value="selectedCategoryIndex"
              @change="onCategoryChange"
            >
              <view class="picker-value">
                {{ selectedCategoryName || '请选择分类' }}
                <text class="picker-arrow">▼</text>
              </view>
            </picker>
          </view>

          <!-- 权重设置 -->
          <view class="form-item">
            <text class="form-label">权重</text>
            <view class="weight-slider">
              <slider
                :value="formData.weight"
                min="1"
                max="10"
                step="1"
                activeColor="#FF69B4"
                @change="onWeightChange"
              />
              <text class="weight-value">{{ formData.weight }}</text>
            </view>
          </view>

          <!-- 时间敏感设置 -->
          <view class="form-item">
            <view class="switch-row">
              <text class="form-label">时间敏感</text>
              <switch
                :checked="formData.timeSensitive"
                color="#FF69B4"
                @change="onTimeSensitiveChange"
              />
            </view>
          </view>

          <!-- 时间范围（仅时间敏感时显示） -->
          <view v-if="formData.timeSensitive" class="form-item">
            <text class="form-label">有效时间范围</text>
            <view class="time-range">
              <picker
                mode="date"
                :value="formData.startTime"
                @change="onStartTimeChange"
              >
                <view class="time-picker">
                  {{ formData.startTime || '开始日期' }}
                </view>
              </picker>
              <text class="time-separator">至</text>
              <picker
                mode="date"
                :value="formData.endTime"
                @change="onEndTimeChange"
              >
                <view class="time-picker">
                  {{ formData.endTime || '结束日期' }}
                </view>
              </picker>
            </view>
          </view>
        </view>

        <view class="modal-footer">
          <button class="modal-btn cancel-btn" @click="closeModal">取消</button>
          <button
            class="modal-btn submit-btn"
            :disabled="isSubmitting"
            @click="submitForm"
          >
            {{ isSubmitting ? '提交中...' : '提交' }}
          </button>
        </view>
      </view>
    </view>

    <!-- 删除确认弹窗 -->
    <view v-if="showDeleteConfirm" class="modal-overlay" @click.self="cancelDelete">
      <view class="confirm-modal">
        <view class="confirm-icon">⚠️</view>
        <text class="confirm-title">确认删除</text>
        <text class="confirm-text">确定要删除这条内容吗？此操作不可恢复。</text>
        <view class="confirm-btns">
          <button class="confirm-btn cancel" @click="cancelDelete">取消</button>
          <button class="confirm-btn delete" @click="doDelete">删除</button>
        </view>
      </view>
    </view>

    <!-- 内容申诉弹窗 -->
    <ContentAppealDialog
      :visible="showAppealDialog"
      :content-id="appealContentId"
      @close="showAppealDialog = false"
      @success="onAppealSuccess"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useContentStore } from '@/stores/content'
import { useWheelStore } from '@/stores/wheel'
import type { WheelContent, ContentSubmitDTO } from '@/api/content'
import { membershipAPI } from '@/api/membership'
import type { MembershipStatusVO } from '@/types/api'
import ContentAppealDialog from '@/components/ContentAppealDialog.vue'

const contentStore = useContentStore()
const wheelStore = useWheelStore()

// 状态
const activeTab = ref<'all' | 'pending' | 'approved' | 'rejected'>('all')
const isRefreshing = ref(false)
const showAppealDialog = ref(false)
const appealContentId = ref<number | string>(0)
const showModal = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const isSubmitting = ref(false)
const showDeleteConfirm = ref(false)
const deleteTarget = ref<WheelContent | null>(null)
const membershipStatus = ref<MembershipStatusVO | null>(null)

// 表单数据
const formData = ref<ContentSubmitDTO>({
  contentText: '',
  categoryId: 0,
  weight: 5,
  timeSensitive: false,
  startTime: '',
  endTime: ''
})

// 分类数据
const categories = computed(() => wheelStore.categories)
const selectedCategoryIndex = computed(() => {
  if (!formData.value.categoryId) return -1
  return categories.value.findIndex(c => c.id === formData.value.categoryId)
})
const selectedCategoryName = computed(() => {
  const category = categories.value.find(c => c.id === formData.value.categoryId)
  return category?.name || ''
})

// 过滤后的内容列表
const filteredContents = computed(() => {
  switch (activeTab.value) {
    case 'pending':
      return contentStore.pendingContents
    case 'approved':
      return contentStore.approvedContents
    case 'rejected':
      return contentStore.rejectedContents
    default:
      return contentStore.myContents
  }
})

// 页面加载
onMounted(async () => {
  await loadData()
  await loadMembershipStatus()
})

// 加载会员状态
async function loadMembershipStatus() {
  try {
    membershipStatus.value = await membershipAPI.getMembershipStatus()
  } catch (error) {
    console.error('获取会员状态失败:', error)
  }
}

// 加载数据
async function loadData() {
  // 加载分类
  if (wheelStore.categories.length === 0) {
    await wheelStore.fetchCategories()
  }
  // 加载内容
  await contentStore.fetchMyContents(1, 10)
}

// 切换标签
function switchTab(tab: 'all' | 'pending' | 'approved' | 'rejected') {
  activeTab.value = tab
}

// 下拉刷新
async function onRefresh() {
  isRefreshing.value = true
  await contentStore.refresh()
  isRefreshing.value = false
}

// 加载更多
async function loadMore() {
  if (contentStore.hasMore && !contentStore.loading) {
    await contentStore.loadMore()
  }
}

// 获取状态样式类
function getStatusClass(status: number): string {
  switch (status) {
    case 0: return 'status-pending'
    case 1: return 'status-approved'
    case 2: return 'status-rejected'
    default: return ''
  }
}

// 获取状态文本
function getStatusText(status: number): string {
  switch (status) {
    case 0: return '待审核'
    case 1: return '已通过'
    case 2: return '已拒绝'
    default: return '未知'
  }
}

// 格式化时间
function formatTime(timeStr: string): string {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 打开添加弹窗
function openAddModal() {
  isEditing.value = false
  editingId.value = null
  resetForm()
  showModal.value = true
}

// 打开编辑弹窗
function openEditModal(item: WheelContent) {
  isEditing.value = true
  editingId.value = item.id
  formData.value = {
    contentText: item.contentText,
    categoryId: item.categoryId,
    weight: item.weight || 5,
    timeSensitive: false,
    startTime: '',
    endTime: ''
  }
  showModal.value = true
}

// 关闭弹窗
function closeModal() {
  showModal.value = false
  resetForm()
}

// 重置表单
function resetForm() {
  formData.value = {
    contentText: '',
    categoryId: 0,
    weight: 5,
    timeSensitive: false,
    startTime: '',
    endTime: ''
  }
}

// 分类选择变化
function onCategoryChange(e: any) {
  const index = e.detail.value
  if (index >= 0 && index < categories.value.length) {
    formData.value.categoryId = categories.value[index].id
  }
}

// 权重变化
function onWeightChange(e: any) {
  formData.value.weight = e.detail.value
}

// 时间敏感开关变化
function onTimeSensitiveChange(e: any) {
  formData.value.timeSensitive = e.detail.value
  if (!e.detail.value) {
    formData.value.startTime = ''
    formData.value.endTime = ''
  }
}

// 开始时间变化
function onStartTimeChange(e: any) {
  formData.value.startTime = e.detail.value
}

// 结束时间变化
function onEndTimeChange(e: any) {
  formData.value.endTime = e.detail.value
}

// 提交表单
async function submitForm() {
  // 验证
  if (!formData.value.contentText.trim()) {
    uni.showToast({ title: '请输入内容文本', icon: 'none' })
    return
  }
  if (!formData.value.categoryId) {
    uni.showToast({ title: '请选择分类', icon: 'none' })
    return
  }
  if (formData.value.timeSensitive) {
    if (!formData.value.startTime || !formData.value.endTime) {
      uni.showToast({ title: '请选择有效时间范围', icon: 'none' })
      return
    }
    if (formData.value.startTime > formData.value.endTime) {
      uni.showToast({ title: '开始时间不能晚于结束时间', icon: 'none' })
      return
    }
  }

  isSubmitting.value = true

  try {
    let res
    if (isEditing.value && editingId.value) {
      res = await contentStore.updateContent(editingId.value, formData.value)
    } else {
      res = await contentStore.submitContent(formData.value)
    }

    if (res.success) {
      // 根据会员等级显示不同的提示消息
      let successMessage = isEditing.value ? '更新成功' : '提交成功'

      if (!isEditing.value && membershipStatus.value) {
        const tier = membershipStatus.value.tier
        if (tier >= 2) {
          successMessage += '！SVIP用户，您的内容将被优先审核'
        } else if (tier >= 1) {
          successMessage += '！VIP用户，您的内容将获得优先处理'
        }
      }

      uni.showToast({
        title: successMessage,
        icon: 'success',
        duration: 2500
      })
      closeModal()
    } else {
      uni.showToast({
        title: res.message || '操作失败',
        icon: 'none'
      })
    }
  } finally {
    isSubmitting.value = false
  }
}

// 确认删除
function confirmDelete(item: WheelContent) {
  deleteTarget.value = item
  showDeleteConfirm.value = true
}

// 取消删除
function cancelDelete() {
  deleteTarget.value = null
  showDeleteConfirm.value = false
}

// 执行删除
async function doDelete() {
  if (!deleteTarget.value) return

  const res = await contentStore.deleteContent(deleteTarget.value.id)

  if (res.success) {
    uni.showToast({ title: '删除成功', icon: 'success' })
  } else {
    uni.showToast({ title: res.message || '删除失败', icon: 'none' })
  }

  cancelDelete()
}

// 打开申诉弹窗
function openAppealDialog(item: WheelContent) {
  appealContentId.value = item.id
  showAppealDialog.value = true
}

// 申诉成功回调
function onAppealSuccess() {
  uni.showToast({ title: '申诉已提交', icon: 'success' })
  // 刷新内容列表
  contentStore.refresh()
}
</script>


<style scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF0F5 0%, #FFFFFF 100%);
  display: flex;
  flex-direction: column;
}

/* 标签栏 */
.tab-bar {
  display: flex;
  background: #fff;
  padding: 20rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15rpx 0;
  border-radius: 10rpx;
  transition: all 0.3s;
}

.tab-item.active {
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  color: #fff;
}

.tab-item text:first-child {
  font-size: 28rpx;
  margin-bottom: 5rpx;
}

.tab-count {
  font-size: 24rpx;
  padding: 2rpx 12rpx;
  border-radius: 20rpx;
  background: rgba(0, 0, 0, 0.1);
}

.tab-item.active .tab-count {
  background: rgba(255, 255, 255, 0.3);
}

.tab-count.pending { color: #FFA500; }
.tab-count.approved { color: #32CD32; }
.tab-count.rejected { color: #DC143C; }

/* 内容列表 */
.content-list {
  flex: 1;
  padding: 20rpx;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
}

.empty-icon {
  font-size: 100rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 32rpx;
  color: #333;
  margin-bottom: 10rpx;
}

.empty-desc {
  font-size: 26rpx;
  color: #999;
}

/* 内容卡片 */
.content-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.category-tag {
  font-size: 24rpx;
  color: #FF69B4;
  background: #FFF0F5;
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
}

.status-tag {
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
}

.status-pending {
  color: #FFA500;
  background: #FFF8E1;
}

.status-approved {
  color: #32CD32;
  background: #E8F5E9;
}

.status-rejected {
  color: #DC143C;
  background: #FFEBEE;
}

.card-body {
  margin-bottom: 20rpx;
}

.content-text {
  font-size: 30rpx;
  color: #333;
  line-height: 1.6;
  word-break: break-all;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1rpx solid #f0f0f0;
  padding-top: 20rpx;
}

.time-text {
  font-size: 24rpx;
  color: #999;
}

.action-btns {
  display: flex;
  gap: 15rpx;
}

.action-btn {
  font-size: 24rpx;
  padding: 10rpx 25rpx;
  border-radius: 20rpx;
  border: none;
  line-height: 1.5;
}

.edit-btn {
  background: #FFF0F5;
  color: #FF69B4;
}

.delete-btn {
  background: #FFEBEE;
  color: #DC143C;
}

.appeal-btn {
  background: #E8F5E9;
  color: #4CAF50;
}

/* 加载更多 */
.loading-more,
.no-more {
  text-align: center;
  padding: 30rpx;
  color: #999;
  font-size: 26rpx;
}

/* 添加按钮 */
.add-btn {
  position: fixed;
  right: 40rpx;
  bottom: 100rpx;
  width: 100rpx;
  height: 100rpx;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 20rpx rgba(255, 105, 180, 0.4);
}

.add-icon {
  font-size: 60rpx;
  color: #fff;
  font-weight: 300;
}

/* 弹窗遮罩 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

/* 弹窗内容 */
.modal-content {
  width: 90%;
  max-height: 80vh;
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.modal-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

.modal-close {
  font-size: 50rpx;
  color: #999;
  line-height: 1;
}

.modal-body {
  padding: 30rpx;
  max-height: 60vh;
  overflow-y: auto;
}

.form-item {
  margin-bottom: 30rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #666;
  margin-bottom: 15rpx;
}

.required {
  color: #DC143C;
}

.form-textarea {
  width: 100%;
  min-height: 150rpx;
  padding: 20rpx;
  background: #f5f5f5;
  border-radius: 10rpx;
  font-size: 28rpx;
  color: #333;
  box-sizing: border-box;
}

.char-count {
  display: block;
  text-align: right;
  font-size: 24rpx;
  color: #999;
  margin-top: 10rpx;
}

.picker-value {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  background: #f5f5f5;
  border-radius: 10rpx;
  font-size: 28rpx;
  color: #333;
}

.picker-arrow {
  font-size: 24rpx;
  color: #999;
}

.weight-slider {
  display: flex;
  align-items: center;
}

.weight-slider slider {
  flex: 1;
}

.weight-value {
  width: 60rpx;
  text-align: center;
  font-size: 32rpx;
  color: #FF1493;
  font-weight: bold;
}

.switch-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.time-range {
  display: flex;
  align-items: center;
}

.time-picker {
  flex: 1;
  padding: 20rpx;
  background: #f5f5f5;
  border-radius: 10rpx;
  font-size: 28rpx;
  color: #333;
  text-align: center;
}

.time-separator {
  padding: 0 20rpx;
  color: #999;
}

.modal-footer {
  display: flex;
  padding: 30rpx;
  border-top: 1rpx solid #f0f0f0;
  gap: 20rpx;
}

.modal-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 40rpx;
  font-size: 30rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
}

.submit-btn {
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  color: #fff;
}

.submit-btn[disabled] {
  opacity: 0.6;
}

/* 删除确认弹窗 */
.confirm-modal {
  width: 80%;
  background: #fff;
  border-radius: 20rpx;
  padding: 40rpx;
  text-align: center;
}

.confirm-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.confirm-title {
  display: block;
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 15rpx;
}

.confirm-text {
  display: block;
  font-size: 28rpx;
  color: #666;
  margin-bottom: 40rpx;
}

.confirm-btns {
  display: flex;
  gap: 20rpx;
}

.confirm-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 40rpx;
  font-size: 30rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.confirm-btn.cancel {
  background: #f5f5f5;
  color: #666;
}

.confirm-btn.delete {
  background: #DC143C;
  color: #fff;
}
</style>
