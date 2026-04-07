import { create } from 'zustand'
import { chatApi } from '../api/chatApi'
import { chatSocketClient } from '../lib/chatSocketClient'
import type { ChatMessage, ChatSocketEventEnvelope, PresenceServerUpdate } from './types'

type ChatState = {
  messagesByChannel: Record<string, ChatMessage[]>
  loadingByChannel: Record<string, boolean>
  errorByChannel: Record<string, string | null>
  onlineUsernamesByServer: Record<string, string[]>
  onlineCountByServer: Record<string, number>
  socketStatus: 'disconnected' | 'connecting' | 'connected'
  connect: (token: string) => void
  disconnect: () => void
  loadChannelMessages: (serverId: string, channelId: string) => Promise<void>
  sendMessage: (serverId: string, channelId: string, content: string) => Promise<void>
  clearChat: () => void
}

function mergeMessage(messages: ChatMessage[], message: ChatMessage) {
  if (messages.some((existingMessage) => existingMessage.id === message.id)) {
    return messages
  }

  return [...messages, message].sort((left, right) => {
    const leftTime = Date.parse(left.createdAt)
    const rightTime = Date.parse(right.createdAt)
    return leftTime - rightTime
  })
}

function isChatMessagePayload(payload: unknown): payload is ChatMessage {
  if (payload == null || typeof payload !== 'object') {
    return false
  }

  const value = payload as Record<string, unknown>
  return (
    typeof value.id === 'string' &&
    typeof value.serverId === 'string' &&
    typeof value.channelId === 'string' &&
    typeof value.authorUsername === 'string' &&
    typeof value.content === 'string' &&
    typeof value.createdAt === 'string'
  )
}

function isPresencePayload(payload: unknown): payload is PresenceServerUpdate {
  if (payload == null || typeof payload !== 'object') {
    return false
  }

  const value = payload as Record<string, unknown>
  return (
    typeof value.serverId === 'string' &&
    typeof value.onlineMembers === 'number' &&
    Array.isArray(value.onlineUsernames)
  )
}

export const useChatStore = create<ChatState>((set, get) => ({
  messagesByChannel: {},
  loadingByChannel: {},
  errorByChannel: {},
  onlineUsernamesByServer: {},
  onlineCountByServer: {},
  socketStatus: 'disconnected',
  connect: (token) => {
    if (!token || get().socketStatus === 'connected') {
      return
    }

    set({ socketStatus: 'connecting' })

    chatSocketClient.connect(token, {
      onOpen: () => set({ socketStatus: 'connected' }),
      onClose: () => set({ socketStatus: 'disconnected' }),
      onEvent: (event: ChatSocketEventEnvelope) => {
        if (event.type === 'chat_message_created' && isChatMessagePayload(event.payload)) {
          const payload = event.payload

          set((state) => ({
            messagesByChannel: {
              ...state.messagesByChannel,
              [payload.channelId]: mergeMessage(
                state.messagesByChannel[payload.channelId] ?? [],
                payload,
              ),
            },
            errorByChannel: {
              ...state.errorByChannel,
              [payload.channelId]: null,
            },
          }))
          return
        }

        if (event.type === 'presence_server_updated' && isPresencePayload(event.payload)) {
          const payload = event.payload

          const onlineUsernames = payload.onlineUsernames
            .filter((username) => typeof username === 'string')
            .map((username) => username.toLowerCase())

          set((state) => ({
            onlineUsernamesByServer: {
              ...state.onlineUsernamesByServer,
              [payload.serverId]: onlineUsernames,
            },
            onlineCountByServer: {
              ...state.onlineCountByServer,
              [payload.serverId]: payload.onlineMembers,
            },
          }))
        }
      },
    })
  },
  disconnect: () => {
    chatSocketClient.disconnect()
    set({ socketStatus: 'disconnected' })
  },
  loadChannelMessages: async (serverId, channelId) => {
    set((state) => ({
      loadingByChannel: { ...state.loadingByChannel, [channelId]: true },
      errorByChannel: { ...state.errorByChannel, [channelId]: null },
    }))

    try {
      const messages = await chatApi.getMessages(serverId, channelId)
      set((state) => ({
        messagesByChannel: {
          ...state.messagesByChannel,
          [channelId]: messages,
        },
        loadingByChannel: { ...state.loadingByChannel, [channelId]: false },
        errorByChannel: { ...state.errorByChannel, [channelId]: null },
      }))
    } catch (error) {
      set((state) => ({
        loadingByChannel: { ...state.loadingByChannel, [channelId]: false },
        errorByChannel: {
          ...state.errorByChannel,
          [channelId]: error instanceof Error ? error.message : 'Failed to load messages',
        },
      }))
    }
  },
  sendMessage: async (serverId, channelId, content) => {
    const normalized = content.trim()
    if (normalized.length === 0) {
      return
    }

    const sentOverSocket = chatSocketClient.sendMessage({
      type: 'send_message',
      serverId,
      channelId,
      content: normalized,
    })

    if (sentOverSocket) {
      return
    }

    const created = await chatApi.sendMessage(serverId, channelId, normalized)
    set((state) => ({
      messagesByChannel: {
        ...state.messagesByChannel,
        [channelId]: mergeMessage(state.messagesByChannel[channelId] ?? [], created),
      },
      errorByChannel: {
        ...state.errorByChannel,
        [channelId]: null,
      },
    }))
  },
  clearChat: () => {
    chatSocketClient.disconnect()
    set({
      messagesByChannel: {},
      loadingByChannel: {},
      errorByChannel: {},
      onlineUsernamesByServer: {},
      onlineCountByServer: {},
      socketStatus: 'disconnected',
    })
  },
}))
