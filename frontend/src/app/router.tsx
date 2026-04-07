import { Navigate, RouterProvider, createBrowserRouter } from 'react-router-dom'
import { RequireAuth } from './RequireAuth'
import { RequireGuest } from './RequireGuest'
import { useAuthStore } from '../modules/auth/model/authStore'
import { isTokenActive } from '../modules/auth/model/token'
import { LoginPage } from '../modules/auth/pages/LoginPage'
import { SignupPage } from '../modules/auth/pages/SignupPage'
import { ChannelsPage } from '../modules/channels/pages/ChannelsPage'

function RootRedirect() {
  const token = useAuthStore((state) => state.token)
  const clearSession = useAuthStore((state) => state.clearSession)
  const hasValidToken = isTokenActive(token)

  if (token && !hasValidToken) {
    clearSession()
  }

  return <Navigate to={hasValidToken ? '/channels' : '/login'} replace />
}

const router = createBrowserRouter([
  {
    path: '/',
    element: <RootRedirect />,
  },
  {
    path: '/login',
    element: (
      <RequireGuest>
        <LoginPage />
      </RequireGuest>
    ),
  },
  {
    path: '/signup',
    element: (
      <RequireGuest>
        <SignupPage />
      </RequireGuest>
    ),
  },
  {
    path: '/channels',
    element: (
      <RequireAuth>
        <ChannelsPage />
      </RequireAuth>
    ),
  },
  {
    path: '/channels/@me',
    element: (
      <RequireAuth>
        <ChannelsPage />
      </RequireAuth>
    ),
  },
  {
    path: '/channels/:serverId',
    element: (
      <RequireAuth>
        <ChannelsPage />
      </RequireAuth>
    ),
  },
  {
    path: '/channels/:serverId/:channelId',
    element: (
      <RequireAuth>
        <ChannelsPage />
      </RequireAuth>
    ),
  },
])

export function AppRouter() {
  return <RouterProvider router={router} />
}
