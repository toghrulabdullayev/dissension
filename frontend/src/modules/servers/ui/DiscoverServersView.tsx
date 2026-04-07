import { ImageIcon, Search } from 'lucide-react'
import { useState } from 'react'
import type { FormEvent } from 'react'
import { Button } from '../../../shared/ui/button'
import { Card } from '../../../shared/ui/card'
import { Input } from '../../../shared/ui/input'
import type { DiscoverServer } from '../model/types'

type DiscoverServersViewProps = {
  query: string
  onQueryChange: (value: string) => void
  onSearch: () => Promise<void>
  onJoinServer: (serverId: string) => Promise<void>
  servers: DiscoverServer[]
  loading: boolean
  error: string | null
}

function normalizeDescription(description: string | null) {
  const fallback = 'No description provided.'
  return description?.trim() || fallback
}

export function DiscoverServersView({
  query,
  onQueryChange,
  onSearch,
  onJoinServer,
  servers,
  loading,
  error,
}: DiscoverServersViewProps) {
  const [joiningServerId, setJoiningServerId] = useState<string | null>(null)

  const handleJoinServer = async (serverId: string, alreadyJoined: boolean) => {
    if (alreadyJoined || joiningServerId != null) {
      return
    }

    setJoiningServerId(serverId)

    try {
      await onJoinServer(serverId)
    } finally {
      setJoiningServerId(null)
    }
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    await onSearch()
  }

  return (
    <section className="flex flex-1 flex-col px-4 py-6">
      <div className="mb-5 rounded-md border border-slate-200 bg-slate-50 p-4">
        <p className="text-xs uppercase tracking-wide text-slate-500">Discover Servers</p>
        <h2 className="mt-1 text-xl font-semibold">Find communities by name or description</h2>
        <form className="mt-4 flex gap-2" onSubmit={handleSubmit}>
          <Input
            value={query}
            onChange={(event) => onQueryChange(event.target.value)}
            placeholder="Search servers"
            className="flex-1"
          />
          <Button type="submit" disabled={loading}>
            <Search className="h-4 w-4" />
            Search
          </Button>
        </form>
      </div>

      {error ? (
        <p className="mb-4 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>
      ) : null}

      {loading ? (
        <p className="text-sm text-slate-500">Searching servers...</p>
      ) : servers.length === 0 ? (
        <p className="text-sm text-slate-500">No servers available for this search.</p>
      ) : (
        <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
          {servers.map((server) => (
            <Card
              key={server.id}
              className={[
                'group flex h-96 w-full flex-col overflow-hidden rounded-lg p-0 shadow-sm transition-[border-color,box-shadow,background-color] duration-300',
                server.joined
                  ? 'cursor-default border-slate-300 bg-slate-50/60 hover:border-slate-300 hover:shadow-sm'
                  : 'cursor-pointer border-slate-200 hover:border-slate-400 hover:shadow-md',
              ].join(' ')}
              onClick={() => {
                void handleJoinServer(server.id, server.joined)
              }}
            >
              <div className="relative z-10 h-[8.8rem] border-b border-slate-200 bg-slate-200">
                <div className="absolute inset-0 flex items-center justify-center gap-1.5 text-slate-500">
                  <ImageIcon className="h-4 w-4" />
                  <span className="text-xs font-medium">Banner Placeholder</span>
                </div>

                <div className="absolute -bottom-7 left-4 z-20 flex h-14 w-14 items-center justify-center rounded-2xl border-4 border-white bg-slate-300 text-slate-600">
                  <ImageIcon className="h-5 w-5" />
                </div>
              </div>

              <div className="relative z-0 flex flex-1 flex-col px-4 pb-12 pt-11">
                <div className="mb-1.5 flex items-center gap-2">
                  <h3 className="line-clamp-1 text-xl font-bold leading-tight">{server.name}</h3>
                  {server.joined ? (
                    <span className="ml-auto rounded-full border border-slate-300 bg-slate-100 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-slate-600">
                      Joined
                    </span>
                  ) : joiningServerId === server.id ? (
                    <span className="ml-auto rounded-full border border-slate-300 bg-slate-100 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-slate-600">
                      Joining...
                    </span>
                  ) : null}
                </div>

                <p className="mb-3 text-xs text-slate-600">Owner: {server.owner}</p>

                <p className="min-h-16 overflow-hidden whitespace-pre-wrap break-words [overflow-wrap:anywhere] text-sm leading-5 text-slate-700 line-clamp-4">
                  {normalizeDescription(server.description)}
                </p>

                <div className="absolute bottom-4 left-4 right-4 flex flex-wrap items-center justify-between gap-y-1 text-sm leading-4 text-slate-600">
                  <p className="shrink-0 whitespace-nowrap text-left">{server.onlineMembers} online</p>
                  <p className="ml-auto shrink-0 whitespace-nowrap text-right max-[360px]:ml-0 max-[360px]:w-full max-[360px]:text-left">
                    {server.members} members
                  </p>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}
    </section>
  )
}
