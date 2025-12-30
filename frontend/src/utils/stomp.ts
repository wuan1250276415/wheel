import { Client, StompConfig, IFrame } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

export interface ChatMessage {
  id?: number
  receiverId: number
  messageType: number
  content?: string
  extraData?: string
}

export interface WebSocketConfig {
  url: string
  token: string
  onConnect?: () => void
  onDisconnect?: () => void
  onMessage?: (message: any) => void
  onError?: (error: any) => void
}

class StompClient {
  private client: Client | null = null
  private config: WebSocketConfig | null = null
  private reconnectDelay = 1000
  private maxReconnectDelay = 30000
  private reconnectAttempts = 0
  private shouldReconnect = true

  connect(config: WebSocketConfig) {
    this.config = config
    this.shouldReconnect = true

    const stompConfig: StompConfig = {
      webSocketFactory: () => {
        const url = config.url.replace('ws://', 'http://').replace('wss://', 'https://')
        return new SockJS(`${url}?token=${config.token}`)
      },
      connectHeaders: {
        Authorization: `Bearer ${config.token}`
      },
      debug: (str: string) => {
        console.log('[STOMP Debug]:', str)
      },
      reconnectDelay: this.reconnectDelay,
      heartbeatIncoming: 20000,
      heartbeatOutgoing: 20000,
      onConnect: (frame: IFrame) => {
        console.log('[STOMP] Connected:', frame)
        this.reconnectAttempts = 0
        this.reconnectDelay = 1000

        this.client?.subscribe('/user/queue/messages', (message) => {
          try {
            const payload = JSON.parse(message.body)
            config.onMessage?.(payload)
          } catch (error) {
            console.error('[STOMP] Message parse error:', error)
          }
        })

        config.onConnect?.()
      },
      onDisconnect: (frame: IFrame) => {
        console.log('[STOMP] Disconnected:', frame)
        config.onDisconnect?.()

        if (this.shouldReconnect) {
          this.scheduleReconnect()
        }
      },
      onStompError: (frame: IFrame) => {
        console.error('[STOMP] Error:', frame)
        config.onError?.(frame)
      },
      onWebSocketError: (event: Event) => {
        console.error('[WebSocket] Error:', event)
        config.onError?.(event)
      }
    }

    this.client = new Client(stompConfig)
    this.client.activate()
  }

  disconnect() {
    this.shouldReconnect = false
    if (this.client) {
      this.client.deactivate()
      this.client = null
    }
  }

  sendMessage(message: ChatMessage) {
    if (!this.client || !this.client.connected) {
      console.error('[STOMP] Not connected')
      return false
    }

    try {
      this.client.publish({
        destination: '/app/chat.send',
        body: JSON.stringify(message)
      })
      return true
    } catch (error) {
      console.error('[STOMP] Send message error:', error)
      return false
    }
  }

  isConnected(): boolean {
    return this.client?.connected || false
  }

  private scheduleReconnect() {
    this.reconnectAttempts++
    this.reconnectDelay = Math.min(
      this.reconnectDelay * 2,
      this.maxReconnectDelay
    )

    console.log(
      `[STOMP] Reconnecting in ${this.reconnectDelay}ms (attempt ${this.reconnectAttempts})`
    )

    setTimeout(() => {
      if (this.shouldReconnect && this.config) {
        this.connect(this.config)
      }
    }, this.reconnectDelay)
  }
}

export const stompClient = new StompClient()
