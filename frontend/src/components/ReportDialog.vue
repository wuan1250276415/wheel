<template>
  <view v-if="visible" class="report-dialog">
    <view class="dialog-mask" @click="handleClose"></view>
    <view class="dialog-content">
      <view class="dialog-header">
        <text class="dialog-title">举报内容</text>
        <text class="dialog-close" @click="handleClose">×</text>
      </view>

      <view class="dialog-body">
        <!-- 举报类型选择 -->
        <view class="form-section">
          <text class="section-title">举报类型</text>
          <view class="type-options">
            <view
              v-for="type in reportTypes"
              :key="type.value"
              class="type-option"
              :class="{ active: formData.reportType === type.value }"
              @click="formData.reportType = type.value"
            >
              <text class="type-icon">{{ type.icon }}</text>
              <text class="type-name">{{ type.name }}</text>
            </view>
          </view>
        </view>

        <!-- 举报原因选择 -->
        <view class="form-section">
          <text class="section-title">举报原因 <text class="required">*</text></text>
          <view class="reason-options">
            <view
              v-for="reason in reportReasons"
              :key="reason.value"
              class="reason-option"
              :class="{ active: formData.reportReason === reason.value }"
              @click="formData.reportReason = reason.value"
            >
              <text class="reason-icon">{{ reason.icon }}</text>
              <text class="reason-name">{{ reason.name }}</text>
            </view>
          </view>
        </view>

        <!-- 详细描述 -->
        <view class="form-section">
          <text class="section-title">详细描述</text>
          <textarea
            v-model="formData.description"
            class="description-input"
            placeholder="请详细描述举报原因（选填）"
            maxlength="500"
            :auto-height="true"
          />
          <text class="char-count">{{ formData.description.length }}/500</text>
        </view>

        <!-- 证据上传 -->
        <view class="form-section">
          <text class="section-title">证据截图</text>
          <text class="section-hint">上传截图可以帮助我们更快处理（最多3张）</text>
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
          {{ submitting ? '提交中...' : '提交举报' }}
        </button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { submitContentReport, type ContentReportRequest } from '@/api/content-report'

const props = defineProps<{
  visible: boolean
  contentId: number | string
  contentType?: number // 0-内容 1-用户 2-评论
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'success'): void
}>()

// 举报类型选项
const reportTypes = [
  { value: 0, name: '内容', icon: '📄' },
  { value: 1, name: '用户', icon: '👤' },
  { value: 2, name: '评论', icon: '💬' }
]

// 举报原因选项
const reportReasons = [
  { value: 0, name: '色情低俗', icon: '🔞' },
  { value: 1, name: '暴力血腥', icon: '⚠️' },
  { value: 2, name: '政治敏感', icon: '🚫' },
  { value: 3, name: '广告骚扰', icon: '📢' },
  { value: 4, name: '侵权内容', icon: '©️' },
  { value: 5, name: '其他', icon: '❓' }
]

// 表单数据
const formData = ref<{
  reportType: number
  reportReason: number | null
  description: string
  evidenceUrls: string[]
}>({
  reportType: props.contentType ?? 0,
  reportReason: null,
  description: '',
  evidenceUrls: []
})

const submitting = ref(false)

// 表单验证
const isFormValid = computed(() => {
  return formData.value.reportReason !== null
})

// 监听visible变化，重置表单
watch(() => props.visible, (newVal) => {
  if (newVal) {
    formData.value = {
      reportType: props.contentType ?? 0,
      reportReason: null,
      description: '',
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

// 提交举报
async function handleSubmit() {
  if (!isFormValid.value || submitting.value) return

  submitting.value = true
  try {
    const request: ContentReportRequest = {
      contentId: Number(props.contentId),
      reportType: formData.value.reportType,
      reportReason: formData.value.reportReason!,
      description: formData.value.description || undefined,
      evidenceUrls: formData.value.evidenceUrls.length > 0 ? formData.value.evidenceUrls : undefined
    }

    await submitContentReport(request)
    
    uni.showToast({ title: '举报提交成功', icon: 'success' })
    emit('success')
    emit('close')
  } catch (error: any) {
    uni.showToast({ title: error.message || '举报失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.report-dialog {
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
  max-height: 85vh;
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

/* 举报类型选项 */
.type-options {
  display: flex;
  gap: 15rpx;
}

.type-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx;
  background: #f5f5f5;
  border-radius: 15rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.type-option.active {
  background: #FFF0F5;
  border-color: #FF69B4;
}

.type-icon {
  font-size: 36rpx;
  margin-bottom: 8rpx;
}

.type-name {
  font-size: 24rpx;
  color: #666;
}

.type-option.active .type-name {
  color: #FF1493;
}

/* 举报原因选项 */
.reason-options {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15rpx;
}

.reason-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 10rpx;
  background: #f5f5f5;
  border-radius: 15rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.reason-option.active {
  background: #FFEBEE;
  border-color: #DC143C;
}

.reason-icon {
  font-size: 32rpx;
  margin-bottom: 8rpx;
}

.reason-name {
  font-size: 22rpx;
  color: #666;
  text-align: center;
}

.reason-option.active .reason-name {
  color: #DC143C;
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
  background: linear-gradient(135deg, #DC143C, #FF4500);
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
