import { MonitorUp, Mic, MicOff, Send, Video, VideoOff, PhoneOff, Info } from 'lucide-react'
import { useState } from 'react'
import { Button } from '../../../shared/ui/button'
import { Input } from '../../../shared/ui/input'
import type { Channel } from '../model/types'
import type { ServerRole } from '../../servers/model/types'

type ChannelWorkspaceProps = {
  username: string | null
  selectedChannel: Channel | null
  currentRole?: ServerRole
}

export function ChannelWorkspace({
  username,
  selectedChannel,
  currentRole = 'USER',
}: ChannelWorkspaceProps) {
  const [messageDraft, setMessageDraft] = useState('')
  const [micEnabled, setMicEnabled] = useState(true)
  const [cameraEnabled, setCameraEnabled] = useState(true)

  const canWriteInInfoChannel =
    currentRole === 'OWNER' || currentRole === 'ADMIN' || currentRole === 'MOD'

  if (!selectedChannel) {
    return (
      <section className="flex flex-1 items-center justify-center p-6">
        <div className="rounded-md border border-slate-200 bg-slate-50 px-6 py-5 text-center">
          <p className="text-sm text-slate-600">Select a server to view channels.</p>
        </div>
      </section>
    )
  }

  return (
    <section className="flex flex-1 flex-col p-6">
      <div className="mb-4 rounded-md border border-slate-200 bg-slate-50 p-4">
        <p className="text-xs uppercase tracking-wide text-slate-500">Selected Channel</p>
        <p className="mt-1 text-lg font-semibold">{selectedChannel.name}</p>
        <p className="text-sm text-slate-500">Type: {selectedChannel.type}</p>
        {selectedChannel.type === 'INFO' ? (
          <p className="text-sm text-slate-500">
            Info channels are writable only by owners, admins, and moderators.
          </p>
        ) : null}
        <p className="text-sm text-slate-500">Signed in as: {username ?? 'User'}</p>
      </div>

      {selectedChannel.type === 'CALL' ? (
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
              {selectedChannel.type === 'INFO' ? (
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

          <div className="mt-3 flex gap-2">
            <Input
              value={messageDraft}
              onChange={(event) => setMessageDraft(event.target.value)}
              placeholder={
                selectedChannel.type === 'INFO'
                  ? 'Post an announcement...'
                  : 'Type your message...'
              }
              className="flex-1"
              disabled={selectedChannel.type === 'INFO' && !canWriteInInfoChannel}
            />
            <Button
              type="button"
              disabled={selectedChannel.type === 'INFO' && !canWriteInInfoChannel}
            >
              <Send className="h-4 w-4" />
              {selectedChannel.type === 'INFO' ? 'Post' : 'Send'}
            </Button>
          </div>

          {selectedChannel.type === 'INFO' && !canWriteInInfoChannel ? (
            <p className="mt-2 text-sm text-amber-700">
              You are a casual user in this server. Only owners, admins, and moderators can post here.
            </p>
          ) : null}
        </>
      )}
    </section>
  )
}
