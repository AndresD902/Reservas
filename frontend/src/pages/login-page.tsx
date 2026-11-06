import { useState } from 'react'
import type { FormEvent } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useLogin, useSession } from '../hooks/use-auth'
import { describeApiError } from '../utils/error-messages'

interface LoginLocationState {
  from?: { pathname?: string }
}

const inputClass =
  'w-full rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200'

export function LoginPage() {
  const [correo, setCorreo] = useState('')
  const [password, setPassword] = useState('')
  const { data: session } = useSession()
  const loginMutation = useLogin()
  const navigate = useNavigate()
  const location = useLocation()

  const locationState = location.state as LoginLocationState | null
  const redirectTo = locationState?.from?.pathname ?? '/admin/usuarios'

  if (session) {
    return <Navigate to={redirectTo} replace />
  }

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    loginMutation.mutate(
      { correo, password },
      { onSuccess: () => navigate(redirectTo, { replace: true }) },
    )
  }

  return (
    <div className="mx-auto max-w-md">
      <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
        <p className="text-xs font-semibold uppercase tracking-wide text-indigo-700">
          Administración
        </p>
        <h1 className="mt-1 text-2xl font-semibold tracking-tight text-slate-900">
          Iniciar sesión
        </h1>
        <p className="mt-2 text-sm text-slate-600">
          Accede con tus credenciales de administrador para gestionar reservas,
          usuarios y recursos.
        </p>

        <form className="mt-6 space-y-4" onSubmit={handleSubmit} noValidate>
          <div>
            <label htmlFor="login-correo" className="mb-1 block text-sm font-medium text-slate-700">
              Correo electrónico
            </label>
            <input
              id="login-correo"
              type="email"
              autoComplete="username"
              required
              value={correo}
              onChange={(event) => setCorreo(event.target.value)}
              aria-invalid={loginMutation.isError && loginMutation.error instanceof Error}
              className={inputClass}
            />
          </div>

          <div>
            <label htmlFor="login-password" className="mb-1 block text-sm font-medium text-slate-700">
              Contraseña
            </label>
            <input
              id="login-password"
              type="password"
              autoComplete="current-password"
              required
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              aria-invalid={loginMutation.isError && loginMutation.error instanceof Error}
              className={inputClass}
            />
          </div>

          {loginMutation.isError && (
            <p role="alert" className="text-sm text-rose-600">
              {describeApiError(loginMutation.error)}
            </p>
          )}

          <button
            type="submit"
            disabled={loginMutation.isPending}
            className="w-full rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-300 disabled:cursor-not-allowed disabled:bg-indigo-300"
          >
            {loginMutation.isPending ? 'Iniciando sesión…' : 'Iniciar sesión'}
          </button>
        </form>
      </section>
    </div>
  )
}
