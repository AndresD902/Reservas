import type { ApiErrorBody } from './types'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api'

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

    response = await fetch(`${API_BASE_URL}${path}`, { ...init, headers })
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
