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
            <Card
              key={server.id}
              className="group mx-auto flex h-96 w-[90%] flex-col overflow-hidden rounded-lg border-slate-200 p-0 shadow-sm transition-[border-color,box-shadow] duration-300 hover:border-slate-400 hover:shadow-md"
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

              <div className="relative z-0 flex flex-1 flex-col bg-white px-4 pb-12 pt-11">
                <div className="mb-1.5 flex items-center gap-1.5">
                  <span className="h-2.5 w-2.5 rounded-full bg-slate-500" />
                  <h3 className="line-clamp-1 text-xl font-bold leading-tight">{server.name}</h3>
                </div>

                <p className="mb-3 text-xs text-slate-600">Owner: {server.owner}</p>

                <p className="h-14 overflow-hidden text-sm leading-5 text-slate-700 line-clamp-3">
                  {normalizeDescription(server.description)}
                </p>

                <div className="absolute bottom-3 left-4 z-10 -translate-x-2 translate-y-2 opacity-0 transition-all duration-300 ease-out group-hover:translate-x-0 group-hover:translate-y-0 group-hover:opacity-100">
                  <Button type="button" size="sm" className="h-7 px-3 text-xs">
                    Join
                  </Button>
                </div>

                <p className="absolute bottom-4 left-28 right-4 text-right text-sm text-slate-600">
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
