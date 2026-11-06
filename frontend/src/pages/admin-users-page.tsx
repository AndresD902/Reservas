import { useMemo, useState } from 'react'
import type { User, UserStatus } from '../api/types'
import { AdminNav } from '../components/admin/admin-nav'
import { UserDetail } from '../components/admin/user-detail'
import { UserForm } from '../components/admin/user-form'
import { EmptyState } from '../components/empty-state'
import { ErrorState } from '../components/error-state'
import { LoadingState } from '../components/loading-state'
import { StatusBadge } from '../components/status-badge'
import { useAdminUsers } from '../hooks/use-admin-users'

const EMPTY_USERS: User[] = []
const inputClass =
  'w-full rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200'

type StatusFilter = 'TODOS' | UserStatus

export function AdminUsersPage() {
  const { data, isLoading, isError, error, refetch } = useAdminUsers()
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('TODOS')
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null)
  const users = data?.content ?? EMPTY_USERS

  const filteredUsers = useMemo(() => {
    const term = search.trim().toLocaleLowerCase('es')

    return users.filter((user) => {
      const matchesStatus =
        statusFilter === 'TODOS' || user.estado === statusFilter
      const matchesSearch =
        !term ||
        [user.nombre, user.documento, user.correo].some((value) =>
          value.toLocaleLowerCase('es').includes(term),
        )

      return matchesStatus && matchesSearch
    })
  }, [search, statusFilter, users])

  const selectedUser = users.find((user) => user.id === selectedUserId) ?? null

  return (
    <div className="space-y-6">
      <section className="space-y-4">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wide text-indigo-700">
            Administración
          </p>
          <h1 className="mt-1 text-3xl font-semibold tracking-tight text-slate-900">
            Administración de usuarios
          </h1>
          <p className="mt-2 max-w-2xl text-slate-600">
            Registra usuarios, consulta su detalle y activa o inactiva su participación
            en el sistema.
          </p>
        </div>
        <AdminNav />
        <div className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
          Sin autenticación en esta fase: este panel se considera de administración.
        </div>
      </section>

      <div className="grid gap-6 xl:grid-cols-[minmax(0,1.35fr)_minmax(0,0.85fr)]">
        <section className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label
                htmlFor="user-search"
                className="mb-1 block text-sm font-medium text-slate-700"
              >
                Buscar usuario
              </label>
              <input
                id="user-search"
                type="search"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder="Nombre, documento o correo"
                className={inputClass}
              />
            </div>
            <div>
              <label
                htmlFor="user-status-filter"
                className="mb-1 block text-sm font-medium text-slate-700"
              >
                Filtrar usuarios por estado
              </label>
              <select
                id="user-status-filter"
                value={statusFilter}
                onChange={(event) =>
                  setStatusFilter(event.target.value as StatusFilter)
                }
                className={inputClass}
              >
                <option value="TODOS">Todos</option>
                <option value="ACTIVO">Activo</option>
                <option value="INACTIVO">Inactivo</option>
              </select>
            </div>
          </div>

          {isLoading && <LoadingState message="Cargando usuarios…" />}

          {isError && (
            <ErrorState
              message="No se pudieron cargar los usuarios."
              error={error}
              onRetry={() => void refetch()}
            />
          )}

          {!isLoading && !isError && users.length === 0 && (
            <EmptyState
              title="No hay usuarios registrados"
              description="Registra el primer usuario desde el formulario."
            />
          )}

          {!isLoading && !isError && users.length > 0 && filteredUsers.length === 0 && (
            <EmptyState
              title="No encontramos usuarios"
              description="Prueba con otro término de búsqueda o estado."
            />
          )}

          {!isLoading && !isError && filteredUsers.length > 0 && (
            <div className="overflow-x-auto rounded-2xl border border-slate-200 bg-white shadow-sm">
              <table
                className="min-w-full divide-y divide-slate-200"
                aria-label="Usuarios registrados"
              >
                <thead className="bg-slate-50">
                  <tr>
                    <th scope="col" className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Nombre
                    </th>
                    <th scope="col" className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Documento
                    </th>
                    <th scope="col" className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Correo
                    </th>
                    <th scope="col" className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Estado
                    </th>
                    <th scope="col" className="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Acciones
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {filteredUsers.map((user) => (
                    <tr
                      key={user.id}
                      className={user.id === selectedUserId ? 'bg-indigo-50/60' : undefined}
                    >
                      <td className="px-4 py-3 text-sm font-medium text-slate-900">
                        {user.nombre}
                      </td>
                      <td className="px-4 py-3 text-sm text-slate-600">
                        {user.documento}
                      </td>
                      <td className="px-4 py-3 text-sm text-slate-600">
                        {user.correo}
                      </td>
                      <td className="px-4 py-3">
                        <StatusBadge status={user.estado} />
                      </td>
                      <td className="px-4 py-3 text-right">
                        <button
                          type="button"
                          onClick={() => setSelectedUserId(user.id)}
                          className="rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 transition hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-slate-300"
                        >
                          Ver detalle
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>

        <aside className="space-y-6">
          <UserForm onCreated={(user) => setSelectedUserId(user.id)} />
          {selectedUser ? (
            <UserDetail user={selectedUser} />
          ) : (
            <section className="rounded-2xl border border-dashed border-slate-300 bg-white px-5 py-6 text-sm text-slate-600">
              Selecciona «Ver detalle» en un usuario para consultar sus datos y cambiar su estado.
            </section>
          )}
        </aside>
      </div>
    </div>
  )
}
