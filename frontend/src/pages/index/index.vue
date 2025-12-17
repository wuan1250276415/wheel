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
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useWheelStore } from '@/stores/wheel'
import * as statisticsApi from '@/api/statistics'

const userStore = useUserStore()
const wheelStore = useWheelStore()

const userInfo = ref(userStore.userInfo)
const coupleInfo = ref<any>(null)
const todayStats = ref<any>({})
const recentResults = ref<any[]>([])

// 页面加载
onMounted(() => {
  // 初始化用户数据
  initData()
})

// 初始化数据
async function initData() {
  // 检查登录状态
  if (!userStore.isLoggedIn) {
    // 跳转到登录页
    uni.showModal({
      title: '提示',
      content: '请先登录',
      showCancel: false,
      success: () => {
        // 这里应该跳转到登录页
        console.log('跳转到登录页')
      }
    })
    return
  }

  try {
    // 获取用户统计
    const statsRes = await statisticsApi.getUserStatistics()
    if (statsRes.code === 200) {
      todayStats.value = {
        spins: statsRes.data.todaySpins,
        duration: Math.round(statsRes.data.avgSpinDuration / 1000 / 60),
        coupleSpins: statsRes.data.totalCoupleSpins,
        consecutiveDays: statsRes.data.consecutiveDays
      }
    }

    // 获取最近的历史记录
    await wheelStore.fetchHistory(1, 5)
    recentResults.value = wheelStore.history

  } catch (error) {
    console.error('初始化数据失败:', error)
  }
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
function navigateTo(url: string) {
  uni.navigateTo({ url })
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
