<template>
  <view class="recommendation-card" @click="handleClick">
    <!-- 内容区域 -->
    <view class="card-content">
      <text class="content-text">{{ recommendation.contentText }}</text>
    </view>
    
    <!-- 分类标签 -->
    <view class="card-category">
      <view class="category-tag">
        <text class="category-name">{{ recommendation.categoryName }}</text>
      </view>
    </view>
    
    <!-- 推荐原因 -->
    <view class="card-reason" v-if="showReason && recommendation.reason">
      <text class="reason-icon">💡</text>
      <text class="reason-text">{{ recommendation.reason }}</text>
    </view>
    
    <!-- 分数指示器（可选） -->
    <view class="card-score" v-if="showScore">
      <view class="score-bar">
        <view class="score-fill" :style="{ width: scorePercent + '%' }"></view>
      </view>
      <text class="score-text">匹配度 {{ scorePercent }}%</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { RecommendationVO } from '@/types/recommendation'
import { useRecommendationStore } from '@/stores/recommendation'

interface Props {
  recommendation: RecommendationVO
  showReason?: boolean
  showScore?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showReason: true,
  showScore: false
})

const emit = defineEmits<{
  (e: 'click', recommendation: RecommendationVO): void
}>()

const recommendationStore = useRecommendationStore()

// 计算分数百分比（假设分数范围0-1）
const scorePercent = computed(() => {
  const score = props.recommendation.score || 0
  return Math.round(Math.min(score * 100, 100))
})

// 处理点击事件
async function handleClick() {
  // 记录点击反馈
  await recommendationStore.recordClick(props.recommendation.contentId)
  // 触发点击事件
  emit('click', props.recommendation)
}
</script>

<style scoped>
.recommendation-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.recommendation-card:active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.08);
}

.card-content {
  margin-bottom: 20rpx;
}

.content-text {
  font-size: 32rpx;
  color: #333;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-category {
  margin-bottom: 16rpx;
}

.category-tag {
  display: inline-flex;
  align-items: center;
  background: linear-gradient(135deg, #FFF0F5, #FFE4EC);
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
}

.category-name {
  font-size: 24rpx;
  color: #FF69B4;
}

.card-reason {
  display: flex;
  align-items: flex-start;
  background: #FFF9FB;
  padding: 16rpx 20rpx;
  border-radius: 12rpx;
  margin-top: 16rpx;
}

.reason-icon {
  font-size: 24rpx;
  margin-right: 10rpx;
  flex-shrink: 0;
}

.reason-text {
  font-size: 24rpx;
  color: #999;
  line-height: 1.5;
}

.card-score {
  margin-top: 16rpx;
}

.score-bar {
  height: 8rpx;
  background: #f0f0f0;
  border-radius: 4rpx;
  overflow: hidden;
  margin-bottom: 8rpx;
}

.score-fill {
  height: 100%;
  background: linear-gradient(90deg, #FF69B4, #FF1493);
  border-radius: 4rpx;
  transition: width 0.3s;
}

.score-text {
  font-size: 22rpx;
  color: #999;
}
</style>
