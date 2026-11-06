import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import type { User } from '../api/types'
import { activeUser, pageOf } from '../test/fixtures'
import { jsonResponse, requestUrl, stubFetch } from '../test/http'
import { renderWithProviders } from '../test/render'
import { AdminUsersPage } from './admin-users-page'

const inactiveUser: User = {
  id: 2,
  nombre: 'Carlos Ruiz',
  documento: '200000002',
  correo: 'carlos.ruiz@example.com',
  estado: 'INACTIVO',
}

function mockUsersApi(initialUsers: User[]) {
  let users = initialUsers

  return stubFetch((input, init) => {
    const url = requestUrl(input)
    const method = init?.method ?? 'GET'

    if (url.endsWith('/api/usuarios') && method === 'POST') {
      const body = JSON.parse(String(init?.body)) as Omit<User, 'id'>
      const createdUser: User = { id: 99, ...body }
      users = [...users, createdUser]
      return jsonResponse(createdUser, 201)
    }

    if (url.endsWith('/estado') && method === 'PATCH') {
      const userId = Number(url.match(/\/usuarios\/(\d+)\/estado/)?.[1])
      const body = JSON.parse(String(init?.body)) as Pick<User, 'estado'>
      const updatedUser = users.find((user) => user.id === userId)
      const result = { ...updatedUser!, estado: body.estado }
      users = users.map((user) => (user.id === userId ? result : user))
      return jsonResponse(result)
    }

    if (url.includes('/api/usuarios?')) {
      return jsonResponse(pageOf(users))
    }

    return jsonResponse({}, 404)
  })
}

function fillUserForm() {
  return {
    name: screen.getByLabelText('Nombre completo'),
    document: screen.getByLabelText('Documento'),
    email: screen.getByLabelText('Correo electrónico'),
  }
}

describe('AdminUsersPage', () => {
  it('lists users and registers a new one', async () => {
    mockUsersApi([activeUser])

    renderWithProviders(<AdminUsersPage />)

    expect(await screen.findByText('Ana Torres')).toBeInTheDocument()

    const fields = fillUserForm()
    await userEvent.type(fields.name, 'Laura Gómez')
    await userEvent.type(fields.document, '300000003')
    await userEvent.type(fields.email, 'laura.gomez@example.com')
    await userEvent.click(screen.getByRole('button', { name: 'Registrar usuario' }))

    expect(
      await screen.findByText('Usuario registrado correctamente.'),
    ).toBeInTheDocument()
    expect(
      within(screen.getByRole('table', { name: 'Usuarios registrados' })).getByText(
        'Laura Gómez',
      ),
    ).toBeInTheDocument()
  })

  it('shows a clear error when the document already exists', async () => {
    stubFetch((input, init) => {
      if (requestUrl(input).endsWith('/api/usuarios') && init?.method === 'POST') {
        return jsonResponse(
          {
            status: 400,
            error: 'DOCUMENTO_DUPLICADO',
            message: 'El documento ya está registrado',
          },
          400,
        )
      }

      return jsonResponse(pageOf([activeUser]))
    })

    renderWithProviders(<AdminUsersPage />)
    await screen.findByText('Ana Torres')

    const fields = fillUserForm()
    await userEvent.type(fields.name, 'Laura Gómez')
    await userEvent.type(fields.document, activeUser.documento)
    await userEvent.type(fields.email, 'laura.gomez@example.com')
    await userEvent.click(screen.getByRole('button', { name: 'Registrar usuario' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Ya existe un usuario con ese documento.',
    )
  })

  it('shows the detail and changes the user status', async () => {
    mockUsersApi([activeUser, inactiveUser])

    renderWithProviders(<AdminUsersPage />)
    const table = await screen.findByRole('table', { name: 'Usuarios registrados' })
    const row = within(table).getByRole('row', { name: /Ana Torres/ })

    await userEvent.click(within(row).getByRole('button', { name: 'Ver detalle' }))

    expect(screen.getByText('Detalle del usuario')).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: 'Inactivar usuario' }))

    expect(
      await screen.findByText('Estado del usuario actualizado.'),
    ).toBeInTheDocument()
    expect(
      await within(table).findByRole('row', { name: /Ana Torres.*Inactivo/ }),
    ).toBeInTheDocument()
  })
})
