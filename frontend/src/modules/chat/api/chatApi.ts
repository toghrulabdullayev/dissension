import { useAuthStore } from '../../auth/model/authStore'
import { isTokenActive } from '../../auth/model/token'
import type { ChatMessage } from '../model/types'

const API_BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

function getAuthHeaders(): HeadersInit {
  const authState = useAuthStore.getState()
  const token = authState.token

  if (!isTokenActive(token)) {
    if (token) {
      authState.clearSession()
    }

    throw new Error('Your session has expired. Please sign in again.')
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

  if (response.status === 401) {
    useAuthStore.getState().clearSession()
    throw new Error('Your session has expired. Please sign in again.')
  }

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

export const chatApi = {
  getMessages: (serverId: string, channelId: string) =>
    request<ChatMessage[]>(`/api/servers/${serverId}/channels/${channelId}/messages`),
  sendMessage: (serverId: string, channelId: string, content: string) =>
    request<ChatMessage>(`/api/servers/${serverId}/channels/${channelId}/messages`, {
      method: 'POST',
      body: JSON.stringify({ content }),
    }),
}
