import { randomUUID } from 'node:crypto'
import { expect, test } from '@playwright/test'
import type { APIRequestContext } from '@playwright/test'
import { csrfHeaders, loginAsAdmin } from './support/auth.js'

const API_URL = process.env.E2E_API_URL ?? 'http://localhost:8080'

test.beforeEach(async ({ request, context }) => {
  await loginAsAdmin(request, context)
})

interface CreatedUser {
  id: number
  nombre: string
}

interface CreatedResource {
  id: number
  nombre: string
}

function pad(value: number): string {
  return String(value).padStart(2, '0')
}

function toApiValue(date: Date): string {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:00`
}

function futureRange(): { start: Date; end: Date } {
  const start = new Date()
  start.setDate(start.getDate() + 1)
  start.setHours(10, 0, 0, 0)

  return {
    start,
    end: new Date(start.getTime() + 60 * 60 * 1000),
  }
}

async function createUser(
  request: APIRequestContext,
  suffix: string,
): Promise<CreatedUser> {
  const nombre = `000 Usuario E2E ${suffix}`
  const response = await request.post(`${API_URL}/usuarios`, {
    headers: csrfHeaders(),
    data: {
      nombre,
      documento: `E2EU${suffix}`.slice(0, 20),
      correo: `e2e.mis-reservas.${suffix}@example.com`,
      estado: 'ACTIVO',
    },
  })

  expect(response.status()).toBe(201)
  return { id: (await response.json()).id as number, nombre }
}

async function createResource(
  request: APIRequestContext,
  suffix: string,
): Promise<CreatedResource> {
  const nombre = `000 E2E ${suffix}`
  const response = await request.post(`${API_URL}/recursos`, {
    headers: csrfHeaders(),
    data: {
      nombre,
      descripcion: 'Recurso creado por la prueba E2E de mis reservas',
      tipo: 'SALA',
      capacidad: 4,
      estado: 'DISPONIBLE',
    },
  })

  expect(response.status()).toBe(201)
  return { id: (await response.json()).id as number, nombre }
}

test('un usuario consulta y cancela una reserva activa desde la interfaz', async ({
  page,
  request,
}) => {
  const suffix = randomUUID().slice(0, 8)
  const user = await createUser(request, suffix)
  const resource = await createResource(request, suffix)
  const { start, end } = futureRange()

  const reservationResponse = await request.post(`${API_URL}/reservas`, {
    headers: csrfHeaders(),
    data: {
      usuarioId: user.id,
      recursoId: resource.id,
      fechaInicio: toApiValue(start),
      fechaFin: toApiValue(end),
    },
  })
  expect(reservationResponse.status()).toBe(201)
  const reservation = (await reservationResponse.json()) as { id: number }

  await page.goto('/mis-reservas')
  await expect(page.getByRole('option', { name: user.nombre })).toBeAttached()
  await page.getByLabel('Usuario').selectOption({ label: user.nombre })

  const activeReservation = page.getByRole('article', {
    name: `${resource.nombre} en estado Activa`,
  })
  await expect(activeReservation).toBeVisible()
  await expect(
    activeReservation.getByRole('button', { name: 'Cancelar reserva' }),
  ).toBeVisible()

  await page.getByRole('button', { name: 'Cancelar reserva' }).click()
  const dialog = page.getByRole('dialog', { name: 'Cancelar reserva' })
  await expect(dialog).toContainText(resource.nombre)
  await dialog.getByRole('button', { name: 'Sí, cancelar' }).click()

  await expect(
    page.getByRole('status').filter({
      hasText: 'Reserva cancelada correctamente.',
    }),
  ).toBeVisible()
  await expect(
    page.getByRole('article', {
      name: `${resource.nombre} en estado Cancelada`,
    }),
  ).toBeVisible()
  await expect(
    page.getByRole('button', { name: 'Cancelar reserva' }),
  ).toHaveCount(0)

  const reservationsResponse = await request.get(
    `${API_URL}/reservas/usuario/${user.id}`,
  )
  expect(reservationsResponse.status()).toBe(200)
  const reservations = (await reservationsResponse.json()) as Array<{
    id: number
    estado: string
  }>
  expect(reservations).toEqual(
    expect.arrayContaining([
      expect.objectContaining({ id: reservation.id, estado: 'CANCELADA' }),
    ]),
  )
})
