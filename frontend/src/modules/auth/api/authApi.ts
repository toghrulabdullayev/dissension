import type {
  AuthResponse,
  ErrorResponse,
  LoginPayload,
  SignupPayload,
} from '../model/types'

const API_BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export class ApiError extends Error {
  validationErrors: Record<string, string>

  constructor(message: string, validationErrors: Record<string, string> = {}) {
    super(message)
    this.validationErrors = validationErrors
  }
}

async function parseErrorResponse(response: Response): Promise<ErrorResponse> {
  const raw = await response.text()

  if (!raw) {
    return {}
  }

  try {
    return JSON.parse(raw) as ErrorResponse
  } catch {
    return {}
  }
}

async function postAuth<TPayload>(
  endpoint: '/api/auth/login' | '/api/auth/signup',
  payload: TPayload,
): Promise<AuthResponse> {
  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    const errorBody = await parseErrorResponse(response)
    throw new ApiError(
      errorBody.message ?? 'Authentication request failed',
      errorBody.validationErrors ?? {},
    )
  }

  return (await response.json()) as AuthResponse
}

export const authApi = {
  login: (payload: LoginPayload) => postAuth('/api/auth/login', payload),
  signup: (payload: SignupPayload) => postAuth('/api/auth/signup', payload),
}
