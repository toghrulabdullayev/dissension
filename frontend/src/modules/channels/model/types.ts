export type ChannelType = 'INFO' | 'CHAT' | 'CALL'

export type Channel = {
  id: number
  name: string
  type: ChannelType
  position: number
}
