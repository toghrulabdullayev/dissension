import { useEffect, useState } from 'react'
import { Hash, Info, MoreHorizontal, Phone, Plus } from 'lucide-react'
import type { Channel, ChannelType } from '../model/types'

type ChannelsPanelProps = {
  channels: Channel[]
  serverName: string
  hasActiveServer: boolean
  selectedChannelId: string | null
  onSelectChannel: (channelId: string) => void
  onOpenCreateChannel: () => void
  canManageChannels?: boolean
  onOpenUpdateChannel?: (channel: Channel) => void
  onDeleteChannel?: (channel: Channel) => Promise<void>
  onLeaveServer?: () => void
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
  serverName,
  hasActiveServer,
  selectedChannelId,
  onSelectChannel,
  onOpenCreateChannel,
  canManageChannels = false,
  onOpenUpdateChannel,
  onDeleteChannel,
  onLeaveServer,
}: ChannelsPanelProps) {
  const [openChannelMenuFor, setOpenChannelMenuFor] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)

  useEffect(() => {
    if (openChannelMenuFor == null) {
      return
    }

    const handlePointerDown = (event: MouseEvent) => {
      const target = event.target as HTMLElement | null
      if (target?.closest('[data-channel-menu="true"]')) {
        return
      }

      setOpenChannelMenuFor(null)
    }

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setOpenChannelMenuFor(null)
      }
    }

    document.addEventListener('mousedown', handlePointerDown)
    document.addEventListener('keydown', handleKeyDown)

    return () => {
      document.removeEventListener('mousedown', handlePointerDown)
      document.removeEventListener('keydown', handleKeyDown)
    }
  }, [openChannelMenuFor])

  return (
    <section className="flex h-screen w-72 flex-col border-r border-slate-200 bg-white">
      <div className="shrink-0 border-b border-slate-200 px-4 pb-3 pt-4">
        <div className="flex items-center justify-between gap-2">
          <p className="truncate text-sm font-semibold text-slate-800">{serverName}</p>
          <button
            type="button"
            onClick={() => onLeaveServer?.()}
            className="inline-flex items-center rounded-md border border-red-200 px-2 py-1 text-xs font-medium text-red-600 transition hover:bg-red-50"
          >
            Leave
          </button>
        </div>

        <div className="-mx-4 my-3 border-t border-slate-200" />

        <div className="flex items-center justify-between">
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
      </div>

      <div className="servers-scroll-region flex-1 overflow-y-auto overflow-x-hidden p-4">
        {channels.length === 0 ? (
          <p className="text-sm text-slate-500">No channels in this server yet.</p>
        ) : (
          <div className="space-y-1">
            {channels
              .slice()
              .sort((a, b) => a.position - b.position)
              .map((channel) => {
                const active = channel.id === selectedChannelId
                const menuOpen = openChannelMenuFor === channel.id

                return (
                  <div key={channel.id} className="group relative">
                    <button
                      type="button"
                      onClick={() => {
                        setOpenChannelMenuFor(null)
                        onSelectChannel(channel.id)
                      }}
                      className={[
                        'flex w-full items-center gap-2 rounded-md px-3 py-2 text-left text-sm transition',
                        active
                          ? 'bg-slate-900 text-white'
                          : 'text-slate-500 hover:bg-slate-100 hover:text-slate-900',
                      ].join(' ')}
                    >
                      {channelIcon(channel.type)}
                      <span className="truncate">{channel.name}</span>
                      <span
                        className={[
                          'ml-auto text-[10px] uppercase tracking-wide transition',
                          canManageChannels
                            ? (menuOpen ? 'opacity-0' : 'opacity-75 group-hover:opacity-0')
                            : 'opacity-75',
                        ].join(' ')}
                      >
                        {channelTypeLabel(channel.type)}
                      </span>
                    </button>

                    {canManageChannels ? (
                      <button
                        type="button"
                        data-channel-menu="true"
                        onClick={(event) => {
                          event.stopPropagation()
                          setActionError(null)
                          setOpenChannelMenuFor((value) => (value === channel.id ? null : channel.id))
                        }}
                        className={[
                          'absolute right-1.5 top-1/2 inline-flex h-7 w-7 -translate-y-1/2 items-center justify-center rounded-md border transition',
                          menuOpen ? 'opacity-100' : 'opacity-0 group-hover:opacity-100',
                          active
                            ? 'border-slate-700 text-slate-200 hover:bg-slate-800'
                            : 'border-slate-200 text-slate-500 hover:bg-slate-100',
                        ].join(' ')}
                      >
                        <MoreHorizontal className="h-4 w-4" />
                      </button>
                    ) : null}

                    {menuOpen ? (
                      <div
                        data-channel-menu="true"
                        className="absolute right-2 top-10 z-20 w-40 rounded-md border border-slate-200 bg-white p-1 shadow-lg"
                      >
                        <button
                          type="button"
                          className="w-full rounded-sm px-2 py-1.5 text-left text-xs text-slate-700 transition hover:bg-slate-100"
                          onClick={() => {
                            onOpenUpdateChannel?.(channel)
                            setOpenChannelMenuFor(null)
                          }}
                        >
                          Update channel
                        </button>
                        <button
                          type="button"
                          className="w-full rounded-sm px-2 py-1.5 text-left text-xs text-red-600 transition hover:bg-red-50"
                          onClick={async () => {
                            if (!onDeleteChannel) {
                              setOpenChannelMenuFor(null)
                              return
                            }

                            setActionError(null)

                            try {
                              await onDeleteChannel(channel)
                            } catch (error) {
                              setActionError(
                                error instanceof Error ? error.message : `Failed to delete ${channel.name}`,
                              )
                            }

                            setOpenChannelMenuFor(null)
                          }}
                        >
                          Delete channel
                        </button>
                      </div>
                    ) : null}
                  </div>
                )
              })}

            {actionError ? <p className="pt-2 text-xs text-red-600">{actionError}</p> : null}
          </div>
        )}
      </div>
    </section>
  )
}
