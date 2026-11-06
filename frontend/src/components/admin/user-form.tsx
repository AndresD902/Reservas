import { useState } from 'react'
import type { FormEvent } from 'react'
import type { User, UserStatus } from '../../api/types'
import { useCreateUser } from '../../hooks/use-admin-users'
import { describeApiError } from '../../utils/error-messages'
import { ErrorAlert } from '../error-alert'

interface UserFormProps {
  onCreated?: (user: User) => void
}

const inputClass =
  'w-full rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200'

export function UserForm({ onCreated }: UserFormProps) {
  const [nombre, setNombre] = useState('')
  const [documento, setDocumento] = useState('')
  const [correo, setCorreo] = useState('')
  const [estado, setEstado] = useState<UserStatus>('ACTIVO')
  const [created, setCreated] = useState(false)
  const mutation = useCreateUser()

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setCreated(false)

    mutation.mutate(
      {
        nombre: nombre.trim(),
        documento: documento.trim(),
        correo: correo.trim(),
        estado,
      },
      {
        onSuccess: (user) => {
          setNombre('')
          setDocumento('')
          setCorreo('')
          setEstado('ACTIVO')
          setCreated(true)
          onCreated?.(user)
        },
      },
    )
  }

  return (
    <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <h2 className="text-lg font-semibold text-slate-900">Registrar usuario</h2>
      <p className="mt-1 text-sm text-slate-600">
        El documento y el correo deben ser únicos.
      </p>

      <form onSubmit={handleSubmit} className="mt-4 space-y-4">
        <div>
          <label
            htmlFor="user-name"
            className="mb-1 block text-sm font-medium text-slate-700"
          >
            Nombre completo
          </label>
          <input
            id="user-name"
            type="text"
            required
            maxLength={100}
            value={nombre}
            onChange={(event) => setNombre(event.target.value)}
            className={inputClass}
          />
        </div>

        <div>
          <label
            htmlFor="user-document"
            className="mb-1 block text-sm font-medium text-slate-700"
          >
            Documento
          </label>
          <input
            id="user-document"
            type="text"
            required
            maxLength={20}
            value={documento}
            onChange={(event) => setDocumento(event.target.value)}
            className={inputClass}
          />
        </div>

        <div>
          <label
            htmlFor="user-email"
            className="mb-1 block text-sm font-medium text-slate-700"
          >
            Correo electrónico
          </label>
          <input
            id="user-email"
            type="email"
            required
            maxLength={100}
            value={correo}
            onChange={(event) => setCorreo(event.target.value)}
            className={inputClass}
          />
        </div>

        <div>
          <label
            htmlFor="user-initial-status"
            className="mb-1 block text-sm font-medium text-slate-700"
          >
            Estado inicial
          </label>
          <select
            id="user-initial-status"
            value={estado}
            onChange={(event) => setEstado(event.target.value as UserStatus)}
            className={inputClass}
          >
            <option value="ACTIVO">Activo</option>
            <option value="INACTIVO">Inactivo</option>
          </select>
        </div>

        {mutation.isError && <ErrorAlert message={describeApiError(mutation.error)} title="No se pudo registrar el usuario" />}

        {created && (
          <p role="status" className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-800">
            Usuario registrado correctamente.
          </p>
        )}

        <button
          type="submit"
          disabled={mutation.isPending}
          className="inline-flex w-full items-center justify-center rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-300 disabled:cursor-not-allowed disabled:bg-indigo-300"
        >
          {mutation.isPending ? 'Registrando…' : 'Registrar usuario'}
        </button>
      </form>
    </section>
  )
}
