import { useEffect, type ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuthStore } from '../modules/auth/model/authStore'
import { isTokenActive } from '../modules/auth/model/token'

type RequireGuestProps = {
  children: ReactNode
}

export function RequireGuest({ children }: RequireGuestProps) {
  const token = useAuthStore((state) => state.token)
  const clearSession = useAuthStore((state) => state.clearSession)
  const hasValidToken = isTokenActive(token)

  useEffect(() => {
    if (token && !hasValidToken) {
      clearSession()
    }
  }, [token, hasValidToken, clearSession])

  if (hasValidToken) {
    return <Navigate to="/channels" replace />
  }

  return <>{children}</>
}