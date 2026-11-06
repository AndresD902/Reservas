import { randomUUID } from 'node:crypto'
import { expect, test } from '@playwright/test'
import type { APIRequestContext, Page } from '@playwright/test'

const API_URL = process.env.E2E_API_URL ?? 'http://localhost:8080'

interface CreatedEntity {
  id: number
  nombre: string
}

function futureRange(): { start: Date; end: Date } {
  const start = new Date()
  start.setDate(start.getDate() + 1)
  start.setHours(11, 0, 0, 0)

  return {
    start,
    end: new Date(start.getTime() + 60 * 60 * 1000),
  }
}

function toApiValue(date: Date): string {
  const pad = (value: number) => String(value).padStart(2, '0')

  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:00`
}

async function createUserViaApi(
  request: APIRequestContext,
  suffix: string,
): Promise<CreatedEntity> {
  const nombre = `000 Usuario Admin E2E ${suffix}`
  const response = await request.post(`${API_URL}/usuarios`, {
    data: {
      nombre,
      documento: `ADM${suffix}`,
      correo: `admin.${suffix}@example.com`,
      estado: 'ACTIVO',
    },
  })

  expect(response.status()).toBe(201)
  return { id: (await response.json()).id as number, nombre }
}

async function createResourceViaApi(
  request: APIRequestContext,
  suffix: string,
): Promise<CreatedEntity> {
  const nombre = `000 Recurso Admin E2E ${suffix}`
  const response = await request.post(`${API_URL}/recursos`, {
    data: {
      nombre,
      descripcion: 'Recurso creado por la prueba E2E de administración',
      tipo: 'SALA',
      capacidad: 6,
      estado: 'DISPONIBLE',
    },
  })

  expect(response.status()).toBe(201)
  return { id: (await response.json()).id as number, nombre }
}

function userForm(page: Page) {
  return page
    .locator('section')
    .filter({ has: page.getByRole('heading', { name: 'Registrar usuario' }) })
}

function resourceForm(page: Page) {
  return page
    .locator('section')
    .filter({ has: page.getByRole('heading', { name: 'Registrar recurso' }) })
}

test('desde /admin se registra un usuario y se cambia su estado', async ({
  page,
}) => {
  const suffix = randomUUID().slice(0, 8)
  const name = `000 Usuario Admin UI ${suffix}`

  await page.goto('/admin')
  await expect(page).toHaveURL(/\/admin\/usuarios$/)
  await expect(
    page.getByRole('heading', { name: 'Administración de usuarios' }),
  ).toBeVisible()

  const form = userForm(page)
  await form.getByLabel('Nombre completo').fill(name)
  await form.getByLabel('Documento').fill(`UI${suffix}`)
  await form.getByLabel('Correo electrónico').fill(`ui.${suffix}@example.com`)
  await form.getByRole('button', { name: 'Registrar usuario' }).click()

  await expect(
    page.getByRole('status').filter({ hasText: 'Usuario registrado correctamente.' }),
  ).toBeVisible()

  const row = page.getByRole('row').filter({ hasText: name })
  await expect(row).toBeVisible()
  await row.getByRole('button', { name: 'Ver detalle' }).click()
  await page.getByRole('button', { name: 'Inactivar usuario' }).click()

  await expect(
    page.getByRole('status').filter({ hasText: 'Estado del usuario actualizado.' }),
  ).toBeVisible()
  await expect(row).toContainText('Inactivo')
})

test('un documento duplicado se informa en el formulario', async ({
  page,
  request,
}) => {
  const suffix = randomUUID().slice(0, 8)
  await createUserViaApi(request, suffix)

  await page.goto('/admin/usuarios')
  await expect(
    page.getByRole('heading', { name: 'Administración de usuarios' }),
  ).toBeVisible()

  const form = userForm(page)
  await form.getByLabel('Nombre completo').fill(`Duplicado ${suffix}`)
  await form.getByLabel('Documento').fill(`ADM${suffix}`)
  await form.getByLabel('Correo electrónico').fill(`nuevo.${suffix}@example.com`)
  await form.getByRole('button', { name: 'Registrar usuario' }).click()

  await expect(page.getByRole('alert')).toContainText(
    'Ya existe un usuario con ese documento.',
  )
})

test('se registra un recurso, se edita y se pone en mantenimiento', async ({
  page,
}) => {
  const suffix = randomUUID().slice(0, 8)
  const name = `000 Recurso Admin UI ${suffix}`
  const updatedName = `${name} actualizado`

  await page.goto('/admin/recursos')
  await expect(
    page.getByRole('heading', { name: 'Administración de recursos' }),
  ).toBeVisible()

  const form = resourceForm(page)
  await form.getByLabel('Nombre del recurso').fill(name)
  await form.getByLabel('Descripción').fill('Sala para pruebas de administración')
  await form.getByLabel('Tipo').fill('SALA')
  await form.getByLabel('Capacidad').fill('10')
  await form.getByRole('button', { name: 'Registrar recurso' }).click()

  await expect(
    page.getByRole('status').filter({ hasText: 'Recurso registrado correctamente.' }),
  ).toBeVisible()

  const row = page.getByRole('row').filter({ hasText: name })
  await expect(row).toBeVisible()
  await row.getByRole('button', { name: 'Ver detalle' }).click()
  await page.getByRole('button', { name: 'Editar recurso' }).click()

  const editForm = page
    .locator('section')
    .filter({ has: page.getByRole('heading', { name: 'Editar recurso' }) })
  await editForm.getByLabel('Nombre del recurso').fill(updatedName)
  await editForm.getByRole('button', { name: 'Guardar cambios' }).click()

  await expect(
    page.getByRole('status').filter({ hasText: 'Recurso actualizado correctamente.' }),
  ).toBeVisible()
  await expect(page.getByRole('row').filter({ hasText: updatedName })).toBeVisible()

  await page.getByLabel('Cambiar estado').selectOption('MANTENIMIENTO')
  await page.getByRole('button', { name: 'Actualizar estado' }).click()

  await expect(
    page.getByRole('status').filter({ hasText: 'Estado del recurso actualizado.' }),
  ).toBeVisible()
  await expect(
    page.getByRole('row').filter({ hasText: updatedName }),
  ).toContainText('Mantenimiento')
})

test('el mantenimiento se bloquea si el recurso tiene reservas futuras', async ({
  page,
  request,
}) => {
  const suffix = randomUUID().slice(0, 8)
  const user = await createUserViaApi(request, suffix)
  const resource = await createResourceViaApi(request, suffix)
  const { start, end } = futureRange()

  const reservationResponse = await request.post(`${API_URL}/reservas`, {
    data: {
      usuarioId: user.id,
      recursoId: resource.id,
      fechaInicio: toApiValue(start),
      fechaFin: toApiValue(end),
    },
  })
  expect(reservationResponse.status()).toBe(201)

  await page.goto('/admin/recursos')
  const row = page.getByRole('row').filter({ hasText: resource.nombre })
  await expect(row).toBeVisible()
  await row.getByRole('button', { name: 'Ver detalle' }).click()
  await page.getByLabel('Cambiar estado').selectOption('MANTENIMIENTO')
  await page.getByRole('button', { name: 'Actualizar estado' }).click()

  await expect(page.getByRole('alert')).toContainText(
    'No se puede poner el recurso en mantenimiento porque tiene reservas activas futuras.',
  )
})
