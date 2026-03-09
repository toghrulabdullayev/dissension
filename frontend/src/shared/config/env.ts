export const env = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL as string,
  wsUrl: import.meta.env.VITE_WS_URL as string,
} as const;
