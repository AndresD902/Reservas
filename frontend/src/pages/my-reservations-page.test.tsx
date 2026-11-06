import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it } from 'vitest'
import type { Reservation } from '../api/types'
import { reservation } from '../test/fixtures'
import { jsonResponse, requestUrl, stubFetch } from '../test/http'
import { renderWithProviders } from '../test/render'
import { MyReservationsPage } from './my-reservations-page'

const cancelledReservation: Reservation = {
  ...reservation,
  id: 101,
  estado: 'CANCELADA',
}

const finishedReservation: Reservation = {
  ...reservation,
  id: 102,
  estado: 'FINALIZADA',
  fechaInicio: '2026-11-05T10:00:00',
  fechaFin: '2026-11-05T11:00:00',
}

function mockReservationsApi(initialReservations: Reservation[]) {
  let reservations = initialReservations

  return stubFetch((input, init) => {
    const url = requestUrl(input)
    const method = init?.method ?? 'GET'

    if (url.endsWith('/api/auth/csrf')) {
      return jsonResponse({})
    }

    if (url.endsWith('/api/reservas/usuario/1') && method === 'GET') {
      return jsonResponse(reservations)
    }

    if (url.endsWith('/api/reservas/100') && method === 'DELETE') {
      const cancelled = {
        ...reservations.find((item) => item.id === 100)!,
        estado: 'CANCELADA' as const,
      }
      reservations = reservations.map((item) =>
        item.id === 100 ? cancelled : item,
      )
      return jsonResponse(cancelled)
    }

    return jsonResponse({}, 404)
  })
}

describe('MyReservationsPage', () => {
  beforeEach(() => {
    window.localStorage.setItem('reservas.selectedUserId', '1')
  })

  it('lists the selected user reservations with their status', async () => {
    mockReservationsApi([reservation, cancelledReservation, finishedReservation])

    renderWithProviders(<MyReservationsPage />)

    const list = await screen.findByRole('list', { name: 'Mis reservas' })
    expect(within(list).getByText('Activa')).toBeInTheDocument()
    expect(within(list).getByText('Cancelada')).toBeInTheDocument()
    expect(within(list).getByText('Finalizada')).toBeInTheDocument()
    expect(within(list).getAllByText('Sala 101')).toHaveLength(3)
  })

  it('only offers cancellation for active reservations', async () => {
    mockReservationsApi([reservation, cancelledReservation, finishedReservation])

    renderWithProviders(<MyReservationsPage />)

    const list = await screen.findByRole('list', { name: 'Mis reservas' })
    expect(
      within(list).getAllByRole('button', { name: 'Cancelar reserva' }),
    ).toHaveLength(1)
  })

  it('cancels an active reservation after confirmation', async () => {
    const fetchMock = mockReservationsApi([reservation])

    renderWithProviders(<MyReservationsPage />)

    await screen.findByRole('list', { name: 'Mis reservas' })
    await userEvent.click(
      screen.getByRole('button', { name: 'Cancelar reserva' }),
    )

    const dialog = screen.getByRole('dialog', { name: 'Cancelar reserva' })
    expect(dialog).toHaveTextContent('Sala 101')
    expect(
      fetchMock.mock.calls.some(([, init]) => init?.method === 'DELETE'),
    ).toBe(false)

    await userEvent.click(within(dialog).getByRole('button', { name: 'Sí, cancelar' }))

    expect(await screen.findByRole('status')).toHaveTextContent(
      'Reserva cancelada correctamente.',
    )
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(
      await screen.findByRole('article', { name: /Sala 101.*Cancelada/ }),
    ).toBeInTheDocument()
  })
})
