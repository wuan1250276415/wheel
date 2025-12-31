<template>
  <view class="blocked-page">
    <view class="blocked-card">
      <!-- 封禁图标 -->
      <view class="blocked-icon">🚫</view>
      
      <!-- 封禁标题 -->
      <text class="blocked-title">账号已被限制</text>
      
      <!-- 封禁信息 -->
      <view class="blocked-info">
        <view class="info-item">
          <text class="info-label">封禁原因：</text>
          <text class="info-value">{{ blockInfo.reason || '违反平台规定' }}</text>
        </view>
        
        <view class="info-item">
          <text class="info-label">封禁类型：</text>
          <text class="info-value">{{ blockTypeText }}</text>
        </view>
        
        <view v-if="blockInfo.expireAt" class="info-item">
          <text class="info-label">解封时间：</text>
          <text class="info-value">{{ formatExpireTime }}</text>
        </view>
        
        <view v-else class="info-item">
          <text class="info-label">封禁时长：</text>
          <text class="info-value warning">永久封禁</text>
        </view>
      </view>
      
      <!-- 提示信息 -->
      <view class="tips-section">
        <text class="tips-title">温馨提示</text>
        <view class="tips-list">
          <text class="tips-item">• 封禁期间无法发布内容、评论和互动</text>
          <text class="tips-item">• 如有异议，可提交申诉申请</text>
          <text class="tips-item">• 申诉将在1-3个工作日内处理</text>
        </view>
      </view>
      
      <!-- 操作按钮 -->
      <view class="action-buttons">
        <button 
          v-if="blockInfo.canAppeal" 
          class="appeal-btn" 
          @click="showAppealForm = true"
        >
          提交申诉
        </button>
        <button class="back-btn" @click="goBack">返回首页</button>
      </view>
    </view>
    
    <!-- 申诉表单弹窗 -->
    <view v-if="showAppealForm" class="appeal-modal">
      <view class="modal-mask" @click="showAppealForm = false"></view>
      <view class="modal-content">
        <view class="modal-header">
          <text class="modal-title">提交申诉</text>
          <text class="modal-close" @click="showAppealForm = false">×</text>
        </view>
        
        <view class="modal-body">
          <view class="form-item">
            <text class="form-label">申诉理由 <text class="required">*</text></text>
            <textarea
              v-model="appealReason"
              class="form-textarea"
              placeholder="请详细说明您的申诉理由..."
              maxlength="500"
              :auto-height="true"
            />
            <text class="char-count">{{ appealReason.length }}/500</text>
          </view>
          
          <view class="form-item">
            <text class="form-label">联系方式</text>
            <input
              v-model="contactInfo"
              class="form-input"
              placeholder="请留下您的联系方式（选填）"
              maxlength="100"
            />
          </view>
        </view>
        
        <view class="modal-footer">
          <button class="cancel-btn" @click="showAppealForm = false">取消</button>
          <button 
            class="submit-btn" 
            :disabled="!appealReason.trim() || submitting"
            @click="submitAppeal"
          >
            {{ submitting ? '提交中...' : '提交申诉' }}
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { submitBlacklistAppeal, type BlacklistAppealRequest } from '@/api/blacklist'

// 封禁信息
const blockInfo = ref<{
  id?: number
  type?: number
  reason?: string
  expireAt?: string
  canAppeal?: boolean
}>({})

// 申诉表单
const showAppealForm = ref(false)
const appealReason = ref('')
const contactInfo = ref('')
const submitting = ref(false)

// 封禁类型文本
const blockTypeText = computed(() => {
  switch (blockInfo.value.type) {
    case 1: return '用户封禁'
    case 2: return 'IP封禁'
    case 3: return '设备封禁'
    default: return '账号限制'
  }
})

// 格式化解封时间
const formatExpireTime = computed(() => {
  if (!blockInfo.value.expireAt) return '永久'
  const date = new Date(blockInfo.value.expireAt)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
})

onMounted(() => {
  // 从页面参数获取封禁信息
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1] as any
  const options = currentPage?.options || {}
  
  blockInfo.value = {
    id: options.id ? Number(options.id) : undefined,
    type: options.type ? Number(options.type) : 1,
    reason: options.reason ? decodeURIComponent(options.reason) : '违反平台规定',
    expireAt: options.expireAt ? decodeURIComponent(options.expireAt) : undefined,
    canAppeal: options.canAppeal !== 'false'
  }
})

// 提交申诉
async function submitAppeal() {
  if (!appealReason.value.trim() || submitting.value) return
  
  submitting.value = true
  try {
    const request: BlacklistAppealRequest = {
      blacklistId: blockInfo.value.id,
      appealReason: appealReason.value.trim(),
      contactInfo: contactInfo.value.trim() || undefined
    }
    
    await submitBlacklistAppeal(request)
    
    uni.showToast({ title: '申诉提交成功', icon: 'success' })
    showAppealForm.value = false
    appealReason.value = ''
    contactInfo.value = ''
    
    // 更新状态，不允许重复申诉
    blockInfo.value.canAppeal = false
  } catch (error: any) {
    uni.showToast({ title: error.message || '申诉提交失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

// 返回首页
function goBack() {
  uni.reLaunch({ url: '/pages/index/index' })
}
</script>

<style scoped>
.blocked-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFE4E1 0%, #FFF0F5 100%);
  padding: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.blocked-card {
  width: 100%;
  background: #fff;
  border-radius: 30rpx;
  padding: 60rpx 40rpx;
  box-shadow: 0 10rpx 40rpx rgba(220, 20, 60, 0.1);
}

.blocked-icon {
  text-align: center;
  font-size: 120rpx;
  margin-bottom: 30rpx;
}

.blocked-title {
  display: block;
  text-align: center;
  font-size: 40rpx;
  font-weight: bold;
  color: #DC143C;
  margin-bottom: 40rpx;
}

.blocked-info {
  background: #FFF5F5;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.info-item {
  display: flex;
  margin-bottom: 20rpx;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-label {
  font-size: 28rpx;
  color: #666;
  width: 160rpx;
  flex-shrink: 0;
}

.info-value {
  font-size: 28rpx;
  color: #333;
  flex: 1;
}

.info-value.warning {
  color: #DC143C;
  font-weight: bold;
}

.tips-section {
  background: #FFF8E1;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 40rpx;
}

.tips-title {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  color: #FF8C00;
  margin-bottom: 15rpx;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.tips-item {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.appeal-btn {
  width: 100%;
  height: 90rpx;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  color: #fff;
  border: none;
  border-radius: 45rpx;
  font-size: 32rpx;
  font-weight: bold;
}

.back-btn {
  width: 100%;
  height: 90rpx;
  background: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 45rpx;
  font-size: 32rpx;
}

/* 申诉弹窗 */
.appeal-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
}

.modal-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
}

.modal-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-radius: 30rpx 30rpx 0 0;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
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
  flex: 1;
  overflow-y: auto;
}

.form-item {
  margin-bottom: 30rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 15rpx;
}

.required {
  color: #DC143C;
}

.form-textarea {
  width: 100%;
  min-height: 200rpx;
  padding: 20rpx;
  background: #f5f5f5;
  border-radius: 15rpx;
  font-size: 28rpx;
  color: #333;
  box-sizing: border-box;
}

.form-input {
  width: 100%;
  height: 80rpx;
  padding: 0 20rpx;
  background: #f5f5f5;
  border-radius: 15rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.char-count {
  display: block;
  text-align: right;
  font-size: 24rpx;
  color: #999;
  margin-top: 10rpx;
}

.modal-footer {
  display: flex;
  gap: 20rpx;
  padding: 20rpx 30rpx 40rpx;
  border-top: 1rpx solid #f0f0f0;
}

.cancel-btn {
  flex: 1;
  height: 90rpx;
  background: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 45rpx;
  font-size: 30rpx;
}

.submit-btn {
  flex: 1;
  height: 90rpx;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  color: #fff;
  border: none;
  border-radius: 45rpx;
  font-size: 30rpx;
  font-weight: bold;
}

.submit-btn:disabled {
  opacity: 0.6;
}
</style>
