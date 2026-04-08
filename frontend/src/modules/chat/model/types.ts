export type ChatMessage = {
  id: string
  serverId: string
  channelId: string
  authorUsername: string
  authorImageUrl: string | null
  content: string
  createdAt: string
}

export type PresenceServerUpdate = {
  serverId: string
  onlineMembers: number
  onlineUsernames: string[]
}

export type ServerMembersUpdatedPayload = {
  serverId: string
}

export type UserBannedFromServerPayload = {
  serverId: string
  serverName: string
  bannedByUsername: string
}

export type ChatSocketEventEnvelope = {
  type: string
  payload: unknown
}
