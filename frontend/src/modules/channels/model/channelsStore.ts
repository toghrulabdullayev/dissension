import { create } from 'zustand'
import { channelsApi } from '../api/channelsApi'
import type { Channel } from './types'

type ChannelsState = {
  channelsByServer: Record<string, Channel[]>
  selectedChannelIdByServer: Record<string, string | null>
  loadingByServer: Record<string, boolean>
  errorByServer: Record<string, string | null>
  loadChannels: (serverId: string) => Promise<void>
  createChannel: (serverId: string, name: string, type: Channel['type']) => Promise<Channel | null>
  updateChannel: (
    serverId: string,
    channelId: string,
    name: string,
    type: Channel['type'],
  ) => Promise<Channel | null>
  deleteChannel: (serverId: string, channelId: string) => Promise<void>
  selectChannel: (serverId: string, channelId: string) => void
  clearChannels: () => void
}

export const useChannelsStore = create<ChannelsState>((set, get) => ({
  channelsByServer: {},
  selectedChannelIdByServer: {},
  loadingByServer: {},
  errorByServer: {},
  loadChannels: async (serverId) => {
    set((state) => ({
      loadingByServer: { ...state.loadingByServer, [serverId]: true },
      errorByServer: { ...state.errorByServer, [serverId]: null },
    }))

    try {
      const channels = await channelsApi.getChannels(serverId)

      set((state) => {
        const currentSelection = state.selectedChannelIdByServer[serverId] ?? null
        const hasSelection = currentSelection != null && channels.some((channel) => channel.id === currentSelection)

        return {
          channelsByServer: { ...state.channelsByServer, [serverId]: channels },
          selectedChannelIdByServer: {
            ...state.selectedChannelIdByServer,
            [serverId]: hasSelection ? currentSelection : (channels[0]?.id ?? null),
          },
          loadingByServer: { ...state.loadingByServer, [serverId]: false },
          errorByServer: { ...state.errorByServer, [serverId]: null },
        }
      })
    } catch (error) {
      set((state) => ({
        loadingByServer: { ...state.loadingByServer, [serverId]: false },
        errorByServer: {
          ...state.errorByServer,
          [serverId]: error instanceof Error ? error.message : 'Failed to load channels',
        },
      }))
    }
  },
  createChannel: async (serverId, name, type) => {
    const trimmedName = name.trim()
    if (!trimmedName) {
      return null
    }

    const createdChannel = await channelsApi.createChannel(serverId, {
      name: trimmedName,
      type,
    })

    const existingChannels = get().channelsByServer[serverId] ?? []

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

    return createdChannel
  },
  updateChannel: async (serverId, channelId, name, type) => {
    const trimmedName = name.trim()
    if (!trimmedName) {
      return null
    }

    const updatedChannel = await channelsApi.updateChannel(serverId, channelId, {
      name: trimmedName,
      type,
    })

    set((state) => {
      const existingChannels = state.channelsByServer[serverId] ?? []

      return {
        channelsByServer: {
          ...state.channelsByServer,
          [serverId]: existingChannels.map((channel) =>
            channel.id === channelId ? updatedChannel : channel,
          ),
        },
      }
    })

    return updatedChannel
  },
  deleteChannel: async (serverId, channelId) => {
    await channelsApi.deleteChannel(serverId, channelId)

    set((state) => {
      const existingChannels = state.channelsByServer[serverId] ?? []
      const nextChannels = existingChannels.filter((channel) => channel.id !== channelId)
      const currentSelection = state.selectedChannelIdByServer[serverId] ?? null

      const nextSelection = currentSelection === channelId
        ? (nextChannels[0]?.id ?? null)
        : currentSelection

      return {
        channelsByServer: {
          ...state.channelsByServer,
          [serverId]: nextChannels,
        },
        selectedChannelIdByServer: {
          ...state.selectedChannelIdByServer,
          [serverId]: nextSelection,
        },
      }
    })
  },
  selectChannel: (serverId, channelId) => {
    set((state) => ({
      selectedChannelIdByServer: {
        ...state.selectedChannelIdByServer,
        [serverId]: channelId,
      },
    }))
  },
  clearChannels: () => {
    set({
      channelsByServer: {},
      selectedChannelIdByServer: {},
      loadingByServer: {},
      errorByServer: {},
    })
  },
}))
