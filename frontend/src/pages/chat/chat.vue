<template>
  <view class="chat-container">
    <view class="chat-header">
      <view class="partner-info">
        <image :src="partnerAvatar" class="avatar" />
        <view class="info">
          <text class="nickname">{{ partnerNickname }}</text>
          <view class="status">
            <view :class="['status-dot', { online: isPartnerOnline }]" />
            <text class="status-text">{{ isPartnerOnline ? '在线' : '离线' }}</text>
          </view>
        </view>
      </view>
    </view>

    <scroll-view
      class="message-list"
      scroll-y
      :scroll-top="scrollTop"
      :scroll-into-view="scrollIntoView"
      @scrolltoupper="loadMore"
    >
      <view
        v-for="msg in chatStore.sortedMessages"
        :key="msg.id"
        :id="`msg-${msg.id}`"
        class="message-item"
        :class="{ self: msg.isSelf }"
        @touchstart="handleTouchStart(msg.id, msg.isSelf || false)"
        @touchend="handleTouchEnd"
        @touchcancel="handleTouchEnd"
      >
        <image v-if="!msg.isSelf" :src="msg.senderAvatar" class="msg-avatar" />

        <view class="message-content">
          <view v-if="msg.status === 2" class="recalled-message">
            <text class="recalled-text">{{ msg.isSelf ? '你' : '对方' }}撤回了一条消息</text>
          </view>

          <template v-else>
            <view v-if="msg.messageType === 1" class="text-message">
              {{ msg.content }}
            </view>

            <view v-else-if="msg.messageType === 2" class="image-message">
              <image
                :src="msg.extraData"
                mode="aspectFit"
                @click="previewImage(msg.extraData!)"
              />
            </view>

            <view v-else-if="msg.messageType === 3" class="wheel-message">
              <text class="wheel-icon">🎡</text>
              <text class="wheel-text">{{ msg.content }}</text>
            </view>

            <view class="message-meta">
              <text class="time">{{ formatTime(msg.createdTime) }}</text>
              <view v-if="msg.isSelf" class="read-status">
                <text v-if="msg.isRead === 0" class="icon">✓</text>
                <text v-else class="icon read">✓✓</text>
              </view>
            </view>
          </template>
        </view>

        <image v-if="msg.isSelf" :src="msg.senderAvatar" class="msg-avatar" />
      </view>
    </scroll-view>

    <view class="input-area">
      <view class="input-wrapper">
        <input
          v-model="inputText"
          class="message-input"
          placeholder="输入消息..."
          @confirm="sendTextMessage"
        />
        <view class="actions">
          <view class="action-btn" @click="toggleEmojiPicker">
            <text class="icon">😀</text>
          </view>
          <view class="action-btn" @click="chooseImage">
            <text class="icon">📷</text>
          </view>
          <view class="action-btn send" @click="sendTextMessage">
            <text class="icon">➤</text>
          </view>
        </view>
      </view>

      <view v-if="showEmojiPicker" class="emoji-picker-container">
        <view class="emoji-grid">
          <view
            v-for="emoji in commonEmojis"
            :key="emoji"
            class="emoji-item"
            @click="insertEmoji(emoji)"
          >
            {{ emoji }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useChatStore } from '@/stores/chat'
import { useUserStore } from '@/stores/user'

const chatStore = useChatStore()
const userStore = useUserStore()

const partnerId = ref<number>(0)
const partnerNickname = ref('对方')
const partnerAvatar = ref('')
const isPartnerOnline = ref(false)

const inputText = ref('')
const scrollTop = ref(0)
const scrollIntoView = ref('')
const loading = ref(false)
const hasMore = ref(true)

const longPressTimer = ref<number | null>(null)
const longPressingMessageId = ref<number | null>(null)

const showEmojiPicker = ref(false)
const commonEmojis = [
  '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂',
  '🙂', '🙃', '😉', '😊', '😇', '🥰', '😍', '🤩',
  '😘', '😗', '😚', '😙', '😋', '😛', '😜', '🤪',
  '😝', '🤑', '🤗', '🤭', '🤫', '🤔', '🤐', '🤨',
  '😐', '😑', '😶', '😏', '😒', '🙄', '😬', '🤥',
  '😌', '😔', '😪', '🤤', '😴', '😷', '🤒', '🤕',
  '👍', '👎', '👏', '🙌', '👐', '🤝', '🙏', '✨',
  '❤️', '💕', '💖', '💗', '💙', '💚', '💛', '🧡'
]

onLoad((options: any) => {
  if (options.partnerId) {
    partnerId.value = Number(options.partnerId)
    partnerNickname.value = options.nickname || '对方'
    partnerAvatar.value = options.avatar || ''
    init()
  }
})

onMounted(() => {
  if (!chatStore.isConnected && userStore.token) {
    chatStore.connectWebSocket(userStore.token)
  }
})

onUnmounted(() => {
  if (partnerId.value) {
    chatStore.markAsRead(partnerId.value)
  }
})

async function init() {
  try {
    await chatStore.loadChatHistory(partnerId.value)
    await chatStore.checkUserOnlineStatus(partnerId.value)
    isPartnerOnline.value = chatStore.partnerOnlineStatus[partnerId.value] || false
    scrollToBottom()
  } catch (error) {
    console.error('初始化聊天失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

async function loadMore() {
  if (loading.value || !hasMore.value) return

  loading.value = true
  try {
    const offset = chatStore.messages.length
    const result = await chatStore.loadChatHistory(partnerId.value, offset, 20)
    if (result.length < 20) {
      hasMore.value = false
    }
  } catch (error) {
    console.error('加载更多失败:', error)
  } finally {
    loading.value = false
  }
}

function sendTextMessage() {
  if (!inputText.value.trim()) return

  const success = chatStore.sendMessage({
    receiverId: partnerId.value,
    messageType: 1,
    content: inputText.value.trim()
  })

  if (success) {
    inputText.value = ''
    nextTick(() => scrollToBottom())
  } else {
    uni.showToast({ title: '发送失败，请检查网络', icon: 'none' })
  }
}

function chooseImage() {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      uni.showLoading({ title: '上传中...' })

      uni.uploadFile({
        url: `${import.meta.env.VITE_UPLOAD_BASE_URL}/image`,
        filePath: res.tempFilePaths[0],
        name: 'file',
        header: {
          Authorization: `Bearer ${userStore.token}`
        },
        success: (uploadRes) => {
          const data = JSON.parse(uploadRes.data)
          if (data.code === 200) {
            chatStore.sendMessage({
              receiverId: partnerId.value,
              messageType: 2,
              extraData: data.data
            })
            nextTick(() => scrollToBottom())
          }
        },
        fail: () => {
          uni.showToast({ title: '上传失败', icon: 'none' })
        },
        complete: () => {
          uni.hideLoading()
        }
      })
    }
  })
}

function previewImage(url: string) {
  uni.previewImage({
    urls: [url],
    current: url
  })
}

function scrollToBottom() {
  nextTick(() => {
    const lastMsg = chatStore.sortedMessages[chatStore.sortedMessages.length - 1]
    if (lastMsg) {
      scrollIntoView.value = `msg-${lastMsg.id}`
    }
  })
}

function formatTime(time: string) {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
  return `${date.getMonth() + 1}-${date.getDate()}`
}

function handleTouchStart(messageId: number, isSelf: boolean) {
  if (!isSelf) return

  longPressTimer.value = window.setTimeout(() => {
    longPressingMessageId.value = messageId
    showRecallMenu(messageId)
  }, 500)
}

function handleTouchEnd() {
  if (longPressTimer.value) {
    clearTimeout(longPressTimer.value)
    longPressTimer.value = null
  }
}

function showRecallMenu(messageId: number) {
  const message = chatStore.messages.find(m => m.id === messageId)
  if (!message || message.status === 2) return

  const messageTime = new Date(message.createdTime).getTime()
  const now = Date.now()
  const diff = now - messageTime

  if (diff > 120000) {
    uni.showToast({ title: '超过2分钟无法撤回', icon: 'none' })
    return
  }

  uni.showActionSheet({
    itemList: ['撤回消息'],
    success: (res) => {
      if (res.tapIndex === 0) {
        handleRecall(messageId)
      }
    }
  })
}

async function handleRecall(messageId: number) {
  try {
    await chatStore.recallMessage(messageId)
    uni.showToast({ title: '已撤回', icon: 'success' })
  } catch (error) {
    console.error('撤回失败:', error)
    uni.showToast({ title: '撤回失败', icon: 'none' })
  }
}

function toggleEmojiPicker() {
  showEmojiPicker.value = !showEmojiPicker.value
}

function insertEmoji(emoji: string) {
  inputText.value += emoji
  showEmojiPicker.value = false
}
</script>

<style scoped lang="scss">
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

.chat-header {
  background: white;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #e5e5e5;

  .partner-info {
    display: flex;
    align-items: center;
    gap: 20rpx;

    .avatar {
      width: 80rpx;
      height: 80rpx;
      border-radius: 50%;
    }

    .info {
      flex: 1;

      .nickname {
        font-size: 32rpx;
        font-weight: bold;
        display: block;
      }

      .status {
        display: flex;
        align-items: center;
        gap: 10rpx;
        margin-top: 10rpx;

        .status-dot {
          width: 12rpx;
          height: 12rpx;
          border-radius: 50%;
          background: #ccc;

          &.online {
            background: #52c41a;
          }
        }

        .status-text {
          font-size: 24rpx;
          color: #999;
        }
      }
    }
  }
}

.message-list {
  flex: 1;
  padding: 20rpx 30rpx;
}

.message-item {
  display: flex;
  gap: 20rpx;
  margin-bottom: 30rpx;

  &.self {
    flex-direction: row-reverse;

    .message-content {
      align-items: flex-end;

      .text-message {
        background: #1890ff;
        color: white;
      }

      .message-meta {
        flex-direction: row-reverse;
      }
    }
  }

  .msg-avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    flex-shrink: 0;
  }

  .message-content {
    display: flex;
    flex-direction: column;
    gap: 10rpx;
    max-width: 500rpx;

    .text-message {
      padding: 20rpx;
      border-radius: 16rpx;
      background: white;
      word-break: break-all;
      font-size: 28rpx;
    }

    .image-message {
      image {
        max-width: 400rpx;
        max-height: 400rpx;
        border-radius: 16rpx;
      }
    }

    .wheel-message {
      padding: 20rpx;
      border-radius: 16rpx;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      display: flex;
      align-items: center;
      gap: 10rpx;

      .wheel-icon {
        font-size: 36rpx;
      }

      .wheel-text {
        font-size: 28rpx;
      }
    }

    .recalled-message {
      padding: 15rpx 30rpx;
      border-radius: 16rpx;
      background: #f5f5f5;

      .recalled-text {
        font-size: 24rpx;
        color: #999;
      }
    }

    .message-meta {
      display: flex;
      align-items: center;
      gap: 10rpx;

      .time {
        font-size: 22rpx;
        color: #999;
      }

      .read-status {
        .icon {
          font-size: 24rpx;
          color: #999;

          &.read {
            color: #1890ff;
          }
        }
      }
    }
  }
}

.input-area {
  background: white;
  border-top: 1rpx solid #e5e5e5;

  .input-wrapper {
    display: flex;
    align-items: center;
    gap: 20rpx;
    padding: 20rpx 30rpx;

    .message-input {
      flex: 1;
      padding: 20rpx;
      border-radius: 40rpx;
      background: #f5f5f5;
      font-size: 28rpx;
    }

    .actions {
      display: flex;
      gap: 15rpx;

      .action-btn {
        width: 70rpx;
        height: 70rpx;
        border-radius: 50%;
        background: #f5f5f5;
        display: flex;
        align-items: center;
        justify-content: center;

        &.send {
          background: #1890ff;

          .icon {
            color: white;
          }
        }

        .icon {
          font-size: 36rpx;
        }
      }
    }
  }

  .emoji-picker-container {
    border-top: 1rpx solid #e5e5e5;
    padding: 20rpx;
    background: #fafafa;
    max-height: 400rpx;
    overflow-y: auto;

    .emoji-grid {
      display: grid;
      grid-template-columns: repeat(8, 1fr);
      gap: 20rpx;

      .emoji-item {
        font-size: 48rpx;
        text-align: center;
        padding: 10rpx;
        cursor: pointer;
        transition: transform 0.2s;

        &:active {
          transform: scale(1.2);
        }
      }
    }
  }
}
</style>
