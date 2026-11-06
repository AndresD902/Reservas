import { describe, expect, it } from 'vitest'
import { ApiError } from '../api/client'
import { describeApiError } from './error-messages'

describe('describeApiError', () => {
  it('translates a reservation conflict into a clear message', () => {
    const error = new ApiError(
      409,
      'RECURSO_NO_DISPONIBLE',
      'El recurso ya se encuentra reservado para este horario',
    )

    expect(describeApiError(error)).toBe(
      'El recurso ya está reservado en ese horario. Elige otro intervalo.',
    )
  })

  it('translates an invalid range error', () => {
    const error = new ApiError(400, 'FECHAS_INVALIDAS', 'Las fechas no son válidas')

    expect(describeApiError(error)).toBe(
      'La fecha de fin debe ser posterior a la fecha de inicio.',
    )
  })

  it('translates duplicate user data into clear messages', () => {
    expect(
      describeApiError(new ApiError(400, 'DOCUMENTO_DUPLICADO', 'Duplicado')),
    ).toBe('Ya existe un usuario con ese documento.')
    expect(
      describeApiError(new ApiError(400, 'CORREO_DUPLICADO', 'Duplicado')),
    ).toBe('Ya existe un usuario con ese correo electrónico.')
  })

  it('translates a resource maintenance conflict', () => {
    const error = new ApiError(
      409,
      'RECURSO_CON_RESERVAS_FUTURAS',
      'El recurso tiene reservas activas',
    )

    expect(describeApiError(error)).toBe(
      'No se puede poner el recurso en mantenimiento porque tiene reservas activas futuras.',
    )
  })

  it('falls back to the API message for unknown codes', () => {
    const error = new ApiError(500, 'ALGO_NUEVO', 'Mensaje de la API')

    expect(describeApiError(error)).toBe('Mensaje de la API')
  })

  it('returns a generic message for unexpected errors', () => {
    expect(describeApiError(new Error('boom'))).toBe(
      'No se pudo completar la operación. Inténtalo de nuevo.',
    )
  })
})
