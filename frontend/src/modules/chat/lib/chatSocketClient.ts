import type { ChatSocketEventEnvelope } from '../model/types'

const API_BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

type ChatSocketHandlers = {
  onOpen?: () => void
  onClose?: () => void
  onEvent?: (event: ChatSocketEventEnvelope) => void
}

type SendMessageCommand = {
  type: 'send_message'
  serverId: string
  channelId: string
  content: string
}

function buildChatSocketUrl(token: string) {
  const apiUrl = new URL(API_BASE_URL)
  const socketProtocol = apiUrl.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${socketProtocol}//${apiUrl.host}/ws/chat?token=${encodeURIComponent(token)}`
}

class ChatSocketClient {
  private socket: WebSocket | null = null

  private handlers: ChatSocketHandlers = {}

  connect(token: string, handlers: ChatSocketHandlers) {
    if (
      this.socket &&
      (this.socket.readyState === WebSocket.OPEN || this.socket.readyState === WebSocket.CONNECTING)
    ) {
      this.handlers = handlers
      return
    }

    this.handlers = handlers
    const socket = new WebSocket(buildChatSocketUrl(token))
    this.socket = socket

    socket.onopen = () => {
      if (this.socket !== socket) {
        socket.close()
        return
      }

      this.handlers.onOpen?.()
    }

    socket.onclose = () => {
      if (this.socket !== socket) {
        return
      }

      this.handlers.onClose?.()
      this.socket = null
    }

    socket.onerror = () => {
      if (this.socket !== socket) {
        return
      }

      this.handlers.onClose?.()
    }

    socket.onmessage = (event) => {
      if (this.socket !== socket) {
        return
      }

      try {
        const parsed = JSON.parse(event.data) as ChatSocketEventEnvelope
        this.handlers.onEvent?.(parsed)
      } catch {
        // Ignore invalid websocket payloads.
      }
    }
  }

  disconnect() {
    if (!this.socket) {
      return
    }

    const socket = this.socket
    this.socket = null
    this.handlers = {}

    if (socket.readyState === WebSocket.OPEN) {
      socket.close()
      return
    }

    if (socket.readyState === WebSocket.CONNECTING) {
      // Close after handshake completes to avoid browser warning: closed before established.
      socket.onopen = () => {
        socket.close()
      }
      socket.onmessage = null
      socket.onerror = null
      socket.onclose = null
    }
  }

  isConnected() {
    return this.socket?.readyState === WebSocket.OPEN
  }

  sendMessage(command: SendMessageCommand) {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
      return false
    }

    this.socket.send(JSON.stringify(command))
    return true
  }
}

export const chatSocketClient = new ChatSocketClient()
