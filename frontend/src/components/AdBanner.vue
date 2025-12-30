<template>
  <view class="ad-container" v-if="currentAd">
    <!-- 图片广告 -->
    <view
      v-if="currentAd.adType === AdType.IMAGE"
      class="ad-image-wrapper"
      @click="handleClick"
    >
      <image :src="currentAd.contentUrl" class="ad-image" mode="widthFix" />
    </view>

    <!-- 视频广告 -->
    <view
      v-else-if="currentAd.adType === AdType.VIDEO"
      class="ad-video-wrapper"
      @click="handleClick"
    >
      <video
        :src="currentAd.contentUrl"
        class="ad-video"
        :controls="false"
        :autoplay="true"
        :muted="true"
        :loop="true"
      />
    </view>

    <!-- 文字广告 -->
    <view
      v-else-if="currentAd.adType === AdType.TEXT"
      class="ad-text-wrapper"
      @click="handleClick"
    >
      <text class="ad-text">{{ currentAd.contentText }}</text>
    </view>

    <!-- 关闭按钮（可选） -->
    <view class="ad-close" v-if="closeable" @click.stop="handleClose">×</view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as adApi from '@/api/advertisement'
import type { AdVO } from '@/api/advertisement'
import { AdType, ImpressionType } from '@/api/advertisement'

interface Props {
  placementKey: string
  closeable?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  closeable: false
})

const currentAd = ref<AdVO | null>(null)

onMounted(async () => {
  await loadAd()
})

// 加载广告
async function loadAd() {
  try {
    const ads = await adApi.getAds(props.placementKey)
    if (ads && ads.length > 0) {
      currentAd.value = ads[0]
      // 记录展示
      await adApi.recordImpression(
        currentAd.value.id,
        props.placementKey,
        ImpressionType.VIEW
      )
    }
  } catch (error) {
    console.error('加载广告失败:', error)
    // 加载失败时不显示广告，不影响用户体验
    currentAd.value = null
  }
}

// 处理点击
async function handleClick() {
  if (!currentAd.value) return

  try {
    // 记录点击
    await adApi.recordImpression(
      currentAd.value.id,
      props.placementKey,
      ImpressionType.CLICK
    )

    // 跳转链接
    if (currentAd.value.linkUrl) {
      // 判断是内部页面还是外部链接
      if (currentAd.value.linkUrl.startsWith('/pages/')) {
        uni.navigateTo({
          url: currentAd.value.linkUrl
        })
      } else if (currentAd.value.linkUrl.startsWith('http')) {
        // 外部链接，在小程序中可能需要复制链接或使用web-view
        uni.setClipboardData({
          data: currentAd.value.linkUrl,
          success: () => {
            uni.showToast({
              title: '链接已复制',
              icon: 'success'
            })
          }
        })
      }
    }
  } catch (error) {
    console.error('记录广告点击失败:', error)
  }
}

// 处理关闭
function handleClose() {
  currentAd.value = null
}
</script>

<style scoped>
.ad-container {
  position: relative;
  width: 100%;
  background: #f5f5f5;
  overflow: hidden;
}

.ad-image-wrapper {
  width: 100%;
}

.ad-image {
  width: 100%;
  display: block;
}

.ad-video-wrapper {
  width: 100%;
  height: 200rpx;
}

.ad-video {
  width: 100%;
  height: 100%;
}

.ad-text-wrapper {
  padding: 20rpx 30rpx;
  background: linear-gradient(135deg, #FFE5B4, #FFDAB9);
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100rpx;
}

.ad-text {
  font-size: 28rpx;
  color: #666;
  text-align: center;
  line-height: 1.6;
}

.ad-close {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  width: 50rpx;
  height: 50rpx;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 40rpx;
  font-weight: 300;
  line-height: 1;
}
</style>
