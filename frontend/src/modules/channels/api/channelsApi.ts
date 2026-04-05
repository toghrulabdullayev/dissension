import { useAuthStore } from '../../auth/model/authStore'
import type { Channel, ChannelType } from '../model/types'

const API_BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

type CreateChannelPayload = {
  name: string
  type: ChannelType
}

function getAuthHeaders(): HeadersInit {
  const token = useAuthStore.getState().token

  if (!token) {
    throw new Error('You must be logged in to access channel data.')
  }

  return {
    Authorization: `Bearer ${token}`,
    'Content-Type': 'application/json',
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: {
      ...getAuthHeaders(),
      ...(init?.headers ?? {}),
    },
  })

  if (!response.ok) {
    const body = await response.text()
    throw new Error(body || `Request failed with status ${response.status}`)
  }

  return (await response.json()) as T
}

export const channelsApi = {
  getChannels: (serverId: string) => request<Channel[]>(`/api/servers/${serverId}/channels`),
  createChannel: (serverId: string, payload: CreateChannelPayload) =>
    request<Channel>(`/api/servers/${serverId}/channels`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
}
