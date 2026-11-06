import type { ApiErrorBody } from './types'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api'
const CSRF_COOKIE = 'XSRF-TOKEN'
const CSRF_HEADER = 'X-XSRF-TOKEN'

function getCookie(name: string): string | null {
  const prefix = `${name}=`
  const entry = document.cookie.split('; ').find((row) => row.startsWith(prefix))
  return entry ? decodeURIComponent(entry.slice(prefix.length)) : null
}

let csrfRequest: Promise<void> | null = null

async function ensureCsrfToken(): Promise<void> {
  if (getCookie(CSRF_COOKIE)) {
    return
  }

  if (!csrfRequest) {
    csrfRequest = (async () => {
      await fetch(`${API_BASE_URL}/auth/csrf`, { credentials: 'include' })
    })().finally(() => {
      csrfRequest = null
    })
  }

  await csrfRequest
}

function isMutating(method: string): boolean {
  return !['GET', 'HEAD', 'OPTIONS'].includes(method.toUpperCase())
}

export class ApiError extends Error {
  readonly status: number
  readonly code: string

  constructor(status: number, code: string, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
  }
}

function isApiErrorBody(value: unknown): value is ApiErrorBody {
  if (typeof value !== 'object' || value === null) {
    return false
  }

  const candidate = value as Record<string, unknown>
  return (
    typeof candidate.status === 'number' &&
    typeof candidate.error === 'string' &&
    typeof candidate.message === 'string'
  )
}

async function readErrorBody(response: Response): Promise<ApiErrorBody | null> {
  try {
    const body: unknown = await response.json()
    return isApiErrorBody(body) ? body : null
  } catch {
    return null
  }
}

export async function apiFetch<T>(path: string, init: RequestInit = {}): Promise<T> {
  let response: Response

  try {
    const headers = new Headers(init.headers)
    headers.set('Accept', 'application/json')
    if (init.body !== undefined && !headers.has('Content-Type')) {
      headers.set('Content-Type', 'application/json')
    }

    if (isMutating(init.method ?? 'GET')) {
      await ensureCsrfToken()
      const csrfToken = getCookie(CSRF_COOKIE)
      if (csrfToken) {
        headers.set(CSRF_HEADER, csrfToken)
      }
    }

    response = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      headers,
      credentials: 'include',
    })
  } catch {
    throw new ApiError(
      0,
      'NETWORK_ERROR',
      'No se pudo conectar con la API. Verifica que el backend esté en ejecución.',
    )
  }

  if (!response.ok) {
    const body = await readErrorBody(response)
    throw new ApiError(
      body?.status ?? response.status,
      body?.error ?? 'ERROR_DESCONOCIDO',
      body?.message ?? 'La API devolvió un error inesperado.',
    )
  }

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}
