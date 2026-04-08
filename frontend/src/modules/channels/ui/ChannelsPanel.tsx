import {
  useEffect,
  useRef,
  useState,
  type MouseEvent as ReactMouseEvent,
  type PointerEvent as ReactPointerEvent,
} from 'react'
import { createPortal } from 'react-dom'
import { Hash, Info, LogOut, MoreHorizontal, Phone, Plus, X } from 'lucide-react'
import type { Channel, ChannelType } from '../model/types'

const CHANNELS_PANEL_DEFAULT_WIDTH = 288
const CHANNELS_PANEL_MIN_WIDTH = 220
const CHANNELS_PANEL_MAX_WIDTH = 420
const CHANNELS_PANEL_COLLAPSE_THRESHOLD = 170
const CHANNELS_PANEL_DESKTOP_BREAKPOINT = 768
const CHANNELS_PANEL_MOBILE_MIN_WIDTH = 240
const CHANNELS_PANEL_MOBILE_MAX_WIDTH = 360
const CHANNELS_PANEL_MOBILE_CLOSE_DRAG_THRESHOLD = 72

function getChannelsPanelMaxWidth(viewportWidth: number) {
  if (viewportWidth >= 1440) {
    return CHANNELS_PANEL_MAX_WIDTH
  }

  if (viewportWidth >= 1280) {
    return 340
  }

  if (viewportWidth >= 1160) {
    return 280
  }

  if (viewportWidth >= 1024) {
    return 260
  }

  if (viewportWidth >= 900) {
    return 240
  }

  return 220
}

function getChannelsPanelDrawerWidth(viewportWidth: number) {
  return Math.max(
    CHANNELS_PANEL_MOBILE_MIN_WIDTH,
    Math.min(CHANNELS_PANEL_MOBILE_MAX_WIDTH, viewportWidth - 24),
  )
}

type ChannelsPanelProps = {
  channels: Channel[]
  serverName: string
  hasActiveServer: boolean
  selectedChannelId: string | null
  onSelectChannel: (channelId: string) => void
  onOpenCreateChannel: () => void
  panelCollapsed: boolean
  onPanelCollapsedChange: (collapsed: boolean) => void
  canCreateChannels?: boolean
  canManageChannels?: boolean
  onOpenUpdateChannel?: (channel: Channel) => void
  onDeleteChannel?: (channel: Channel) => Promise<void>
  onLeaveServer?: () => void
}

function channelIcon(type: ChannelType) {
  if (type === 'INFO') {
    return <Info className="h-4 w-4 shrink-0" />
  }

  if (type === 'CALL') {
    return <Phone className="h-4 w-4 shrink-0" />
  }

  return <Hash className="h-4 w-4 shrink-0" />
}

function channelTypeLabel(type: ChannelType) {
  if (type === 'INFO') {
    return 'info'
  }

  if (type === 'CALL') {
    return 'call'
  }

  return 'chat'
}

export function ChannelsPanel({
  channels,
  serverName,
  hasActiveServer,
  selectedChannelId,
  onSelectChannel,
  onOpenCreateChannel,
  panelCollapsed,
  onPanelCollapsedChange,
  canCreateChannels = false,
  canManageChannels = false,
  onOpenUpdateChannel,
  onDeleteChannel,
  onLeaveServer,
}: ChannelsPanelProps) {
  const [openChannelMenuFor, setOpenChannelMenuFor] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [panelWidth, setPanelWidth] = useState(CHANNELS_PANEL_DEFAULT_WIDTH)
  const [viewportWidth, setViewportWidth] = useState(
    () => (typeof window === 'undefined' ? 1440 : window.innerWidth),
  )
  const [tooltip, setTooltip] = useState<{
    name: string
    top: number
    left: number
  } | null>(null)
  const [isLeaveConfirmOpen, setIsLeaveConfirmOpen] = useState(false)
  const [isLeavingServer, setIsLeavingServer] = useState(false)
  const [mobileDragOffset, setMobileDragOffset] = useState(0)
  const [isDraggingMobileDrawer, setIsDraggingMobileDrawer] = useState(false)
  const resizeStartXRef = useRef(0)
  const resizeStartWidthRef = useRef(CHANNELS_PANEL_DEFAULT_WIDTH)
  const mobileDragStartXRef = useRef<number | null>(null)
  const mobileDragStartYRef = useRef<number | null>(null)
  const mobileDragStartOffsetRef = useRef(0)
  const isMobileViewport = viewportWidth < CHANNELS_PANEL_DESKTOP_BREAKPOINT
  const maxPanelWidth = getChannelsPanelMaxWidth(viewportWidth)
  const effectivePanelWidth = isMobileViewport
    ? getChannelsPanelDrawerWidth(viewportWidth)
    : Math.max(CHANNELS_PANEL_MIN_WIDTH, Math.min(maxPanelWidth, panelWidth))

  const closeMobileDrawer = () => {
    setOpenChannelMenuFor(null)
    hideChannelTooltip()
    setIsDraggingMobileDrawer(false)
    setMobileDragOffset(0)
    mobileDragStartXRef.current = null
    mobileDragStartYRef.current = null
    onPanelCollapsedChange(true)
  }

  const showChannelTooltip = (name: string, element: HTMLElement) => {
    const rect = element.getBoundingClientRect()
    setTooltip({
      name,
      top: rect.top + rect.height / 2,
      left: rect.right + 9,
    })
  }

  const hideChannelTooltip = () => {
    setTooltip(null)
  }

  const startResize = (event: ReactMouseEvent<HTMLDivElement>) => {
    if (isMobileViewport) {
      return
    }

    event.preventDefault()
    resizeStartXRef.current = event.clientX
    resizeStartWidthRef.current = panelWidth

    const handleMouseMove = (moveEvent: MouseEvent) => {
      const delta = moveEvent.clientX - resizeStartXRef.current
      const nextWidth = resizeStartWidthRef.current + delta

      if (nextWidth < CHANNELS_PANEL_COLLAPSE_THRESHOLD) {
        onPanelCollapsedChange(true)
        return
      }

      onPanelCollapsedChange(false)
      setPanelWidth(
        Math.max(CHANNELS_PANEL_MIN_WIDTH, Math.min(maxPanelWidth, nextWidth)),
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
      setViewportWidth(window.innerWidth)
    }

    window.addEventListener('resize', handleResize)

    return () => {
      window.removeEventListener('resize', handleResize)
    }
  }, [])

  useEffect(() => {
    if (!panelCollapsed) {
      return
    }

    const timeout = window.setTimeout(() => {
      setTooltip(null)
      setOpenChannelMenuFor(null)
      setMobileDragOffset(0)
      setIsDraggingMobileDrawer(false)
      mobileDragStartXRef.current = null
      mobileDragStartYRef.current = null
    }, 0)

    return () => {
      window.clearTimeout(timeout)
    }
  }, [panelCollapsed])

  useEffect(() => {
    if (!isMobileViewport || panelCollapsed) {
      return
    }

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        onPanelCollapsedChange(true)
      }
    }

    document.addEventListener('keydown', handleKeyDown)

    return () => {
      document.removeEventListener('keydown', handleKeyDown)
    }
  }, [isMobileViewport, panelCollapsed, onPanelCollapsedChange])

  useEffect(() => {
    if (openChannelMenuFor == null) {
      return
    }

    const handlePointerDown = (event: MouseEvent) => {
      const target = event.target as HTMLElement | null
      if (target?.closest('[data-channel-menu="true"]')) {
        return
      }

      setOpenChannelMenuFor(null)
    }

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setOpenChannelMenuFor(null)
      }
    }

    document.addEventListener('mousedown', handlePointerDown)
    document.addEventListener('keydown', handleKeyDown)

    return () => {
      document.removeEventListener('mousedown', handlePointerDown)
      document.removeEventListener('keydown', handleKeyDown)
    }
  }, [openChannelMenuFor])

  const startMobileDrag = (event: ReactPointerEvent<HTMLElement>) => {
    if (!isMobileViewport || panelCollapsed) {
      return
    }

    const target = event.target as HTMLElement | null
    if (target?.closest('button, input, textarea, select, a, [data-channel-menu="true"]')) {
      return
    }

    mobileDragStartXRef.current = event.clientX
    mobileDragStartYRef.current = event.clientY
    mobileDragStartOffsetRef.current = mobileDragOffset
    setIsDraggingMobileDrawer(false)
  }

  const updateMobileDrag = (event: ReactPointerEvent<HTMLElement>) => {
    if (mobileDragStartXRef.current == null || mobileDragStartYRef.current == null) {
      return
    }

    const deltaX = event.clientX - mobileDragStartXRef.current
    const deltaY = event.clientY - mobileDragStartYRef.current

    if (!isDraggingMobileDrawer) {
      if (Math.abs(deltaX) < 8 && Math.abs(deltaY) < 8) {
        return
      }

      if (Math.abs(deltaY) > Math.abs(deltaX)) {
        mobileDragStartXRef.current = null
        mobileDragStartYRef.current = null
        return
      }

      setIsDraggingMobileDrawer(true)
      event.currentTarget.setPointerCapture(event.pointerId)
    }

    const nextOffset = Math.min(0, mobileDragStartOffsetRef.current + deltaX)

    setMobileDragOffset(nextOffset)
    event.preventDefault()
  }

  const endMobileDrag = () => {
    if (mobileDragStartXRef.current == null) {
      return
    }

    const didDrag = isDraggingMobileDrawer

    const threshold = Math.min(CHANNELS_PANEL_MOBILE_CLOSE_DRAG_THRESHOLD, effectivePanelWidth * 0.28)
    const shouldClose = Math.abs(mobileDragOffset) >= threshold

    setIsDraggingMobileDrawer(false)
    mobileDragStartXRef.current = null
    mobileDragStartYRef.current = null

    if (!didDrag) {
      return
    }

    if (shouldClose) {
      closeMobileDrawer()
      return
    }

    setMobileDragOffset(0)
  }

  if (panelCollapsed && !isMobileViewport) {
    return null
  }

  const panelContent = (
    <section
      className={[
        isMobileViewport
          ? [
              'fixed inset-y-0 left-0 z-50 flex max-w-[calc(100vw-1rem)] flex-col border-r border-slate-200 bg-white shadow-2xl will-change-transform',
              isDraggingMobileDrawer ? 'transition-none' : 'transition-transform duration-300 ease-out',
              panelCollapsed ? 'pointer-events-none' : '',
            ].join(' ')
          : 'flex h-screen shrink-0 flex-col border-r border-slate-200 bg-white',
      ].join(' ')}
      style={{
        width: `${effectivePanelWidth}px`,
        transform: isMobileViewport
          ? panelCollapsed
            ? 'translateX(-100%)'
            : `translateX(${mobileDragOffset}px)`
          : undefined,
        touchAction: isMobileViewport ? 'pan-y' : undefined,
      }}
      onPointerDown={isMobileViewport ? startMobileDrag : undefined}
      onPointerMove={isMobileViewport ? updateMobileDrag : undefined}
      onPointerUp={isMobileViewport ? endMobileDrag : undefined}
      onPointerCancel={isMobileViewport ? endMobileDrag : undefined}
    >
      <div className="shrink-0 px-4 pt-5.5">
        <div className="flex h-8 items-center gap-2 pb-3">
          <p className="min-w-0 flex-1 truncate text-sm font-semibold text-slate-800">{serverName}</p>
          {isMobileViewport ? (
            <div className="ml-auto flex items-center gap-2">
              <button
                type="button"
                onClick={() => {
                  setIsLeaveConfirmOpen(true)
                }}
                aria-label="Leave server"
                title="Leave server"
                className="inline-flex h-8 w-8 items-center justify-center rounded-md border border-red-200 text-red-600 transition hover:bg-red-50"
              >
                <LogOut className="h-4 w-4" />
              </button>

              <button
                type="button"
                onClick={closeMobileDrawer}
                aria-label="Close channels drawer"
                className="inline-flex h-8 w-8 items-center justify-center rounded-md border border-slate-200 text-slate-500 transition hover:bg-slate-100"
              >
                <X className="h-4 w-4" />
              </button>
            </div>
          ) : (
            <button
              type="button"
              onClick={() => {
                setIsLeaveConfirmOpen(true)
              }}
              aria-label="Leave server"
              title="Leave server"
              className="inline-flex h-8 w-8 items-center justify-center rounded-md border border-red-200 text-red-600 transition hover:bg-red-50"
            >
              <LogOut className="h-4 w-4" />
            </button>
          )}
        </div>

        <div className="mt-1.5 -mx-4 border-t border-slate-200" />

        <div className="flex items-center justify-between gap-1 pt-3">
          <h2 className="text-sm font-semibold text-slate-500">Channels</h2>
          {canCreateChannels ? (
            <button
              type="button"
              onClick={() => {
                onOpenCreateChannel()
                if (isMobileViewport) {
                  closeMobileDrawer()
                }
              }}
              disabled={!hasActiveServer}
              className="inline-flex items-center gap-1 rounded-md border border-slate-200 px-2 py-1 text-xs text-slate-500 transition hover:bg-slate-100"
            >
              <Plus className="h-3 w-3" />
              New
            </button>
          ) : null}
        </div>
      </div>

      <div
        className="servers-scroll-region flex-1 overflow-y-auto overflow-x-hidden p-4"
        onScroll={hideChannelTooltip}
      >
        {channels.length === 0 ? (
          <p className="text-sm text-slate-500">No channels in this server yet.</p>
        ) : (
          <div className="space-y-1">
            {channels
              .slice()
              .sort((a, b) => a.position - b.position)
              .map((channel) => {
                const active = channel.id === selectedChannelId
                const menuOpen = openChannelMenuFor === channel.id

                return (
                  <div key={channel.id} className="group relative">
                    <button
                      type="button"
                      onClick={() => {
                        setOpenChannelMenuFor(null)
                        onSelectChannel(channel.id)
                        if (isMobileViewport) {
                          onPanelCollapsedChange(true)
                        }
                      }}
                      className={[
                        'flex w-full items-center gap-2 rounded-md px-3 py-2 text-left text-sm transition',
                        active
                          ? 'bg-slate-900 text-white'
                          : 'text-slate-500 hover:bg-slate-100 hover:text-slate-900',
                      ].join(' ')}
                    >
                      {channelIcon(channel.type)}
                      <span
                        className="min-w-0 flex-1 truncate"
                        onMouseEnter={(event) => showChannelTooltip(channel.name, event.currentTarget)}
                        onMouseLeave={hideChannelTooltip}
                      >
                        {channel.name}
                      </span>
                      <span
                        className={[
                          'ml-auto text-[10px] uppercase tracking-wide transition',
                          canManageChannels
                            ? (menuOpen ? 'opacity-0' : 'opacity-75 group-hover:opacity-0')
                            : 'opacity-75',
                        ].join(' ')}
                      >
                        {channelTypeLabel(channel.type)}
                      </span>
                    </button>

                    {canManageChannels ? (
                      <button
                        type="button"
                        data-channel-menu="true"
                        onClick={(event) => {
                          event.stopPropagation()
                          setActionError(null)
                          setOpenChannelMenuFor((value) => (value === channel.id ? null : channel.id))
                        }}
                        className={[
                          'absolute right-1.5 top-1/2 inline-flex h-7 w-7 -translate-y-1/2 items-center justify-center rounded-md border transition',
                          menuOpen ? 'opacity-100' : 'opacity-0 group-hover:opacity-100',
                          active
                            ? 'border-slate-700 text-slate-200 hover:bg-slate-800'
                            : 'border-slate-200 text-slate-500 hover:bg-slate-100',
                        ].join(' ')}
                      >
                        <MoreHorizontal className="h-4 w-4" />
                      </button>
                    ) : null}

                    {menuOpen ? (
                      <div
                        data-channel-menu="true"
                        className="absolute right-2 top-10 z-20 w-40 rounded-md border border-slate-200 bg-white p-1 shadow-lg"
                      >
                        <button
                          type="button"
                          className="w-full rounded-sm px-2 py-1.5 text-left text-xs text-slate-700 transition hover:bg-slate-100"
                          onClick={() => {
                            onOpenUpdateChannel?.(channel)
                            setOpenChannelMenuFor(null)
                            if (isMobileViewport) {
                              closeMobileDrawer()
                            }
                          }}
                        >
                          Update channel
                        </button>
                        <button
                          type="button"
                          className="w-full rounded-sm px-2 py-1.5 text-left text-xs text-red-600 transition hover:bg-red-50"
                          onClick={async () => {
                            if (!onDeleteChannel) {
                              setOpenChannelMenuFor(null)
                              return
                            }

                            setActionError(null)

                            try {
                              await onDeleteChannel(channel)
                              if (isMobileViewport) {
                                closeMobileDrawer()
                              }
                            } catch (error) {
                              setActionError(
                                error instanceof Error ? error.message : `Failed to delete ${channel.name}`,
                              )
                            }

                            setOpenChannelMenuFor(null)
                          }}
                        >
                          Delete channel
                        </button>
                      </div>
                    ) : null}
                  </div>
                )
              })}

            {actionError ? <p className="pt-2 text-xs text-red-600">{actionError}</p> : null}
          </div>
        )}
      </div>
    </section>
  )

  return (
    <>
      {isMobileViewport
        ? createPortal(
            <>
              <button
                type="button"
                aria-label="Close channels drawer"
                onClick={closeMobileDrawer}
                className={[
                  'fixed inset-0 z-40 bg-slate-900/20 transition-opacity duration-300',
                  panelCollapsed ? 'pointer-events-none opacity-0' : 'opacity-100',
                ].join(' ')}
              />
              {panelContent}
            </>,
            document.body,
          )
        : (
            <>
              {panelContent}
              <div
                role="separator"
                aria-orientation="vertical"
                onMouseDown={startResize}
                className="w-1 cursor-col-resize bg-slate-200 transition hover:bg-slate-300"
              />
            </>
          )}

      {tooltip
        ? createPortal(
            <div
              className="pointer-events-none fixed z-120 -translate-y-1/2"
              style={{ top: tooltip.top, left: tooltip.left }}
            >
              <div className="relative flex items-center gap-1.5 rounded-lg border border-slate-600/50 bg-slate-900 px-2 py-1.5 text-sm font-semibold text-slate-100 shadow-xl">
                <span className="absolute -left-0.5 top-1/2 h-2 w-2 -translate-y-1/2 rotate-45 border-b border-l border-slate-600/50 bg-slate-900" />
                <span>{tooltip.name}</span>
              </div>
            </div>,
            document.body,
          )
        : null}

      {isLeaveConfirmOpen
        ? createPortal(
            <div className="fixed inset-0 z-[130] flex items-center justify-center bg-slate-900/40 p-4">
              <div className="w-full max-w-sm rounded-lg border border-slate-200 bg-white p-4 shadow-2xl">
                <h3 className="text-sm font-semibold text-slate-900">Leave server?</h3>
                <p className="mt-2 text-sm text-slate-600">
                  You will leave this server and can rejoin later from Discover.
                </p>

                <div className="mt-4 flex justify-end gap-2">
                  <button
                    type="button"
                    onClick={() => setIsLeaveConfirmOpen(false)}
                    disabled={isLeavingServer}
                    className="inline-flex items-center rounded-md border border-slate-200 px-3 py-1.5 text-xs font-medium text-slate-600 transition hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-50"
                  >
                    Cancel
                  </button>

                  <button
                    type="button"
                    onClick={async () => {
                      if (!onLeaveServer || isLeavingServer) {
                        return
                      }

                      setIsLeavingServer(true)

                      try {
                        await onLeaveServer()
                        setIsLeaveConfirmOpen(false)
                        if (isMobileViewport) {
                          closeMobileDrawer()
                        }
                      } finally {
                        setIsLeavingServer(false)
                      }
                    }}
                    disabled={isLeavingServer}
                    className="inline-flex items-center rounded-md border border-red-200 bg-red-50 px-3 py-1.5 text-xs font-medium text-red-600 transition hover:bg-red-100 disabled:cursor-not-allowed disabled:opacity-50"
                  >
                    {isLeavingServer ? 'Leaving...' : 'Leave'}
                  </button>
                </div>
              </div>
            </div>,
            document.body,
          )
        : null}
    </>
  )
}
