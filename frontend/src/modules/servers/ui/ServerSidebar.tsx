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
      <aside className="sticky top-0 flex h-screen w-20 shrink-0 flex-col border-r border-(--border) bg-(--surface)/95 p-3">
        <div className="flex justify-center -mt-1.5">
          <img
            src="/logo.png"
            alt="Dissension logo"
            className="h-12 w-12 p-1 ml-0.5"
          />
        </div>

        <div className="mb-3 mt-1.5 -mx-3 border-t border-(--border)" />

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
            className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-2xl border border-dashed border-(--border-visible) bg-(--surface-raised) text-(--text-secondary) transition hover:border-(--text-primary) hover:text-(--text-display)"
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
                ? 'nd-logo-gradient-bg rounded-xl border'
                : 'border-(--border-visible) bg-(--surface-raised) text-(--text-secondary) hover:text-(--text-display)',
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
                    'mx-auto flex h-12 w-12 items-center justify-center rounded-2xl text-sm font-medium transition',
                    active
                      ? 'nd-logo-gradient-bg rounded-xl border'
                      : 'border border-(--border) bg-(--surface-raised) text-(--text-secondary) hover:text-(--text-display)',
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
          className="mx-auto mt-3 h-10 w-12 rounded-2xl px-0"
        >
          <LogOut className="h-4 w-4" />
        </Button>
      </aside>

      {tooltip
        ? createPortal(
            <div
              className="pointer-events-none fixed z-120 -translate-y-1/2"
              style={{ top: tooltip.top, left: tooltip.left }}
            >
              <div className="relative flex items-center gap-1.5 rounded-lg border border-(--border-visible) bg-(--surface-raised) px-2 py-1.5 text-sm font-medium text-(--text-display)">
                <span className="absolute -left-0.5 top-1/2 h-2 w-2 -translate-y-1/2 rotate-45 border-b border-l border-(--border-visible) bg-(--surface-raised)" />
                <span>{tooltip.name}</span>
              </div>
            </div>,
            document.body,
          )
        : null}
    </>
  )
}
