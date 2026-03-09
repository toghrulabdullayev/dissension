import { create } from 'zustand';
import { setAccessToken, clearAccessToken } from '@/shared/api';
import type { UUID, AuthProvider, UserStatus } from '@/shared/types';

type AuthUser = {
  id: UUID;
  username: string;
  email: string;
  avatarUrl: string | null;
  status: UserStatus;
  authProvider: AuthProvider;
};

type AuthState = {
  user: AuthUser | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  setAuth: (user: AuthUser, accessToken: string) => void;
  clearAuth: () => void;
  setLoading: (loading: boolean) => void;
};

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: false,
  isLoading: true,

  setAuth: (user, accessToken) => {
    setAccessToken(accessToken);
    set({ user, isAuthenticated: true, isLoading: false });
  },

  clearAuth: () => {
    clearAccessToken();
    set({ user: null, isAuthenticated: false, isLoading: false });
  },

  setLoading: (loading) => set({ isLoading: loading }),
}));
