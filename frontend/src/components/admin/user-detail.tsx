import { useState } from 'react'
import type { User } from '../../api/types'
import { useUpdateUserStatus } from '../../hooks/use-admin-users'
import { describeApiError } from '../../utils/error-messages'
import { ErrorAlert } from '../error-alert'
import { StatusBadge } from '../status-badge'

export function UserDetail({ user }: { user: User }) {
  const [updated, setUpdated] = useState(false)
  const mutation = useUpdateUserStatus()
  const nextStatus = user.estado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO'

  function handleStatusChange() {
    setUpdated(false)
    mutation.mutate(
      { userId: user.id, estado: nextStatus },
      { onSuccess: () => setUpdated(true) },
    )
  }

  return (
    <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between gap-3">
        <div>
          <h2 className="text-lg font-semibold text-slate-900">Detalle del usuario</h2>
          <p className="mt-1 text-sm text-slate-500">{user.nombre}</p>
        </div>
        <StatusBadge status={user.estado} />
      </div>

      <dl className="mt-4 space-y-3 text-sm">
        <div>
          <dt className="font-medium text-slate-500">Documento</dt>
          <dd className="text-slate-900">{user.documento}</dd>
        </div>
        <div>
          <dt className="font-medium text-slate-500">Correo electrónico</dt>
          <dd className="break-all text-slate-900">{user.correo}</dd>
        </div>
      </dl>

      {mutation.isError && <div className="mt-4"><ErrorAlert message={describeApiError(mutation.error)} title="No se pudo cambiar el estado" /></div>}

      {updated && (
        <p role="status" className="mt-4 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-800">
          Estado del usuario actualizado.
        </p>
      )}

      <button
        type="button"
        onClick={handleStatusChange}
        disabled={mutation.isPending}
        className="mt-4 inline-flex w-full items-center justify-center rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-slate-300 disabled:cursor-not-allowed disabled:bg-slate-100"
      >
        {mutation.isPending
          ? 'Actualizando…'
          : nextStatus === 'ACTIVO'
            ? 'Activar usuario'
            : 'Inactivar usuario'}
      </button>
    </section>
  )
}
