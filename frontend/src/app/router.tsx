import { Navigate, RouterProvider, createBrowserRouter } from 'react-router-dom'
import { LoginPage } from '../modules/auth/pages/LoginPage'
import { SignupPage } from '../modules/auth/pages/SignupPage'
import { ChannelsPage } from '../modules/channels/pages/ChannelsPage'

const router = createBrowserRouter([
  {
    path: '/',
    element: <Navigate to="/login" replace />,
  },
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/signup',
    element: <SignupPage />,
  },
  {
    path: '/channels',
    element: <ChannelsPage />,
  },
])

export function AppRouter() {
  return <RouterProvider router={router} />
}
