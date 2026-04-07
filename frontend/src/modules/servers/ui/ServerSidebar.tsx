import { Compass, LogOut, Plus } from 'lucide-react'
import { useState } from 'react'
import { createPortal } from 'react-dom'
import { Button } from '../../../shared/ui/button'
import type { Server } from '../model/types'

type ServerSidebarProps = {
  servers: Server[]
  activeServerId: string | null
  onSelectServer: (serverId: string) => void
  onOpenCreateServer: () => void
  onOpenDiscover: () => void
  isDiscoverActive: boolean
  onLogout: () => void
}

export function ServerSidebar({
  servers,
  activeServerId,
  onSelectServer,
  onOpenCreateServer,
  onOpenDiscover,
  isDiscoverActive,
  onLogout,
}: ServerSidebarProps) {
  const [tooltip, setTooltip] = useState<{
    name: string
    top: number
    left: number
  } | null>(null)

  const showServerTooltip = (name: string, button: HTMLButtonElement) => {
    const rect = button.getBoundingClientRect()
    setTooltip({
      name,
      top: rect.top + rect.height / 2,
      left: rect.right + 9,
    })
  }

  const hideServerTooltip = () => {
    setTooltip(null)
  }

  return (
    <>
      <aside className="sticky top-0 flex h-screen w-20 shrink-0 flex-col border-r border-slate-200 bg-slate-100/70 p-3">
        <p className="mb-3 px-1 text-[11px] font-semibold uppercase tracking-[0.08em] text-slate-400">
          Servers
        </p>

        <div
          className="servers-scroll-region flex-1 overflow-y-auto overflow-x-hidden"
          onScroll={hideServerTooltip}
        >
          <button
            type="button"
            onClick={onOpenCreateServer}
            onMouseEnter={(event) => showServerTooltip('Create server', event.currentTarget)}
            onMouseLeave={hideServerTooltip}
            onFocus={(event) => showServerTooltip('Create server', event.currentTarget)}
            onBlur={hideServerTooltip}
            className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-2xl border border-dashed border-slate-300 bg-white text-slate-500 transition hover:bg-slate-200"
          >
            <Plus className="h-5 w-5" />
          </button>

          <button
            type="button"
            onClick={onOpenDiscover}
            onMouseEnter={(event) => showServerTooltip('Discover servers', event.currentTarget)}
            onMouseLeave={hideServerTooltip}
            onFocus={(event) => showServerTooltip('Discover servers', event.currentTarget)}
            onBlur={hideServerTooltip}
            className={[
              'mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-2xl border transition',
              isDiscoverActive
                ? 'rounded-xl border-slate-900 bg-slate-900 text-white'
                : 'border-slate-300 bg-white text-slate-600 hover:bg-slate-200',
            ].join(' ')}
          >
            <Compass className="h-5 w-5" />
          </button>

          <div className="flex flex-col gap-2 pb-2">
            {servers.map((server) => {
              const active = server.id === activeServerId

              return (
                <button
                  key={server.id}
                  type="button"
                  onClick={() => onSelectServer(server.id)}
                  onMouseEnter={(event) => showServerTooltip(server.name, event.currentTarget)}
                  onMouseLeave={hideServerTooltip}
                  onFocus={(event) => showServerTooltip(server.name, event.currentTarget)}
                  onBlur={hideServerTooltip}
                  className={[
                    'mx-auto flex h-12 w-12 items-center justify-center rounded-2xl text-sm font-bold transition',
                    active
                      ? 'rounded-xl bg-slate-900 text-white'
                      : 'bg-white text-slate-700 hover:bg-slate-200',
                  ].join(' ')}
                >
                  {server.name.slice(0, 2).toUpperCase()}
                </button>
              )
            })}
          </div>
        </div>

        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={onLogout}
          onMouseEnter={(event) => showServerTooltip('Log out', event.currentTarget)}
          onMouseLeave={hideServerTooltip}
          onFocus={(event) => showServerTooltip('Log out', event.currentTarget)}
          onBlur={hideServerTooltip}
          className="mx-auto mt-3 h-10 w-12 px-0"
        >
          <LogOut className="h-4 w-4" />
        </Button>
      </aside>

      {tooltip
        ? createPortal(
            <div
              className="pointer-events-none fixed z-[120] -translate-y-1/2"
              style={{ top: tooltip.top, left: tooltip.left }}
            >
              <div className="relative flex items-center gap-1.5 rounded-lg border border-slate-600/50 bg-slate-900 px-2 py-1.5 text-sm font-semibold text-slate-100 shadow-xl">
                <span className="absolute -left-0.5 top-1/2 h-2 w-2 -translate-y-1/2 rotate-45 border-b border-l border-slate-600/50 bg-slate-900" />
                <span>{tooltip.name}</span>
              </div>
            </div>,
            document.body,
          )
        : null}
    </>
  )
}
