import { Link, useParams } from 'react-router-dom'
import { ApiError } from '../api/client'
import { ErrorState } from '../components/error-state'
import { LoadingState } from '../components/loading-state'
import { ReservationForm } from '../components/reservation-form'
import { StatusBadge } from '../components/status-badge'
import { useUserSelection } from '../hooks/use-user-selection'
import { useRecurso } from '../hooks/use-recurso'

export function ResourceDetailPage() {
  const params = useParams()
  const resourceId = Number(params.recursoId)
  const { selectedUserId } = useUserSelection()
  const { data: resource, isLoading, isError, error, refetch } = useRecurso(resourceId)
  const invalidId = !Number.isInteger(resourceId) || resourceId <= 0

  if (invalidId) {
    return <ErrorState message="El recurso solicitado no es válido." />
  }

  if (isLoading) {
    return <LoadingState message="Cargando recurso…" />
  }

  if (isError || !resource) {
    const notFound = error instanceof ApiError && error.status === 404

    return (
      <ErrorState
        message={notFound ? 'No encontramos el recurso.' : 'No se pudo cargar el recurso.'}
        error={notFound ? undefined : error}
        onRetry={notFound ? undefined : () => void refetch()}
      />
    )
  }

  const isAvailable = resource.estado === 'DISPONIBLE'

  return (
    <div className="space-y-6">
      <Link
        to="/reservar"
        className="inline-flex items-center gap-2 text-sm font-semibold text-indigo-700 hover:text-indigo-900"
      >
        ← Volver a los recursos
      </Link>

      <div className="grid gap-8 lg:grid-cols-[minmax(0,0.9fr)_minmax(0,1.1fr)]">
        <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div className="flex flex-wrap items-start justify-between gap-3">
            <div>
              <h1 className="text-2xl font-semibold text-slate-900">{resource.nombre}</h1>
              <p className="mt-1 text-sm text-slate-500">
                {resource.tipo} · Capacidad {resource.capacidad}
              </p>
            </div>
            <StatusBadge status={resource.estado} />
          </div>
          <p className="mt-4 text-slate-600">
            {resource.descripcion ?? 'Este recurso no tiene descripción.'}
          </p>
        </section>

        <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 className="text-xl font-semibold text-slate-900">Reservar este recurso</h2>
          <p className="mt-1 text-sm text-slate-600">
            Revisa la disponibilidad antes de confirmar, o envía la reserva directamente.
          </p>

          {!isAvailable && (
            <div className="mt-5 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
              Este recurso no está disponible para reservar en este momento.
            </div>
          )}

          {isAvailable && selectedUserId === null && (
            <div
              role="status"
              className="mt-5 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800"
            >
              Selecciona un usuario activo en la parte superior para reservar.
            </div>
          )}

          {isAvailable && selectedUserId !== null && (
            <div className="mt-5">
              <ReservationForm resource={resource} userId={selectedUserId} />
            </div>
          )}
        </section>
      </div>
    </div>
  )
}
