import { useMemo, useState } from 'react'
import type { Resource } from '../api/types'
import { useUserSelection } from '../hooks/use-user-selection'
import { useRecursos } from '../hooks/use-recursos'
import { EmptyState } from '../components/empty-state'
import { ErrorState } from '../components/error-state'
import { LoadingState } from '../components/loading-state'
import { ResourceList } from '../components/resource-list'
import { SearchField } from '../components/search-field'

const EMPTY_RESOURCES: Resource[] = []

export function ReservationPage() {
  const [search, setSearch] = useState('')
  const { selectedUserId } = useUserSelection()
  const { data, isLoading, isError, error, refetch } = useRecursos()
  const resources = data?.content ?? EMPTY_RESOURCES

  const filteredResources = useMemo(() => {
    const term = search.trim().toLocaleLowerCase('es')

    if (!term) {
      return resources
    }

    return resources.filter((resource) =>
      [resource.nombre, resource.tipo, resource.descripcion ?? ''].some((value) =>
        value.toLocaleLowerCase('es').includes(term),
      ),
    )
  }, [resources, search])

  return (
    <div className="space-y-6">
      <section>
        <h1 className="text-3xl font-semibold tracking-tight text-slate-900">
          Reservar un recurso
        </h1>
        <p className="mt-2 max-w-2xl text-slate-600">
          Elige un usuario activo, busca un recurso disponible y reserva un intervalo
          libre.
        </p>
      </section>

      {selectedUserId === null && (
        <div
          role="status"
          className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800"
        >
          Selecciona un usuario activo en la parte superior para reservar.
        </div>
      )}

      <SearchField value={search} onChange={setSearch} />

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
          description="Cuando el administrador registre recursos, aparecerán aquí."
        />
      )}

      {!isLoading && !isError && resources.length > 0 && filteredResources.length === 0 && (
        <EmptyState
          title="No encontramos recursos"
          description="Prueba con otro nombre o tipo de recurso."
        />
      )}

      {!isLoading && !isError && filteredResources.length > 0 && (
        <ResourceList resources={filteredResources} />
      )}
    </div>
  )
}
