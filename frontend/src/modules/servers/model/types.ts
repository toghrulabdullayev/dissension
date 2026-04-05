export type ServerRole = 'OWNER' | 'ADMIN' | 'MOD' | 'USER'

export type Server = {
  id: string
  name: string
  description: string | null
  members: number
  role: ServerRole
}

export type DiscoverServer = {
  id: string
  name: string
  description: string | null
  owner: string
  members: number
  onlineMembers: number
  joined: boolean
}

export type ServerMember = {
  username: string
  imageUrl: string | null
  role: ServerRole
}
