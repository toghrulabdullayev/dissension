import { create } from 'zustand'
import { serversApi } from '../api/serversApi'
import type { Server } from './types'

type ServersState = {
  servers: Server[]
  activeServerId: number | null
  isLoading: boolean
  error: string | null
  loadServers: () => Promise<void>
  createServer: (name: string) => Promise<Server | null>
  selectServer: (serverId: number) => void
  clearServers: () => void
}

export const useServersStore = create<ServersState>((set) => ({
  servers: [],
  activeServerId: null,
  isLoading: false,
  error: null,
  loadServers: async () => {
    set({ isLoading: true, error: null })

    try {
      const servers = await serversApi.getMyServers()

      set((state) => {
        const hasActiveServer =
          state.activeServerId != null && servers.some((server) => server.id === state.activeServerId)

        return {
          servers,
          isLoading: false,
          error: null,
          activeServerId: hasActiveServer ? state.activeServerId : (servers[0]?.id ?? null),
        }
      })
    } catch (error) {
      set({
        isLoading: false,
        error: error instanceof Error ? error.message : 'Failed to load servers',
      })
    }
  },
  createServer: async (name) => {
    const trimmedName = name.trim()
    if (!trimmedName) {
      return null
    }

    const createdServer = await serversApi.createServer({ name: trimmedName })

    set((state) => ({
      servers: [...state.servers, createdServer],
      activeServerId: createdServer.id,
      error: null,
    }))

    return createdServer
  },
  selectServer: (serverId) => {
    set({ activeServerId: serverId })
  },
  clearServers: () => {
    set({
      servers: [],
      activeServerId: null,
      isLoading: false,
      error: null,
    })
  },
}))
