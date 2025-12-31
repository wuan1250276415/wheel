<template>
  <view v-if="visible" class="appeal-dialog">
    <view class="dialog-mask" @click="handleClose"></view>
    <view class="dialog-content">
      <view class="dialog-header">
        <text class="dialog-title">内容申诉</text>
        <text class="dialog-close" @click="handleClose">×</text>
      </view>

      <view class="dialog-body">
        <!-- 申诉说明 -->
        <view class="appeal-notice">
          <text class="notice-icon">ℹ️</text>
          <text class="notice-text">如果您认为内容被误判，可以提交申诉。我们将在1-3个工作日内处理。</text>
        </view>

        <!-- 申诉理由 -->
        <view class="form-section">
          <text class="section-title">申诉理由 <text class="required">*</text></text>
          <view class="reason-options">
            <view
              v-for="reason in appealReasons"
              :key="reason.value"
              class="reason-option"
              :class="{ active: selectedReason === reason.value }"
              @click="selectedReason = reason.value"
            >
              <text class="reason-icon">{{ reason.icon }}</text>
              <text class="reason-name">{{ reason.name }}</text>
            </view>
          </view>
        </view>

        <!-- 详细说明 -->
        <view class="form-section">
          <text class="section-title">详细说明 <text class="required">*</text></text>
          <textarea
            v-model="formData.appealReason"
            class="description-input"
            placeholder="请详细说明您的申诉理由，帮助我们更好地理解情况..."
            maxlength="500"
            :auto-height="true"
          />
          <text class="char-count">{{ formData.appealReason.length }}/500</text>
        </view>

        <!-- 补充信息 -->
        <view class="form-section">
          <text class="section-title">补充信息</text>
          <textarea
            v-model="formData.additionalInfo"
            class="description-input small"
            placeholder="如有其他需要说明的情况，请在此补充（选填）"
            maxlength="200"
            :auto-height="true"
          />
        </view>

        <!-- 证据上传 -->
        <view class="form-section">
          <text class="section-title">证据截图</text>
          <text class="section-hint">上传相关截图可以帮助我们更快处理（最多3张）</text>
          <view class="evidence-upload">
            <view
              v-for="(url, index) in formData.evidenceUrls"
              :key="index"
              class="evidence-item"
            >
              <image :src="url" class="evidence-image" mode="aspectFill" />
              <view class="remove-btn" @click="removeEvidence(index)">×</view>
            </view>
            <view
              v-if="formData.evidenceUrls.length < 3"
              class="upload-btn"
              @click="chooseImage"
            >
              <text class="upload-icon">+</text>
              <text class="upload-text">添加图片</text>
            </view>
          </view>
        </view>
      </view>

      <view class="dialog-footer">
        <button class="cancel-btn" @click="handleClose">取消</button>
        <button
          class="submit-btn"
          :disabled="!isFormValid || submitting"
          @click="handleSubmit"
        >
          {{ submitting ? '提交中...' : '提交申诉' }}
        </button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { submitContentAppeal, type ContentAppealRequest } from '@/api/content-appeal'

const props = defineProps<{
  visible: boolean
  contentId: number | string
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'success'): void
}>()

// 申诉理由选项
const appealReasons = [
  { value: 'misunderstanding', name: '内容被误解', icon: '🤔' },
  { value: 'context', name: '缺少上下文', icon: '📝' },
  { value: 'false_positive', name: '误判', icon: '❌' },
  { value: 'other', name: '其他原因', icon: '💬' }
]

// 表单数据
const selectedReason = ref<string | null>(null)
const formData = ref<{
  appealReason: string
  additionalInfo: string
  evidenceUrls: string[]
}>({
  appealReason: '',
  additionalInfo: '',
  evidenceUrls: []
})

const submitting = ref(false)

// 表单验证
const isFormValid = computed(() => {
  return selectedReason.value !== null && formData.value.appealReason.trim().length >= 10
})

// 监听visible变化，重置表单
watch(() => props.visible, (newVal) => {
  if (newVal) {
    selectedReason.value = null
    formData.value = {
      appealReason: '',
      additionalInfo: '',
      evidenceUrls: []
    }
  }
})

// 选择图片
function chooseImage() {
  uni.chooseImage({
    count: 3 - formData.value.evidenceUrls.length,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      for (const tempPath of res.tempFilePaths) {
        await uploadImage(tempPath)
      }
    }
  })
}

// 上传图片
async function uploadImage(tempPath: string) {
  try {
    uni.showLoading({ title: '上传中...' })
    
    const uploadResult = await new Promise<string>((resolve, reject) => {
      uni.uploadFile({
        url: '/api/upload/image',
        filePath: tempPath,
        name: 'file',
        success: (res) => {
          if (res.statusCode === 200) {
            const data = JSON.parse(res.data)
            if (data.code === 200 && data.data?.url) {
              resolve(data.data.url)
            } else {
              reject(new Error(data.message || '上传失败'))
            }
          } else {
            reject(new Error('上传失败'))
          }
        },
        fail: (err) => {
          reject(err)
        }
      })
    })
    
    formData.value.evidenceUrls.push(uploadResult)
  } catch (error: any) {
    uni.showToast({ title: error.message || '上传失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

// 移除证据图片
function removeEvidence(index: number) {
  formData.value.evidenceUrls.splice(index, 1)
}

// 关闭弹窗
function handleClose() {
  emit('close')
}

// 提交申诉
async function handleSubmit() {
  if (!isFormValid.value || submitting.value) return

  submitting.value = true
  try {
    // 组合申诉理由
    const reasonOption = appealReasons.find(r => r.value === selectedReason.value)
    const fullReason = `【${reasonOption?.name || '其他'}】${formData.value.appealReason}`

    const request: ContentAppealRequest = {
      contentId: Number(props.contentId),
      appealReason: fullReason,
      additionalInfo: formData.value.additionalInfo || undefined,
      evidenceUrls: formData.value.evidenceUrls.length > 0 ? formData.value.evidenceUrls : undefined
    }

    await submitContentAppeal(request)
    
    uni.showToast({ title: '申诉提交成功', icon: 'success' })
    emit('success')
    emit('close')
  } catch (error: any) {
    uni.showToast({ title: error.message || '申诉失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.appeal-dialog {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
}

.dialog-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
}

.dialog-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-radius: 30rpx 30rpx 0 0;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.dialog-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

.dialog-close {
  font-size: 50rpx;
  color: #999;
  line-height: 1;
}

.dialog-body {
  flex: 1;
  padding: 20rpx 30rpx;
  overflow-y: auto;
}

/* 申诉说明 */
.appeal-notice {
  display: flex;
  align-items: flex-start;
  background: #E3F2FD;
  border-radius: 15rpx;
  padding: 20rpx;
  margin-bottom: 30rpx;
}

.notice-icon {
  font-size: 32rpx;
  margin-right: 15rpx;
  flex-shrink: 0;
}

.notice-text {
  font-size: 26rpx;
  color: #1976D2;
  line-height: 1.6;
}

.form-section {
  margin-bottom: 30rpx;
}

.section-title {
  display: block;
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
  margin-bottom: 15rpx;
}

.section-hint {
  display: block;
  font-size: 24rpx;
  color: #999;
  margin-bottom: 15rpx;
}

.required {
  color: #DC143C;
}

/* 申诉理由选项 */
.reason-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15rpx;
}

.reason-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 25rpx 15rpx;
  background: #f5f5f5;
  border-radius: 15rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.reason-option.active {
  background: #E8F5E9;
  border-color: #4CAF50;
}

.reason-icon {
  font-size: 36rpx;
  margin-bottom: 10rpx;
}

.reason-name {
  font-size: 24rpx;
  color: #666;
  text-align: center;
}

.reason-option.active .reason-name {
  color: #4CAF50;
  font-weight: bold;
}

/* 详细描述 */
.description-input {
  width: 100%;
  min-height: 150rpx;
  padding: 20rpx;
  background: #f5f5f5;
  border-radius: 15rpx;
  font-size: 28rpx;
  color: #333;
  box-sizing: border-box;
}

.description-input.small {
  min-height: 100rpx;
}

.char-count {
  display: block;
  text-align: right;
  font-size: 24rpx;
  color: #999;
  margin-top: 10rpx;
}

/* 证据上传 */
.evidence-upload {
  display: flex;
  flex-wrap: wrap;
  gap: 15rpx;
}

.evidence-item {
  position: relative;
  width: 200rpx;
  height: 200rpx;
}

.evidence-image {
  width: 100%;
  height: 100%;
  border-radius: 15rpx;
}

.remove-btn {
  position: absolute;
  top: -15rpx;
  right: -15rpx;
  width: 40rpx;
  height: 40rpx;
  background: #DC143C;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  line-height: 1;
}

.upload-btn {
  width: 200rpx;
  height: 200rpx;
  background: #f5f5f5;
  border: 2rpx dashed #ccc;
  border-radius: 15rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.upload-icon {
  font-size: 60rpx;
  color: #ccc;
  line-height: 1;
}

.upload-text {
  font-size: 24rpx;
  color: #999;
  margin-top: 10rpx;
}

/* 底部按钮 */
.dialog-footer {
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
  background: linear-gradient(135deg, #4CAF50, #2E7D32);
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
