import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { jsonResponse, requestUrl, stubFetch } from '../test/http'
import { renderWithProviders } from '../test/render'
import { LoginPage } from './login-page'

function mockAuthApi(loginSucceeds: boolean) {
  return stubFetch((input, init) => {
    const url = requestUrl(input)

    if (url.endsWith('/api/auth/me')) {
      return jsonResponse(
        { status: 401, error: 'CREDENCIALES_INVALIDAS', message: 'Sin sesión' },
        401,
      )
    }

    if (url.endsWith('/api/auth/csrf')) {
      return jsonResponse({ headerName: 'X-XSRF-TOKEN', token: 'token-de-prueba' })
    }

    if (url.endsWith('/api/auth/login') && init?.method === 'POST') {
      return loginSucceeds
        ? jsonResponse({ id: 1, nombre: 'Admin', correo: 'admin@example.com' })
        : jsonResponse(
            {
              status: 401,
              error: 'CREDENCIALES_INVALIDAS',
              message: 'El correo o la contraseña no son correctos',
            },
            401,
          )
    }

    return jsonResponse({}, 404)
  })
}

describe('LoginPage', () => {
  it('envía las credenciales al iniciar sesión', async () => {
    const fetchMock = mockAuthApi(true)
    const user = userEvent.setup()
    renderWithProviders(<LoginPage />, { route: '/login', path: '*' })

    await user.type(screen.getByLabelText('Correo electrónico'), 'admin@example.com')
    await user.type(screen.getByLabelText('Contraseña'), 'secreta')
    await user.click(screen.getByRole('button', { name: 'Iniciar sesión' }))

    const loginCall = fetchMock.mock.calls.find(([input]) =>
      requestUrl(input).endsWith('/api/auth/login'),
    )
    expect(loginCall).toBeDefined()
    expect(loginCall?.[1]?.method).toBe('POST')
    expect(JSON.parse(String(loginCall?.[1]?.body))).toEqual({
      correo: 'admin@example.com',
      password: 'secreta',
    })
  })

  it('muestra el error cuando las credenciales no son válidas', async () => {
    mockAuthApi(false)
    const user = userEvent.setup()
    renderWithProviders(<LoginPage />, { route: '/login', path: '*' })

    await user.type(screen.getByLabelText('Correo electrónico'), 'admin@example.com')
    await user.type(screen.getByLabelText('Contraseña'), 'incorrecta')
    await user.click(screen.getByRole('button', { name: 'Iniciar sesión' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'El correo o la contraseña no son correctos.',
    )
  })
})
