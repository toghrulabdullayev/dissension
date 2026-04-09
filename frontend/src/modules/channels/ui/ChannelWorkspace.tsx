import {
  ArrowDown,
  ChevronLeft,
  ChevronRight,
  Info,
  MonitorUp,
  Mic,
  MicOff,
  MoreHorizontal,
  PhoneOff,
  Send,
  Users,
  Video,
  VideoOff,
  X,
} from 'lucide-react'
import {
  useEffect,
  useLayoutEffect,
  useMemo,
  useRef,
  useState,
  type KeyboardEvent as ReactKeyboardEvent,
  type MouseEvent as ReactMouseEvent,
  type PointerEvent as ReactPointerEvent,
} from 'react'
import { createPortal } from 'react-dom'
import { Button } from '../../../shared/ui/button'
import type { Channel } from '../model/types'
import type { ServerMember, ServerRole } from '../../servers/model/types'
import type { ChatMessage } from '../../chat/model/types'

const MEMBERS_SIDEBAR_DEFAULT_WIDTH = 280
const MEMBERS_SIDEBAR_MIN_WIDTH = 200
const MEMBERS_SIDEBAR_MAX_WIDTH = 420
const MEMBERS_SIDEBAR_COLLAPSE_THRESHOLD = 150
const MEMBERS_SIDEBAR_DESKTOP_BREAKPOINT = 768
const MEMBERS_SIDEBAR_MOBILE_MIN_WIDTH = 240
const MEMBERS_SIDEBAR_MOBILE_MAX_WIDTH = 360
const MEMBERS_SIDEBAR_MOBILE_CLOSE_DRAG_THRESHOLD = 72
const MESSAGE_COMPOSER_MAX_LINES = 3
const CHAT_AUTO_SCROLL_THRESHOLD = 120

function getMembersSidebarMaxWidth(viewportWidth: number) {
  if (viewportWidth >= 1440) {
    return MEMBERS_SIDEBAR_MAX_WIDTH
  }

  if (viewportWidth >= 1280) {
    return 320
  }

  if (viewportWidth >= 1160) {
    return 260
  }

  if (viewportWidth >= 1024) {
    return 240
  }

  if (viewportWidth >= 900) {
    return 220
  }

  return 200
}

function getMembersSidebarDrawerWidth(viewportWidth: number) {
  return Math.max(
    MEMBERS_SIDEBAR_MOBILE_MIN_WIDTH,
    Math.min(MEMBERS_SIDEBAR_MOBILE_MAX_WIDTH, viewportWidth - 24),
  )
}

type ChannelWorkspaceProps = {
  username: string | null
  selectedChannel: Channel | null
  messages: ChatMessage[]
  messagesLoading: boolean
  messagesError: string | null
  currentRole?: ServerRole
  serverMembers: ServerMember[]
  onlineUsernames: string[]
  membersLoading: boolean
  membersCount: number
  channelsPanelCollapsed: boolean
  onToggleChannelsPanel: () => void
  onSendMessage: (content: string) => Promise<void>
  onUpdateMemberRole: (username: string, role: 'ADMIN' | 'USER') => Promise<void>
  onBanMember: (username: string) => Promise<void>
}

export function ChannelWorkspace({
  username,
  selectedChannel,
  messages,
  messagesLoading,
  messagesError,
  currentRole = 'USER',
  serverMembers,
  onlineUsernames,
  membersLoading,
  membersCount,
  channelsPanelCollapsed,
  onToggleChannelsPanel,
  onSendMessage,
  onUpdateMemberRole,
  onBanMember,
}: ChannelWorkspaceProps) {
  const [messageDraft, setMessageDraft] = useState('')
  const [messageSendError, setMessageSendError] = useState<string | null>(null)
  const [micEnabled, setMicEnabled] = useState(true)
  const [cameraEnabled, setCameraEnabled] = useState(true)
  const [membersSidebarWidth, setMembersSidebarWidth] = useState(MEMBERS_SIDEBAR_DEFAULT_WIDTH)
  const [membersSidebarCollapsed, setMembersSidebarCollapsed] = useState(
    () => typeof window !== 'undefined' && window.innerWidth < MEMBERS_SIDEBAR_DESKTOP_BREAKPOINT,
  )
  const [viewportWidth, setViewportWidth] = useState(
    () => (typeof window === 'undefined' ? 1440 : window.innerWidth),
  )
  const [openMemberMenuFor, setOpenMemberMenuFor] = useState<string | null>(null)
  const [membersActionError, setMembersActionError] = useState<string | null>(null)
  const [mobileMembersDragOffset, setMobileMembersDragOffset] = useState(0)
  const [isDraggingMobileMembersDrawer, setIsDraggingMobileMembersDrawer] = useState(false)
  const [showJumpToLatestButton, setShowJumpToLatestButton] = useState(false)
  const resizeStartXRef = useRef(0)
  const resizeStartWidthRef = useRef(MEMBERS_SIDEBAR_DEFAULT_WIDTH)
  const chatScrollRegionRef = useRef<HTMLDivElement | null>(null)
  const messageTextareaRef = useRef<HTMLTextAreaElement | null>(null)
  const previousChannelIdRef = useRef<string | null>(null)
  const previousMessageCountRef = useRef(0)
  const isNearBottomRef = useRef(true)
  const forceScrollToBottomRef = useRef(false)
  const mobileMembersDragStartXRef = useRef<number | null>(null)
  const mobileMembersDragStartYRef = useRef<number | null>(null)
  const mobileMembersDragStartOffsetRef = useRef(0)
  const isMobileViewport = viewportWidth < MEMBERS_SIDEBAR_DESKTOP_BREAKPOINT
  const maxMembersSidebarWidth = getMembersSidebarMaxWidth(viewportWidth)
  const effectiveMembersSidebarWidth = isMobileViewport
    ? getMembersSidebarDrawerWidth(viewportWidth)
    : Math.max(MEMBERS_SIDEBAR_MIN_WIDTH, Math.min(maxMembersSidebarWidth, membersSidebarWidth))

  const closeMembersDrawer = () => {
    setOpenMemberMenuFor(null)
    setIsDraggingMobileMembersDrawer(false)
    setMobileMembersDragOffset(0)
    mobileMembersDragStartXRef.current = null
    mobileMembersDragStartYRef.current = null
    setMembersSidebarCollapsed(true)
  }

  const normalizedUsername = (username ?? '').toLowerCase()
  const effectiveCurrentRole =
    serverMembers.find((member) => member.username.toLowerCase() === normalizedUsername)?.role ?? currentRole

  const canWriteInInfoChannel =
    effectiveCurrentRole === 'OWNER' || effectiveCurrentRole === 'ADMIN'
  const canSendInSelectedChannel =
    selectedChannel?.type === 'CHAT' || (selectedChannel?.type === 'INFO' && canWriteInInfoChannel)
  const selectedChannelMessages = selectedChannel ? messages : []
  const onlineUsernamesSet = useMemo(
    () => new Set(onlineUsernames.map((value) => value.toLowerCase())),
    [onlineUsernames],
  )

  const updateScrollPositionState = () => {
    const container = chatScrollRegionRef.current

    if (!container) {
      isNearBottomRef.current = true
      setShowJumpToLatestButton(false)
      return
    }

    const distanceFromBottom = container.scrollHeight - (container.scrollTop + container.clientHeight)
    const isNearBottom = distanceFromBottom <= CHAT_AUTO_SCROLL_THRESHOLD

    isNearBottomRef.current = isNearBottom
    setShowJumpToLatestButton(distanceFromBottom > CHAT_AUTO_SCROLL_THRESHOLD)
  }

  const scrollToLatestMessage = (behavior: ScrollBehavior = 'smooth') => {
    const container = chatScrollRegionRef.current
    if (!container) {
      return
    }

    container.scrollTo({
      top: container.scrollHeight,
      behavior,
    })

    window.requestAnimationFrame(() => {
      updateScrollPositionState()
    })
  }

  useEffect(() => {
    setMessageDraft('')
    setMessageSendError(null)
  }, [selectedChannel?.id])

  useEffect(() => {
    const textarea = messageTextareaRef.current
    if (!textarea) {
      return
    }

    textarea.style.height = 'auto'

    const computedStyle = window.getComputedStyle(textarea)
    const lineHeight = Number.parseFloat(computedStyle.lineHeight) || 20
    const verticalPadding =
      Number.parseFloat(computedStyle.paddingTop) + Number.parseFloat(computedStyle.paddingBottom)
    const verticalBorder =
      Number.parseFloat(computedStyle.borderTopWidth) + Number.parseFloat(computedStyle.borderBottomWidth)
    const maxHeight = lineHeight * MESSAGE_COMPOSER_MAX_LINES + verticalPadding + verticalBorder

    const targetHeight = Math.min(textarea.scrollHeight, maxHeight)

    textarea.style.height = `${targetHeight}px`
    textarea.style.overflowY = textarea.scrollHeight > maxHeight ? 'auto' : 'hidden'
  }, [messageDraft, selectedChannel?.id])

  useLayoutEffect(() => {
    if (!selectedChannel || selectedChannel.type === 'CALL') {
      return
    }

    const container = chatScrollRegionRef.current
    if (!container) {
      return
    }

    let nextFrame: number | null = null
    const snapToBottom = () => {
      container.scrollTop = container.scrollHeight
      updateScrollPositionState()
    }

    snapToBottom()

    const frame = window.requestAnimationFrame(() => {
      snapToBottom()
      nextFrame = window.requestAnimationFrame(() => {
        snapToBottom()
      })
    })

    return () => {
      window.cancelAnimationFrame(frame)
      if (nextFrame != null) {
        window.cancelAnimationFrame(nextFrame)
      }
    }
  }, [selectedChannel?.id])

  useEffect(() => {
    const currentChannelId = selectedChannel?.id ?? null
    const messageCount = selectedChannelMessages.length
    const channelChanged = previousChannelIdRef.current !== currentChannelId

    if (channelChanged) {
      previousChannelIdRef.current = currentChannelId
      previousMessageCountRef.current = messageCount
      forceScrollToBottomRef.current = currentChannelId != null
      isNearBottomRef.current = true
      setShowJumpToLatestButton(false)
    }

    const hasNewMessages = messageCount > previousMessageCountRef.current
    previousMessageCountRef.current = messageCount

    if (forceScrollToBottomRef.current) {
      const frame = window.requestAnimationFrame(() => {
        scrollToLatestMessage('auto')
      })

      if (!messagesLoading) {
        forceScrollToBottomRef.current = false
      }

      return () => {
        window.cancelAnimationFrame(frame)
      }
    }

    if (!hasNewMessages) {
      const frame = window.requestAnimationFrame(() => {
        updateScrollPositionState()
      })

      return () => {
        window.cancelAnimationFrame(frame)
      }
    }

    if (!isNearBottomRef.current) {
      setShowJumpToLatestButton(true)
      return
    }

    const frame = window.requestAnimationFrame(() => {
      scrollToLatestMessage('smooth')
    })

    return () => {
      window.cancelAnimationFrame(frame)
    }
  }, [selectedChannel?.id, selectedChannelMessages.length, messagesLoading])

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
    if (isMobileViewport) {
      return
    }

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
        Math.max(MEMBERS_SIDEBAR_MIN_WIDTH, Math.min(maxMembersSidebarWidth, nextWidth)),
      )
    }

    const handleMouseUp = () => {
      document.removeEventListener('mousemove', handleMouseMove)
      document.removeEventListener('mouseup', handleMouseUp)
    }

    document.addEventListener('mousemove', handleMouseMove)
    document.addEventListener('mouseup', handleMouseUp)
  }

  useEffect(() => {
    const handleResize = () => {
      const nextViewportWidth = window.innerWidth
      setViewportWidth(nextViewportWidth)

      if (nextViewportWidth < MEMBERS_SIDEBAR_DESKTOP_BREAKPOINT) {
        setOpenMemberMenuFor(null)
        setMembersSidebarCollapsed(true)
      }
    }

    window.addEventListener('resize', handleResize)

    return () => {
      window.removeEventListener('resize', handleResize)
    }
  }, [])

  useEffect(() => {
    if (!isMobileViewport || membersSidebarCollapsed) {
      return
    }

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setMembersSidebarCollapsed(true)
      }
    }

    document.addEventListener('keydown', handleKeyDown)

    return () => {
      document.removeEventListener('keydown', handleKeyDown)
    }
  }, [isMobileViewport, membersSidebarCollapsed])

  useEffect(() => {
    if (!membersSidebarCollapsed) {
      return
    }

    setMobileMembersDragOffset(0)
    setIsDraggingMobileMembersDrawer(false)
    mobileMembersDragStartXRef.current = null
    mobileMembersDragStartYRef.current = null
  }, [membersSidebarCollapsed])


  const toggleMembersSidebar = () => {
    setMembersSidebarCollapsed((value) => {
      const nextValue = !value

      if (!nextValue) {
        setMembersSidebarWidth((currentWidth) => Math.max(currentWidth, MEMBERS_SIDEBAR_MIN_WIDTH))
      }

      return nextValue
    })
  }

  const startMobileMembersDrag = (event: ReactPointerEvent<HTMLElement>) => {
    if (!isMobileViewport || membersSidebarCollapsed) {
      return
    }

    const target = event.target as HTMLElement | null
    if (target?.closest('button, input, textarea, select, a, [data-member-menu="true"]')) {
      return
    }

    mobileMembersDragStartXRef.current = event.clientX
    mobileMembersDragStartYRef.current = event.clientY
    mobileMembersDragStartOffsetRef.current = mobileMembersDragOffset
    setIsDraggingMobileMembersDrawer(false)
  }

  const updateMobileMembersDrag = (event: ReactPointerEvent<HTMLElement>) => {
    if (mobileMembersDragStartXRef.current == null || mobileMembersDragStartYRef.current == null) {
      return
    }

    const deltaX = event.clientX - mobileMembersDragStartXRef.current
    const deltaY = event.clientY - mobileMembersDragStartYRef.current

    if (!isDraggingMobileMembersDrawer) {
      if (Math.abs(deltaX) < 8 && Math.abs(deltaY) < 8) {
        return
      }

      if (Math.abs(deltaY) > Math.abs(deltaX)) {
        mobileMembersDragStartXRef.current = null
        mobileMembersDragStartYRef.current = null
        return
      }

      setIsDraggingMobileMembersDrawer(true)
      event.currentTarget.setPointerCapture(event.pointerId)
    }

    const nextOffset = Math.max(0, mobileMembersDragStartOffsetRef.current + deltaX)

    setMobileMembersDragOffset(nextOffset)
    event.preventDefault()
  }

  const endMobileMembersDrag = () => {
    if (mobileMembersDragStartXRef.current == null) {
      return
    }

    const didDrag = isDraggingMobileMembersDrawer

    const threshold = Math.min(MEMBERS_SIDEBAR_MOBILE_CLOSE_DRAG_THRESHOLD, effectiveMembersSidebarWidth * 0.28)
    const shouldClose = mobileMembersDragOffset >= threshold

    setIsDraggingMobileMembersDrawer(false)
    mobileMembersDragStartXRef.current = null
    mobileMembersDragStartYRef.current = null

    if (!didDrag) {
      return
    }

    if (shouldClose) {
      closeMembersDrawer()
      return
    }

    setMobileMembersDragOffset(0)
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

  const canChangeRoleForMember = (member: ServerMember) => {
    if (effectiveCurrentRole !== 'OWNER') {
      return false
    }

    if (normalizedUsername === member.username.toLowerCase()) {
      return false
    }

    return member.role !== 'OWNER'
  }

  const canBanMember = (member: ServerMember) => {
    const isSelf = normalizedUsername === member.username.toLowerCase()
    if (isSelf || member.role === 'OWNER') {
      return false
    }

    if (effectiveCurrentRole === 'OWNER') {
      return true
    }

    if (effectiveCurrentRole === 'ADMIN') {
      return member.role === 'USER'
    }

    return false
  }

  const getRoleActionLabel = (member: ServerMember) => {
    if (member.role === 'USER') {
      return `Make ${member.username} admin`
    }

    return `Make ${member.username} user`
  }

  const sendMessage = () => {
    setMessageSendError(null)

    if (!selectedChannel || !canSendInSelectedChannel) {
      return
    }

    const trimmedDraft = messageDraft.trim()
    if (trimmedDraft.length === 0) {
      return
    }

    void onSendMessage(trimmedDraft)
      .then(() => {
        setMessageDraft('')
      })
      .catch((error) => {
        setMessageSendError(error instanceof Error ? error.message : 'Failed to send message')
      })
  }

  const handleDraftKeyDown = (event: ReactKeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key !== 'Enter' || event.shiftKey) {
      return
    }

    event.preventDefault()
    sendMessage()
  }

  const showReadOnlyInfoHint = selectedChannel?.type === 'INFO' && !canSendInSelectedChannel
  const shouldRenderBottomControls =
    showJumpToLatestButton || canSendInSelectedChannel || showReadOnlyInfoHint

  const membersSidebarContent = (
    <aside
      className={[
        isMobileViewport
          ? [
              'fixed inset-y-0 right-0 z-60 border-l border-(--border) bg-(--surface) will-change-transform',
              isDraggingMobileMembersDrawer ? 'transition-none' : 'transition-transform duration-300 ease-out',
              membersSidebarCollapsed ? 'pointer-events-none' : '',
            ].join(' ')
          : 'border-l border-(--border) bg-(--surface)',
      ].join(' ')}
      style={{
        width: `${effectiveMembersSidebarWidth}px`,
        transform: isMobileViewport
          ? membersSidebarCollapsed
            ? 'translateX(100%)'
            : `translateX(${mobileMembersDragOffset}px)`
          : undefined,
        touchAction: isMobileViewport ? 'pan-y' : undefined,
      }}
      onPointerDown={isMobileViewport ? startMobileMembersDrag : undefined}
      onPointerMove={isMobileViewport ? updateMobileMembersDrag : undefined}
      onPointerUp={isMobileViewport ? endMobileMembersDrag : undefined}
      onPointerCancel={isMobileViewport ? endMobileMembersDrag : undefined}
    >
      <div className="flex h-full flex-col">
        <div className="border-b border-(--border) px-4 pb-3 pt-4">
          <div className="flex h-8 items-center gap-2">
            <h3 className="text-sm font-medium text-(--text-display)">Members</h3>
            <span className="nd-label text-(--text-secondary)">{shownMembersCount}</span>
            {isMobileViewport ? (
              <button
                type="button"
                onClick={closeMembersDrawer}
                aria-label="Close members drawer"
                className="ml-auto inline-flex h-7 w-7 items-center justify-center rounded-md border border-(--border-visible) text-(--text-secondary) transition hover:text-(--text-display)"
              >
                <X className="h-4 w-4" />
              </button>
            ) : null}
          </div>
        </div>

        <div className="servers-scroll-region flex-1 overflow-y-auto overflow-x-hidden p-3">
          {membersActionError ? (
            <p className="mb-3 rounded-md border border-(--accent)/60 bg-(--accent-subtle) px-2 py-1.5 text-xs text-(--accent)">
              {membersActionError}
            </p>
          ) : null}

          {membersLoading ? (
            <p className="text-sm text-(--text-secondary)">Loading members...</p>
          ) : serverMembers.length === 0 ? (
            <p className="text-sm text-(--text-secondary)">No members to show.</p>
          ) : (
            <div className="space-y-4">
              {memberGroups.map((group) => {
                if (group.members.length === 0) {
                  return null
                }

                return (
                  <section key={group.title}>
                    <h4 className="nd-label mb-2 text-(--text-secondary)">
                      {group.title}
                    </h4>

                    <ul className="space-y-2">
                      {group.members.map((member) => {
                        const menuOpen = openMemberMenuFor === member.username
                        const canChangeRole = canChangeRoleForMember(member)
                        const canBan = canBanMember(member)
                        const showMenuTrigger = canChangeRole || canBan
                        const isCurrentUser = normalizedUsername === member.username.toLowerCase()
                        const isOnline = onlineUsernamesSet.has(member.username.toLowerCase())

                        return (
                          <li
                            key={member.username}
                            className={[
                              'relative flex items-center gap-3 rounded-md border px-3 py-2',
                              isCurrentUser
                                ? 'border-(--border-visible) bg-(--surface-raised)'
                                : 'border-(--border) bg-(--surface)',
                            ].join(' ')}
                          >
                            <div className="relative h-9 w-9 shrink-0">
                              {member.imageUrl ? (
                                <img
                                  src={member.imageUrl}
                                  alt={`${member.username} avatar`}
                                  className="h-9 w-9 rounded-full object-cover"
                                />
                              ) : (
                                <div className="inline-flex h-9 w-9 items-center justify-center rounded-full bg-(--surface-raised) text-xs font-semibold uppercase text-(--text-secondary)">
                                  {member.username.slice(0, 2)}
                                </div>
                              )}

                              {isOnline ? (
                                <span className="absolute bottom-0 right-0 h-2.5 w-2.5 rounded-full border border-(--surface) bg-(--success)" />
                              ) : null}
                            </div>

                            <p className="min-w-0 flex-1 truncate text-sm font-medium text-(--text-primary)">
                              {member.username}
                            </p>

                            {isCurrentUser ? (
                              <span className="nd-label nd-logo-gradient-bg rounded-full border px-2 py-0.5">
                                You
                              </span>
                            ) : null}

                            {showMenuTrigger ? (
                              <button
                                type="button"
                                data-member-menu="true"
                                onClick={(event) => {
                                  event.stopPropagation()
                                  setOpenMemberMenuFor((value) =>
                                    value === member.username ? null : member.username,
                                  )
                                }}
                                className="inline-flex h-7 w-7 items-center justify-center rounded-md border border-(--border) text-(--text-secondary) transition hover:border-(--border-visible) hover:text-(--text-display)"
                              >
                                <MoreHorizontal className="h-4 w-4" />
                              </button>
                            ) : null}

                            {menuOpen ? (
                              <div
                                data-member-menu="true"
                                className="absolute right-2 top-10 z-20 w-44 rounded-md border border-(--border-visible) bg-(--surface-raised) p-1"
                              >
                                {canChangeRole ? (
                                  <button
                                    type="button"
                                    className="nd-label w-full truncate rounded-sm px-2 py-1.5 text-left text-(--text-primary) transition hover:bg-(--black)/10"
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
                                ) : null}
                                {canBan ? (
                                  <button
                                    type="button"
                                    className="nd-label w-full truncate rounded-sm px-2 py-1.5 text-left text-(--accent) transition hover:bg-(--accent-subtle)"
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
                                ) : null}
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
  )

  return (
    <>
      <section className="flex h-screen min-w-0 flex-1 overflow-hidden">
        <div className="flex min-h-0 min-w-0 flex-1 flex-col px-6 pb-6 pt-0">
          <div className="-mx-6 shrink-0 border-b border-(--border) px-6 pb-3 pt-4">
            <div className="flex h-8 min-w-0 items-center gap-3">
              <div className="flex min-w-0 flex-1 items-center gap-2">
                <button
                  type="button"
                  onClick={onToggleChannelsPanel}
                  aria-label={channelsPanelCollapsed ? 'Expand channels panel' : 'Collapse channels panel'}
                  className="inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-md border border-(--border-visible) text-(--text-secondary) transition hover:text-(--text-display)"
                >
                  {channelsPanelCollapsed ? (
                    <ChevronRight className="h-4 w-4" />
                  ) : (
                    <ChevronLeft className="h-4 w-4" />
                  )}
                </button>

                <p className="min-w-0 flex-1 truncate text-sm font-medium text-(--text-display)">
                  {selectedChannel?.name ?? 'No channel selected'}
                </p>
              </div>

              <button
                type="button"
                onClick={toggleMembersSidebar}
                aria-label={membersSidebarCollapsed ? 'Open members sidebar' : 'Close members sidebar'}
                title="Members"
                className="ml-auto inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-md border border-(--border-visible) text-(--text-secondary) transition hover:text-(--text-display)"
              >
                <Users className="h-4 w-4" />
              </button>
            </div>
          </div>

          {selectedChannel?.type === 'CALL' ? (
            <div className="servers-scroll-region min-h-0 flex-1 overflow-y-auto overflow-x-hidden pt-4">
              <div className="rounded-md border border-(--border) bg-(--surface) p-4">
                <h3 className="text-lg font-medium text-(--text-display)">Call Channel</h3>
                <p className="mt-1 text-sm text-(--text-secondary)">
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

                  <Button
                    type="button"
                    variant="outline"
                    className="w-full border-(--accent)/60 text-(--accent) hover:bg-(--accent-subtle)"
                  >
                    <PhoneOff className="h-4 w-4" />
                    Hang Up
                  </Button>
                </div>
              </div>
            </div>
          ) : (
            <>
              <div
                ref={chatScrollRegionRef}
                className="servers-scroll-region min-h-0 flex-1 overflow-y-auto overflow-x-hidden pt-4"
                onScroll={updateScrollPositionState}
              >
                <div className="min-h-full p-1">
                  {!selectedChannel ? (
                    <div className="flex h-full items-center justify-center rounded-md border border-dashed border-(--border-visible) bg-(--surface) p-4">
                      <p className="text-sm text-(--text-secondary)">Select a channel to start chatting.</p>
                    </div>
                  ) : messagesLoading ? (
                    <div className="flex h-full items-center justify-center rounded-md border border-dashed border-(--border-visible) bg-(--surface) p-4">
                      <p className="text-sm text-(--text-secondary)">Loading messages...</p>
                    </div>
                  ) : messagesError ? (
                    <div className="flex h-full items-center justify-center rounded-md border border-(--accent)/60 bg-(--accent-subtle) p-4">
                      <p className="text-sm text-(--accent)">{messagesError}</p>
                    </div>
                  ) : selectedChannelMessages.length === 0 ? (
                    <div className="flex h-full items-center justify-center rounded-md border border-dashed border-(--border-visible) bg-(--surface) p-4">
                      {selectedChannel.type === 'INFO' ? (
                        <div className="text-center">
                          <div className="mb-2 inline-flex rounded-full bg-(--surface-raised) p-2 text-(--text-secondary)">
                            <Info className="h-4 w-4" />
                          </div>
                          <p className="text-sm text-(--text-secondary)">No announcements posted yet.</p>
                        </div>
                      ) : (
                        <p className="text-sm text-(--text-secondary)">No messages yet in this channel.</p>
                      )}
                    </div>
                  ) : (
                    <div className="flex flex-col">
                      {selectedChannelMessages.map((message, index) => {
                        const isCurrentUserMessage = message.authorUsername.toLowerCase() === normalizedUsername
                        const previousMessage = index > 0 ? selectedChannelMessages[index - 1] : null
                        const isSameAuthorAsPrevious =
                          previousMessage != null &&
                          previousMessage.authorUsername.toLowerCase() === message.authorUsername.toLowerCase()
                        const showAuthorLabel =
                          previousMessage == null ||
                          previousMessage.authorUsername.toLowerCase() !== message.authorUsername.toLowerCase()

                        return (
                        <article
                          key={message.id}
                          className={[
                            'max-w-full md:max-w-2xl',
                            index === 0 ? '' : isSameAuthorAsPrevious ? 'mt-1' : 'mt-3',
                            isCurrentUserMessage ? 'self-end' : 'self-start',
                          ].join(' ')}
                        >
                          {showAuthorLabel ? (
                            <p
                              className={[
                                'nd-label mb-1 text-(--text-secondary)',
                                isCurrentUserMessage ? 'text-right' : '',
                              ].join(' ')}
                            >
                              {message.authorUsername}
                            </p>
                          ) : null}
                          <div className="max-w-full rounded-md border border-(--border) bg-(--surface) px-3 py-2 text-sm text-(--text-primary) whitespace-pre-wrap break-all">
                            {message.content}
                          </div>
                        </article>
                      )})}
                    </div>
                  )}
                </div>
              </div>

              {shouldRenderBottomControls ? (
                <div className="mt-3 shrink-0">
                  <div className="relative">
                    {showJumpToLatestButton ? (
                      <button
                        type="button"
                        onClick={() => scrollToLatestMessage('smooth')}
                        aria-label="Jump to latest message"
                        title="Jump to latest"
                        className="absolute -top-10 right-0 z-10 inline-flex h-8 w-8 items-center justify-center rounded-full nd-logo-gradient-bg border transition hover:brightness-110"
                      >
                        <ArrowDown className="h-4 w-4" />
                      </button>
                    ) : null}

                    {canSendInSelectedChannel ? (
                      <div className="flex gap-2">
                        <textarea
                          ref={messageTextareaRef}
                          rows={1}
                          value={messageDraft}
                          onChange={(event) => setMessageDraft(event.target.value)}
                          onKeyDown={handleDraftKeyDown}
                          placeholder={
                            selectedChannel?.type === 'INFO'
                              ? 'Announce'
                              : 'Message'
                          }
                          className="servers-scroll-region flex w-full flex-1 resize-none rounded-md border border-(--border-visible) bg-(--surface) px-3 py-2 text-sm text-(--text-primary) placeholder:text-(--text-disabled) focus-visible:border-(--text-primary) focus-visible:outline-none disabled:cursor-not-allowed disabled:opacity-50"
                          disabled={!canSendInSelectedChannel}
                        />
                        <Button
                          type="button"
                          onClick={sendMessage}
                          disabled={!canSendInSelectedChannel || messageDraft.trim().length === 0}
                          aria-label={selectedChannel?.type === 'INFO' ? 'Post announcement' : 'Send message'}
                          title={selectedChannel?.type === 'INFO' ? 'Post announcement' : 'Send message'}
                          className="h-10 w-10 shrink-0 self-end nd-logo-gradient-bg p-0 hover:brightness-110"
                        >
                          <Send className="h-4 w-4" />
                        </Button>
                      </div>
                    ) : showReadOnlyInfoHint ? (
                      <p className="text-xs text-(--text-secondary)">
                        Only owners and admins can post announcements in this channel.
                      </p>
                    ) : null}
                  </div>

                  {canSendInSelectedChannel && messageSendError ? (
                    <p className="mt-2 text-xs text-(--accent)">{messageSendError}</p>
                  ) : null}
                </div>
              ) : null}
            </>
          )}
        </div>

      {!membersSidebarCollapsed && !isMobileViewport ? (
        <>
          <div
            role="separator"
            aria-orientation="vertical"
            onMouseDown={startResize}
            className="w-1 cursor-col-resize bg-(--border) transition hover:bg-(--border-visible)"
          />
          {membersSidebarContent}
        </>
      ) : null}
      </section>

      {isMobileViewport
        ? createPortal(
            <>
              <button
                type="button"
                aria-label="Close members drawer"
                onClick={closeMembersDrawer}
                className={[
                  'fixed inset-0 z-55 bg-black/35 transition-opacity duration-300',
                  membersSidebarCollapsed ? 'pointer-events-none opacity-0' : 'opacity-100',
                ].join(' ')}
              />
              {membersSidebarContent}
            </>,
            document.body,
          )
        : null}
    </>
  )
}
