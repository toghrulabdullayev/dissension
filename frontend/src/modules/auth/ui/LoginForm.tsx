import { KeyRound, LogIn, UserRound } from 'lucide-react'
import { useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { authApi, ApiError } from '../api/authApi'
import { useAuthStore } from '../model/authStore'
import { Button } from '../../../shared/ui/button'
import { Input } from '../../../shared/ui/input'
import { Label } from '../../../shared/ui/label'

export function LoginForm() {
  const navigate = useNavigate()
  const setSession = useAuthStore((state) => state.setSession)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [statusMessage, setStatusMessage] = useState('')
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setStatusMessage('')
    setFieldErrors({})
    setIsSubmitting(true)

    try {
      const response = await authApi.login({ username, password })
      setSession(response)
      navigate('/channels')
      setPassword('')
    } catch (error) {
      if (error instanceof ApiError) {
        setStatusMessage(error.message)
        setFieldErrors(error.validationErrors)
      } else {
        setStatusMessage('Unexpected error while signing in.')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <form className="space-y-4" onSubmit={handleSubmit} noValidate>
      <div className="space-y-2">
        <Label htmlFor="login-username">Username</Label>
        <div className="relative">
          <UserRound className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-(--text-disabled)" />
          <Input
            id="login-username"
            className="pl-9"
            autoComplete="username"
            placeholder="Enter your username"
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            required
          />
        </div>
      </div>

      <div className="space-y-2">
        <Label htmlFor="login-password">Password</Label>
        <div className="relative">
          <KeyRound className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-(--text-disabled)" />
          <Input
            id="login-password"
            type="password"
            className="pl-9"
            autoComplete="current-password"
            placeholder="Enter your password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
          />
        </div>
      </div>

      {Object.keys(fieldErrors).length > 0 ? (
        <ul className="rounded-md border border-(--accent)/60 bg-(--accent-subtle) px-3 py-2 text-xs text-(--accent)">
          {Object.entries(fieldErrors).map(([field, message]) => (
            <li key={field}>
              {field}: {message}
            </li>
          ))}
        </ul>
      ) : null}

      {statusMessage ? <p className="text-sm text-(--text-secondary)">{statusMessage}</p> : null}

      <Button
        type="submit"
        className="w-full border-0 text-black hover:brightness-105"
        style={{
          backgroundImage:
            'linear-gradient(100deg, #c0483f 0%, #dd6639 35%, #eda642 70%, #f6dd53 100%)',
        }}
        disabled={isSubmitting}
      >
        <LogIn className="h-4 w-4" />
        {isSubmitting ? 'Signing in...' : 'Sign in'}
      </Button>
    </form>
  )
}
