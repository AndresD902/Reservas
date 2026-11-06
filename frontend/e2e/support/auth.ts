import { expect } from '@playwright/test'
import type { APIRequestContext, BrowserContext } from '@playwright/test'

const API_URL = process.env.E2E_API_URL ?? 'http://localhost:8080'
const ADMIN_EMAIL = process.env.E2E_ADMIN_EMAIL ?? 'admin@example.com'
const ADMIN_PASSWORD = process.env.E2E_ADMIN_PASSWORD ?? 'admin123'

let csrf: { headerName: string; token: string } | null = null

export async function loginAsAdmin(request: APIRequestContext, context: BrowserContext): Promise<void> {
  const csrfResponse = await request.get(`${API_URL}/auth/csrf`)
  expect(csrfResponse.status()).toBe(200)
  csrf = (await csrfResponse.json()) as { headerName: string; token: string }

  const loginResponse = await request.post(`${API_URL}/auth/login`, {
    headers: { [csrf.headerName]: csrf.token },
    data: { correo: ADMIN_EMAIL, password: ADMIN_PASSWORD },
  })

  expect(loginResponse.status()).toBe(200)

  const storageState = await request.storageState()
  await context.addCookies(storageState.cookies)
}

export function csrfHeaders(): Record<string, string> {
  return csrf ? { [csrf.headerName]: csrf.token } : {}
}
