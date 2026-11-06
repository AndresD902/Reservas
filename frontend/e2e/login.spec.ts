import { expect, test } from '@playwright/test'

const ADMIN_EMAIL = process.env.E2E_ADMIN_EMAIL ?? 'admin@example.com'
const ADMIN_PASSWORD = process.env.E2E_ADMIN_PASSWORD ?? 'admin123'

test('un visitante sin sesión va al login y el administrador accede al panel', async ({
  page,
}) => {
  await page.goto('/admin/usuarios')
  await expect(page).toHaveURL(/\/login$/)

  await page.getByLabel('Correo electrónico').fill(ADMIN_EMAIL)
  await page.getByLabel('Contraseña').fill(ADMIN_PASSWORD)
  await page.getByRole('button', { name: 'Iniciar sesión' }).click()

  await expect(page).toHaveURL(/\/admin\/usuarios$/)
  await expect(
    page.getByRole('heading', { name: 'Administración de usuarios' }),
  ).toBeVisible()
})

test('un administrador cierra sesión y debe autenticarse de nuevo', async ({
  page,
}) => {
  await page.goto('/login')
  await page.getByLabel('Correo electrónico').fill(ADMIN_EMAIL)
  await page.getByLabel('Contraseña').fill(ADMIN_PASSWORD)
  await page.getByRole('button', { name: 'Iniciar sesión' }).click()
  await expect(page).toHaveURL(/\/admin\/usuarios$/)

  await page.getByRole('button', { name: 'Cerrar sesión' }).click()
  await expect(page).toHaveURL(/\/login$/)

  await page.goto('/admin/usuarios')
  await expect(page).toHaveURL(/\/login$/)
})
