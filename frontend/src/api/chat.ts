import request from './request'

export interface ChatMessageVO {
  id: number
  coupleId: number
  senderId: number
  senderNickname?: string
  senderAvatar?: string
  receiverId: number
  messageType: number
  content?: string
  extraData?: string
  isRead: number
  readTime?: string
  status?: number
  recalledBy?: number
  recalledAt?: string
  createdTime: string
  isSelf?: boolean
}

export const chatApi = {
  getChatHistory(partnerId: number, offset: number = 0, limit: number = 20) {
    return request.get<ChatMessageVO[]>('/api/chat/history', {
      params: { partnerId, offset, limit }
    })
  },

  markAsRead(senderId: number) {
    return request.post('/api/chat/read', null, {
      params: { senderId }
    })
  },

  getUnreadCount() {
    return request.get<number>('/api/chat/unread')
  },

  getUserStatus(userId: number) {
    return request.get<{ userId: number; isOnline: boolean }>(`/api/chat/status/${userId}`)
  },

  recallMessage(messageId: number) {
    return request.post<ChatMessageVO>(`/api/chat/messages/${messageId}/recall`)
  }
}
