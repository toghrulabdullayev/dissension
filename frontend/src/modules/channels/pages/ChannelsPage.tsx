import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useAuthStore } from '../../auth/model/authStore'
import { useChannelsStore } from '../model/channelsStore'
import { ChannelWorkspace } from '../ui/ChannelWorkspace'
import { ChannelsPanel } from '../ui/ChannelsPanel'
import { CreateChannelDialog } from '../ui/CreateChannelDialog'
import { useServersStore } from '../../servers/model/serversStore'
import { CreateServerDialog } from '../../servers/ui/CreateServerDialog'
import { ServerSidebar } from '../../servers/ui/ServerSidebar'

export function ChannelsPage() {
  const navigate = useNavigate()
  const params = useParams<{ serverId?: string; channelId?: string }>()
  const username = useAuthStore((state) => state.username)
  const token = useAuthStore((state) => state.token)
  const clearSession = useAuthStore((state) => state.clearSession)
  const servers = useServersStore((state) => state.servers)
  const loadServers = useServersStore((state) => state.loadServers)
  const createServer = useServersStore((state) => state.createServer)
  const selectServer = useServersStore((state) => state.selectServer)
  const clearServers = useServersStore((state) => state.clearServers)

  const channelsByServer = useChannelsStore((state) => state.channelsByServer)
  const loadChannels = useChannelsStore((state) => state.loadChannels)
  const createChannel = useChannelsStore((state) => state.createChannel)
  const clearChannels = useChannelsStore((state) => state.clearChannels)
  const [isCreateServerOpen, setIsCreateServerOpen] = useState(false)
  const [isCreateChannelOpen, setIsCreateChannelOpen] = useState(false)

  const routeServerId = params.serverId ? Number(params.serverId) : null
  const routeChannelId = params.channelId ? Number(params.channelId) : null

  const normalizedServerId =
    routeServerId != null && Number.isFinite(routeServerId)
      ? routeServerId
      : null

  const normalizedChannelId =
    routeChannelId != null && Number.isFinite(routeChannelId)
      ? routeChannelId
      : null

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
    if (!token || normalizedServerId == null) {
      return
    }

    void loadChannels(normalizedServerId)
  }, [token, normalizedServerId, loadChannels])

  useEffect(() => {
    if (!token || servers.length === 0 || normalizedServerId != null) {
      return
    }

    navigate(`/channels/${servers[0].id}`, { replace: true })
  }, [token, servers, normalizedServerId, navigate])

  useEffect(() => {
    if (!token || normalizedServerId == null || activeServer) {
      return
    }

    if (servers.length > 0) {
      navigate(`/channels/${servers[0].id}`, { replace: true })
      return
    }

    navigate('/channels', { replace: true })
  }, [token, normalizedServerId, activeServer, servers, navigate])

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
          onLogout={handleLogout}
        />

        <main className="flex flex-1">
          <ChannelsPanel
            channels={channels}
            hasActiveServer={activeServer != null}
            selectedChannelId={normalizedChannelId}
            onSelectChannel={(channelId) => {
              if (!activeServer) {
                return
              }

              navigate(`/channels/${activeServer.id}/${channelId}`)
            }}
            onOpenCreateChannel={() => setIsCreateChannelOpen(true)}
          />
          <ChannelWorkspace
            username={username}
            selectedChannel={selectedChannel}
            currentRole={activeServer?.role ?? 'USER'}
          />
        </main>
      </div>

      <CreateServerDialog
        open={isCreateServerOpen}
        onClose={() => setIsCreateServerOpen(false)}
        onCreateServer={async (name) => {
          const created = await createServer(name)
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
