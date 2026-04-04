import { ImageIcon, Search } from 'lucide-react'
import type { FormEvent } from 'react'
import { Button } from '../../../shared/ui/button'
import { Card } from '../../../shared/ui/card'
import { Input } from '../../../shared/ui/input'
import type { DiscoverServer } from '../model/types'

type DiscoverServersViewProps = {
  query: string
  onQueryChange: (value: string) => void
  onSearch: () => Promise<void>
  servers: DiscoverServer[]
  loading: boolean
  error: string | null
}

function normalizeDescription(description: string | null) {
  const fallback = 'No description provided.'
  const raw = description?.trim() || fallback
  const words = raw.split(/\s+/)

  if (words.length <= 60) {
    return raw
  }

  return `${words.slice(0, 60).join(' ')}...`
}

export function DiscoverServersView({
  query,
  onQueryChange,
  onSearch,
  servers,
  loading,
  error,
}: DiscoverServersViewProps) {
  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    await onSearch()
  }

  return (
    <section className="flex flex-1 flex-col p-6">
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
            <Card key={server.id} className="mx-auto flex h-96 w-[90%] flex-col overflow-hidden rounded-lg p-0 shadow-sm">
              <div className="relative h-[8.8rem] border-b border-slate-200 bg-slate-200">
                <div className="absolute inset-0 flex items-center justify-center gap-1.5 text-slate-500">
                  <ImageIcon className="h-4 w-4" />
                  <span className="text-xs font-medium">Banner Placeholder</span>
                </div>

                <div className="absolute -bottom-7 left-4 flex h-14 w-14 items-center justify-center rounded-2xl border-4 border-white bg-slate-300 text-slate-600">
                  <ImageIcon className="h-5 w-5" />
                </div>
              </div>

              <div className="flex flex-1 flex-col px-4 pb-4 pt-9">
                <div className="mb-1.5 flex items-center gap-1.5">
                  <span className="h-2.5 w-2.5 rounded-full bg-slate-500" />
                  <h3 className="line-clamp-1 text-xl font-bold leading-tight">{server.name}</h3>
                </div>

                <p className="mb-3 text-xs text-slate-600">Owner: {server.owner}</p>

                <p className="h-14 overflow-hidden text-sm leading-5 text-slate-700 line-clamp-3">
                  {normalizeDescription(server.description)}
                </p>

                <p className="mt-3 text-sm text-slate-600">
                  {server.onlineMembers} online • {server.members} members
                </p>
              </div>
            </Card>
          ))}
        </div>
      )}
    </section>
  )
}
