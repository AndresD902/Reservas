import { describe, expect, it } from 'vitest'
import { ApiError, apiFetch } from './client'
import { jsonResponse, requestUrl, stubFetch } from '../test/http'

describe('apiFetch', () => {
  it('returns the parsed JSON body of a successful response', async () => {
    stubFetch(() => jsonResponse({ id: 1, nombre: 'Sala 101' }))

    await expect(apiFetch<{ id: number; nombre: string }>('/recursos/1')).resolves.toEqual({
      id: 1,
      nombre: 'Sala 101',
    })
  })

  it('throws an ApiError with the error body returned by the API', async () => {
    stubFetch(() =>
      jsonResponse(
        {
          status: 409,
          error: 'RECURSO_NO_DISPONIBLE',
          message: 'El recurso ya se encuentra reservado para este horario',
        },
        409,
      ),
    )

    await expect(apiFetch('/reservas')).rejects.toMatchObject({
      name: 'ApiError',
      status: 409,
      code: 'RECURSO_NO_DISPONIBLE',
      message: 'El recurso ya se encuentra reservado para este horario',
    })
  })

  it('throws a network error when the API cannot be reached', async () => {
    stubFetch(() => {
      throw new Error('connection refused')
    })

    await expect(apiFetch('/recursos')).rejects.toMatchObject({
      name: 'ApiError',
      status: 0,
      code: 'NETWORK_ERROR',
    })
  })

  it('returns undefined for a 204 response', async () => {
    stubFetch(() => new Response(null, { status: 204 }))

    await expect(apiFetch('/reservas/1')).resolves.toBeUndefined()
  })

  it('wraps the error in an ApiError instance', async () => {
    stubFetch(() => new Response('not json', { status: 500 }))

    await expect(apiFetch('/recursos')).rejects.toBeInstanceOf(ApiError)
  })

  it('sends the CSRF token from the cookie on mutating requests', async () => {
    document.cookie = 'XSRF-TOKEN=token-de-prueba'
    const fetchMock = stubFetch(() => jsonResponse({ id: 1 }))

    await apiFetch('/usuarios', { method: 'POST', body: '{}' })

    const headers = fetchMock.mock.calls[0]?.[1]?.headers as Headers
    expect(headers.get('X-XSRF-TOKEN')).toBe('token-de-prueba')

    document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT'
  })

  it('obtiene el token CSRF cuando la cookie no existe', async () => {
    document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT'
    const fetchMock = stubFetch((input) =>
      requestUrl(input).endsWith('/auth/csrf')
        ? jsonResponse({ headerName: 'X-XSRF-TOKEN', token: 'nuevo' })
        : jsonResponse({ id: 1 }),
    )

    await apiFetch('/usuarios', { method: 'POST', body: '{}' })

    expect(fetchMock).toHaveBeenCalledTimes(2)
    expect(requestUrl(fetchMock.mock.calls[0]![0])).toContain('/auth/csrf')
  })
})
