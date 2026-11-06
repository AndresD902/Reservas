import { QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { UserSelectionProvider } from '../context/user-selection-provider'
import { activeUser, pageOf } from '../test/fixtures'
import { jsonResponse, requestUrl, stubFetch } from '../test/http'
import { createTestQueryClient } from '../test/render'
import { UserSelector } from './user-selector'

function renderSelector() {
  return render(
    <QueryClientProvider client={createTestQueryClient()}>
      <UserSelectionProvider>
        <UserSelector />
      </UserSelectionProvider>
    </QueryClientProvider>,
  )
}

describe('UserSelector', () => {
  it('loads only active users and keeps the selection', async () => {
    const fetchMock = stubFetch(() => jsonResponse(pageOf([activeUser])))

    renderSelector()

    expect(await screen.findByRole('option', { name: 'Ana Torres' })).toBeInTheDocument()
    expect(requestUrl(fetchMock.mock.calls[0]?.[0] as RequestInfo)).toContain(
      'estado=ACTIVO',
    )

    await userEvent.selectOptions(screen.getByLabelText('Usuario'), '1')

    expect(screen.getByLabelText('Usuario')).toHaveValue('1')
    expect(window.localStorage.getItem('reservas.selectedUserId')).toBe('1')
  })

  it('shows an error with a retry action when the users cannot be loaded', async () => {
    stubFetch(() => {
      throw new Error('connection refused')
    })

    renderSelector()

    expect(
      await screen.findByText(/No se pudieron cargar los usuarios/),
    ).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Reintentar' })).toBeInTheDocument()
  })
})
