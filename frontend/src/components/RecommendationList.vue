<template>
  <view class="recommendation-list">
    <!-- 标题区域 -->
    <view class="list-header" v-if="title">
      <view class="header-left">
        <text class="header-icon" v-if="icon">{{ icon }}</text>
        <text class="header-title">{{ title }}</text>
      </view>
      <view class="header-right" v-if="showMore" @click="handleMore">
        <text class="more-text">更多</text>
        <text class="more-arrow">></text>
      </view>
    </view>

    <!-- 加载状态 -->
    <view class="loading-state" v-if="loading && recommendations.length === 0">
      <text class="loading-icon">⏳</text>
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-else-if="!loading && recommendations.length === 0">
      <text class="empty-icon">📭</text>
      <text class="empty-text">{{ emptyText }}</text>
    </view>

    <!-- 推荐列表 -->
    <view class="list-content" v-else>
      <!-- 横向滚动模式 -->
      <scroll-view
        v-if="horizontal"
        class="horizontal-scroll"
        scroll-x
        :show-scrollbar="false"
      >
        <view class="horizontal-list">
          <view
            v-for="item in recommendations"
            :key="item.contentId"
            class="horizontal-item"
          >
            <RecommendationCard
              :recommendation="item"
              :show-reason="showReason"
              :show-score="showScore"
              @click="handleItemClick"
            />
          </view>
        </view>
      </scroll-view>

      <!-- 垂直列表模式 -->
      <view v-else class="vertical-list">
        <RecommendationCard
          v-for="item in recommendations"
          :key="item.contentId"
          :recommendation="item"
          :show-reason="showReason"
          :show-score="showScore"
          @click="handleItemClick"
        />
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="!horizontal && hasMore">
        <button
          class="load-more-btn"
          :disabled="loadingMore"
          @click="handleLoadMore"
        >
          <text v-if="loadingMore">加载中...</text>
          <text v-else>加载更多</text>
        </button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { RecommendationVO } from '@/types/recommendation'
import RecommendationCard from './RecommendationCard.vue'

interface Props {
  recommendations: RecommendationVO[]
  title?: string
  icon?: string
  loading?: boolean
  horizontal?: boolean
  showMore?: boolean
  showReason?: boolean
  showScore?: boolean
  hasMore?: boolean
  emptyText?: string
}

const props = withDefaults(defineProps<Props>(), {
  title: '',
  icon: '',
  loading: false,
  horizontal: false,
  showMore: false,
  showReason: true,
  showScore: false,
  hasMore: false,
  emptyText: '暂无推荐内容'
})

const emit = defineEmits<{
  (e: 'click', item: RecommendationVO): void
  (e: 'more'): void
  (e: 'loadMore'): void
}>()

const loadingMore = ref(false)

// 处理项目点击
function handleItemClick(item: RecommendationVO) {
  emit('click', item)
}

// 处理更多点击
function handleMore() {
  emit('more')
}

// 处理加载更多
async function handleLoadMore() {
  if (loadingMore.value) return
  loadingMore.value = true
  emit('loadMore')
  // 延迟重置状态，让父组件有时间更新
  setTimeout(() => {
    loadingMore.value = false
  }, 500)
}
</script>

<style scoped>
.recommendation-list {
  margin-bottom: 30rpx;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 10rpx;
  margin-bottom: 20rpx;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.header-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
}

.more-text {
  font-size: 28rpx;
  color: #FF69B4;
}

.more-arrow {
  font-size: 24rpx;
  color: #FF69B4;
  margin-left: 6rpx;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 40rpx;
  background: #fff;
  border-radius: 20rpx;
}

.loading-icon,
.empty-icon {
  font-size: 60rpx;
  margin-bottom: 20rpx;
}

.loading-text,
.empty-text {
  font-size: 28rpx;
  color: #999;
}

.horizontal-scroll {
  width: 100%;
}

.horizontal-list {
  display: flex;
  padding: 10rpx 0;
}

.horizontal-item {
  flex-shrink: 0;
  width: 320rpx;
  margin-right: 20rpx;
}

.horizontal-item:last-child {
  margin-right: 0;
}

.horizontal-item .recommendation-card {
  margin-bottom: 0;
}

.vertical-list {
  /* 垂直列表样式由 RecommendationCard 自带 margin-bottom */
}

.load-more {
  padding: 20rpx 0;
  text-align: center;
}

.load-more-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 16rpx 40rpx;
  background: #FFF0F5;
  border: 2rpx solid #FF69B4;
  border-radius: 30rpx;
  font-size: 28rpx;
  color: #FF69B4;
}

.load-more-btn:disabled {
  opacity: 0.6;
}

.load-more-btn:active {
  background: #FFE4EC;
}
</style>
