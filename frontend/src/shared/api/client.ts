import axios from 'axios';
import { env } from '@/shared/config/env';

export const apiClient = axios.create({
  baseURL: env.apiBaseUrl,
  withCredentials: true, // required for httpOnly refresh token cookie
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach the in-memory access token to every request
apiClient.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// ---------------------------------------------------------------
// In-memory access token store (never touches localStorage)
// ---------------------------------------------------------------
let _accessToken: string | null = null;

export function setAccessToken(token: string): void {
  _accessToken = token;
}

export function getAccessToken(): string | null {
  return _accessToken;
}

export function clearAccessToken(): void {
  _accessToken = null;
}
