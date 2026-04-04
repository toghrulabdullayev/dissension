import { Compass, LogOut, Plus } from 'lucide-react'
import { Button } from '../../../shared/ui/button'
import type { Server } from '../model/types'

type ServerSidebarProps = {
  servers: Server[]
  activeServerId: number | null
  onSelectServer: (serverId: number) => void
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
  return (
    <aside className="sticky top-0 flex h-screen w-20 shrink-0 flex-col border-r border-slate-200 bg-slate-100/70 p-3">
      <div className="mb-3 text-center text-xs font-semibold text-slate-500">SERVERS</div>

      <div className="servers-scroll-region flex-1 overflow-y-auto overflow-x-hidden">

        <button
          type="button"
          onClick={onOpenCreateServer}
          className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-2xl border border-dashed border-slate-300 bg-white text-slate-500 transition hover:bg-slate-200"
          title="Create server"
        >
          <Plus className="h-5 w-5" />
        </button>

        <button
          type="button"
          onClick={onOpenDiscover}
          className={[
            'mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-2xl border transition',
            isDiscoverActive
              ? 'rounded-xl border-slate-900 bg-slate-900 text-white'
              : 'border-slate-300 bg-white text-slate-600 hover:bg-slate-200',
          ].join(' ')}
          title="Discover servers"
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
                className={[
                  'mx-auto flex h-12 w-12 items-center justify-center rounded-2xl text-sm font-bold transition',
                  active
                    ? 'rounded-xl bg-slate-900 text-white'
                    : 'bg-white text-slate-700 hover:bg-slate-200',
                ].join(' ')}
                title={server.name}
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
        className="mx-auto mt-3 h-10 w-12 px-0"
        title="Log out"
      >
        <LogOut className="h-4 w-4" />
      </Button>
    </aside>
  )
}
