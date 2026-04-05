import {
  ChevronLeft,
  ChevronRight,
  Info,
  MonitorUp,
  Mic,
  MicOff,
  MoreHorizontal,
  PhoneOff,
  Send,
  Video,
  VideoOff,
} from 'lucide-react'
import { useEffect, useRef, useState, type MouseEvent as ReactMouseEvent } from 'react'
import { Button } from '../../../shared/ui/button'
import { Input } from '../../../shared/ui/input'
import type { Channel } from '../model/types'
import type { ServerMember, ServerRole } from '../../servers/model/types'

const MEMBERS_SIDEBAR_DEFAULT_WIDTH = 280
const MEMBERS_SIDEBAR_MIN_WIDTH = 200
const MEMBERS_SIDEBAR_MAX_WIDTH = 420
const MEMBERS_SIDEBAR_COLLAPSE_THRESHOLD = 150

type ChannelWorkspaceProps = {
  username: string | null
  selectedChannel: Channel | null
  currentRole?: ServerRole
  serverMembers: ServerMember[]
  membersLoading: boolean
  membersCount: number
  onUpdateMemberRole: (username: string, role: 'ADMIN' | 'USER') => Promise<void>
  onBanMember: (username: string) => Promise<void>
}

export function ChannelWorkspace({
  username,
  selectedChannel,
  currentRole = 'USER',
  serverMembers,
  membersLoading,
  membersCount,
  onUpdateMemberRole,
  onBanMember,
}: ChannelWorkspaceProps) {
  const [messageDraft, setMessageDraft] = useState('')
  const [micEnabled, setMicEnabled] = useState(true)
  const [cameraEnabled, setCameraEnabled] = useState(true)
  const [membersSidebarWidth, setMembersSidebarWidth] = useState(MEMBERS_SIDEBAR_DEFAULT_WIDTH)
  const [membersSidebarCollapsed, setMembersSidebarCollapsed] = useState(false)
  const [openMemberMenuFor, setOpenMemberMenuFor] = useState<string | null>(null)
  const [membersActionError, setMembersActionError] = useState<string | null>(null)
  const resizeStartXRef = useRef(0)
  const resizeStartWidthRef = useRef(MEMBERS_SIDEBAR_DEFAULT_WIDTH)

  const canWriteInInfoChannel =
    currentRole === 'OWNER' || currentRole === 'ADMIN' || currentRole === 'MOD'

  useEffect(() => {
    if (openMemberMenuFor == null) {
      return
    }

    const handleOutsideClick = (event: MouseEvent) => {
      const target = event.target as HTMLElement | null

      if (target?.closest('[data-member-menu="true"]')) {
        return
      }

      setOpenMemberMenuFor(null)
    }

    document.addEventListener('mousedown', handleOutsideClick)

    return () => {
      document.removeEventListener('mousedown', handleOutsideClick)
    }
  }, [openMemberMenuFor])

  const startResize = (event: ReactMouseEvent<HTMLDivElement>) => {
    event.preventDefault()
    resizeStartXRef.current = event.clientX
    resizeStartWidthRef.current = membersSidebarWidth

    const handleMouseMove = (moveEvent: MouseEvent) => {
      const delta = moveEvent.clientX - resizeStartXRef.current
      const nextWidth = resizeStartWidthRef.current - delta

      if (nextWidth < MEMBERS_SIDEBAR_COLLAPSE_THRESHOLD) {
        setMembersSidebarCollapsed(true)
        return
      }

      setMembersSidebarCollapsed(false)
      setMembersSidebarWidth(
        Math.max(MEMBERS_SIDEBAR_MIN_WIDTH, Math.min(MEMBERS_SIDEBAR_MAX_WIDTH, nextWidth)),
      )
    }

    const handleMouseUp = () => {
      document.removeEventListener('mousemove', handleMouseMove)
      document.removeEventListener('mouseup', handleMouseUp)
    }

    document.addEventListener('mousemove', handleMouseMove)
    document.addEventListener('mouseup', handleMouseUp)
  }

  const toggleMembersSidebar = () => {
    setMembersSidebarCollapsed((value) => !value)

    if (membersSidebarCollapsed) {
      setMembersSidebarWidth((value) => Math.max(value, MEMBERS_SIDEBAR_MIN_WIDTH))
    }
  }

  const shownMembersCount = Math.max(membersCount, serverMembers.length)
  const ownerMembers = serverMembers
    .filter((member) => member.role === 'OWNER')
    .sort((a, b) => a.username.localeCompare(b.username))
  const adminMembers = serverMembers
    .filter((member) => member.role === 'ADMIN' || member.role === 'MOD')
    .sort((a, b) => a.username.localeCompare(b.username))
  const userMembers = serverMembers
    .filter((member) => member.role === 'USER')
    .sort((a, b) => a.username.localeCompare(b.username))

  const memberGroups = [
    { title: 'Owner', members: ownerMembers },
    { title: 'Admin', members: adminMembers },
    { title: 'User', members: userMembers },
  ]

  const getRoleActionLabel = (member: ServerMember) => {
    if (member.role === 'USER') {
      return `Make ${member.username} admin`
    }

    return `Make ${member.username} user`
  }

  return (
    <section className="flex flex-1 overflow-hidden">
      <div className="flex min-w-0 flex-1 flex-col p-6">
        <div className="mb-4 rounded-md border border-slate-200 bg-slate-50 p-4">
          <div className="flex items-start justify-between gap-3">
            <div>
              <p className="text-xs uppercase tracking-wide text-slate-500">Selected Channel</p>
              <p className="mt-1 text-lg font-semibold">{selectedChannel?.name ?? 'No channel selected'}</p>
              {selectedChannel ? (
                <p className="text-sm text-slate-500">Type: {selectedChannel.type}</p>
              ) : null}
              {selectedChannel?.type === 'INFO' ? (
                <p className="text-sm text-slate-500">
                  Info channels are writable only by owners, admins, and moderators.
                </p>
              ) : null}
              <p className="text-sm text-slate-500">Signed in as: {username ?? 'User'}</p>
            </div>

            <div className="inline-flex items-center gap-2 rounded-md border border-slate-200 bg-white px-3 py-1.5 text-sm text-slate-600">
              <span>{shownMembersCount} members</span>
              <button
                type="button"
                onClick={toggleMembersSidebar}
                className="inline-flex items-center gap-1 rounded border border-slate-200 px-2 py-1 text-xs font-medium transition hover:bg-slate-100"
              >
                {membersSidebarCollapsed ? <ChevronLeft className="h-3 w-3" /> : <ChevronRight className="h-3 w-3" />}
                {membersSidebarCollapsed ? 'Show' : 'Hide'}
              </button>
            </div>
          </div>
        </div>

        {selectedChannel?.type === 'CALL' ? (
          <div className="rounded-md border border-slate-200 bg-white p-4">
            <h3 className="text-lg font-semibold">Call Channel</h3>
            <p className="mt-1 text-sm text-slate-500">
              Voice, video, and screen sharing UI controls are ready for real-time integration.
            </p>

            <div className="mt-4 grid grid-cols-2 gap-2 sm:grid-cols-4">
              <Button
                type="button"
                variant={micEnabled ? 'default' : 'outline'}
                onClick={() => setMicEnabled((value) => !value)}
                className="w-full"
              >
                {micEnabled ? <Mic className="h-4 w-4" /> : <MicOff className="h-4 w-4" />}
                {micEnabled ? 'Mic On' : 'Mic Off'}
              </Button>

              <Button
                type="button"
                variant={cameraEnabled ? 'default' : 'outline'}
                onClick={() => setCameraEnabled((value) => !value)}
                className="w-full"
              >
                {cameraEnabled ? <Video className="h-4 w-4" /> : <VideoOff className="h-4 w-4" />}
                {cameraEnabled ? 'Camera On' : 'Camera Off'}
              </Button>

              <Button type="button" variant="outline" className="w-full">
                <MonitorUp className="h-4 w-4" />
                Share Screen
              </Button>

              <Button type="button" variant="outline" className="w-full text-red-600 hover:bg-red-50">
                <PhoneOff className="h-4 w-4" />
                Hang Up
              </Button>
            </div>
          </div>
        ) : (
          <>
            <div className="flex-1 rounded-md border border-slate-200 bg-white p-4">
              <div className="flex h-full items-center justify-center rounded-md border border-dashed border-slate-200 bg-slate-50 p-4">
                {!selectedChannel ? (
                  <p className="text-sm text-slate-500">Select a channel to start chatting.</p>
                ) : selectedChannel.type === 'INFO' ? (
                  <div className="text-center">
                    <div className="mb-2 inline-flex rounded-full bg-slate-200 p-2 text-slate-600">
                      <Info className="h-4 w-4" />
                    </div>
                    <p className="text-sm text-slate-600">No announcements posted yet.</p>
                  </div>
                ) : (
                  <p className="text-sm text-slate-500">No messages yet in this channel.</p>
                )}
              </div>
            </div>

            {selectedChannel?.type !== 'INFO' || canWriteInInfoChannel ? (
              <div className="mt-3 flex gap-2">
                <Input
                  value={messageDraft}
                  onChange={(event) => setMessageDraft(event.target.value)}
                  placeholder={
                    selectedChannel?.type === 'INFO'
                      ? 'Post an announcement...'
                      : 'Type your message...'
                  }
                  className="flex-1"
                  disabled={selectedChannel == null}
                />
                <Button
                  type="button"
                  disabled={selectedChannel == null}
                >
                  <Send className="h-4 w-4" />
                  {selectedChannel?.type === 'INFO' ? 'Post' : 'Send'}
                </Button>
              </div>
            ) : null}
          </>
        )}
      </div>

      {!membersSidebarCollapsed ? (
        <>
          <div
            role="separator"
            aria-orientation="vertical"
            onMouseDown={startResize}
            className="w-1 cursor-col-resize bg-slate-200 transition hover:bg-slate-300"
          />
          <aside
            className="border-l border-slate-200 bg-slate-50"
            style={{ width: `${membersSidebarWidth}px` }}
          >
            <div className="flex h-full flex-col">
              <div className="border-b border-slate-200 px-4 py-3">
                <h3 className="text-sm font-semibold text-slate-700">Members</h3>
              </div>

              <div className="servers-scroll-region flex-1 overflow-y-auto overflow-x-hidden p-3">
                {membersActionError ? (
                  <p className="mb-3 rounded-md border border-red-200 bg-red-50 px-2 py-1.5 text-xs text-red-700">
                    {membersActionError}
                  </p>
                ) : null}

                {membersLoading ? (
                  <p className="text-sm text-slate-500">Loading members...</p>
                ) : serverMembers.length === 0 ? (
                  <p className="text-sm text-slate-500">No members to show.</p>
                ) : (
                  <div className="space-y-4">
                    {memberGroups.map((group) => {
                      if (group.members.length === 0) {
                        return null
                      }

                      return (
                        <section key={group.title}>
                          <h4 className="mb-2 text-xs font-semibold tracking-wide text-slate-500">
                            {group.title}
                          </h4>

                          <ul className="space-y-2">
                            {group.members.map((member) => {
                              const menuOpen = openMemberMenuFor === member.username

                              return (
                                <li
                                  key={member.username}
                                  className="relative flex items-center gap-3 rounded-md border border-slate-200 bg-white px-3 py-2"
                                >
                                  {member.imageUrl ? (
                                    <img
                                      src={member.imageUrl}
                                      alt={`${member.username} avatar`}
                                      className="h-9 w-9 rounded-full object-cover"
                                    />
                                  ) : (
                                    <div className="inline-flex h-9 w-9 items-center justify-center rounded-full bg-slate-200 text-xs font-semibold uppercase text-slate-600">
                                      {member.username.slice(0, 2)}
                                    </div>
                                  )}

                                  <p className="min-w-0 flex-1 truncate text-sm font-medium text-slate-800">
                                    {member.username}
                                  </p>

                                  <button
                                    type="button"
                                    data-member-menu="true"
                                    onClick={(event) => {
                                      event.stopPropagation()
                                      setOpenMemberMenuFor((value) =>
                                        value === member.username ? null : member.username,
                                      )
                                    }}
                                    className="inline-flex h-7 w-7 items-center justify-center rounded-md border border-slate-200 text-slate-500 transition hover:bg-slate-100"
                                  >
                                    <MoreHorizontal className="h-4 w-4" />
                                  </button>

                                  {menuOpen ? (
                                    <div
                                      data-member-menu="true"
                                      className="absolute right-2 top-10 z-20 w-44 rounded-md border border-slate-200 bg-white p-1 shadow-lg"
                                    >
                                      <button
                                        type="button"
                                        className="w-full rounded-sm px-2 py-1.5 text-left text-xs text-slate-700 transition hover:bg-slate-100"
                                        onClick={async () => {
                                          setMembersActionError(null)

                                          const nextRole = member.role === 'USER' ? 'ADMIN' : 'USER'

                                          try {
                                            await onUpdateMemberRole(member.username, nextRole)
                                          } catch (error) {
                                            setMembersActionError(
                                              error instanceof Error
                                                ? error.message
                                                : 'Failed to update member role',
                                            )
                                          }

                                          setOpenMemberMenuFor(null)
                                        }}
                                      >
                                        {getRoleActionLabel(member)}
                                      </button>
                                      <button
                                        type="button"
                                        className="w-full rounded-sm px-2 py-1.5 text-left text-xs text-red-600 transition hover:bg-red-50"
                                        onClick={async () => {
                                          setMembersActionError(null)

                                          try {
                                            await onBanMember(member.username)
                                          } catch (error) {
                                            setMembersActionError(
                                              error instanceof Error
                                                ? error.message
                                                : `Failed to ban ${member.username}`,
                                            )
                                          }

                                          setOpenMemberMenuFor(null)
                                        }}
                                      >
                                        Ban {member.username}
                                      </button>
                                    </div>
                                  ) : null}
                                </li>
                              )
                            })}
                          </ul>
                        </section>
                      )
                    })}
                  </div>
                )}
              </div>
            </div>
          </aside>
        </>
      ) : null}
    </section>
  )
}
