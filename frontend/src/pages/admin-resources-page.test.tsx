import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import type { Resource } from '../api/types'
import { availableResource, pageOf } from '../test/fixtures'
import { jsonResponse, requestUrl, stubFetch } from '../test/http'
import { renderWithProviders } from '../test/render'
import { AdminResourcesPage } from './admin-resources-page'

function mockResourcesApi(initialResources: Resource[]) {
  let resources = initialResources

  return stubFetch((input, init) => {
    const url = requestUrl(input)
    const method = init?.method ?? 'GET'

    if (url.endsWith('/api/recursos') && method === 'POST') {
      const body = JSON.parse(String(init?.body)) as Omit<Resource, 'id'>
      const createdResource: Resource = { id: 99, ...body }
      resources = [...resources, createdResource]
      return jsonResponse(createdResource, 201)
    }

    if (url.match(/\/api\/recursos\/\d+$/) && method === 'PUT') {
      const resourceId = Number(url.split('/').at(-1))
      const body = JSON.parse(String(init?.body)) as Omit<Resource, 'id'>
      const result = { ...body, id: resourceId }
      resources = resources.map((resource) =>
        resource.id === resourceId ? result : resource,
      )
      return jsonResponse(result)
    }

    if (url.endsWith('/estado') && method === 'PATCH') {
      const resourceId = Number(url.match(/\/recursos\/(\d+)\/estado/)?.[1])
      const body = JSON.parse(String(init?.body)) as Pick<Resource, 'estado'>
      const currentResource = resources.find(
        (resource) => resource.id === resourceId,
      )
      const result = { ...currentResource!, estado: body.estado }
      resources = resources.map((resource) =>
        resource.id === resourceId ? result : resource,
      )
      return jsonResponse(result)
    }

    if (url.includes('/api/recursos?')) {
      return jsonResponse(pageOf(resources))
    }

    return jsonResponse({}, 404)
  })
}

function fillResourceForm() {
  return {
    name: screen.getByLabelText('Nombre del recurso'),
    description: screen.getByLabelText('Descripción'),
    type: screen.getByLabelText('Tipo'),
    capacity: screen.getByLabelText('Capacidad'),
  }
}

describe('AdminResourcesPage', () => {
  it('lists resources and registers a new one', async () => {
    mockResourcesApi([availableResource])

    renderWithProviders(<AdminResourcesPage />)

    expect(await screen.findByText('Sala 101')).toBeInTheDocument()

    const form = fillResourceForm()
    await userEvent.type(form.name, 'Sala 202')
    await userEvent.type(form.description, 'Sala para capacitaciones')
    await userEvent.type(form.type, 'SALA')
    await userEvent.type(form.capacity, '12')
    await userEvent.click(screen.getByRole('button', { name: 'Registrar recurso' }))

    expect(
      await screen.findByText('Recurso registrado correctamente.'),
    ).toBeInTheDocument()
    expect(
      within(screen.getByRole('table', { name: 'Recursos registrados' })).getByText(
        'Sala 202',
      ),
    ).toBeInTheDocument()
  })

  it('edits a resource from its detail', async () => {
    mockResourcesApi([availableResource])

    renderWithProviders(<AdminResourcesPage />)
    const table = await screen.findByRole('table', { name: 'Recursos registrados' })
    const row = within(table).getByRole('row', { name: /Sala 101/ })

    await userEvent.click(within(row).getByRole('button', { name: 'Ver detalle' }))
    await userEvent.click(screen.getByRole('button', { name: 'Editar recurso' }))

    const nameInput = screen.getByLabelText('Nombre del recurso')
    await userEvent.clear(nameInput)
    await userEvent.type(nameInput, 'Sala 101 actualizada')
    await userEvent.click(screen.getByRole('button', { name: 'Guardar cambios' }))

    expect(
      await screen.findByText('Recurso actualizado correctamente.'),
    ).toBeInTheDocument()
    expect(
      within(table).getByText('Sala 101 actualizada'),
    ).toBeInTheDocument()
  })

  it('changes the resource status to maintenance', async () => {
    mockResourcesApi([availableResource])

    renderWithProviders(<AdminResourcesPage />)
    const table = await screen.findByRole('table', { name: 'Recursos registrados' })
    const row = within(table).getByRole('row', { name: /Sala 101/ })

    await userEvent.click(within(row).getByRole('button', { name: 'Ver detalle' }))
    await userEvent.selectOptions(screen.getByLabelText('Cambiar estado'), 'MANTENIMIENTO')
    await userEvent.click(screen.getByRole('button', { name: 'Actualizar estado' }))

    expect(
      await screen.findByText('Estado del recurso actualizado.'),
    ).toBeInTheDocument()
    expect(
      await within(table).findByRole('row', { name: /Sala 101.*Mantenimiento/ }),
    ).toBeInTheDocument()
  })

  it('shows the maintenance conflict when the resource has future reservations', async () => {
    stubFetch((input, init) => {
      const url = requestUrl(input)

      if (url.endsWith('/estado') && init?.method === 'PATCH') {
        return jsonResponse(
          {
            status: 409,
            error: 'RECURSO_CON_RESERVAS_FUTURAS',
            message: 'El recurso tiene reservas activas futuras',
          },
          409,
        )
      }

      return jsonResponse(pageOf([availableResource]))
    })

    renderWithProviders(<AdminResourcesPage />)
    const table = await screen.findByRole('table', { name: 'Recursos registrados' })
    const row = within(table).getByRole('row', { name: /Sala 101/ })

    await userEvent.click(within(row).getByRole('button', { name: 'Ver detalle' }))
    await userEvent.selectOptions(screen.getByLabelText('Cambiar estado'), 'MANTENIMIENTO')
    await userEvent.click(screen.getByRole('button', { name: 'Actualizar estado' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'No se puede poner el recurso en mantenimiento porque tiene reservas activas futuras.',
    )
  })
})
