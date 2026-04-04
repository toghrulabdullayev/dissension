export type ServerRole = 'OWNER' | 'ADMIN' | 'MOD' | 'USER'

export type Server = {
  id: number
  name: string
  role: ServerRole
}
