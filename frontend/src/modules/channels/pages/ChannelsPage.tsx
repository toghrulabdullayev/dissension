import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useAuthStore } from '../../auth/model/authStore'
import { useChannelsStore } from '../model/channelsStore'
import { ChannelWorkspace } from '../ui/ChannelWorkspace'
import { ChannelsPanel } from '../ui/ChannelsPanel'
import { CreateChannelDialog } from '../ui/CreateChannelDialog'
import { useServersStore } from '../../servers/model/serversStore'
import { serversApi } from '../../servers/api/serversApi'
import { CreateServerDialog } from '../../servers/ui/CreateServerDialog'
import { DiscoverServersView } from '../../servers/ui/DiscoverServersView'
import { ServerSidebar } from '../../servers/ui/ServerSidebar'
import type { ServerMember } from '../../servers/model/types'

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i

function normalizeUuid(value: string | undefined): string | null {
  if (!value) {
    return null
  }

  return UUID_PATTERN.test(value) ? value : null
}

export function ChannelsPage() {
  const navigate = useNavigate()
  const params = useParams<{ serverId?: string; channelId?: string }>()
  const username = useAuthStore((state) => state.username)
  const token = useAuthStore((state) => state.token)
  const clearSession = useAuthStore((state) => state.clearSession)
  const servers = useServersStore((state) => state.servers)
  const loadServers = useServersStore((state) => state.loadServers)
  const createServer = useServersStore((state) => state.createServer)
  const discoverServers = useServersStore((state) => state.discoverServers)
  const joinServer = useServersStore((state) => state.joinServer)
  const serversLoading = useServersStore((state) => state.isLoading)
  const discoverResults = useServersStore((state) => state.discoverResults)
  const discoverLoading = useServersStore((state) => state.discoverLoading)
  const discoverError = useServersStore((state) => state.discoverError)
  const selectServer = useServersStore((state) => state.selectServer)
  const clearServers = useServersStore((state) => state.clearServers)

  const channelsByServer = useChannelsStore((state) => state.channelsByServer)
  const loadChannels = useChannelsStore((state) => state.loadChannels)
  const createChannel = useChannelsStore((state) => state.createChannel)
  const clearChannels = useChannelsStore((state) => state.clearChannels)
  const [isCreateServerOpen, setIsCreateServerOpen] = useState(false)
  const [isCreateChannelOpen, setIsCreateChannelOpen] = useState(false)
  const [discoverQuery, setDiscoverQuery] = useState('')
  const [serverMembers, setServerMembers] = useState<ServerMember[]>([])
  const [membersLoading, setMembersLoading] = useState(false)

  const normalizedServerId = normalizeUuid(params.serverId)
  const normalizedChannelId = normalizeUuid(params.channelId)

  const channels = useMemo(() => {
    if (!normalizedServerId) {
      return []
    }

    return channelsByServer[normalizedServerId] ?? []
  }, [normalizedServerId, channelsByServer])

  const selectedChannel =
    channels.find((channel) => channel.id === normalizedChannelId) ?? null

  const activeServer =
    servers.find((server) => server.id === normalizedServerId) ?? null

  const handleLogout = () => {
    clearServers()
    clearChannels()
    clearSession()
    navigate('/login')
  }

  useEffect(() => {
    if (!token) {
      return
    }

    void loadServers()
  }, [token, loadServers])

  useEffect(() => {
    if (!token || normalizedServerId == null || !activeServer) {
      return
    }

    void loadChannels(normalizedServerId)
  }, [token, normalizedServerId, activeServer, loadChannels])

  useEffect(() => {
    if (!token || normalizedServerId == null || activeServer || serversLoading) {
      return
    }

    if (servers.length > 0) {
      navigate(`/channels/${servers[0].id}`, { replace: true })
      return
    }

    navigate('/channels', { replace: true })
  }, [token, normalizedServerId, activeServer, serversLoading, servers, navigate])

  useEffect(() => {
    if (!token || !activeServer) {
      return
    }

    if (channels.length === 0) {
      if (params.channelId) {
        navigate(`/channels/${activeServer.id}`, { replace: true })
      }
      return
    }

    const channelExists = normalizedChannelId != null && channels.some((channel) => channel.id === normalizedChannelId)

    if (!channelExists) {
      navigate(`/channels/${activeServer.id}/${channels[0].id}`, { replace: true })
    }
  }, [
    token,
    activeServer,
    channels,
    normalizedChannelId,
    params.channelId,
    navigate,
  ])

  useEffect(() => {
    if (!token || activeServer || serversLoading || normalizedServerId != null) {
      return
    }

    void discoverServers('')
  }, [token, activeServer, serversLoading, normalizedServerId, discoverServers])

  useEffect(() => {
    if (!token || !activeServer) {
      setServerMembers([])
      setMembersLoading(false)
      return
    }

    let cancelled = false
    setMembersLoading(true)

    void serversApi
      .getServerMembers(activeServer.id)
      .then((members) => {
        if (cancelled) {
          return
        }

        setServerMembers(members)
      })
      .catch(() => {
        if (cancelled) {
          return
        }

        setServerMembers([])
      })
      .finally(() => {
        if (cancelled) {
          return
        }

        setMembersLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [token, activeServer])

  return (
    <div className="min-h-screen bg-white text-slate-900">
      <div className="flex min-h-screen">
        <ServerSidebar
          servers={servers}
          activeServerId={normalizedServerId}
          onSelectServer={(serverId) => {
            selectServer(serverId)

            const targetChannels = channelsByServer[serverId] ?? []
            if (targetChannels.length > 0) {
              navigate(`/channels/${serverId}/${targetChannels[0].id}`)
              return
            }

            navigate(`/channels/${serverId}`)
          }}
          onOpenCreateServer={() => setIsCreateServerOpen(true)}
          onOpenDiscover={() => navigate('/channels')}
          isDiscoverActive={activeServer == null}
          onLogout={handleLogout}
        />

        <main className="flex flex-1">
          {activeServer ? (
            <>
              <ChannelsPanel
                channels={channels}
                serverName={activeServer.name}
                hasActiveServer={true}
                selectedChannelId={normalizedChannelId}
                onSelectChannel={(channelId) => {
                  navigate(`/channels/${activeServer.id}/${channelId}`)
                }}
                onOpenCreateChannel={() => setIsCreateChannelOpen(true)}
                onLeaveServer={async () => {
                  try {
                    await serversApi.leaveServer(activeServer.id)
                    setServerMembers([])
                    await loadServers()
                    navigate('/channels', { replace: true })
                  } catch (error) {
                    console.error(error)
                  }
                }}
              />
              <ChannelWorkspace
                username={username}
                selectedChannel={selectedChannel}
                currentRole={activeServer?.role ?? 'USER'}
                serverMembers={serverMembers}
                membersLoading={membersLoading}
                membersCount={serverMembers.length > 0 ? serverMembers.length : activeServer.members}
                onUpdateMemberRole={async (targetUsername, role) => {
                  const updatedMembers = await serversApi.updateServerMemberRole(activeServer.id, targetUsername, role)
                  setServerMembers(updatedMembers)
                }}
                onBanMember={async (targetUsername) => {
                  const updatedMembers = await serversApi.banServerMember(activeServer.id, targetUsername)
                  setServerMembers(updatedMembers)
                }}
              />
            </>
          ) : (
            <DiscoverServersView
              query={discoverQuery}
              onQueryChange={setDiscoverQuery}
              onSearch={async () => {
                await discoverServers(discoverQuery)
              }}
              onJoinServer={async (serverId) => {
                const joined = await joinServer(serverId)

                if (joined) {
                  navigate(`/channels/${joined.id}`)
                }
              }}
              servers={discoverResults}
              loading={discoverLoading}
              error={discoverError}
            />
          )}
        </main>
      </div>

      <CreateServerDialog
        open={isCreateServerOpen}
        onClose={() => setIsCreateServerOpen(false)}
        onCreateServer={async (name, description) => {
          const created = await createServer(name, description)
          if (created) {
            navigate(`/channels/${created.id}`)
          }
        }}
      />

      <CreateChannelDialog
        open={isCreateChannelOpen}
        onClose={() => setIsCreateChannelOpen(false)}
        onCreateChannel={async (name, type) => {
          if (!activeServer) {
            return
          }

          const created = await createChannel(activeServer.id, name, type)
          if (created) {
            navigate(`/channels/${activeServer.id}/${created.id}`)
          }
        }}
      />
    </div>
  )
}
