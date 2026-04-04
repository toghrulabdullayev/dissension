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
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [error, setError] = useState('')

  if (!open) {
    return null
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const trimmed = name.trim()
    const trimmedDescription = description.trim()

    if (trimmed.length < 2 || trimmed.length > 100) {
      setError('Server name must be between 2 and 100 characters.')
      return
    }

    if (trimmedDescription.length > 128) {
      setError('Server description must be at most 128 characters.')
      return
    }

    await onCreateServer(trimmed, trimmedDescription)
    setName('')
    setDescription('')
    setError('')
    onClose()
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
              <Input
                id="server-description"
                value={description}
                onChange={(event) => {
                  setDescription(event.target.value)
                  if (error) {
                    setError('')
                  }
                }}
                placeholder="Optional short description"
                maxLength={128}
              />
            </div>

            {error ? <p className="text-sm text-red-600">{error}</p> : null}

            <div className="flex justify-end gap-2">
              <Button type="button" variant="outline" onClick={onClose}>
                Cancel
              </Button>
              <Button type="submit">Create</Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
