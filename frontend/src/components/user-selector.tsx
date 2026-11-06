import { useEffect } from 'react'
import type { User } from '../api/types'
import { useUserSelection } from '../hooks/use-user-selection'
import { useUsuarios } from '../hooks/use-usuarios'

const EMPTY_USERS: User[] = []

export function UserSelector() {
  const { selectedUserId, selectUser } = useUserSelection()
  const { data, isLoading, isError, refetch } = useUsuarios()
  const users = data?.content ?? EMPTY_USERS

  useEffect(() => {
    if (
      selectedUserId !== null &&
      users.length > 0 &&
      !users.some((user) => user.id === selectedUserId)
    ) {
      selectUser(null)
    }
  }, [selectedUserId, users, selectUser])

  return (
    <div className="w-full sm:w-80">
      <label
        htmlFor="user-select"
        className="mb-1 block text-xs font-semibold uppercase tracking-wide text-slate-500"
      >
        Usuario
      </label>
      <select
        id="user-select"
        value={selectedUserId ?? ''}
        onChange={(event) =>
          selectUser(event.target.value ? Number(event.target.value) : null)
        }
        disabled={isLoading || isError}
        className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200 disabled:cursor-not-allowed disabled:bg-slate-100"
      >
        <option value="">
          {isLoading ? 'Cargando usuarios…' : 'Selecciona un usuario'}
        </option>
        {users.map((user) => (
          <option key={user.id} value={user.id}>
            {user.nombre}
          </option>
        ))}
      </select>
      {isError && (
        <p className="mt-1 text-xs text-rose-600">
          No se pudieron cargar los usuarios.{' '}
          <button
            type="button"
            onClick={() => void refetch()}
            className="font-semibold underline underline-offset-2"
          >
            Reintentar
          </button>
        </p>
      )}
    </div>
  )
}
