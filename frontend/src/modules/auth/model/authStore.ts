import { create } from 'zustand'
import { createJSONStorage, persist } from 'zustand/middleware'
import type { AuthResponse } from './types'

type AuthState = {
  token: string | null
  username: string | null
  setSession: (session: AuthResponse) => void
  clearSession: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      username: null,
      setSession: (session) => {
        set({ token: session.token, username: session.username })
      },
      clearSession: () => {
        set({ token: null, username: null })
      },
    }),
    {
      name: 'auth-session',
      storage: createJSONStorage(() => localStorage),
    },
  ),
)
