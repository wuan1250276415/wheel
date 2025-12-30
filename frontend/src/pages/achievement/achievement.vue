<template>
  <view class="achievement-container">
    <view class="header">
      <text class="title">成就墙</text>
      <text class="subtitle">收集你的成就徽章</text>
    </view>

    <scroll-view class="achievement-list" scroll-y>
      <view v-if="loading" class="loading">加载中...</view>
      <view v-else class="grid">
        <view
          v-for="achievement in achievements"
          :key="achievement.id"
          :class="['achievement-card', `rarity-${achievement.rarity}`, { unlocked: achievement.isUnlocked }]"
        >
          <view v-if="!achievement.isUnlocked && achievement.isHidden" class="locked-badge">
            <text>🔒 隐藏成就</text>
          </view>
          <view v-else>
            <view class="icon">{{ achievement.isUnlocked ? '🏆' : '⭐' }}</view>
            <view class="name">{{ achievement.achievementName }}</view>
            <view class="desc">{{ achievement.description }}</view>
            <view v-if="!achievement.isUnlocked" class="progress">
              <text>{{ achievement.progress }}/{{ achievement.targetValue }}</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useAchievementStore } from '@/stores/achievement'

const achievementStore = useAchievementStore()

const loading = computed(() => achievementStore.loading)
const achievements = computed(() => achievementStore.achievements)

onMounted(() => {
  achievementStore.fetchAchievements()
})
</script>

<style lang="scss" scoped>
.achievement-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20rpx;
}

.header {
  text-align: center;
  padding: 40rpx 0;

  .title {
    display: block;
    font-size: 48rpx;
    font-weight: bold;
    margin-bottom: 10rpx;
  }

  .subtitle {
    display: block;
    font-size: 28rpx;
    color: #999;
  }
}

.achievement-list {
  height: calc(100vh - 200rpx);
}

.loading {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.achievement-card {
  background: white;
  border-radius: 20rpx;
  padding: 30rpx;
  text-align: center;
  position: relative;
  border: 3px solid transparent;

  &.rarity-1 { border-color: #95a5a6; }
  &.rarity-2 { border-color: #3498db; }
  &.rarity-3 { border-color: #9b59b6; }
  &.rarity-4 { border-color: #f39c12; }

  &:not(.unlocked) {
    opacity: 0.5;
  }

  .locked-badge {
    padding: 60rpx 0;
    color: #999;
  }

  .icon {
    font-size: 80rpx;
    margin-bottom: 20rpx;
  }

  .name {
    font-size: 28rpx;
    font-weight: bold;
    margin-bottom: 10rpx;
  }

  .desc {
    font-size: 24rpx;
    color: #666;
    margin-bottom: 15rpx;
  }

  .progress {
    font-size: 22rpx;
    color: #999;
  }
}
</style>
