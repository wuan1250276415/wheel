<template>
  <view class="moment-page">
    <!-- 头部 -->
    <view class="header">
      <text class="title">情侣动态</text>
      <view class="add-btn" @click="goToCreate">
        <text class="add-icon">+</text>
      </view>
    </view>

    <!-- 动态列表 -->
    <scroll-view
      class="moment-list"
      scroll-y
      @scrolltolower="loadMore"
      refresher-enabled
      @refresherrefresh="onRefresh"
      :refresher-triggered="refreshing"
    >
      <view v-if="moments.length === 0 && !loading" class="empty">
        <text class="empty-text">暂无动态</text>
      </view>

      <view
        v-for="moment in moments"
        :key="moment.id"
        class="moment-item"
        @click="goToDetail(moment.id)"
      >
        <!-- 用户信息 -->
        <view class="user-info">
          <image
            class="avatar"
            :src="moment.avatarUrl || '/static/default-avatar.png'"
            mode="aspectFill"
          />
          <view class="user-content">
            <text class="nickname">{{ moment.nickname }}</text>
            <text class="time">{{ formatTime(moment.createTime) }}</text>
          </view>
          <!-- 操作按钮（仅自己的动态） -->
          <view v-if="canEditMoment(moment)" class="action-btns">
            <view class="edit-btn" @click.stop="goToEdit(moment.id)">
              <text class="edit-icon">✎</text>
            </view>
            <view class="delete-btn" @click.stop="confirmDelete(moment.id)">
              <text class="delete-icon">×</text>
            </view>
          </view>
        </view>

        <!-- 动态内容 -->
        <view class="content">
          <text class="content-text">{{ moment.content }}</text>
        </view>

        <!-- 图片列表 -->
        <view v-if="moment.images && moment.images.length > 0" class="images">
          <image
            v-for="(image, index) in moment.images"
            :key="index"
            class="image-item"
            :class="{ single: moment.images.length === 1 }"
            :src="image"
            mode="aspectFill"
            @click.stop="previewImages(moment.images, index)"
          />
        </view>

        <!-- 操作栏 -->
        <view class="actions">
          <view class="action-item" @click.stop="toggleLike(moment)">
            <text class="icon" :class="{ liked: moment.isLiked }">♥</text>
            <text class="count">{{ moment.likeCount }}</text>
          </view>

          <view class="action-item" @click.stop="goToDetail(moment.id)">
            <text class="icon">💬</text>
            <text class="count">{{ moment.commentCount }}</text>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view v-if="loading" class="loading">
        <text>加载中...</text>
      </view>

      <view v-if="!hasMore && moments.length > 0" class="no-more">
        <text>没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useMomentStore } from '@/stores/moment'
import { useUserStore } from '@/stores/user'
import { formatRelativeTime } from '@/utils'

const momentStore = useMomentStore()
const userStore = useUserStore()

const refreshing = ref(false)

const moments = computed(() => momentStore.moments)
const loading = computed(() => momentStore.isLoadingTimeline)
const hasMore = computed(() => momentStore.hasMore)
const currentUserId = computed(() => userStore.userInfo?.userId || userStore.userInfo?.id)

onMounted(() => {
  momentStore.loadTimeline(true)
})

// 下拉刷新
async function onRefresh() {
  refreshing.value = true
  await momentStore.loadTimeline(true)
  refreshing.value = false
}

// 加载更多
async function loadMore() {
  if (!loading.value && hasMore.value) {
    await momentStore.loadTimeline(false)
  }
}

// 跳转到创建页面
function goToCreate() {
  uni.navigateTo({
    url: '/pages/moment/create'
  })
}

// 跳转到编辑页面
function goToEdit(momentId: string) {
  uni.navigateTo({
    url: `/pages/moment/edit?id=${momentId}`
  })
}

// 跳转到详情页面
function goToDetail(momentId: string) {
  uni.navigateTo({
    url: `/pages/moment/detail?id=${momentId}`
  })
}

// 点赞/取消点赞
async function toggleLike(moment: any) {
  await momentStore.toggleLike(moment.id, moment.isLiked)
}

// 预览图片
function previewImages(images: string[], current: number) {
  uni.previewImage({
    urls: images,
    current: current
  })
}

// 格式化时间
function formatTime(time: string) {
  return formatRelativeTime(time)
}

// 判断是否可以编辑/删除动态
function canEditMoment(moment: any) {
  if (!currentUserId.value || !moment.userId) return false
  return String(moment.userId) === String(currentUserId.value)
}

// 确认删除动态
function confirmDelete(momentId: string | number) {
  uni.showModal({
    title: '确认删除',
    content: '删除后无法恢复，确定要删除这条动态吗？',
    confirmText: '删除',
    confirmColor: '#ff4d4f',
    success: async (res) => {
      if (res.confirm) {
        await momentStore.deleteMoment(momentId)
      }
    }
  })
}
</script>

<style scoped lang="scss">
.moment-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f5f5;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  background-color: #fff;
  border-bottom: 1rpx solid #e5e5e5;

  .title {
    font-size: 36rpx;
    font-weight: bold;
  }

  .add-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #007aff;
    border-radius: 50%;

    .add-icon {
      color: #fff;
      font-size: 40rpx;
      line-height: 1;
    }
  }
}

.moment-list {
  flex: 1;
}

.empty {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 200rpx 0;

  .empty-text {
    color: #999;
    font-size: 28rpx;
  }
}

.moment-item {
  background-color: #fff;
  margin-bottom: 20rpx;
  padding: 30rpx;

  .user-info {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;
    position: relative;

    .avatar {
      width: 80rpx;
      height: 80rpx;
      border-radius: 50%;
      margin-right: 20rpx;
    }

    .user-content {
      flex: 1;
      display: flex;
      flex-direction: column;

      .nickname {
        font-size: 30rpx;
        font-weight: bold;
        margin-bottom: 8rpx;
      }

      .time {
        font-size: 24rpx;
        color: #999;
      }
    }

    .action-btns {
      display: flex;
      gap: 10rpx;

      .edit-btn,
      .delete-btn {
        width: 50rpx;
        height: 50rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: rgba(0, 0, 0, 0.05);
        border-radius: 50%;
      }

      .edit-btn {
        .edit-icon {
          font-size: 32rpx;
          color: #007aff;
          line-height: 1;
        }
      }

      .delete-btn {
        .delete-icon {
          font-size: 40rpx;
          color: #999;
          line-height: 1;
        }
      }
    }
  }

  .content {
    margin-bottom: 20rpx;

    .content-text {
      font-size: 28rpx;
      line-height: 1.6;
      color: #333;
    }
  }

  .images {
    display: flex;
    flex-wrap: wrap;
    gap: 10rpx;
    margin-bottom: 20rpx;

    .image-item {
      width: 220rpx;
      height: 220rpx;
      border-radius: 10rpx;

      &.single {
        width: 400rpx;
        height: 400rpx;
      }
    }
  }

  .actions {
    display: flex;
    gap: 40rpx;
    padding-top: 20rpx;
    border-top: 1rpx solid #f0f0f0;

    .action-item {
      display: flex;
      align-items: center;
      gap: 10rpx;

      .icon {
        font-size: 36rpx;
        color: #666;

        &.liked {
          color: #ff4d4f;
        }
      }

      .count {
        font-size: 24rpx;
        color: #999;
      }
    }
  }
}

.loading,
.no-more {
  padding: 30rpx;
  text-align: center;
  color: #999;
  font-size: 24rpx;
}
</style>
