import { useDisponibilidad } from '../hooks/use-disponibilidad'
import { describeApiError } from '../utils/error-messages'
import { formatDateTime } from '../utils/date-time'
import { ErrorAlert } from './error-alert'
import { LoadingState } from './loading-state'

interface AvailabilityRange {
  start: string
  end: string
}

interface AvailabilityPanelProps {
  resourceId: number
  range: AvailabilityRange | null
}

export function AvailabilityPanel({ resourceId, range }: AvailabilityPanelProps) {
  const availability = useDisponibilidad(
    resourceId,
    range?.start ?? '',
    range?.end ?? '',
    range !== null,
  )

  if (!range) {
    return null
  }

  if (availability.isLoading) {
    return <LoadingState message="Comprobando disponibilidad…" />
  }

  if (availability.isError) {
    return (
      <ErrorAlert
        title="No se pudo comprobar la disponibilidad"
        message={describeApiError(availability.error)}
      />
    )
  }

  if (!availability.data) {
    return null
  }

  if (availability.data.disponible) {
    return (
      <div
        role="status"
        className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800"
      >
        <p className="font-semibold">El recurso está disponible en ese intervalo.</p>
        <p className="mt-1">
          {formatDateTime(availability.data.fechaInicio)} —{' '}
          {formatDateTime(availability.data.fechaFin)}
        </p>
      </div>
    )
  }

  return (
    <div role="alert" className="rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-800">
      <p className="font-semibold">El recurso no está disponible en ese intervalo.</p>
      {availability.data.reservasSolapadas.length > 0 && (
        <ul className="mt-2 list-disc space-y-1 pl-5">
          {availability.data.reservasSolapadas.map((reservation) => (
            <li key={reservation.id}>
              {formatDateTime(reservation.fechaInicio)} —{' '}
              {formatDateTime(reservation.fechaFin)} · {reservation.usuarioNombre}
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
