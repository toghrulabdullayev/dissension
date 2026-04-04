import { create } from 'zustand'
import type { Channel } from './types'

type ChannelsState = {
  channelsByServer: Record<string, Channel[]>
  selectedChannelIdByServer: Record<string, string | null>
  createChannel: (serverId: string, name: string, type: Channel['type']) => void
  selectChannel: (serverId: string, channelId: string) => void
}

function makeId(prefix: string): string {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return `${prefix}-${crypto.randomUUID()}`
  }

  return `${prefix}-${Date.now()}-${Math.round(Math.random() * 10000)}`
}

export const useChannelsStore = create<ChannelsState>((set, get) => ({
  channelsByServer: {},
  selectedChannelIdByServer: {},
  createChannel: (serverId, name, type) => {
    const trimmedName = name.trim()
    if (!trimmedName) {
      return
    }

    const existingChannels = get().channelsByServer[serverId] ?? []
    const createdChannel: Channel = {
      id: makeId('chn'),
      name: trimmedName,
      type,
      position: existingChannels.length + 1,
    }

    set((state) => ({
      channelsByServer: {
        ...state.channelsByServer,
        [serverId]: [...existingChannels, createdChannel],
      },
      selectedChannelIdByServer: {
        ...state.selectedChannelIdByServer,
        [serverId]: createdChannel.id,
      },
    }))
  },
  selectChannel: (serverId, channelId) => {
    set((state) => ({
      selectedChannelIdByServer: {
        ...state.selectedChannelIdByServer,
        [serverId]: channelId,
      },
    }))
  },
}))
