import { useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import type { Reservation, Resource } from '../api/types'
import { useCrearReserva } from '../hooks/use-crear-reserva'
import { describeApiError } from '../utils/error-messages'
import {
  formatDateTime,
  toApiDateTime,
  toDateTimeLocalValue,
  validateReservationRange,
} from '../utils/date-time'
import { AvailabilityPanel } from './availability-panel'
import { ErrorAlert } from './error-alert'
import { StatusBadge } from './status-badge'

interface AvailabilityRange {
  start: string
  end: string
}

interface ReservationFormProps {
  resource: Resource
  userId: number
}

const INITIAL_MIN_DATE_TIME = toDateTimeLocalValue(new Date())

export function ReservationForm({ resource, userId }: ReservationFormProps) {
  const [start, setStart] = useState('')
  const [end, setEnd] = useState('')
  const [submitted, setSubmitted] = useState(false)
  const [checkedRange, setCheckedRange] = useState<AvailabilityRange | null>(null)
  const [confirmation, setConfirmation] = useState<Reservation | null>(null)
  const mutation = useCrearReserva()

  const validationError = useMemo(
    () => validateReservationRange(start, end),
    [start, end],
  )
  const showValidationError = submitted && validationError !== null

  function handleCheckAvailability() {
    setSubmitted(true)

    if (validationError) {
      return
    }

    setCheckedRange({
      start: toApiDateTime(start),
      end: toApiDateTime(end),
    })
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitted(true)
    setConfirmation(null)

    if (validationError) {
      return
    }

    mutation.mutate(
      {
        usuarioId: userId,
        recursoId: resource.id,
        fechaInicio: toApiDateTime(start),
        fechaFin: toApiDateTime(end),
      },
      {
        onSuccess: (reservation) => {
          setConfirmation(reservation)
          setCheckedRange(null)
        },
      },
    )
  }

  function resetForm() {
    setStart('')
    setEnd('')
    setSubmitted(false)
    setCheckedRange(null)
    setConfirmation(null)
    mutation.reset()
  }

  if (confirmation) {
    return (
      <div role="status" className="space-y-4 rounded-2xl border border-emerald-200 bg-emerald-50 p-5">
        <div>
          <p className="text-lg font-semibold text-emerald-900">Reserva confirmada</p>
          <p className="mt-1 text-sm text-emerald-800">
            {confirmation.recursoNombre} · {confirmation.usuarioNombre}
          </p>
        </div>
        <dl className="grid gap-2 text-sm text-emerald-900 sm:grid-cols-2">
          <div>
            <dt className="font-medium">Inicio</dt>
            <dd>{formatDateTime(confirmation.fechaInicio)}</dd>
          </div>
          <div>
            <dt className="font-medium">Fin</dt>
            <dd>{formatDateTime(confirmation.fechaFin)}</dd>
          </div>
        </dl>
        <div className="flex items-center gap-3">
          <StatusBadge status={confirmation.estado} />
          <button
            type="button"
            onClick={resetForm}
            className="rounded-lg border border-emerald-300 bg-white px-4 py-2 text-sm font-semibold text-emerald-800 transition hover:bg-emerald-100 focus:outline-none focus:ring-2 focus:ring-emerald-300"
          >
            Hacer otra reserva
          </button>
        </div>
      </div>
    )
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-5" noValidate>
      <div className="grid gap-4 sm:grid-cols-2">
        <div>
          <label
            htmlFor="reservation-start"
            className="mb-1 block text-sm font-medium text-slate-700"
          >
            Fecha y hora de inicio
          </label>
          <input
            id="reservation-start"
            type="datetime-local"
            value={start}
            min={INITIAL_MIN_DATE_TIME}
            onChange={(event) => setStart(event.target.value)}
            aria-invalid={showValidationError}
            aria-describedby={showValidationError ? 'reservation-range-error' : undefined}
            className="w-full rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200"
          />
        </div>
        <div>
          <label
            htmlFor="reservation-end"
            className="mb-1 block text-sm font-medium text-slate-700"
          >
            Fecha y hora de fin
          </label>
          <input
            id="reservation-end"
            type="datetime-local"
            value={end}
            min={start || INITIAL_MIN_DATE_TIME}
            onChange={(event) => setEnd(event.target.value)}
            aria-invalid={showValidationError}
            aria-describedby={showValidationError ? 'reservation-range-error' : undefined}
            className="w-full rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200"
          />
        </div>
      </div>

      {showValidationError && (
        <p id="reservation-range-error" role="alert" className="text-sm font-medium text-rose-700">
          {validationError}
        </p>
      )}

      {mutation.isError && <ErrorAlert message={describeApiError(mutation.error)} />}

      <div className="flex flex-col gap-3 sm:flex-row">
        <button
          type="button"
          onClick={handleCheckAvailability}
          className="inline-flex items-center justify-center rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-slate-300"
        >
          Comprobar disponibilidad
        </button>
        <button
          type="submit"
          disabled={mutation.isPending}
          className="inline-flex items-center justify-center rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-300 disabled:cursor-not-allowed disabled:bg-indigo-300"
        >
          {mutation.isPending ? 'Reservando…' : 'Reservar'}
        </button>
      </div>

      <AvailabilityPanel resourceId={resource.id} range={checkedRange} />
    </form>
  )
}
