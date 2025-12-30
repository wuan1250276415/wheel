<template>
  <view class="theme-shop-page">
    <!-- 主预览区域 -->
    <view class="preview-container">
      <WheelCanvas v-if="previewTheme" :key="selectedThemeId" :theme="previewTheme" />
    </view>

    <!-- 页面内容 -->
    <view class="shop-content">
      <view class="header">
        <text class="title">主题商店</text>
        <text class="subtitle">选择你心仪的转盘外观</text>
      </view>

      <!-- 主题列表 -->
      <scroll-view class="theme-list-scroll" scroll-x="true">
        <view class="theme-list">
          <view
            v-for="theme in themeList"
            :key="theme.id"
            class="theme-item"
            :class="{ 'item-selected': theme.id === selectedThemeId }"
            @click="selectTheme(theme)"
          >
            <image class="theme-preview-img" :src="theme.previewUrl" mode="aspectFill" />
            <text class="theme-name">{{ theme.themeName }}</text>
            <view class="status-badge" :class="getThemeStatusClass(theme)">
              <text>{{ getThemeStatusText(theme) }}</text>
            </view>
          </view>
        </view>
      </scroll-view>

      <!-- 底部间距 -->
      <view style="height: 180rpx;"></view>
    </view>

    <!-- 底部操作按钮 -->
    <view class="action-footer">
      <button class="action-btn" :disabled="actionButtonDisabled" @click="handleAction">
        {{ actionButtonText }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import WheelCanvas from '@/components/WheelCanvas.vue'
import type { Theme } from '@/themes'
import * as themeApi from '@/api/theme'
import type { ThemeVO } from '@/api/theme'

// 状态
const themeList = ref<ThemeVO[]>([])
const selectedThemeId = ref<number>(0)
const currentThemeId = ref<number>(0)

// 计算当前选中的主题 - 从API数据解析
const previewTheme = computed((): Theme | null => {
  const selected = themeList.value.find(t => t.id === selectedThemeId.value)
  if (!selected) return null

  try {
    return {
      id: selected.themeKey,
      name: selected.themeName,
      previewImage: selected.previewUrl,
      price: selected.price,
      styles: JSON.parse(selected.themeConfig)
    }
  } catch (e) {
    console.error('Failed to parse theme config:', selected.themeConfig, e)
    return null
  }
})

// 获取主题状态文本
const getThemeStatusText = (theme: ThemeVO): string => {
  if (theme.active) return '使用中'
  if (theme.owned) return '已拥有'
  return theme.price === 0 ? '免费' : `${theme.price}积分`
}

// 获取主题状态样式类
const getThemeStatusClass = (theme: ThemeVO): string => {
  if (theme.active) return 'status-in-use'
  if (theme.owned) return 'status-owned'
  return 'status-for-sale'
}

// 操作按钮文本
const actionButtonText = computed(() => {
  const theme = themeList.value.find(t => t.id === selectedThemeId.value)
  if (!theme) return '加载中...'

  if (theme.active) return '已应用'
  if (theme.owned) return '应用主题'
  return theme.price === 0 ? '免费获取' : `购买 ${theme.price}积分`
})

// 操作按钮禁用状态
const actionButtonDisabled = computed(() => {
  const theme = themeList.value.find(t => t.id === selectedThemeId.value)
  return theme?.active || false
})

// 选择主题
function selectTheme(theme: ThemeVO) {
  selectedThemeId.value = theme.id
}

// 处理操作按钮点击
async function handleAction() {
  const theme = themeList.value.find(t => t.id === selectedThemeId.value)
  if (!theme) return

  try {
    if (theme.owned) {
      // 应用主题
      await themeApi.applyTheme(theme.id)
      uni.showToast({ title: '主题已应用', icon: 'success' })
      await loadThemes()
    } else {
      // 购买主题
      await themeApi.purchaseTheme(theme.id)
      await themeApi.applyTheme(theme.id)
      uni.showToast({ title: `成功获取 ${theme.themeName}`, icon: 'success' })
      await loadThemes()
    }
  } catch (error: any) {
    uni.showToast({
      title: error.message || '操作失败',
      icon: 'none'
    })
  }
}

// 加载主题列表
async function loadThemes() {
  try {
    const data = await themeApi.getAllThemes()
    themeList.value = data

    // 找到当前使用的主题
    const activeTheme = data.find(t => t.active)
    if (activeTheme) {
      currentThemeId.value = activeTheme.id
      selectedThemeId.value = activeTheme.id
    } else if (data.length > 0) {
      selectedThemeId.value = data[0].id
    }
  } catch (error: any) {
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
  }
}

// 页面加载时获取主题列表
onMounted(() => {
  loadThemes()
})
</script>

<style scoped>
.theme-shop-page {
  background-color: #f7f8fa;
  min-height: 100vh;
}

.preview-container {
  background: #e0e0e0;
  padding-bottom: 50rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 700rpx;
  background-image: linear-gradient(to top, #f7f8fa 0%, #e9edf2 100%);
}

.shop-content {
  padding: 0 40rpx;
}

.header {
  padding: 40rpx 0;
}

.title {
  font-size: 48rpx;
  font-weight: bold;
  color: #333;
}

.subtitle {
  font-size: 28rpx;
  color: #999;
  margin-top: 10rpx;
  display: block;
}

/* 主题列表样式 */
.theme-list-scroll {
  width: 100%;
  white-space: nowrap;
}

.theme-list {
  display: flex;
  gap: 24rpx;
  padding: 10rpx 0;
}

.theme-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  border: 4rpx solid transparent;
  border-radius: 24rpx;
  padding: 16rpx;
  background: #fff;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.05);
  transition: all 0.2s ease-in-out;
}

.theme-item.item-selected {
  border-color: #007aff;
  box-shadow: 0 8rpx 30rpx rgba(0, 122, 255, 0.2);
}

.theme-preview-img {
  width: 200rpx;
  height: 200rpx;
  border-radius: 16rpx;
  background-color: #eee;
}

.theme-name {
  font-size: 26rpx;
  color: #333;
  margin-top: 16rpx;
  font-weight: 500;
}

.status-badge {
  margin-top: 8rpx;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
}

.status-in-use {
  background-color: #007aff;
  color: white;
}

.status-owned {
  background-color: #e5e5ea;
  color: #8e8e93;
}

.status-for-sale {
  background-color: #fff2e8;
  color: #fa8c16;
}

/* 底部操作栏 */
.action-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  padding: 20rpx 40rpx;
  padding-bottom: calc(20rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #e5e5e5;
}

.action-btn {
  width: 100%;
  height: 90rpx;
  background: #007aff;
  color: white;
  border-radius: 45rpx;
  font-size: 32rpx;
  font-weight: 500;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: background 0.2s;
}

.action-btn:disabled {
  background: #c7c7cc;
  color: #f5f5f5;
}
</style>
