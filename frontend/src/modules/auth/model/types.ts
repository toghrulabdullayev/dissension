export type LoginPayload = {
  username: string
  password: string
}

export type SignupPayload = {
  username: string
  password: string
  confirmPassword: string
}

export type AuthResponse = {
  token: string
  username: string
}

export type ErrorResponse = {
  message?: string
  validationErrors?: Record<string, string>
}
