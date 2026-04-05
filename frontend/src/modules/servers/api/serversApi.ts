import { useAuthStore } from '../../auth/model/authStore'
import type { DiscoverServer, Server, ServerMember } from '../model/types'

const API_BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

type CreateServerPayload = {
  name: string
  description?: string | null
}

function getAuthHeaders(): HeadersInit {
  const token = useAuthStore.getState().token

  if (!token) {
    throw new Error('You must be logged in to access server data.')
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

export const serversApi = {
  getMyServers: () => request<Server[]>('/api/servers/my'),
  discoverServers: (query: string) => {
    const params = new URLSearchParams()

    if (query.trim().length > 0) {
      params.set('query', query.trim())
    }

    const queryString = params.toString()
    const path = queryString ? `/api/servers/discover?${queryString}` : '/api/servers/discover'

    return request<DiscoverServer[]>(path)
  },
  joinServer: (serverId: string) =>
    request<Server>(`/api/servers/${serverId}/join`, {
      method: 'POST',
    }),
  getServerMembers: (serverId: string) => request<ServerMember[]>(`/api/servers/${serverId}/members`),
  updateServerMemberRole: (serverId: string, memberUsername: string, role: 'ADMIN' | 'USER') =>
    request<ServerMember[]>(`/api/servers/${serverId}/members/${encodeURIComponent(memberUsername)}/role`, {
      method: 'PATCH',
      body: JSON.stringify({ role }),
    }),
  banServerMember: (serverId: string, memberUsername: string) =>
    request<ServerMember[]>(`/api/servers/${serverId}/members/${encodeURIComponent(memberUsername)}`, {
      method: 'DELETE',
    }),
  createServer: (payload: CreateServerPayload) =>
    request<Server>('/api/servers', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
}
