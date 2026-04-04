export type ChannelType = 'INFO' | 'CHAT' | 'CALL'

export type Channel = {
  id: string
  name: string
  type: ChannelType
  position: number
}
