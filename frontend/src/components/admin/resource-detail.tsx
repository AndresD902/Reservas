import { useState } from 'react'
import type { FormEvent } from 'react'
import type { Resource, ResourceStatus } from '../../api/types'
import { useUpdateResourceStatus } from '../../hooks/use-admin-resources'
import { describeApiError } from '../../utils/error-messages'
import { ErrorAlert } from '../error-alert'
import { StatusBadge } from '../status-badge'

interface ResourceDetailProps {
  resource: Resource
  onEdit: () => void
}

export function ResourceDetail({ resource, onEdit }: ResourceDetailProps) {
  const [status, setStatus] = useState<ResourceStatus>(resource.estado)
  const [previousResourceStatus, setPreviousResourceStatus] = useState(
    resource.estado,
  )
  const [updated, setUpdated] = useState(false)
  const mutation = useUpdateResourceStatus()

  if (resource.estado !== previousResourceStatus) {
    setPreviousResourceStatus(resource.estado)
    setStatus(resource.estado)
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setUpdated(false)
    mutation.mutate(
      { resourceId: resource.id, estado: status },
      { onSuccess: () => setUpdated(true) },
    )
  }

  return (
    <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between gap-3">
        <div>
          <h2 className="text-lg font-semibold text-slate-900">Detalle del recurso</h2>
          <p className="mt-1 text-sm text-slate-500">{resource.nombre}</p>
        </div>
        <StatusBadge status={resource.estado} />
      </div>

      <dl className="mt-4 grid gap-3 text-sm sm:grid-cols-2">
        <div>
          <dt className="font-medium text-slate-500">Tipo</dt>
          <dd className="text-slate-900">{resource.tipo}</dd>
        </div>
        <div>
          <dt className="font-medium text-slate-500">Capacidad</dt>
          <dd className="text-slate-900">{resource.capacidad}</dd>
        </div>
        <div className="sm:col-span-2">
          <dt className="font-medium text-slate-500">Descripción</dt>
          <dd className="text-slate-900">
            {resource.descripcion ?? 'Sin descripción.'}
          </dd>
        </div>
      </dl>

      <form onSubmit={handleSubmit} className="mt-5 space-y-3 border-t border-slate-200 pt-5">
        <div>
          <label
            htmlFor="resource-quick-status"
            className="mb-1 block text-sm font-medium text-slate-700"
          >
            Cambiar estado
          </label>
          <select
            id="resource-quick-status"
            value={status}
            onChange={(event) => setStatus(event.target.value as ResourceStatus)}
            className="w-full rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200"
          >
            <option value="DISPONIBLE">Disponible</option>
            <option value="MANTENIMIENTO">Mantenimiento</option>
            <option value="FUERA_DE_SERVICIO">Fuera de servicio</option>
          </select>
        </div>

        {mutation.isError && (
          <ErrorAlert
            message={describeApiError(mutation.error)}
            title="No se pudo cambiar el estado"
          />
        )}

        {updated && (
          <p
            role="status"
            className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-800"
          >
            Estado del recurso actualizado.
          </p>
        )}

        <div className="flex flex-col gap-3 sm:flex-row">
          <button
            type="submit"
            disabled={mutation.isPending || status === resource.estado}
            className="inline-flex flex-1 items-center justify-center rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-slate-300 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400"
          >
            {mutation.isPending ? 'Actualizando…' : 'Actualizar estado'}
          </button>
          <button
            type="button"
            onClick={onEdit}
            className="inline-flex flex-1 items-center justify-center rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white transition hover:bg-slate-700 focus:outline-none focus:ring-2 focus:ring-slate-300"
          >
            Editar recurso
          </button>
        </div>
      </form>
    </section>
  )
}
