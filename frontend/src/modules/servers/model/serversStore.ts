import { create } from 'zustand'
import { serversApi } from '../api/serversApi'
import type { DiscoverServer, Server } from './types'

type ServersState = {
  servers: Server[]
  discoverResults: DiscoverServer[]
  discoverLoading: boolean
  discoverError: string | null
  activeServerId: string | null
  isLoading: boolean
  error: string | null
  loadServers: () => Promise<void>
  discoverServers: (query: string) => Promise<void>
  joinServer: (serverId: string) => Promise<Server | null>
  createServer: (name: string, description: string) => Promise<Server | null>
  selectServer: (serverId: string) => void
  clearServers: () => void
}

export const useServersStore = create<ServersState>((set) => ({
  servers: [],
  discoverResults: [],
  discoverLoading: false,
  discoverError: null,
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
  discoverServers: async (query) => {
    set({ discoverLoading: true, discoverError: null })

    try {
      const discoverResults = await serversApi.discoverServers(query)
      set({ discoverResults, discoverLoading: false, discoverError: null })
    } catch (error) {
      set({
        discoverLoading: false,
        discoverError: error instanceof Error ? error.message : 'Failed to discover servers',
      })
    }
  },
  joinServer: async (serverId) => {
    try {
      const joinedServer = await serversApi.joinServer(serverId)

      set((state) => {
        const alreadyInServers = state.servers.some((server) => server.id === joinedServer.id)

        return {
          servers: alreadyInServers
            ? state.servers.map((server) => (server.id === joinedServer.id ? joinedServer : server))
            : [...state.servers, joinedServer],
          discoverResults: state.discoverResults.map((server) =>
            server.id === serverId
              ? { ...server, joined: true, members: joinedServer.members }
              : server,
          ),
          error: null,
        }
      })

      return joinedServer
    } catch (error) {
      set({
        error: error instanceof Error ? error.message : 'Failed to join server',
      })
      return null
    }
  },
  createServer: async (name, description) => {
    const trimmedName = name.trim()
    if (!trimmedName) {
      return null
    }

    const normalizedDescription = description.trim()

    const createdServer = await serversApi.createServer({
      name: trimmedName,
      description: normalizedDescription.length > 0 ? normalizedDescription : null,
    })

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
      discoverResults: [],
      discoverLoading: false,
      discoverError: null,
      activeServerId: null,
      isLoading: false,
      error: null,
    })
  },
}))
