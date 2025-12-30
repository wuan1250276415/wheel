import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { chatApi, ChatMessageVO } from '@/api/chat'
import { stompClient, ChatMessage, WebSocketConfig } from '@/utils/stomp'
import { useUserStore } from './user'

export const useChatStore = defineStore('chat', () => {
  const userStore = useUserStore()

  const messages = ref<ChatMessageVO[]>([])
  const unreadCount = ref(0)
  const partnerOnlineStatus = ref<Record<number, boolean>>({})
  const isConnected = ref(false)
  const currentPartnerId = ref<number | null>(null)

  const sortedMessages = computed(() => {
    return [...messages.value].sort((a, b) => {
      return new Date(a.createdTime).getTime() - new Date(b.createdTime).getTime()
    })
  })

  function connectWebSocket(token: string) {
    const config: WebSocketConfig = {
      url: import.meta.env.VITE_WS_BASE_URL || 'ws://localhost:8080/ws',
      token,
      onConnect: () => {
        console.log('[Chat] WebSocket connected')
        isConnected.value = true
      },
      onDisconnect: () => {
        console.log('[Chat] WebSocket disconnected')
        isConnected.value = false
      },
      onMessage: (message: ChatMessageVO) => {
        console.log('[Chat] Received message:', message)

        // 判断消息是否是自己发送的
        const currentUserId = userStore.userInfo?.userId || userStore.userInfo?.id
        const isSelfMessage = message.senderId === currentUserId

        // 确保 isSelf 字段正确设置
        if (message.isSelf === undefined) {
          message.isSelf = isSelfMessage
        }

        if (message.status === 2) {
          updateMessage(message)
        } else {
          addMessage(message)
          // 只有接收到他人发送的消息才增加未读计数
          if (!isSelfMessage && message.senderId !== currentUserId) {
            unreadCount.value++
          }
        }
      },
      onError: (error) => {
        console.error('[Chat] WebSocket error:', error)
      }
    }

    stompClient.connect(config)
  }

  function disconnectWebSocket() {
    stompClient.disconnect()
    isConnected.value = false
  }

  async function loadChatHistory(partnerId: number, offset: number = 0, limit: number = 20) {
    try {
      currentPartnerId.value = partnerId
      const result = await chatApi.getChatHistory(partnerId, offset, limit)
      if (offset === 0) {
        messages.value = result.reverse()
      } else {
        messages.value = [...result.reverse(), ...messages.value]
      }
      return result
    } catch (error) {
      console.error('[Chat] Load history error:', error)
      throw error
    }
  }

  function sendMessage(message: ChatMessage) {
    if (!isConnected.value) {
      console.error('[Chat] WebSocket not connected')
      return false
    }

    const success = stompClient.sendMessage(message)
    if (success) {
      const currentUserId = userStore.userInfo?.userId || userStore.userInfo?.id || 0
      const tempId = `temp_${Date.now()}_${Math.random()}`
      const tempMessage: ChatMessageVO = {
        id: tempId as any,
        coupleId: 0,
        senderId: currentUserId,
        senderNickname: userStore.userInfo?.nickname,
        senderAvatar: userStore.userInfo?.avatar,
        receiverId: message.receiverId,
        messageType: message.messageType,
        content: message.content,
        extraData: message.extraData,
        isRead: 0,
        createdTime: new Date().toISOString(),
        isSelf: true
      }
      messages.value.push(tempMessage)
    }
    return success
  }

  function addMessage(message: ChatMessageVO) {
    // 查找本地生成的临时消息进行替换
    const tempIndex = messages.value.findIndex(m => {
      const isTemp = typeof m.id === 'string' && (m.id as any).startsWith('temp_')
      if (!isTemp) return false

      // 匹配规则：发送者相同、类型相同、内容相同（针对文本消息）
      const sameSender = Number(m.senderId) === Number(message.senderId)
      const sameType = m.messageType === message.messageType
      const sameContent = m.content === message.content

      // 时间差在 5 秒内
      const timeDiff = Math.abs(new Date(m.createdTime).getTime() - new Date(message.createdTime).getTime())
      const closelyTimed = timeDiff < 5000

      return sameSender && sameType && sameContent && closelyTimed
    })

    if (tempIndex !== -1) {
      // 找到匹配的临时消息，直接用服务器返回的真实消息替换它
      messages.value[tempIndex] = message
    } else {
      // 如果没找到临时消息（可能是别人发的，或者本地还没渲染出来），检查是否已存在（避免 WS 重复推送）
      const exists = messages.value.find(m => m.id === message.id)
      if (!exists) {
        messages.value.push(message)
      }
    }
  }

  function updateMessage(updatedMessage: ChatMessageVO) {
    const index = messages.value.findIndex(m => m.id === updatedMessage.id)
    if (index !== -1) {
      messages.value[index] = { ...messages.value[index], ...updatedMessage }
    }
  }

  async function markAsRead(senderId: number) {
    try {
      await chatApi.markAsRead(senderId)
      messages.value.forEach(msg => {
        if (msg.senderId === senderId && msg.isRead === 0) {
          msg.isRead = 1
          msg.readTime = new Date().toISOString()
        }
      })
      await loadUnreadCount()
    } catch (error) {
      console.error('[Chat] Mark as read error:', error)
    }
  }

  async function loadUnreadCount() {
    try {
      unreadCount.value = await chatApi.getUnreadCount()
    } catch (error) {
      console.error('[Chat] Load unread count error:', error)
    }
  }

  async function checkUserOnlineStatus(userId: number) {
    try {
      const result = await chatApi.getUserStatus(userId)
      partnerOnlineStatus.value[userId] = result.isOnline
      return result.isOnline
    } catch (error) {
      console.error('[Chat] Check online status error:', error)
      return false
    }
  }

  async function recallMessage(messageId: number) {
    try {
      const recalled = await chatApi.recallMessage(messageId)
      updateMessage(recalled)
      return recalled
    } catch (error) {
      console.error('[Chat] Recall message error:', error)
      throw error
    }
  }

  function clearMessages() {
    messages.value = []
    currentPartnerId.value = null
  }

  return {
    messages,
    sortedMessages,
    unreadCount,
    partnerOnlineStatus,
    isConnected,
    currentPartnerId,
    connectWebSocket,
    disconnectWebSocket,
    loadChatHistory,
    sendMessage,
    addMessage,
    updateMessage,
    markAsRead,
    loadUnreadCount,
    checkUserOnlineStatus,
    recallMessage,
    clearMessages
  }
})
