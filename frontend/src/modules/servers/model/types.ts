export type ServerRole = 'OWNER' | 'ADMIN' | 'MOD' | 'USER'

export type Server = {
  id: number
  name: string
  description: string | null
  members: number
  role: ServerRole
}

export type DiscoverServer = {
  id: number
  name: string
  description: string | null
  owner: string
  members: number
  onlineMembers: number
  joined: boolean
}
