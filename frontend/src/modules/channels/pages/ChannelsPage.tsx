import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
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
  const username = useAuthStore((state) => state.username)
  const clearSession = useAuthStore((state) => state.clearSession)
  const servers = useServersStore((state) => state.servers)
  const activeServerId = useServersStore((state) => state.activeServerId)
  const createServer = useServersStore((state) => state.createServer)
  const selectServer = useServersStore((state) => state.selectServer)

  const channelsByServer = useChannelsStore((state) => state.channelsByServer)
  const selectedChannelIdByServer = useChannelsStore((state) => state.selectedChannelIdByServer)
  const createChannel = useChannelsStore((state) => state.createChannel)
  const selectChannel = useChannelsStore((state) => state.selectChannel)
  const [isCreateServerOpen, setIsCreateServerOpen] = useState(false)
  const [isCreateChannelOpen, setIsCreateChannelOpen] = useState(false)

  const selectedChannelId = activeServerId
    ? selectedChannelIdByServer[activeServerId] ?? null
    : null

  const channels = useMemo(() => {
    if (!activeServerId) {
      return []
    }

    return channelsByServer[activeServerId] ?? []
  }, [activeServerId, channelsByServer])

  const selectedChannel =
    channels.find((channel) => channel.id === selectedChannelId) ?? channels[0] ?? null

  const activeServer =
    servers.find((server) => server.id === activeServerId) ?? null

  const handleLogout = () => {
    clearSession()
    navigate('/login')
  }

  return (
    <div className="min-h-screen bg-white text-slate-900">
      <div className="flex min-h-screen">
        <ServerSidebar
          servers={servers}
          activeServerId={activeServerId}
          onSelectServer={selectServer}
          onOpenCreateServer={() => setIsCreateServerOpen(true)}
          onLogout={handleLogout}
        />

        <main className="flex flex-1">
          <ChannelsPanel
            channels={channels}
            hasActiveServer={activeServer != null}
            selectedChannelId={selectedChannelId}
            onSelectChannel={(channelId) => {
              if (!activeServerId) {
                return
              }

              selectChannel(activeServerId, channelId)
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
        onCreateServer={createServer}
      />

      <CreateChannelDialog
        open={isCreateChannelOpen}
        onClose={() => setIsCreateChannelOpen(false)}
        onCreateChannel={(name, type) => {
          if (!activeServerId) {
            return
          }
          createChannel(activeServerId, name, type)
        }}
      />
    </div>
  )
}
