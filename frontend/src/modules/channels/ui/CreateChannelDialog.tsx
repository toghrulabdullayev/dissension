import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Button } from '../../../shared/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '../../../shared/ui/card'
import { Input } from '../../../shared/ui/input'
import { Label } from '../../../shared/ui/label'
import type { ChannelType } from '../model/types'

type CreateChannelDialogProps = {
  open: boolean
  onClose: () => void
  mode?: 'create' | 'update'
  initialName?: string
  initialType?: ChannelType
  onSubmitChannel: (name: string, type: ChannelType) => Promise<void>
}

export function CreateChannelDialog({
  open,
  onClose,
  mode = 'create',
  initialName,
  initialType,
  onSubmitChannel,
}: CreateChannelDialogProps) {
  const [name, setName] = useState('')
  const [type, setType] = useState<ChannelType>('CHAT')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    if (!open) {
      return
    }

    setName(initialName ?? '')
    setType(initialType ?? 'CHAT')
    setError('')
    setIsSubmitting(false)
  }, [open, initialName, initialType])

  if (!open) {
    return null
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (isSubmitting) {
      return
    }

    const trimmed = name.trim()

    if (trimmed.length < 2 || trimmed.length > 100) {
      setError('Channel name must be between 2 and 100 characters.')
      return
    }

    setIsSubmitting(true)

    try {
      await onSubmitChannel(trimmed, type)
      setName('')
      setType('CHAT')
      setError('')
      onClose()
    } catch (submitError) {
      setError(
        submitError instanceof Error
          ? submitError.message
          : mode === 'update'
            ? 'Failed to update channel.'
            : 'Failed to create channel.',
      )
    } finally {
      setIsSubmitting(false)
    }
  }

  const isUpdateMode = mode === 'update'

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 p-4">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle className="text-xl text-(--text-display)">{isUpdateMode ? 'Update channel' : 'Create channel'}</CardTitle>
          <CardDescription>
            {isUpdateMode
              ? 'Update the channel name and type.'
              : 'Define the channel name and type. Call channels will host voice/video/screen sharing later.'}
          </CardDescription>
        </CardHeader>

        <CardContent>
          <form className="space-y-4" onSubmit={handleSubmit}>
            <div className="space-y-2">
              <Label htmlFor="channel-name">Channel name</Label>
              <Input
                id="channel-name"
                value={name}
                onChange={(event) => {
                  setName(event.target.value)
                  if (error) {
                    setError('')
                  }
                }}
                placeholder="e.g. announcements"
                autoFocus
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="channel-type">Channel type</Label>
              <select
                id="channel-type"
                value={type}
                onChange={(event) => setType(event.target.value as ChannelType)}
                className="flex h-11 w-full rounded-lg border border-(--border-visible) bg-(--surface) px-3 py-2 text-sm text-(--text-primary) focus-visible:border-(--text-primary) focus-visible:outline-none"
              >
                <option value="INFO">Info (owners/admins/mods can post)</option>
                <option value="CHAT">Chat (everyone can post)</option>
                <option value="CALL">Call (voice/video/screenshare in future)</option>
              </select>
            </div>

            {error ? <p className="text-sm text-(--accent)">{error}</p> : null}

            <div className="flex justify-end gap-2">
              <Button type="button" variant="outline" onClick={onClose}>
                Cancel
              </Button>
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting ? (isUpdateMode ? 'Updating...' : 'Creating...') : isUpdateMode ? 'Update' : 'Create'}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
