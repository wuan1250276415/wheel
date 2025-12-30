<template>
  <view class="task-container">
    <view class="header">
      <view class="title">任务挑战</view>
      <view class="stats">
        <view class="stat-item">
          <text class="stat-value">{{ totalCompleted }}</text>
          <text class="stat-label">已完成</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ totalRewards }}</text>
          <text class="stat-label">已领奖</text>
        </view>
      </view>
    </view>

    <view class="tabs">
      <view
        :class="['tab-item', { active: activeTab === 'daily' }]"
        @click="activeTab = 'daily'"
      >
        每日任务
      </view>
      <view
        :class="['tab-item', { active: activeTab === 'weekly' }]"
        @click="activeTab = 'weekly'"
      >
        每周任务
      </view>
    </view>

    <scroll-view class="task-list" scroll-y>
      <view v-if="loading" class="loading">加载中...</view>
      <view v-else>
        <view
          v-for="task in currentTasks"
          :key="task.userTaskId"
          class="task-card"
        >
          <view class="task-header">
            <view class="task-info">
              <text class="task-name">{{ task.taskName }}</text>
              <text :class="['difficulty', `difficulty-${task.difficulty}`]">
                {{ task.difficultyDesc }}
              </text>
            </view>
            <text :class="['status', `status-${task.status}`]">
              {{ task.statusDesc }}
            </text>
          </view>

          <view class="task-desc">{{ task.description }}</view>

          <view class="progress-section">
            <view class="progress-info">
              <text class="progress-text">
                {{ task.currentCount }} / {{ task.targetCount }}
              </text>
              <text class="progress-percent">{{ task.progress }}%</text>
            </view>
            <view class="progress-bar">
              <view
                class="progress-fill"
                :style="{ width: task.progress + '%' }"
              ></view>
            </view>
          </view>

          <view class="task-footer">
            <view class="reward">
              <text class="reward-icon">🎁</text>
              <text class="reward-text">
                {{ task.rewardTypeDesc }}: {{ task.rewardValue }}
              </text>
            </view>
            <button
              v-if="task.status === 1"
              class="claim-btn"
              :disabled="claimingTaskId === task.userTaskId"
              @click="handleClaimReward(task.userTaskId)"
            >
              {{ claimingTaskId === task.userTaskId ? '领取中...' : '领取奖励' }}
            </button>
            <button
              v-else-if="task.status === 2"
              class="claimed-btn"
              disabled
            >
              已领取
            </button>
          </view>
        </view>

        <view v-if="currentTasks.length === 0" class="empty">
          暂无任务
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useTaskStore } from '@/stores/task'

const taskStore = useTaskStore()
const activeTab = ref<'daily' | 'weekly'>('daily')
const claimingTaskId = ref<string | number | null>(null)

const loading = computed(() => taskStore.loading)
const dailyTasks = computed(() => taskStore.dailyTasks)
const weeklyTasks = computed(() => taskStore.weeklyTasks)
const totalCompleted = computed(() => taskStore.totalCompleted)
const totalRewards = computed(() => taskStore.totalRewards)

const currentTasks = computed(() => {
  return activeTab.value === 'daily' ? dailyTasks.value : weeklyTasks.value
})

const handleClaimReward = async (userTaskId: string | number) => {
  claimingTaskId.value = userTaskId
  await taskStore.claimReward(userTaskId)
  claimingTaskId.value = null
}

onMounted(() => {
  taskStore.fetchTasks()
})
</script>

<style lang="scss" scoped>
.task-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20rpx;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20rpx;
  padding: 40rpx;
  margin-bottom: 20rpx;
  color: white;

  .title {
    font-size: 44rpx;
    font-weight: bold;
    margin-bottom: 30rpx;
  }

  .stats {
    display: flex;
    gap: 40rpx;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;

      .stat-value {
        font-size: 48rpx;
        font-weight: bold;
      }

      .stat-label {
        font-size: 24rpx;
        opacity: 0.8;
        margin-top: 10rpx;
      }
    }
  }
}

.tabs {
  display: flex;
  background: white;
  border-radius: 20rpx;
  padding: 10rpx;
  margin-bottom: 20rpx;

  .tab-item {
    flex: 1;
    text-align: center;
    padding: 20rpx;
    border-radius: 15rpx;
    font-size: 28rpx;
    transition: all 0.3s;

    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      font-weight: bold;
    }
  }
}

.task-list {
  height: calc(100vh - 400rpx);
}

.loading, .empty {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
  font-size: 28rpx;
}

.task-card {
  background: white;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;

  .task-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    .task-info {
      display: flex;
      align-items: center;
      gap: 15rpx;

      .task-name {
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
      }

      .difficulty {
        font-size: 22rpx;
        padding: 8rpx 16rpx;
        border-radius: 20rpx;

        &.difficulty-1 {
          background: #d4edda;
          color: #155724;
        }

        &.difficulty-2 {
          background: #fff3cd;
          color: #856404;
        }

        &.difficulty-3 {
          background: #f8d7da;
          color: #721c24;
        }
      }
    }

    .status {
      font-size: 24rpx;
      padding: 8rpx 16rpx;
      border-radius: 20rpx;

      &.status-0 {
        background: #e7f3ff;
        color: #007bff;
      }

      &.status-1 {
        background: #d4edda;
        color: #155724;
      }

      &.status-2 {
        background: #f8f9fa;
        color: #6c757d;
      }
    }
  }

  .task-desc {
    font-size: 26rpx;
    color: #666;
    margin-bottom: 25rpx;
  }

  .progress-section {
    margin-bottom: 25rpx;

    .progress-info {
      display: flex;
      justify-content: space-between;
      margin-bottom: 15rpx;
      font-size: 24rpx;

      .progress-text {
        color: #333;
        font-weight: 500;
      }

      .progress-percent {
        color: #667eea;
        font-weight: bold;
      }
    }

    .progress-bar {
      height: 12rpx;
      background: #e9ecef;
      border-radius: 10rpx;
      overflow: hidden;

      .progress-fill {
        height: 100%;
        background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
        transition: width 0.3s;
      }
    }
  }

  .task-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .reward {
      display: flex;
      align-items: center;
      gap: 10rpx;
      font-size: 26rpx;
      color: #666;

      .reward-icon {
        font-size: 32rpx;
      }
    }

    button {
      padding: 15rpx 40rpx;
      border-radius: 50rpx;
      font-size: 26rpx;
      border: none;
    }

    .claim-btn {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
    }

    .claimed-btn {
      background: #f8f9fa;
      color: #6c757d;
    }
  }
}
</style>
