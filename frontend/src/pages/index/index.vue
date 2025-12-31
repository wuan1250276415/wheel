<template>
  <view class="container">
    <!-- 欢迎区域 -->
    <view class="welcome-section">
      <image
        v-if="userInfo?.avatar"
        :src="userInfo.avatar"
        class="avatar"
        mode="aspectFill"
      />
      <view v-else class="avatar-placeholder">
        <text>{{ userInfo?.nickname?.[0] || '用' }}</text>
      </view>

      <view class="welcome-text">
        <text class="greeting">你好，{{ userInfo?.nickname || '朋友' }}</text>
        <text class="subtitle">{{ coupleInfo ? '和TA一起享受甜蜜时光' : '快来邀请你的另一半吧' }}</text>
      </view>
    </view>

    <!-- 广告横幅 -->
    <AdBanner placement-key="home_banner" />

    <!-- 个性化分类排名 -->
    <view class="category-section" v-if="categoryRanking.length > 0">
      <view class="section-title">
        <text>🎯 为你推荐的分类</text>
      </view>
      <scroll-view class="category-scroll" scroll-x :show-scrollbar="false">
        <view class="category-list">
          <view
            v-for="(category, index) in categoryRanking"
            :key="category.categoryId"
            class="category-rank-item"
            @click="navigateToWheel(category.categoryId)"
          >
            <view class="rank-badge" :class="'rank-' + (index + 1)">{{ index + 1 }}</view>
            <text class="category-rank-name">{{ category.categoryName }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 为你推荐 -->
    <view class="recommend-section" v-if="recommendedForYou.length > 0">
      <RecommendationList
        :recommendations="recommendedForYou"
        title="为你推荐"
        icon="💡"
        :show-more="true"
        :horizontal="true"
        :show-reason="true"
        @click="handleRecommendationClick"
        @more="navigateTo('/pages/wheel/wheel')"
      />
    </view>

    <!-- 情侣精选（仅情侣用户显示） -->
    <view class="couple-section" v-if="coupleInfo && couplePicks.length > 0">
      <RecommendationList
        :recommendations="couplePicks"
        title="情侣精选"
        icon="💑"
        :show-more="true"
        :horizontal="true"
        :show-reason="true"
        @click="handleRecommendationClick"
        @more="navigateTo('/pages/couple/couple')"
      />
    </view>

    <!-- 快速操作 -->
    <view class="quick-actions">
      <view class="action-card" @click="navigateTo('/pages/wheel/wheel')">
        <view class="action-icon wheel-icon"></view>
        <text class="action-title">开始转盘</text>
        <text class="action-desc">探索有趣的互动</text>
      </view>

      <view class="action-card" @click="navigateTo('/pages/couple/couple')">
        <view class="action-icon couple-icon"></view>
        <text class="action-title">{{ coupleInfo ? '我的情侣' : '邀请情侣' }}</text>
        <text class="action-desc">{{ coupleInfo ? '查看关系信息' : '建立情侣关系' }}</text>
      </view>
    </view>

    <!-- 今日数据 -->
    <view class="stats-section">
      <view class="section-title">
        <text>今日数据</text>
      </view>

      <view class="stats-grid">
        <view class="stat-item">
          <text class="stat-value">{{ todayStats.spins || 0 }}</text>
          <text class="stat-label">转盘次数</text>
        </view>

        <view class="stat-item">
          <text class="stat-value">{{ todayStats.duration || 0 }}分钟</text>
          <text class="stat-label">使用时长</text>
        </view>

        <view class="stat-item">
          <text class="stat-value">{{ coupleInfo ? todayStats.coupleSpins || 0 : 0 }}</text>
          <text class="stat-label">情侣转盘</text>
        </view>

        <view class="stat-item">
          <text class="stat-value">{{ todayStats.consecutiveDays || 0 }}</text>
          <text class="stat-label">连续天数</text>
        </view>
      </view>
    </view>

    <!-- 最近结果 -->
    <view class="recent-section" v-if="recentResults.length > 0">
      <view class="section-title">
        <text>最近结果</text>
        <text class="more" @click="navigateTo('/pages/wheel/wheel')">更多 ></text>
      </view>

      <scroll-view class="recent-list" scroll-x>
        <view class="result-item" v-for="item in recentResults" :key="item.id">
          <text class="result-text">{{ item.resultText }}</text>
          <text class="result-time">{{ formatTime(item.spinTime) }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 底部导航提示 -->
    <view class="tabbar-hint">
      <text>点击底部导航体验更多功能</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useWheelStore } from '@/stores/wheel'
import { useRecommendationStore } from '@/stores/recommendation'
import * as statisticsApi from '@/api/statistics'
import AdBanner from '@/components/AdBanner.vue'
import RecommendationList from '@/components/RecommendationList.vue'
import * as coupleApi from '@/api/couple'
import type { RecommendationVO } from '@/types/recommendation'

const userStore = useUserStore()
const wheelStore = useWheelStore()
const recommendationStore = useRecommendationStore()

const userInfo = ref(userStore.userInfo)
const coupleInfo = ref<any>(null)
const todayStats = ref<any>({})
const recentResults = ref<any[]>([])
const isLoading = ref(false)

// 推荐数据计算属性
const categoryRanking = computed(() => recommendationStore.categoryRanking)
const recommendedForYou = computed(() => recommendationStore.recommendedForYou)
const couplePicks = computed(() => recommendationStore.couplePicks)

// 页面加载
onMounted(() => {
  // 初始化用户Store
  userStore.init()
  // 初始化用户数据
  initData()
})

// 检查登录状态并跳转
function checkLoginAndRedirect(): boolean {
  if (!userStore.isLoggedIn) {
    uni.redirectTo({
      url: '/pages/login/login'
    })
    return false
  }
  return true
}

// 初始化数据
async function initData() {
  // 检查登录状态，未登录则跳转到登录页
  if (!checkLoginAndRedirect()) {
    return
  }

  // 更新用户信息引用
  userInfo.value = userStore.userInfo

  isLoading.value = true

  try {
    // 并行获取数据
    const [statsResult, coupleResult, homepageResult] = await Promise.allSettled([
      statisticsApi.getUserStatistics(),
      coupleApi.getCoupleInfo(),
      recommendationStore.fetchHomepageRecommendations()
    ])

    
    // 处理用户统计
    if (statsResult.status === 'fulfilled') {
      const statsData = statsResult.value
      todayStats.value = {
        spins: statsData?.todaySpins || 0,
        duration: statsData?.avgSpinDuration ? Math.round(Number(statsData.avgSpinDuration) / 1000 / 60) : 0,
        coupleSpins: statsData?.totalCoupleSpins || 0,
        consecutiveDays: statsData?.consecutiveDays || 0
      }
    }

    // 处理情侣信息
    if (coupleResult.status === 'fulfilled') {
      coupleInfo.value = coupleResult.value
    }

    // 获取最近的历史记录
    await wheelStore.fetchHistory(1, 5)
    recentResults.value = wheelStore.history

  } catch (error) {
    console.error('初始化数据失败:', error)
    uni.showToast({
      title: '数据加载失败',
      icon: 'none'
    })
  } finally {
    isLoading.value = false
  }
}

// 处理推荐内容点击
function handleRecommendationClick(item: RecommendationVO) {
  // 跳转到转盘页面，并传递分类ID
  navigateTo(`/pages/wheel/wheel?categoryId=${item.categoryId}`)
}

// 跳转到转盘页面并选择分类
function navigateToWheel(categoryId: number) {
  navigateTo(`/pages/wheel/wheel?categoryId=${categoryId}`)
}

// 格式化时间
function formatTime(timeStr: string) {
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 60 * 1000) {
    return '刚刚'
  } else if (diff < 60 * 60 * 1000) {
    return `${Math.floor(diff / 60 / 1000)}分钟前`
  } else if (diff < 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / 60 / 60 / 1000)}小时前`
  } else {
    return date.toLocaleDateString()
  }
}

// 导航到指定页面
// 导航到指定页面
function navigateTo(url: string) {
  const tabBarPages = [
    '/pages/index/index',
    '/pages/wheel/wheel',
    '/pages/profile/profile'
  ]
  
  // 提取基础路径（不含查询参数）
  const basePath = url.split('?')[0]
  
  if (tabBarPages.includes(basePath)) {
    uni.switchTab({ url: basePath })
  } else {
    uni.navigateTo({ url })
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF0F5 0%, #FFFFFF 100%);
  padding: 40rpx;
}

.welcome-section {
  display: flex;
  align-items: center;
  padding: 40rpx;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  border-radius: 20rpx;
  margin-bottom: 40rpx;
  box-shadow: 0 8rpx 20rpx rgba(255, 105, 180, 0.3);
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 4rpx solid #fff;
}

.avatar-placeholder {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  font-weight: bold;
  color: #fff;
  border: 4rpx solid #fff;
}

.welcome-text {
  margin-left: 30rpx;
  flex: 1;
}

.greeting {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 10rpx;
}

.subtitle {
  display: block;
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
}

/* 个性化分类排名 */
.category-section {
  margin-bottom: 30rpx;
}

.category-scroll {
  width: 100%;
}

.category-list {
  display: flex;
  padding: 10rpx 0;
}

.category-rank-item {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  background: #fff;
  padding: 16rpx 24rpx;
  border-radius: 30rpx;
  margin-right: 16rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.category-rank-item:active {
  transform: scale(0.95);
}

.rank-badge {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: bold;
  color: #fff;
  margin-right: 12rpx;
  background: #ccc;
}

.rank-1 {
  background: linear-gradient(135deg, #FFD700, #FFA500);
}

.rank-2 {
  background: linear-gradient(135deg, #C0C0C0, #A0A0A0);
}

.rank-3 {
  background: linear-gradient(135deg, #CD7F32, #B87333);
}

.category-rank-name {
  font-size: 28rpx;
  color: #333;
}

/* 推荐区域 */
.recommend-section,
.couple-section {
  margin-bottom: 20rpx;
}

.quick-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
  margin-bottom: 40rpx;
}

.action-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 40rpx;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: all 0.3s;
}

.action-card:active {
  transform: scale(0.95);
}

.action-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-bottom: 20rpx;
}

.wheel-icon {
  background: linear-gradient(135deg, #FF69B4, #FF1493);
}

.couple-icon {
  background: linear-gradient(135deg, #FFA500, #FF6347);
}

.action-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}

.action-desc {
  font-size: 24rpx;
  color: #999;
}

.stats-section {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 40rpx;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 30rpx;
}

.more {
  font-size: 28rpx;
  color: #FF69B4;
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

.recent-section {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 40rpx;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}

.recent-list {
  white-space: nowrap;
}

.result-item {
  display: inline-block;
  width: 200rpx;
  padding: 20rpx;
  background: #FFF0F5;
  border-radius: 15rpx;
  margin-right: 20rpx;
}

.result-text {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 10rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-time {
  display: block;
  font-size: 22rpx;
  color: #999;
}

.tabbar-hint {
  text-align: center;
  padding: 20rpx;
  font-size: 24rpx;
  color: #ccc;
}
</style>
