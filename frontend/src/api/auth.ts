import { apiFetch } from './client'
import type { Administrator, LoginRequest } from './types'

export async function getCurrentAdministrator(): Promise<Administrator> {
  return apiFetch<Administrator>('/auth/me')
}

export async function login(request: LoginRequest): Promise<Administrator> {
  return apiFetch<Administrator>('/auth/login', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export async function logout(): Promise<void> {
  return apiFetch<void>('/auth/logout', { method: 'POST' })
}
