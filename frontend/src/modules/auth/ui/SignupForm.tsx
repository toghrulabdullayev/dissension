import { KeyRound, UserPlus, UserRound } from 'lucide-react'
import { useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { authApi, ApiError } from '../api/authApi'
import { useAuthStore } from '../model/authStore'
import { Button } from '../../../shared/ui/button'
import { Input } from '../../../shared/ui/input'
import { Label } from '../../../shared/ui/label'

export function SignupForm() {
  const navigate = useNavigate()
  const setSession = useAuthStore((state) => state.setSession)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [statusMessage, setStatusMessage] = useState('')
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setStatusMessage('')
    setFieldErrors({})

    if (password !== confirmPassword) {
      setFieldErrors({ confirmPassword: 'Passwords do not match' })
      return
    }

    setIsSubmitting(true)

    try {
      const response = await authApi.signup({ username, password, confirmPassword })
      setSession(response)
      navigate('/channels')
      setPassword('')
      setConfirmPassword('')
    } catch (error) {
      if (error instanceof ApiError) {
        setStatusMessage(error.message)
        setFieldErrors(error.validationErrors)
      } else {
        setStatusMessage('Unexpected error while creating account.')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <form className="space-y-4" onSubmit={handleSubmit} noValidate>
      <div className="space-y-2">
        <Label htmlFor="signup-username">Username</Label>
        <div className="relative">
          <UserRound className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <Input
            id="signup-username"
            className="pl-9"
            autoComplete="username"
            placeholder="Choose a username"
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            required
          />
        </div>
      </div>

      <div className="space-y-2">
        <Label htmlFor="signup-password">Password</Label>
        <div className="relative">
          <KeyRound className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <Input
            id="signup-password"
            type="password"
            className="pl-9"
            autoComplete="new-password"
            placeholder="Create a password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
          />
        </div>
      </div>

      <div className="space-y-2">
        <Label htmlFor="signup-confirm-password">Confirm password</Label>
        <div className="relative">
          <KeyRound className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <Input
            id="signup-confirm-password"
            type="password"
            className="pl-9"
            autoComplete="new-password"
            placeholder="Repeat the password"
            value={confirmPassword}
            onChange={(event) => setConfirmPassword(event.target.value)}
            required
          />
        </div>
      </div>

      {Object.keys(fieldErrors).length > 0 ? (
        <ul className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-xs text-red-700">
          {Object.entries(fieldErrors).map(([field, message]) => (
            <li key={field}>
              {field}: {message}
            </li>
          ))}
        </ul>
      ) : null}

      {statusMessage ? <p className="text-sm text-slate-600">{statusMessage}</p> : null}

      <Button type="submit" className="w-full" disabled={isSubmitting}>
        <UserPlus className="h-4 w-4" />
        {isSubmitting ? 'Creating account...' : 'Create account'}
      </Button>
    </form>
  )
}
