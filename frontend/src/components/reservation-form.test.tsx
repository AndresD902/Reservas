import { fireEvent, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { availableResource, reservation } from '../test/fixtures'
import { jsonResponse, requestUrl, stubFetch } from '../test/http'
import { renderWithProviders } from '../test/render'
import { toDateTimeLocalValue } from '../utils/date-time'
import { ReservationForm } from './reservation-form'

function futureRange(): { start: string; end: string } {
  const start = new Date()
  start.setDate(start.getDate() + 1)
  start.setHours(10, 0, 0, 0)
  const end = new Date(start.getTime() + 60 * 60 * 1000)

  return {
    start: toDateTimeLocalValue(start),
    end: toDateTimeLocalValue(end),
  }
}

function fillRange(start: string, end: string): void {
  fireEvent.change(screen.getByLabelText('Fecha y hora de inicio'), {
    target: { value: start },
  })
  fireEvent.change(screen.getByLabelText('Fecha y hora de fin'), {
    target: { value: end },
  })
}

function renderForm() {
  return renderWithProviders(
    <ReservationForm resource={availableResource} userId={1} />,
  )
}

describe('ReservationForm', () => {
  it('shows a validation error and skips the API when the end is before the start', async () => {
    const fetchMock = stubFetch(() => jsonResponse({}))
    const { start } = futureRange()
    const earlierEnd = toDateTimeLocalValue(
      new Date(new Date(start).getTime() - 60 * 60 * 1000),
    )

    renderForm()
    fillRange(start, earlierEnd)
    await userEvent.click(screen.getByRole('button', { name: 'Reservar' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'La fecha de fin debe ser posterior a la fecha de inicio.',
    )
    expect(fetchMock).not.toHaveBeenCalled()
  })

  it('shows a clear conflict message when the API responds with 409', async () => {
    stubFetch((input) => {
      if (requestUrl(input).endsWith('/reservas')) {
        return jsonResponse(
          {
            status: 409,
            error: 'RECURSO_NO_DISPONIBLE',
            message: 'El recurso ya se encuentra reservado para este horario',
          },
          409,
        )
      }

      return jsonResponse({})
    })
    const { start, end } = futureRange()

    renderForm()
    fillRange(start, end)
    await userEvent.click(screen.getByRole('button', { name: 'Reservar' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'El recurso ya está reservado en ese horario. Elige otro intervalo.',
    )
  })

  it('shows the reservation confirmation when the API accepts the request', async () => {
    stubFetch((input) =>
      requestUrl(input).endsWith('/reservas')
        ? jsonResponse(reservation, 201)
        : jsonResponse({}),
    )
    const { start, end } = futureRange()

    renderForm()
    fillRange(start, end)
    await userEvent.click(screen.getByRole('button', { name: 'Reservar' }))

    expect(await screen.findByRole('status')).toHaveTextContent('Reserva confirmada')
    expect(screen.getByText('Sala 101 · Ana Torres')).toBeInTheDocument()
  })

  it('shows the overlapping reservations when the resource is not available', async () => {
    stubFetch((input) =>
      requestUrl(input).includes('/disponibilidad')
        ? jsonResponse({
            recursoId: 10,
            fechaInicio: '2026-11-07T10:00:00',
            fechaFin: '2026-11-07T11:00:00',
            disponible: false,
            reservasSolapadas: [reservation],
          })
        : jsonResponse({}),
    )
    const { start, end } = futureRange()

    renderForm()
    fillRange(start, end)
    await userEvent.click(
      screen.getByRole('button', { name: 'Comprobar disponibilidad' }),
    )

    expect(
      await screen.findByText('El recurso no está disponible en ese intervalo.'),
    ).toBeInTheDocument()
    expect(screen.getByText(/Ana Torres/)).toBeInTheDocument()
  })

  it('shows the availability confirmation when the resource is free', async () => {
    stubFetch((input) =>
      requestUrl(input).includes('/disponibilidad')
        ? jsonResponse({
            recursoId: 10,
            fechaInicio: '2026-11-07T10:00:00',
            fechaFin: '2026-11-07T11:00:00',
            disponible: true,
            reservasSolapadas: [],
          })
        : jsonResponse({}),
    )
    const { start, end } = futureRange()

    renderForm()
    fillRange(start, end)
    await userEvent.click(
      screen.getByRole('button', { name: 'Comprobar disponibilidad' }),
    )

    expect(
      await screen.findByText('El recurso está disponible en ese intervalo.'),
    ).toBeInTheDocument()
  })
})
