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
      <div className="mb-5 rounded-xl border border-(--border) bg-(--surface) p-4">
        <p className="nd-label text-(--text-secondary)">Discover Servers</p>
        <h2 className="mt-1 text-xl font-medium text-[#f0c265]">Find communities by name or description</h2>
        <form className="mt-4 flex gap-2" onSubmit={handleSubmit}>
          <Input
            value={query}
            onChange={(event) => onQueryChange(event.target.value)}
            placeholder="Search servers"
            className="flex-1"
          />
          <Button
            type="submit"
            disabled={loading}
            className="nd-logo-gradient-bg hover:brightness-110"
          >
            <Search className="h-4 w-4" />
            Search
          </Button>
        </form>
      </div>

      {error ? (
        <p className="mb-4 rounded-md border border-(--accent)/60 bg-(--accent-subtle) px-3 py-2 text-sm text-(--accent)">{error}</p>
      ) : null}

      {loading ? (
        <p className="text-sm text-(--text-secondary)">Searching servers...</p>
      ) : servers.length === 0 ? (
        <p className="text-sm text-(--text-secondary)">No servers available for this search.</p>
      ) : (
        <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
          {servers.map((server) => (
            <Card
              key={server.id}
              className={[
                'group flex h-96 w-full flex-col overflow-hidden rounded-xl p-0 transition-colors duration-300',
                server.joined
                  ? 'cursor-default border-(--border-visible) bg-(--surface-raised)/70 hover:border-(--border-visible)'
                  : 'cursor-pointer border-(--border) hover:border-(--text-secondary)',
              ].join(' ')}
              onClick={() => {
                void handleJoinServer(server.id, server.joined)
              }}
            >
              <div className="relative z-10 h-[8.8rem] border-b border-(--border) bg-(--surface-raised)">
                <div className="absolute inset-0 flex items-center justify-center gap-1.5 text-(--text-secondary)">
                  <ImageIcon className="h-4 w-4" />
                  <span className="nd-label text-(--text-secondary)">Banner Placeholder</span>
                </div>

                <div className="absolute -bottom-7 left-4 z-20 flex h-14 w-14 items-center justify-center rounded-2xl border-2 border-(--border-visible) bg-(--surface) text-(--text-secondary)">
                  <ImageIcon className="h-5 w-5" />
                </div>
              </div>

              <div className="relative z-0 flex flex-1 flex-col px-4 pb-12 pt-11">
                <div className="mb-1.5 flex items-center gap-2">
                  <h3 className="line-clamp-1 text-xl font-medium leading-tight text-(--text-display)">{server.name}</h3>
                  {server.joined ? (
                    <span className="nd-label ml-auto rounded-full border border-(--border-visible) bg-(--surface-raised) px-2 py-0.5 text-(--text-secondary)">
                      Joined
                    </span>
                  ) : joiningServerId === server.id ? (
                    <span className="nd-label ml-auto rounded-full border border-(--border-visible) bg-(--surface-raised) px-2 py-0.5 text-(--text-secondary)">
                      Joining...
                    </span>
                  ) : null}
                </div>

                <p className="mb-3 nd-label text-(--text-secondary)">Owner: {server.owner}</p>

                <p className="min-h-16 overflow-hidden whitespace-pre-wrap wrap-anywhere text-sm leading-5 text-(--text-primary) line-clamp-4">
                  {normalizeDescription(server.description)}
                </p>

                <div className="absolute bottom-4 left-4 right-4 flex flex-wrap items-center justify-between gap-y-1 text-sm leading-4 text-(--text-secondary)">
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
