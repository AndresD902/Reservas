import { useMemo, useState } from 'react'
import type { Resource, ResourceStatus } from '../api/types'
import { AdminNav } from '../components/admin/admin-nav'
import { ResourceDetail } from '../components/admin/resource-detail'
import { ResourceForm } from '../components/admin/resource-form'
import { EmptyState } from '../components/empty-state'
import { ErrorState } from '../components/error-state'
import { LoadingState } from '../components/loading-state'
import { StatusBadge } from '../components/status-badge'
import { useAdminResources } from '../hooks/use-admin-resources'

const EMPTY_RESOURCES: Resource[] = []
const inputClass =
  'w-full rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200'

type StatusFilter = 'TODOS' | ResourceStatus

export function AdminResourcesPage() {
  const { data, isLoading, isError, error, refetch } = useAdminResources()
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('TODOS')
  const [selectedResourceId, setSelectedResourceId] = useState<number | null>(null)
  const [isEditing, setIsEditing] = useState(false)
  const resources = data?.content ?? EMPTY_RESOURCES

  const filteredResources = useMemo(() => {
    const term = search.trim().toLocaleLowerCase('es')

    return resources.filter((resource) => {
      const matchesStatus =
        statusFilter === 'TODOS' || resource.estado === statusFilter
      const matchesSearch =
        !term ||
        [resource.nombre, resource.tipo, resource.descripcion ?? ''].some((value) =>
          value.toLocaleLowerCase('es').includes(term),
        )

      return matchesStatus && matchesSearch
    })
  }, [resources, search, statusFilter])

  const selectedResource =
    resources.find((resource) => resource.id === selectedResourceId) ?? null
  const resourceForForm = isEditing ? selectedResource : null

  function handleSaved(resource: Resource) {
    setSelectedResourceId(resource.id)
  }

  return (
    <div className="space-y-6">
      <section className="space-y-4">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wide text-indigo-700">
            Administración
          </p>
          <h1 className="mt-1 text-3xl font-semibold tracking-tight text-slate-900">
            Administración de recursos
          </h1>
          <p className="mt-2 max-w-2xl text-slate-600">
            Registra espacios y equipos, actualiza sus datos y controla su estado de
            disponibilidad.
          </p>
        </div>
        <AdminNav />
        <div className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
          Sesión de administrador requerida para gestionar este panel.</div>
      </section>

      <div className="grid gap-6 xl:grid-cols-[minmax(0,1.35fr)_minmax(0,0.85fr)]">
        <section className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label
                htmlFor="admin-resource-search"
                className="mb-1 block text-sm font-medium text-slate-700"
              >
                Buscar recurso
              </label>
              <input
                id="admin-resource-search"
                type="search"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder="Nombre, tipo o descripción"
                className={inputClass}
              />
            </div>
            <div>
              <label
                htmlFor="admin-resource-status-filter"
                className="mb-1 block text-sm font-medium text-slate-700"
              >
                Filtrar recursos por estado
              </label>
              <select
                id="admin-resource-status-filter"
                value={statusFilter}
                onChange={(event) =>
                  setStatusFilter(event.target.value as StatusFilter)
                }
                className={inputClass}
              >
                <option value="TODOS">Todos</option>
                <option value="DISPONIBLE">Disponible</option>
                <option value="MANTENIMIENTO">Mantenimiento</option>
                <option value="FUERA_DE_SERVICIO">Fuera de servicio</option>
              </select>
            </div>
          </div>

          {isLoading && <LoadingState message="Cargando recursos…" />}

          {isError && (
            <ErrorState
              message="No se pudieron cargar los recursos."
              error={error}
              onRetry={() => void refetch()}
            />
          )}

          {!isLoading && !isError && resources.length === 0 && (
            <EmptyState
              title="No hay recursos registrados"
              description="Registra el primer recurso desde el formulario."
            />
          )}

          {!isLoading && !isError && resources.length > 0 && filteredResources.length === 0 && (
            <EmptyState
              title="No encontramos recursos"
              description="Prueba con otro término de búsqueda o estado."
            />
          )}

          {!isLoading && !isError && filteredResources.length > 0 && (
            <div className="overflow-x-auto rounded-2xl border border-slate-200 bg-white shadow-sm">
              <table
                className="min-w-full divide-y divide-slate-200"
                aria-label="Recursos registrados"
              >
                <thead className="bg-slate-50">
                  <tr>
                    <th scope="col" className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Nombre
                    </th>
                    <th scope="col" className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Tipo
                    </th>
                    <th scope="col" className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Capacidad
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
                  {filteredResources.map((resource) => (
                    <tr
                      key={resource.id}
                      className={
                        resource.id === selectedResourceId
                          ? 'bg-indigo-50/60'
                          : undefined
                      }
                    >
                      <td className="px-4 py-3 text-sm font-medium text-slate-900">
                        {resource.nombre}
                      </td>
                      <td className="px-4 py-3 text-sm text-slate-600">
                        {resource.tipo}
                      </td>
                      <td className="px-4 py-3 text-sm text-slate-600">
                        {resource.capacidad}
                      </td>
                      <td className="px-4 py-3">
                        <StatusBadge status={resource.estado} />
                      </td>
                      <td className="px-4 py-3 text-right">
                        <button
                          type="button"
                          onClick={() => {
                            setSelectedResourceId(resource.id)
                            setIsEditing(false)
                          }}
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
          <ResourceForm
            key={resourceForForm ? `form-${resourceForForm.id}` : 'new'}
            resource={resourceForForm}
            onSaved={handleSaved}
            onCancel={() => setIsEditing(false)}
          />
          {selectedResource ? (
            <ResourceDetail
              key={`detail-${selectedResource.id}`}
              resource={selectedResource}
              onEdit={() => setIsEditing(true)}
            />
          ) : (
            <section className="rounded-2xl border border-dashed border-slate-300 bg-white px-5 py-6 text-sm text-slate-600">
              Selecciona «Ver detalle» en un recurso para consultar sus datos o editar sus campos.
            </section>
          )}
        </aside>
      </div>
    </div>
  )
}
