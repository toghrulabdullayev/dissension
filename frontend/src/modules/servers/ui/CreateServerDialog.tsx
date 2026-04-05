import { useState } from 'react'
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

type CreateServerDialogProps = {
  open: boolean
  onClose: () => void
  onCreateServer: (name: string, description: string) => Promise<void>
}

export function CreateServerDialog({
  open,
  onClose,
  onCreateServer,
}: CreateServerDialogProps) {
  const DESCRIPTION_MAX_LENGTH = 150
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  if (!open) {
    return null
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (isSubmitting) {
      return
    }

    const trimmed = name.trim()
    const trimmedDescription = description.trim()

    if (trimmed.length < 2 || trimmed.length > 100) {
      setError('Server name must be between 2 and 100 characters.')
      return
    }

    if (trimmedDescription.length > DESCRIPTION_MAX_LENGTH) {
      setError(`Server description must be at most ${DESCRIPTION_MAX_LENGTH} characters.`)
      return
    }

    setIsSubmitting(true)

    try {
      await onCreateServer(trimmed, trimmedDescription)
      setName('')
      setDescription('')
      setError('')
      onClose()
    } catch (submitError) {
      setError(submitError instanceof Error ? submitError.message : 'Failed to create server.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/45 p-4">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle className="text-xl">Create server</CardTitle>
          <CardDescription>Enter a server name to create a new workspace.</CardDescription>
        </CardHeader>

        <CardContent>
          <form className="space-y-4" onSubmit={handleSubmit}>
            <div className="space-y-2">
              <Label htmlFor="server-name">Server name</Label>
              <Input
                id="server-name"
                value={name}
                onChange={(event) => {
                  setName(event.target.value)
                  if (error) {
                    setError('')
                  }
                }}
                placeholder="e.g. Team Alpha"
                autoFocus
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="server-description">Description</Label>
              <textarea
                id="server-description"
                value={description}
                onChange={(event) => {
                  setDescription(event.target.value)
                  if (error) {
                    setError('')
                  }
                }}
                placeholder="Optional short description"
                maxLength={DESCRIPTION_MAX_LENGTH}
                rows={4}
                className="w-full resize-none rounded-md border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 outline-none ring-offset-white transition placeholder:text-slate-400 focus-visible:ring-2 focus-visible:ring-slate-300 disabled:cursor-not-allowed disabled:opacity-50"
              />
              <p className="text-right text-xs text-slate-500">
                {DESCRIPTION_MAX_LENGTH - description.length} chars left
              </p>
            </div>

            {error ? <p className="text-sm text-red-600">{error}</p> : null}

            <div className="flex justify-end gap-2">
              <Button type="button" variant="outline" onClick={onClose}>
                Cancel
              </Button>
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting ? 'Creating...' : 'Create'}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
