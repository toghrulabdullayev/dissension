import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useAuthStore } from '../../auth/model/authStore'
import { useChatStore } from '../../chat/model/chatStore'
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
import type { Channel } from '../model/types'

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i
const MOBILE_BREAKPOINT = 768

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
  const updateChannel = useChannelsStore((state) => state.updateChannel)
  const deleteChannel = useChannelsStore((state) => state.deleteChannel)
  const clearChannels = useChannelsStore((state) => state.clearChannels)
  const removeServerChannels = useChannelsStore((state) => state.removeServerChannels)
  const connectChat = useChatStore((state) => state.connect)
  const disconnectChat = useChatStore((state) => state.disconnect)
  const loadChannelMessages = useChatStore((state) => state.loadChannelMessages)
  const sendChatMessage = useChatStore((state) => state.sendMessage)
  const clearChat = useChatStore((state) => state.clearChat)
  const banNotice = useChatStore((state) => state.banNotice)
  const clearBanNotice = useChatStore((state) => state.clearBanNotice)
  const messagesByChannel = useChatStore((state) => state.messagesByChannel)
  const loadingByChannel = useChatStore((state) => state.loadingByChannel)
  const errorByChannel = useChatStore((state) => state.errorByChannel)
  const onlineUsernamesByServer = useChatStore((state) => state.onlineUsernamesByServer)
  const onlineCountByServer = useChatStore((state) => state.onlineCountByServer)
  const serverMembersVersionByServer = useChatStore((state) => state.serverMembersVersionByServer)
  const removeServer = useServersStore((state) => state.removeServer)
  const [isCreateServerOpen, setIsCreateServerOpen] = useState(false)
  const [isCreateChannelOpen, setIsCreateChannelOpen] = useState(false)
  const [channelsPanelCollapsed, setChannelsPanelCollapsed] = useState(
    () => typeof window !== 'undefined' && window.innerWidth < MOBILE_BREAKPOINT,
  )
  const [editingChannel, setEditingChannel] = useState<Channel | null>(null)
  const [discoverQuery, setDiscoverQuery] = useState('')
  const [serverMembers, setServerMembers] = useState<ServerMember[]>([])
  const [membersLoading, setMembersLoading] = useState(false)
  const [hasServerBootstrapCompleted, setHasServerBootstrapCompleted] = useState(false)

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

  const selectedChannelMessages = useMemo(() => {
    if (!selectedChannel) {
      return []
    }

    return messagesByChannel[selectedChannel.id] ?? []
  }, [selectedChannel, messagesByChannel])

  const selectedChannelLoading =
    selectedChannel != null ? (loadingByChannel[selectedChannel.id] ?? false) : false
  const selectedChannelError =
    selectedChannel != null ? (errorByChannel[selectedChannel.id] ?? null) : null

  const activeServerOnlineUsernames =
    activeServer != null ? (onlineUsernamesByServer[activeServer.id] ?? []) : []

  const activeServerMembersVersion =
    activeServer != null ? (serverMembersVersionByServer[activeServer.id] ?? 0) : 0

  const discoverResultsWithPresence = useMemo(
    () => discoverResults.map((server) => ({
      ...server,
      onlineMembers: onlineCountByServer[server.id] ?? server.onlineMembers,
    })),
    [discoverResults, onlineCountByServer],
  )

  const handleLogout = () => {
    clearServers()
    clearChannels()
    clearChat()
    clearSession()
    navigate('/login')
  }

  useEffect(() => {
    if (!token) {
      disconnectChat()
      return
    }

    connectChat(token)

    return () => {
      disconnectChat()
    }
  }, [token, connectChat, disconnectChat])

  useEffect(() => {
    if (!token) {
      setHasServerBootstrapCompleted(false)
      return
    }

    let cancelled = false

    void (async () => {
      try {
        await loadServers()
      } finally {
        if (!cancelled) {
          setHasServerBootstrapCompleted(true)
        }
      }
    })()

    return () => {
      cancelled = true
    }
  }, [token, loadServers])

  useEffect(() => {
    if (!token || normalizedServerId == null || !activeServer) {
      return
    }

    void loadChannels(normalizedServerId)
  }, [token, normalizedServerId, activeServer, loadChannels])

  useEffect(() => {
    if (!token || !activeServer || !selectedChannel || selectedChannel.type === 'CALL') {
      return
    }

    void loadChannelMessages(activeServer.id, selectedChannel.id)
  }, [token, activeServer, selectedChannel, loadChannelMessages])

  useEffect(() => {
    if (
      !token ||
      !hasServerBootstrapCompleted ||
      banNotice != null ||
      normalizedServerId == null ||
      activeServer ||
      serversLoading
    ) {
      return
    }

    if (servers.length > 0) {
      navigate(`/channels/${servers[0].id}`, { replace: true })
      return
    }

    navigate('/channels', { replace: true })
  }, [token, hasServerBootstrapCompleted, banNotice, normalizedServerId, activeServer, serversLoading, servers, navigate])

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
  }, [token, activeServer, channels, normalizedChannelId, params.channelId, navigate])

  useEffect(() => {
    if (
      !token ||
      !hasServerBootstrapCompleted ||
      activeServer ||
      serversLoading ||
      normalizedServerId != null
    ) {
      return
    }

    void discoverServers('')
  }, [token, hasServerBootstrapCompleted, activeServer, serversLoading, normalizedServerId, discoverServers])

  useEffect(() => {
    if (!banNotice) {
      return
    }

    const isBannedServerOpen =
      normalizedServerId != null &&
      normalizedServerId.toLowerCase() === banNotice.serverId.toLowerCase()

    removeServer(banNotice.serverId)
    removeServerChannels(banNotice.serverId)

    if (isBannedServerOpen) {
      navigate('/channels', { replace: true })
    }
  }, [banNotice, normalizedServerId, removeServer, removeServerChannels, navigate])

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
  }, [token, activeServer, activeServerMembersVersion])

  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth < MOBILE_BREAKPOINT) {
        setChannelsPanelCollapsed(true)
      }
    }

    window.addEventListener('resize', handleResize)

    return () => {
      window.removeEventListener('resize', handleResize)
    }
  }, [])

  return (
    <div
      className="min-h-screen bg-transparent text-(--text-primary)"
      style={{
        backgroundImage:
          'radial-gradient(circle at 18% 8%, rgba(192,72,63,0.1), transparent 38%), radial-gradient(circle at 88% 85%, rgba(246,221,83,0.08), transparent 36%)',
      }}
    >
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

        <main className="flex min-w-0 flex-1 overflow-hidden">
          {activeServer ? (
            <>
              <ChannelsPanel
                channels={channels}
                serverName={activeServer.name}
                hasActiveServer={true}
                selectedChannelId={normalizedChannelId}
                panelCollapsed={channelsPanelCollapsed}
                onPanelCollapsedChange={setChannelsPanelCollapsed}
                onSelectChannel={(channelId) => {
                  navigate(`/channels/${activeServer.id}/${channelId}`)
                }}
                onOpenCreateChannel={() => {
                  setEditingChannel(null)
                  setIsCreateChannelOpen(true)
                }}
                canCreateChannels={activeServer.role !== 'USER'}
                canManageChannels={activeServer.role === 'OWNER'}
                onOpenUpdateChannel={(channel) => {
                  setIsCreateChannelOpen(false)
                  setEditingChannel(channel)
                }}
                onDeleteChannel={async (channel) => {
                  const fallbackChannel = channels
                    .slice()
                    .sort((a, b) => a.position - b.position)
                    .find((candidate) => candidate.id !== channel.id)

                  await deleteChannel(activeServer.id, channel.id)

                  if (editingChannel?.id === channel.id) {
                    setEditingChannel(null)
                  }

                  if (normalizedChannelId === channel.id) {
                    if (fallbackChannel) {
                      navigate(`/channels/${activeServer.id}/${fallbackChannel.id}`, {
                        replace: true,
                      })
                    } else {
                      navigate(`/channels/${activeServer.id}`, { replace: true })
                    }
                  }
                }}
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
                messages={selectedChannelMessages}
                messagesLoading={selectedChannelLoading}
                messagesError={selectedChannelError}
                currentRole={activeServer?.role ?? 'USER'}
                serverMembers={serverMembers}
                onlineUsernames={activeServerOnlineUsernames}
                membersLoading={membersLoading}
                membersCount={serverMembers.length > 0 ? serverMembers.length : activeServer.members}
                channelsPanelCollapsed={channelsPanelCollapsed}
                onToggleChannelsPanel={() => setChannelsPanelCollapsed((value) => !value)}
                onSendMessage={async (content) => {
                  if (!selectedChannel) {
                    return
                  }

                  await sendChatMessage(activeServer.id, selectedChannel.id, content)
                }}
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
              servers={discoverResultsWithPresence}
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
        open={isCreateChannelOpen || editingChannel != null}
        onClose={() => {
          setIsCreateChannelOpen(false)
          setEditingChannel(null)
        }}
        mode={editingChannel ? 'update' : 'create'}
        initialName={editingChannel?.name}
        initialType={editingChannel?.type}
        onSubmitChannel={async (name, type) => {
          if (!activeServer) {
            return
          }

          if (editingChannel) {
            await updateChannel(activeServer.id, editingChannel.id, name, type)
            return
          }

          const createdChannel = await createChannel(activeServer.id, name, type)
          if (createdChannel) {
            navigate(`/channels/${activeServer.id}/${createdChannel.id}`)
          }
        }}
      />

      {banNotice ? (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 p-4">
          <div className="w-full max-w-md rounded-2xl border border-(--border-visible) bg-(--surface) p-6">
            <h3 className="text-lg font-medium text-(--text-display)">You Were Banned</h3>
            <p className="mt-2 text-sm text-(--text-primary)">
              You have been banned from <span className="font-medium">{banNotice.serverName}</span> by{' '}
              <span className="font-medium">{banNotice.bannedByUsername}</span>.
            </p>
            <div className="mt-5 flex justify-end">
              <button
                type="button"
                className="nd-label inline-flex h-10 items-center rounded-full border border-(--text-display) bg-(--text-display) px-4 text-(--black) transition hover:bg-(--text-primary)"
                onClick={() => {
                  clearBanNotice()
                }}
              >
                Ok
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  )
}
