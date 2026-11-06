import { describe, expect, it } from 'vitest'
import { ApiError, apiFetch } from './client'
import { jsonResponse, stubFetch } from '../test/http'

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
})
