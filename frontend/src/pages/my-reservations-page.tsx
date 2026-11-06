import { useState } from 'react'
import type { Reservation } from '../api/types'
import { ConfirmDialog } from '../components/confirm-dialog'
import { EmptyState } from '../components/empty-state'
import { ErrorAlert } from '../components/error-alert'
import { ErrorState } from '../components/error-state'
import { LoadingState } from '../components/loading-state'
import { StatusBadge } from '../components/status-badge'
import { useCancelReservation } from '../hooks/use-cancel-reservation'
import { useUserReservations } from '../hooks/use-user-reservations'
import { useUserSelection } from '../hooks/use-user-selection'
import { formatDateTime } from '../utils/date-time'
import { describeApiError } from '../utils/error-messages'

const STATUS_LABELS = {
  ACTIVA: 'Activa',
  CANCELADA: 'Cancelada',
  FINALIZADA: 'Finalizada',
} as const

export function MyReservationsPage() {
  const { selectedUserId } = useUserSelection()

  return (
    <MyReservationsContent
      key={selectedUserId ?? 'no-user'}
      selectedUserId={selectedUserId}
    />
  )
}

function MyReservationsContent({
  selectedUserId,
}: {
  selectedUserId: number | null
}) {
  const { data, isLoading, isError, error, refetch } =
    useUserReservations(selectedUserId)
  const cancelReservation = useCancelReservation()
  const [reservationToCancel, setReservationToCancel] =
    useState<Reservation | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const reservations = data ?? []

  function handleCancel() {
    if (!reservationToCancel) {
      return
    }

    cancelReservation.mutate(reservationToCancel.id, {
      onSuccess: () => {
        setReservationToCancel(null)
        setSuccessMessage('Reserva cancelada correctamente.')
      },
      onError: () => {
        setReservationToCancel(null)
      },
    })
  }

  return (
    <div className="space-y-6">
      <section>
        <h1 className="text-3xl font-semibold tracking-tight text-slate-900">
          Mis reservas
        </h1>
        <p className="mt-2 max-w-2xl text-slate-600">
          Consulta las reservas del usuario seleccionado y cancela las que
          todavía estén activas.
        </p>
      </section>

      {selectedUserId === null && (
        <div
          role="status"
          className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800"
        >
          Selecciona un usuario activo en la parte superior para ver sus
          reservas.
        </div>
      )}

      {successMessage && (
        <div
          role="status"
          className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-800"
        >
          {successMessage}
        </div>
      )}

      {cancelReservation.isError && (
        <ErrorAlert
          title="No se pudo cancelar la reserva"
          message={describeApiError(cancelReservation.error)}
        />
      )}

      {selectedUserId !== null && isLoading && (
        <LoadingState message="Cargando reservas…" />
      )}

      {selectedUserId !== null && isError && (
        <ErrorState
          message="No se pudieron cargar las reservas."
          error={error}
          onRetry={() => void refetch()}
        />
      )}

      {selectedUserId !== null &&
        !isLoading &&
        !isError &&
        reservations.length === 0 && (
          <EmptyState
            title="No hay reservas registradas"
            description="Cuando el usuario seleccionado reserve un recurso, la reserva aparecerá aquí."
          />
        )}

      {selectedUserId !== null &&
        !isLoading &&
        !isError &&
        reservations.length > 0 && (
          <ul aria-label="Mis reservas" className="grid gap-4 md:grid-cols-2">
            {reservations.map((reservation) => (
              <li key={reservation.id}>
                <article
                  aria-label={`Reserva de ${reservation.recursoNombre} en estado ${STATUS_LABELS[reservation.estado]}`}
                  className="flex h-full flex-col rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"
                >
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <h2 className="text-lg font-semibold text-slate-900">
                        {reservation.recursoNombre}
                      </h2>
                      <p className="mt-1 text-sm text-slate-500">
                        {formatDateTime(reservation.fechaInicio)} —{' '}
                        {formatDateTime(reservation.fechaFin)}
                      </p>
                    </div>
                    <StatusBadge status={reservation.estado} />
                  </div>
                  <p className="mt-3 text-xs text-slate-500">
                    Reservada el {formatDateTime(reservation.fechaCreacion)}
                  </p>
                  {reservation.estado === 'ACTIVA' && (
                    <div className="mt-5">
                      <button
                        type="button"
                        onClick={() => {
                          setSuccessMessage(null)
                          cancelReservation.reset()
                          setReservationToCancel(reservation)
                        }}
                        className="rounded-lg border border-rose-300 px-4 py-2 text-sm font-semibold text-rose-700 transition hover:bg-rose-50 focus:outline-none focus:ring-2 focus:ring-rose-300"
                      >
                        Cancelar reserva
                      </button>
                    </div>
                  )}
                </article>
              </li>
            ))}
          </ul>
        )}

      {reservationToCancel && (
        <ConfirmDialog
          title="Cancelar reserva"
          description={`¿Seguro que quieres cancelar la reserva de ${reservationToCancel.recursoNombre} del ${formatDateTime(reservationToCancel.fechaInicio)}? Esta acción no se puede deshacer.`}
          confirmLabel="Sí, cancelar"
          pendingLabel="Cancelando…"
          isPending={cancelReservation.isPending}
          onConfirm={handleCancel}
          onCancel={() => setReservationToCancel(null)}
        />
      )}
    </div>
  )
}
