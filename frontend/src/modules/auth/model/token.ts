export function isTokenActive(token: string | null): boolean {
  if (!token) {
    return false
  }

  const parts = token.split('.')
  if (parts.length !== 3) {
    return false
  }

  try {
    const payloadBase64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const padded = payloadBase64.padEnd(payloadBase64.length + ((4 - (payloadBase64.length % 4)) % 4), '=')
    const payload = JSON.parse(atob(padded)) as { exp?: number }

    if (typeof payload.exp !== 'number') {
      return false
    }

    const nowInSeconds = Math.floor(Date.now() / 1000)
    return payload.exp > nowInSeconds
  } catch {
    return false
  }
}