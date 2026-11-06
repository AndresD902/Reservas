import { describe, expect, it } from 'vitest'
import {
  formatDateTime,
  toApiDateTime,
  toDateTimeLocalValue,
  validateReservationRange,
} from './date-time'

describe('toDateTimeLocalValue', () => {
  it('formats a date for a datetime-local input', () => {
    const value = toDateTimeLocalValue(new Date(2026, 10, 7, 9, 5))

    expect(value).toBe('2026-11-07T09:05')
  })
})

describe('toApiDateTime', () => {
  it('adds seconds when the value comes from a datetime-local input', () => {
    expect(toApiDateTime('2026-11-07T09:05')).toBe('2026-11-07T09:05:00')
  })

  it('keeps a value that already includes seconds', () => {
    expect(toApiDateTime('2026-11-07T09:05:30')).toBe('2026-11-07T09:05:30')
  })
})

describe('validateReservationRange', () => {
  const now = new Date(2026, 10, 6, 8, 0)

  it('rejects a range without both dates', () => {
    expect(validateReservationRange('', '', now)).toBe(
      'Selecciona la fecha y hora de inicio y de fin.',
    )
  })

  it('rejects an end date before the start date', () => {
    expect(
      validateReservationRange('2026-11-07T10:00', '2026-11-07T09:00', now),
    ).toBe('La fecha de fin debe ser posterior a la fecha de inicio.')
  })

  it('rejects an end date equal to the start date', () => {
    expect(
      validateReservationRange('2026-11-07T10:00', '2026-11-07T10:00', now),
    ).toBe('La fecha de fin debe ser posterior a la fecha de inicio.')
  })

  it('rejects a start date in the past', () => {
    expect(
      validateReservationRange('2026-11-05T10:00', '2026-11-05T11:00', now),
    ).toBe('No se puede reservar en el pasado.')
  })

  it('accepts a future range with a later end date', () => {
    expect(
      validateReservationRange('2026-11-07T10:00', '2026-11-07T11:00', now),
    ).toBeNull()
  })
})

describe('formatDateTime', () => {
  it('keeps the original value when it cannot be parsed', () => {
    expect(formatDateTime('no-es-una-fecha')).toBe('no-es-una-fecha')
  })

  it('formats a valid ISO date', () => {
    expect(formatDateTime('2026-11-07T10:00:00')).toContain('2026')
  })
})
