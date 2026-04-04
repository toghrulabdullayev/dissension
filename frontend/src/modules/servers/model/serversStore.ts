import { create } from 'zustand'
import type { Server } from './types'

type ServersState = {
  servers: Server[]
  activeServerId: string | null
  createServer: (name: string) => void
  selectServer: (serverId: string) => void
}

function makeId(prefix: string): string {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return `${prefix}-${crypto.randomUUID()}`
  }

  return `${prefix}-${Date.now()}-${Math.round(Math.random() * 10000)}`
}

export const useServersStore = create<ServersState>((set) => ({
  servers: [],
  activeServerId: null,
  createServer: (name) => {
    const trimmedName = name.trim()
    if (!trimmedName) {
      return
    }

    const createdServer: Server = {
      id: makeId('srv'),
      name: trimmedName,
      // Creator is owner in this UI-only phase.
      role: 'OWNER',
    }

    set((state) => ({
      servers: [...state.servers, createdServer],
      activeServerId: createdServer.id,
    }))
  },
  selectServer: (serverId) => {
    set({ activeServerId: serverId })
  },
}))
