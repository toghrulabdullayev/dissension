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

  if (response.status === 204) {
    return undefined as T
  }

  const raw = await response.text()
  if (!raw) {
    return undefined as T
  }

  return JSON.parse(raw) as T
}

export const channelsApi = {
  getChannels: (serverId: string) => request<Channel[]>(`/api/servers/${serverId}/channels`),
  createChannel: (serverId: string, payload: CreateChannelPayload) =>
    request<Channel>(`/api/servers/${serverId}/channels`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  updateChannel: (serverId: string, channelId: string, payload: CreateChannelPayload) =>
    request<Channel>(`/api/servers/${serverId}/channels/${channelId}`, {
      method: 'PATCH',
      body: JSON.stringify(payload),
    }),
  deleteChannel: (serverId: string, channelId: string) =>
    request<void>(`/api/servers/${serverId}/channels/${channelId}`, {
      method: 'DELETE',
    }),
}
