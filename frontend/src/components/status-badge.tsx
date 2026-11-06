import type { ReservationStatus, ResourceStatus, UserStatus } from '../api/types'

type Status = UserStatus | ResourceStatus | ReservationStatus

const STATUS_STYLES: Record<Status, string> = {
  ACTIVO: 'bg-emerald-50 text-emerald-700 ring-emerald-600/20',
  INACTIVO: 'bg-slate-100 text-slate-600 ring-slate-500/20',
  DISPONIBLE: 'bg-emerald-50 text-emerald-700 ring-emerald-600/20',
  MANTENIMIENTO: 'bg-amber-50 text-amber-700 ring-amber-600/20',
  FUERA_DE_SERVICIO: 'bg-rose-50 text-rose-700 ring-rose-600/20',
  ACTIVA: 'bg-emerald-50 text-emerald-700 ring-emerald-600/20',
  CANCELADA: 'bg-slate-100 text-slate-600 ring-slate-500/20',
  FINALIZADA: 'bg-sky-50 text-sky-700 ring-sky-600/20',
}

const STATUS_LABELS: Record<Status, string> = {
  ACTIVO: 'Activo',
  INACTIVO: 'Inactivo',
  DISPONIBLE: 'Disponible',
  MANTENIMIENTO: 'Mantenimiento',
  FUERA_DE_SERVICIO: 'Fuera de servicio',
  ACTIVA: 'Activa',
  CANCELADA: 'Cancelada',
  FINALIZADA: 'Finalizada',
}

export function StatusBadge({ status }: { status: Status }) {
  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ring-1 ring-inset ${STATUS_STYLES[status]}`}
    >
      {STATUS_LABELS[status]}
    </span>
  )
}
