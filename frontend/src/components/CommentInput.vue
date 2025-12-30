<template>
  <view class="comment-input-container">
    <view class="input-wrapper">
      <textarea
        v-model="commentText"
        class="comment-input"
        :placeholder="placeholder"
        :maxlength="1000"
        :auto-height="true"
        @input="onInput"
        @focus="onFocus"
      />
      <view class="actions">
        <view class="mention-btn" @click="showMentionPicker">
          <text class="mention-icon">@</text>
        </view>
        <button
          class="send-btn"
          :class="{ disabled: !canSend }"
          :disabled="!canSend"
          @click="onSend"
        >
          发送
        </button>
      </view>
    </view>

    <!-- @ 用户选择器 -->
    <view v-if="showPicker" class="mention-picker">
      <view class="picker-header">
        <text>选择要@的用户</text>
        <view class="close-btn" @click="closePicker">×</view>
      </view>
      <view class="user-list">
        <view
          v-for="user in mentionableUsers"
          :key="user.id"
          class="user-item"
          @click="selectUser(user)"
        >
          <image :src="user.avatarUrl" class="avatar" mode="aspectFill" />
          <text class="nickname">{{ user.nickname }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import type { UserBrief } from '@/types/moment'
import * as momentApi from '@/api/moment'

interface Props {
  placeholder?: string
}

interface Emits {
  (e: 'send', data: { content: string; mentionedUserIds: number[] }): void
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '写评论...'
})

const emit = defineEmits<Emits>()

const commentText = ref('')
const showPicker = ref(false)
const mentionableUsers = ref<UserBrief[]>([])
const mentionedUsers = ref<UserBrief[]>([])

const canSend = computed(() => {
  return commentText.value.trim().length > 0
})

onMounted(async () => {
  try {
    const users = await momentApi.getMentionableUsers()
    if (users) {
      mentionableUsers.value = users
    }
  } catch (error) {
    console.error('获取可提及用户失败:', error)
  }
})

function onInput(e: any) {
  const value = e.detail.value
  // 检测是否输入了 @ 符号
  if (value.endsWith('@')) {
    showMentionPicker()
  }
}

function onFocus() {
  // 聚焦时的处理
}

function showMentionPicker() {
  if (mentionableUsers.value.length > 0) {
    showPicker.value = true
  } else {
    uni.showToast({
      title: '暂无可@的用户',
      icon: 'none'
    })
  }
}

function closePicker() {
  showPicker.value = false
}

function selectUser(user: UserBrief) {
  // 添加到已提及列表
  if (!mentionedUsers.value.find(u => u.id === user.id)) {
    mentionedUsers.value.push(user)
  }

  // 在输入框中插入 @用户名
  let text = commentText.value
  // 如果最后一个字符是 @，替换它
  if (text.endsWith('@')) {
    text = text.slice(0, -1)
  }
  commentText.value = `${text}@${user.nickname} `

  closePicker()
}

function onSend() {
  if (!canSend.value) return

  const mentionedUserIds = mentionedUsers.value.map(u => u.id)

  emit('send', {
    content: commentText.value.trim(),
    mentionedUserIds
  })

  // 清空输入
  commentText.value = ''
  mentionedUsers.value = []
}

// 暴露清空方法
defineExpose({
  clear: () => {
    commentText.value = ''
    mentionedUsers.value = []
  }
})
</script>

<style scoped lang="scss">
.comment-input-container {
  position: relative;
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  padding: 20rpx;
  background-color: #fff;
  border-top: 1rpx solid #e5e5e5;

  .comment-input {
    flex: 1;
    min-height: 70rpx;
    max-height: 200rpx;
    padding: 15rpx 20rpx;
    background-color: #f5f5f5;
    border-radius: 10rpx;
    font-size: 28rpx;
    line-height: 1.5;
    margin-right: 20rpx;
  }

  .actions {
    display: flex;
    align-items: center;
    gap: 10rpx;

    .mention-btn {
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #f5f5f5;
      border-radius: 50%;

      .mention-icon {
        font-size: 36rpx;
        color: #666;
        font-weight: bold;
      }
    }

    .send-btn {
      height: 60rpx;
      line-height: 60rpx;
      padding: 0 30rpx;
      background-color: #007aff;
      color: #fff;
      border: none;
      border-radius: 30rpx;
      font-size: 28rpx;

      &.disabled {
        background-color: #ccc;
        color: #999;
      }
    }
  }
}

.mention-picker {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  max-height: 60vh;
  background-color: #fff;
  border-radius: 20rpx 20rpx 0 0;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.1);
  z-index: 1000;

  .picker-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx;
    border-bottom: 1rpx solid #e5e5e5;

    text {
      font-size: 32rpx;
      font-weight: bold;
    }

    .close-btn {
      width: 50rpx;
      height: 50rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 48rpx;
      color: #999;
      line-height: 1;
    }
  }

  .user-list {
    max-height: 50vh;
    overflow-y: auto;

    .user-item {
      display: flex;
      align-items: center;
      padding: 25rpx 30rpx;
      border-bottom: 1rpx solid #f0f0f0;

      .avatar {
        width: 80rpx;
        height: 80rpx;
        border-radius: 50%;
        margin-right: 20rpx;
      }

      .nickname {
        font-size: 30rpx;
        color: #333;
      }

      &:active {
        background-color: #f5f5f5;
      }
    }
  }
}
</style>
