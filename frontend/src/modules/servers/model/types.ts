export type ServerRole = 'OWNER' | 'ADMIN' | 'MOD' | 'USER'

export type Server = {
  id: string
  name: string
  role: ServerRole
}
