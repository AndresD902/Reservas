import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { availableResource, maintenanceResource, pageOf } from '../test/fixtures'
import { jsonResponse, requestUrl, stubFetch } from '../test/http'
import { renderWithProviders } from '../test/render'
import { ReservationPage } from './reservation-page'

describe('ReservationPage', () => {
  it('lists the resources and only offers the reservation link for available ones', async () => {
    stubFetch(() =>
      jsonResponse(pageOf([availableResource, maintenanceResource])),
    )

    renderWithProviders(<ReservationPage />)

    expect(await screen.findByText('Sala 101')).toBeInTheDocument()
    expect(screen.getByText('Equipo audiovisual')).toBeInTheDocument()
    expect(
      screen.getByRole('link', { name: 'Reservar Sala 101' }),
    ).toBeInTheDocument()
    expect(screen.getByText('No disponible para reservar')).toBeInTheDocument()
  })

  it('filters the resources by the search term', async () => {
    stubFetch(() =>
      jsonResponse(pageOf([availableResource, maintenanceResource])),
    )

    renderWithProviders(<ReservationPage />)
    await screen.findByText('Sala 101')

    await userEvent.type(screen.getByLabelText('Buscar recurso'), 'audiovisual')

    expect(screen.queryByText('Sala 101')).not.toBeInTheDocument()
    expect(screen.getByText('Equipo audiovisual')).toBeInTheDocument()
  })

  it('shows an empty state when there are no resources', async () => {
    stubFetch(() => jsonResponse(pageOf([])))

    renderWithProviders(<ReservationPage />)

    expect(await screen.findByText('No hay recursos registrados')).toBeInTheDocument()
  })

  it('shows an error state when the resource request fails', async () => {
    stubFetch(() =>
      jsonResponse(
        {
          status: 500,
          error: 'ERROR_INTERNO',
          message: 'No se pudo consultar el listado',
        },
        500,
      ),
    )

    renderWithProviders(<ReservationPage />)

    expect(
      await screen.findByText('No se pudieron cargar los recursos.'),
    ).toBeInTheDocument()
    expect(screen.getByText('No se pudo consultar el listado')).toBeInTheDocument()
  })

  it('asks for an active user before starting the reservation', async () => {
    stubFetch(() => jsonResponse(pageOf([availableResource])))

    renderWithProviders(<ReservationPage />)

    expect(
      await screen.findByText(
        'Selecciona un usuario activo en la parte superior para reservar.',
      ),
    ).toBeInTheDocument()
  })

  it('requests the resource listing with pagination parameters', async () => {
    const fetchMock = stubFetch(() => jsonResponse(pageOf([availableResource])))

    renderWithProviders(<ReservationPage />)
    await screen.findByText('Sala 101')

    expect(requestUrl(fetchMock.mock.calls[0]?.[0] as RequestInfo)).toContain(
      '/api/recursos?size=100',
    )
  })
})
