<template>
  <view class="container">
    <!-- 用户统计卡片 -->
    <view class="stats-card">
      <view class="card-title">我的数据</view>

      <view class="stats-grid">
        <view class="stat-item">
          <text class="stat-value">{{ userStats.totalSpins || 0 }}</text>
          <text class="stat-label">总转盘次数</text>
        </view>

        <view class="stat-item">
          <text class="stat-value">{{ userStats.todaySpins || 0 }}</text>
          <text class="stat-label">今日次数</text>
        </view>

        <view class="stat-item">
          <text class="stat-value">{{ userStats.weekSpins || 0 }}</text>
          <text class="stat-label">本周次数</text>
        </view>

        <view class="stat-item">
          <text class="stat-value">{{ userStats.consecutiveDays || 0 }}</text>
          <text class="stat-label">连续天数</text>
        </view>

        <view class="stat-item">
          <text class="stat-value">{{ userStats.favoriteCategory || '未知' }}</text>
          <text class="stat-label">偏好分类</text>
        </view>

        <view class="stat-item">
          <text class="stat-value">{{ formatDuration(userStats.avgSpinDuration) }}</text>
          <text class="stat-label">平均时长</text>
        </view>
      </view>
    </view>

    <!-- 转盘使用趋势 -->
    <view class="trend-section">
      <view class="section-title">
        <text>转盘使用趋势</text>
        <picker
          mode="date"
          :value="trendDateRange.start"
          @change="onStartDateChange"
          class="date-picker"
        >
          <text class="date-text">{{ trendDateRange.start }}</text>
        </picker>
        <text class="date-separator">至</text>
        <picker
          mode="date"
          :value="trendDateRange.end"
          @change="onEndDateChange"
          class="date-picker"
        >
          <text class="date-text">{{ trendDateRange.end }}</text>
        </picker>
      </view>

      <view class="trend-chart">
        <!-- 这里应该使用图表组件，暂时用列表代替 -->
        <view
          class="trend-item"
          v-for="item in spinTrend"
          :key="item.date"
        >
          <text class="trend-date">{{ item.date }}</text>
          <view class="trend-bars">
            <view class="bar-group">
              <text class="bar-label">转盘</text>
              <view class="bar-container">
                <view
                  class="bar bar-spins"
                  :style="{ width: (item.spinCount / maxSpinCount * 100) + '%' }"
                ></view>
              </view>
              <text class="bar-value">{{ item.spinCount }}</text>
            </view>

            <view class="bar-group">
              <text class="bar-label">用户</text>
              <view class="bar-container">
                <view
                  class="bar bar-users"
                  :style="{ width: (item.userCount / maxUserCount * 100) + '%' }"
                ></view>
              </view>
              <text class="bar-value">{{ item.userCount }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 热门内容 -->
    <view class="popular-section">
      <view class="section-title">
        <text>热门内容 TOP10</text>
      </view>

      <view class="popular-list">
        <view
          class="popular-item"
          v-for="(item, index) in popularContent"
          :key="item.contentId"
        >
          <view class="rank" :class="{ top3: index < 3 }">{{ index + 1 }}</view>
          <view class="content-info">
            <text class="content-text">{{ item.contentText }}</text>
            <text class="content-category">{{ item.categoryName }}</text>
          </view>
          <view class="content-stats">
            <text class="spin-count">{{ item.spinCount }}次</text>
            <text class="win-rate">{{ item.winRate.toFixed(2) }}%</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 分类统计 -->
    <view class="category-section">
      <view class="section-title">
        <text>分类统计</text>
      </view>

      <view class="category-list">
        <view
          class="category-item"
          v-for="item in categoryStats"
          :key="item.categoryId"
        >
          <view class="category-info">
            <text class="category-name">{{ item.categoryName }}</text>
            <text class="category-desc">
              {{ item.contentCount }}个内容 · {{ item.totalSpins }}次转盘
            </text>
          </view>
          <view class="category-users">
            <text class="user-count">{{ item.uniqueUsers }}人</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import * as statisticsApi from '@/api/statistics'

const userStats = ref<any>({})
const spinTrend = ref<any[]>([])
const popularContent = ref<any[]>([])
const categoryStats = ref<any[]>([])

const trendDateRange = ref({
  start: getDateString(-7),
  end: getDateString(0)
})

// 计算最大值（用于图表显示）
const maxSpinCount = computed(() => {
  return Math.max(...spinTrend.value.map(item => item.spinCount), 1)
})

const maxUserCount = computed(() => {
  return Math.max(...spinTrend.value.map(item => item.userCount), 1)
})

// 页面加载
onMounted(() => {
  loadData()
})

// 加载数据
async function loadData() {
  try {
    // 并行加载所有数据
    const [userRes, trendRes, popularRes, categoryRes] = await Promise.all([
      statisticsApi.getUserStatistics(),
      statisticsApi.getSpinTrend(trendDateRange.value),
      statisticsApi.getPopularContent({ limit: 10 }),
      statisticsApi.getCategoryStatistics()
    ])

    if (userRes.code === 200) {
      userStats.value = userRes.data
    }

    if (trendRes.code === 200) {
      spinTrend.value = trendRes.data
    }

    if (popularRes.code === 200) {
      popularContent.value = popularRes.data
    }

    if (categoryRes.code === 200) {
      categoryStats.value = categoryRes.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

// 格式化时长
function formatDuration(duration?: number) {
  if (!duration) return '0s'
  const seconds = Math.floor(duration / 1000)
  if (seconds < 60) {
    return `${seconds}s`
  } else {
    const minutes = Math.floor(seconds / 60)
    const remainingSeconds = seconds % 60
    return `${minutes}m${remainingSeconds}s`
  }
}

// 获取日期字符串
function getDateString(offset: number) {
  const date = new Date()
  date.setDate(date.getDate() + offset)
  return date.toISOString().split('T')[0]
}

// 开始日期变化
function onStartDateChange(e: any) {
  trendDateRange.value.start = e.detail.value
  loadTrendData()
}

// 结束日期变化
function onEndDateChange(e: any) {
  trendDateRange.value.end = e.detail.value
  loadTrendData()
}

// 加载趋势数据
async function loadTrendData() {
  try {
    const res = await statisticsApi.getSpinTrend(trendDateRange.value)

    if (res.code === 200) {
      spinTrend.value = res.data
    }
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF0F5 0%, #FFFFFF 100%);
  padding: 20rpx;
}

.stats-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
  margin-bottom: 30rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 30rpx;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 40rpx;
  font-weight: bold;
  color: #FF1493;
  margin-bottom: 10rpx;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
}

.trend-section {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
  margin-bottom: 30rpx;
}

.section-title {
  display: flex;
  align-items: center;
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 30rpx;
}

.date-picker {
  margin-left: auto;
}

.date-text {
  padding: 10rpx 20rpx;
  background: #f5f5f5;
  border-radius: 10rpx;
  font-size: 26rpx;
  color: #666;
}

.date-separator {
  margin: 0 10rpx;
  color: #999;
}

.trend-chart {
  max-height: 400rpx;
  overflow-y: scroll;
}

.trend-item {
  margin-bottom: 20rpx;
}

.trend-date {
  display: block;
  font-size: 28rpx;
  color: #666;
  margin-bottom: 10rpx;
}

.trend-bars {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.bar-group {
  display: flex;
  align-items: center;
}

.bar-label {
  width: 80rpx;
  font-size: 24rpx;
  color: #999;
}

.bar-container {
  flex: 1;
  height: 30rpx;
  background: #f5f5f5;
  border-radius: 15rpx;
  overflow: hidden;
  margin: 0 15rpx;
}

.bar {
  height: 100%;
  border-radius: 15rpx;
  transition: width 0.3s;
}

.bar-spins {
  background: linear-gradient(90deg, #FF69B4, #FF1493);
}

.bar-users {
  background: linear-gradient(90deg, #4169E1, #6495ED);
}

.bar-value {
  width: 80rpx;
  text-align: right;
  font-size: 24rpx;
  color: #666;
}

.popular-section {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
  margin-bottom: 30rpx;
}

.popular-list {
  max-height: 500rpx;
  overflow-y: scroll;
}

.popular-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.popular-item:last-child {
  border-bottom: none;
}

.rank {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 50%;
  font-size: 28rpx;
  font-weight: bold;
  color: #666;
  margin-right: 20rpx;
}

.rank.top3 {
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: #fff;
}

.content-info {
  flex: 1;
  margin-right: 20rpx;
}

.content-text {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 5rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.content-category {
  display: block;
  font-size: 24rpx;
  color: #999;
}

.content-stats {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.spin-count {
  font-size: 26rpx;
  color: #FF1493;
  margin-bottom: 5rpx;
}

.win-rate {
  font-size: 24rpx;
  color: #999;
}

.category-section {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}

.category-list {
  max-height: 400rpx;
  overflow-y: scroll;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.category-item:last-child {
  border-bottom: none;
}

.category-info {
  flex: 1;
}

.category-name {
  display: block;
  font-size: 30rpx;
  color: #333;
  margin-bottom: 5rpx;
}

.category-desc {
  display: block;
  font-size: 24rpx;
  color: #999;
}

.user-count {
  font-size: 26rpx;
  color: #FF1493;
}
</style>
