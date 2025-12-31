<template>
  <view class="survey-container">
    <!-- 标题区域 -->
    <view class="survey-header">
      <text class="survey-title">选择你感兴趣的分类</text>
      <text class="survey-subtitle">帮助我们为你推荐更合适的内容（最多选择5个）</text>
    </view>

    <!-- 分类选择区域 -->
    <view class="category-grid">
      <view
        v-for="category in categories"
        :key="category.id"
        class="category-item"
        :class="{ 'category-selected': isSelected(category.id) }"
        @click="toggleCategory(category.id)"
      >
        <view class="category-icon" :style="{ background: category.themeColor || defaultGradient }">
          <text class="icon-text">{{ getCategoryIcon(category) }}</text>
        </view>
        <text class="category-name">{{ category.categoryName }}</text>
        <view class="check-mark" v-if="isSelected(category.id)">
          <text>✓</text>
        </view>
      </view>
    </view>

    <!-- 已选择提示 -->
    <view class="selection-hint" v-if="selectedIds.length > 0">
      <text>已选择 {{ selectedIds.length }}/5 个分类</text>
    </view>

    <!-- 操作按钮 -->
    <view class="survey-actions">
      <button
        class="submit-btn"
        :class="{ 'btn-disabled': selectedIds.length === 0 }"
        :disabled="selectedIds.length === 0 || isSubmitting"
        @click="handleSubmit"
      >
        <text v-if="isSubmitting">提交中...</text>
        <text v-else>完成选择</text>
      </button>
      
      <view class="skip-link" @click="handleSkip">
        <text>跳过，稍后设置</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCategories, type WheelCategory } from '@/api/wheel'
import { useRecommendationStore } from '@/stores/recommendation'

const emit = defineEmits<{
  (e: 'complete'): void
  (e: 'skip'): void
}>()

const recommendationStore = useRecommendationStore()

// 状态
const categories = ref<WheelCategory[]>([])
const selectedIds = ref<number[]>([])
const isLoading = ref(false)
const isSubmitting = ref(false)

// 默认渐变色
const defaultGradient = 'linear-gradient(135deg, #FF69B4, #FF1493)'

// 分类图标映射
const categoryIcons: Record<string, string> = {
  '浪漫': '💕',
  '冒险': '🎯',
  '美食': '🍜',
  '运动': '⚽',
  '电影': '🎬',
  '音乐': '🎵',
  '旅行': '✈️',
  '游戏': '🎮',
  '学习': '📚',
  '休闲': '☕',
  '约会': '💑',
  '惊喜': '🎁'
}

// 加载分类
onMounted(async () => {
  await loadCategories()
})

async function loadCategories() {
  isLoading.value = true
  try {
    const data = await getCategories()
    categories.value = data.filter(c => c.status)
  } catch (error) {
    console.error('加载分类失败:', error)
    uni.showToast({
      title: '加载分类失败',
      icon: 'none'
    })
  } finally {
    isLoading.value = false
  }
}

// 获取分类图标
function getCategoryIcon(category: WheelCategory): string {
  // 尝试从名称匹配图标
  for (const [key, icon] of Object.entries(categoryIcons)) {
    if (category.categoryName.includes(key)) {
      return icon
    }
  }
  // 默认图标
  return '🎯'
}

// 检查是否已选择
function isSelected(categoryId: number): boolean {
  return selectedIds.value.includes(categoryId)
}

// 切换分类选择
function toggleCategory(categoryId: number) {
  const index = selectedIds.value.indexOf(categoryId)
  if (index > -1) {
    // 取消选择
    selectedIds.value.splice(index, 1)
  } else {
    // 添加选择（最多5个）
    if (selectedIds.value.length >= 5) {
      uni.showToast({
        title: '最多选择5个分类',
        icon: 'none'
      })
      return
    }
    selectedIds.value.push(categoryId)
  }
}

// 提交选择
async function handleSubmit() {
  if (selectedIds.value.length === 0) {
    uni.showToast({
      title: '请至少选择一个分类',
      icon: 'none'
    })
    return
  }

  isSubmitting.value = true
  try {
    const result = await recommendationStore.submitPreferenceSurvey(selectedIds.value)
    if (result.success) {
      uni.showToast({
        title: '设置成功',
        icon: 'success'
      })
      emit('complete')
    } else {
      uni.showToast({
        title: result.message || '提交失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('提交偏好失败:', error)
    uni.showToast({
      title: '提交失败，请重试',
      icon: 'none'
    })
  } finally {
    isSubmitting.value = false
  }
}

// 跳过调查
async function handleSkip() {
  isSubmitting.value = true
  try {
    const result = await recommendationStore.skipPreferenceSurvey()
    if (result.success) {
      emit('skip')
    } else {
      uni.showToast({
        title: result.message || '操作失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('跳过调查失败:', error)
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.survey-container {
  padding: 40rpx;
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF0F5 0%, #FFFFFF 100%);
}

.survey-header {
  text-align: center;
  margin-bottom: 50rpx;
}

.survey-title {
  display: block;
  font-size: 40rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 16rpx;
}

.survey-subtitle {
  display: block;
  font-size: 28rpx;
  color: #999;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24rpx;
  margin-bottom: 40rpx;
}

.category-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30rpx 16rpx;
  background: #fff;
  border-radius: 20rpx;
  border: 2rpx solid #eee;
  transition: all 0.3s;
}

.category-item:active {
  transform: scale(0.95);
}

.category-selected {
  border-color: #FF69B4;
  background: #FFF0F5;
  box-shadow: 0 4rpx 15rpx rgba(255, 105, 180, 0.2);
}

.category-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
}

.icon-text {
  font-size: 36rpx;
}

.category-name {
  font-size: 26rpx;
  color: #333;
  text-align: center;
}

.check-mark {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  width: 36rpx;
  height: 36rpx;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.check-mark text {
  color: #fff;
  font-size: 24rpx;
  font-weight: bold;
}

.selection-hint {
  text-align: center;
  margin-bottom: 40rpx;
}

.selection-hint text {
  font-size: 28rpx;
  color: #FF69B4;
}

.survey-actions {
  padding: 0 20rpx;
}

.submit-btn {
  width: 100%;
  height: 100rpx;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  border-radius: 50rpx;
  border: none;
  color: #fff;
  font-size: 36rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 20rpx rgba(255, 105, 180, 0.4);
  transition: all 0.3s;
}

.submit-btn:active {
  transform: scale(0.98);
  opacity: 0.9;
}

.btn-disabled {
  opacity: 0.5;
  box-shadow: none;
}

.skip-link {
  text-align: center;
  margin-top: 30rpx;
  padding: 20rpx;
}

.skip-link text {
  font-size: 28rpx;
  color: #999;
}
</style>
