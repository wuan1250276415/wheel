<template>
  <view class="edit-page">
    <!-- 内容输入 -->
    <view class="content-section">
      <textarea
        v-model="formData.content"
        class="content-input"
        placeholder="分享此刻的心情..."
        maxlength="2000"
        :auto-height="true"
      />
      <view class="char-count">{{ formData.content.length }}/2000</view>
    </view>

    <!-- 图片上传 -->
    <view class="image-section">
      <view class="image-list">
        <view
          v-for="(image, index) in formData.images"
          :key="index"
          class="image-item"
        >
          <image :src="image" mode="aspectFill" class="image" />
          <view class="image-delete" @click="removeImage(index)">
            <text class="delete-icon">×</text>
          </view>
        </view>

        <view
          v-if="formData.images.length < 9"
          class="image-add"
          @click="chooseImage"
        >
          <text class="add-icon">+</text>
          <text class="add-text">添加图片</text>
        </view>
      </view>
      <view class="image-tip">最多上传9张图片</view>
    </view>

    <!-- 隐私设置 -->
    <view class="privacy-section">
      <view class="section-title">可见范围</view>
      <radio-group @change="onVisibilityChange">
        <label class="radio-item">
          <radio
            value="0"
            :checked="formData.visibility === 0"
            color="#007aff"
          />
          <text class="radio-text">仅情侣可见</text>
        </label>
        <label class="radio-item">
          <radio
            value="1"
            :checked="formData.visibility === 1"
            color="#007aff"
          />
          <text class="radio-text">公开</text>
        </label>
      </radio-group>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <button class="cancel-btn" @click="onCancel">取消</button>
      <button
        class="submit-btn"
        :class="{ disabled: !canSubmit }"
        :disabled="!canSubmit || submitting"
        @click="onSubmit"
      >
        {{ submitting ? '更新中...' : '更新' }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useMomentStore } from '@/stores/moment'
import { MomentVisibility } from '@/types/moment'

const momentStore = useMomentStore()

const momentId = ref<string | number>('')
const formData = ref({
  content: '',
  images: [] as string[],
  visibility: MomentVisibility.COUPLE_ONLY
})

const loading = ref(false)
const uploading = ref(false)
const submitting = computed(() => momentStore.isUpdating)

const canSubmit = computed(() => {
  return formData.value.content.trim().length > 0 && !uploading.value && !loading.value
})

onMounted(async () => {
  // 获取 URL 参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1] as any
  const options = currentPage.options || {}

  if (options.id) {
    momentId.value = options.id
    await loadMomentData()
  } else {
    uni.showToast({
      title: '参数错误',
      icon: 'none'
    })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  }
})

// 加载动态数据
async function loadMomentData() {
  loading.value = true
  try {
    const moment = await momentStore.getMomentDetail(momentId.value)
    if (moment) {
      formData.value.content = moment.content
      formData.value.images = moment.images || []
      formData.value.visibility = moment.visibility
    } else {
      uni.showToast({
        title: '动态不存在',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  } catch (error) {
    console.error('加载动态失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 选择图片
function chooseImage() {
  uni.chooseImage({
    count: 9 - formData.value.images.length,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const paths = Array.isArray(res.tempFilePaths) ? res.tempFilePaths : [res.tempFilePaths]
      await uploadImages(paths)
    },
    fail: (err) => {
      console.error('选择图片失败:', err)
      uni.showToast({
        title: '选择图片失败',
        icon: 'none'
      })
    }
  })
}

// 上传图片到服务器
async function uploadImages(tempFilePaths: string[]) {
  uploading.value = true
  uni.showLoading({
    title: '上传中...',
    mask: true
  })

  try {
    for (const tempPath of tempFilePaths) {
      const result = await new Promise<string>((resolve, reject) => {
        uni.uploadFile({
          url: `${import.meta.env.VITE_API_BASE_URL}/api/upload/image`,
          filePath: tempPath,
          name: 'file',
          header: {
            'Authorization': uni.getStorageSync('token') || ''
          },
          success: (uploadRes) => {
            if (uploadRes.statusCode === 200) {
              const response = JSON.parse(uploadRes.data)
              if (response.code === 0 && response.data) {
                resolve(response.data.url)
              } else {
                reject(new Error(response.message || '上传失败'))
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

      formData.value.images.push(result)
    }

    uni.hideLoading()
  } catch (error) {
    console.error('上传图片失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: '上传失败',
      icon: 'none'
    })
  } finally {
    uploading.value = false
  }
}

// 移除图片
function removeImage(index: number) {
  formData.value.images.splice(index, 1)
}

// 可见性变更
function onVisibilityChange(e: any) {
  formData.value.visibility = parseInt(e.detail.value)
}

// 取消
function onCancel() {
  uni.showModal({
    title: '提示',
    content: '确定要放弃修改吗？',
    success: (res) => {
      if (res.confirm) {
        uni.navigateBack()
      }
    }
  })
}

// 提交
async function onSubmit() {
  if (!canSubmit.value || submitting.value) {
    return
  }

  const success = await momentStore.updateMoment(momentId.value, {
    content: formData.value.content.trim(),
    images: formData.value.images,
    visibility: formData.value.visibility
  })

  if (success) {
    uni.navigateBack()
  }
}
</script>

<style scoped lang="scss">
.edit-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 120rpx;
}

.content-section {
  background-color: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;

  .content-input {
    width: 100%;
    min-height: 300rpx;
    font-size: 32rpx;
    line-height: 1.6;
    color: #333;
  }

  .char-count {
    text-align: right;
    font-size: 24rpx;
    color: #999;
    margin-top: 10rpx;
  }
}

.image-section {
  background-color: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;

  .image-list {
    display: flex;
    flex-wrap: wrap;
    gap: 20rpx;

    .image-item {
      position: relative;
      width: 220rpx;
      height: 220rpx;

      .image {
        width: 100%;
        height: 100%;
        border-radius: 10rpx;
      }

      .image-delete {
        position: absolute;
        top: -10rpx;
        right: -10rpx;
        width: 50rpx;
        height: 50rpx;
        background-color: rgba(0, 0, 0, 0.6);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;

        .delete-icon {
          color: #fff;
          font-size: 40rpx;
          line-height: 1;
        }
      }
    }

    .image-add {
      width: 220rpx;
      height: 220rpx;
      border: 2rpx dashed #ddd;
      border-radius: 10rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background-color: #fafafa;

      .add-icon {
        font-size: 60rpx;
        color: #999;
        line-height: 1;
        margin-bottom: 10rpx;
      }

      .add-text {
        font-size: 24rpx;
        color: #999;
      }
    }
  }

  .image-tip {
    font-size: 24rpx;
    color: #999;
    margin-top: 20rpx;
  }
}

.privacy-section {
  background-color: #fff;
  padding: 30rpx;

  .section-title {
    font-size: 28rpx;
    font-weight: bold;
    margin-bottom: 20rpx;
    color: #333;
  }

  .radio-item {
    display: flex;
    align-items: center;
    padding: 20rpx 0;

    .radio-text {
      margin-left: 20rpx;
      font-size: 28rpx;
      color: #333;
    }
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 20rpx;
  padding: 20rpx 30rpx;
  background-color: #fff;
  border-top: 1rpx solid #e5e5e5;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);

  .cancel-btn {
    flex: 1;
    height: 80rpx;
    line-height: 80rpx;
    background-color: #f5f5f5;
    color: #666;
    border: none;
    border-radius: 10rpx;
    font-size: 30rpx;
  }

  .submit-btn {
    flex: 2;
    height: 80rpx;
    line-height: 80rpx;
    background-color: #007aff;
    color: #fff;
    border: none;
    border-radius: 10rpx;
    font-size: 30rpx;

    &.disabled {
      background-color: #ccc;
      color: #999;
    }
  }
}
</style>
