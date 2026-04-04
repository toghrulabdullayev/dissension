import { Hash, Info, Phone, Plus } from 'lucide-react'
import type { Channel, ChannelType } from '../model/types'

type ChannelsPanelProps = {
  channels: Channel[]
  hasActiveServer: boolean
  selectedChannelId: string | null
  onSelectChannel: (channelId: string) => void
  onOpenCreateChannel: () => void
}

function channelIcon(type: ChannelType) {
  if (type === 'INFO') {
    return <Info className="h-4 w-4" />
  }

  if (type === 'CALL') {
    return <Phone className="h-4 w-4" />
  }

  return <Hash className="h-4 w-4" />
}

function channelTypeLabel(type: ChannelType) {
  if (type === 'INFO') {
    return 'info'
  }

  if (type === 'CALL') {
    return 'call'
  }

  return 'chat'
}

export function ChannelsPanel({
  channels,
  hasActiveServer,
  selectedChannelId,
  onSelectChannel,
  onOpenCreateChannel,
}: ChannelsPanelProps) {
  return (
    <section className="w-72 border-r border-slate-200 p-4">
      <div className="mb-3 flex items-center justify-between">
        <h2 className="text-sm font-semibold text-slate-500">Channels</h2>
        <button
          type="button"
          onClick={onOpenCreateChannel}
          disabled={!hasActiveServer}
          className="inline-flex items-center gap-1 rounded-md border border-slate-200 px-2 py-1 text-xs text-slate-500 transition hover:bg-slate-100"
        >
          <Plus className="h-3 w-3" />
          New
        </button>
      </div>

      {channels.length === 0 ? (
        <p className="text-sm text-slate-500">No channels in this server yet.</p>
      ) : (
        <div className="space-y-1">
          {channels
            .slice()
            .sort((a, b) => a.position - b.position)
            .map((channel) => {
              const active = channel.id === selectedChannelId

              return (
                <button
                  key={channel.id}
                  type="button"
                  onClick={() => onSelectChannel(channel.id)}
                  className={[
                    'flex w-full items-center gap-2 rounded-md px-3 py-2 text-left text-sm transition',
                    active
                      ? 'bg-slate-900 text-white'
                      : 'text-slate-500 hover:bg-slate-100 hover:text-slate-900',
                  ].join(' ')}
                >
                  {channelIcon(channel.type)}
                  <span className="truncate">{channel.name}</span>
                  <span className="ml-auto text-[10px] uppercase tracking-wide opacity-75">
                    {channelTypeLabel(channel.type)}
                  </span>
                </button>
              )
            })}
        </div>
      )}
    </section>
  )
}
