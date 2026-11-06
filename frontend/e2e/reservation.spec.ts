import { randomUUID } from 'node:crypto'
import { expect, test } from '@playwright/test'
import type { APIRequestContext, Page } from '@playwright/test'
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

interface FutureRange {
  start: Date
  end: Date
}

function pad(value: number): string {
  return String(value).padStart(2, '0')
}

function toInputValue(date: Date): string {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function toApiValue(date: Date): string {
  return `${toInputValue(date)}:00`
}

function futureRange(): FutureRange {
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
      documento: `E2E${suffix}`.slice(0, 20),
      correo: `e2e.${suffix}@example.com`,
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
      descripcion: 'Recurso creado por la prueba E2E de la interfaz',
      tipo: 'SALA',
      capacidad: 6,
      estado: 'DISPONIBLE',
    },
  })

  expect(response.status()).toBe(201)
  return { id: (await response.json()).id as number, nombre }
}

async function openResource(
  page: Page,
  user: CreatedUser,
  resource: CreatedResource,
): Promise<void> {
  await page.goto('/reservar')
  await expect(page.getByRole('option', { name: user.nombre })).toBeAttached()
  await page.getByLabel('Usuario').selectOption({ label: user.nombre })
  await page.getByRole('link', { name: `Reservar ${resource.nombre}` }).click()
  await expect(page.getByRole('heading', { name: resource.nombre })).toBeVisible()
}

test('un usuario activo reserva un recurso disponible desde la interfaz', async ({
  page,
  request,
}) => {
  const suffix = randomUUID().slice(0, 8)
  const user = await createUser(request, suffix)
  const resource = await createResource(request, suffix)
  const { start, end } = futureRange()

  await openResource(page, user, resource)
  await page.getByLabel('Fecha y hora de inicio').fill(toInputValue(start))
  await page.getByLabel('Fecha y hora de fin').fill(toInputValue(end))
  await page.getByRole('button', { name: 'Reservar', exact: true }).click()

  const confirmation = page
    .getByRole('status')
    .filter({ hasText: 'Reserva confirmada' })
  await expect(confirmation).toBeVisible()
  await expect(confirmation).toContainText(resource.nombre)
  await expect(confirmation).toContainText(user.nombre)
})

test('un intervalo ocupado muestra el conflicto en la interfaz', async ({
  page,
  request,
}) => {
  const suffix = randomUUID().slice(0, 8)
  const owner = await createUser(request, `${suffix}a`)
  const visitor = await createUser(request, `${suffix}b`)
  const resource = await createResource(request, suffix)
  const { start, end } = futureRange()

  const reservationResponse = await request.post(`${API_URL}/reservas`, {
    headers: csrfHeaders(),
    data: {
      usuarioId: owner.id,
      recursoId: resource.id,
      fechaInicio: toApiValue(start),
      fechaFin: toApiValue(end),
    },
  })
  expect(reservationResponse.status()).toBe(201)

  await openResource(page, visitor, resource)
  await page.getByLabel('Fecha y hora de inicio').fill(toInputValue(start))
  await page.getByLabel('Fecha y hora de fin').fill(toInputValue(end))
  await page.getByRole('button', { name: 'Reservar', exact: true }).click()

  await expect(page.getByRole('alert')).toContainText(
    'El recurso ya está reservado en ese horario',
  )
})

test('un rango con fin anterior al inicio se rechaza antes de llamar a la API', async ({
  page,
  request,
}) => {
  const suffix = randomUUID().slice(0, 8)
  const user = await createUser(request, suffix)
  const resource = await createResource(request, suffix)
  const { start } = futureRange()
  const earlierEnd = new Date(start.getTime() - 60 * 60 * 1000)
  const reservationPosts: string[] = []

  page.on('request', (requestEvent) => {
    if (
      requestEvent.method() === 'POST' &&
      requestEvent.url().includes('/reservas')
    ) {
      reservationPosts.push(requestEvent.url())
    }
  })

  await openResource(page, user, resource)
  await page.getByLabel('Fecha y hora de inicio').fill(toInputValue(start))
  await page.getByLabel('Fecha y hora de fin').fill(toInputValue(earlierEnd))
  await page.getByRole('button', { name: 'Reservar', exact: true }).click()

  await expect(page.getByRole('alert')).toContainText(
    'La fecha de fin debe ser posterior a la fecha de inicio.',
  )
  expect(reservationPosts).toHaveLength(0)
})

test('la disponibilidad consultada se muestra antes de reservar', async ({
  page,
  request,
}) => {
  const suffix = randomUUID().slice(0, 8)
  const user = await createUser(request, suffix)
  const resource = await createResource(request, suffix)
  const { start, end } = futureRange()

  await openResource(page, user, resource)
  await page.getByLabel('Fecha y hora de inicio').fill(toInputValue(start))
  await page.getByLabel('Fecha y hora de fin').fill(toInputValue(end))
  await page
    .getByRole('button', { name: 'Comprobar disponibilidad', exact: true })
    .click()

  await expect(
    page.getByRole('status').filter({
      hasText: 'El recurso está disponible en ese intervalo.',
    }),
  ).toBeVisible()
})
