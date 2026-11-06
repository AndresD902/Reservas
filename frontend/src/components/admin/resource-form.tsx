import { useState } from 'react'
import type { FormEvent } from 'react'
import type { Resource, ResourceStatus } from '../../api/types'
import {
  useCreateResource,
  useUpdateResource,
} from '../../hooks/use-admin-resources'
import { describeApiError } from '../../utils/error-messages'
import { ErrorAlert } from '../error-alert'

interface ResourceFormProps {
  resource: Resource | null
  onSaved?: (resource: Resource) => void
  onCancel?: () => void
}

const inputClass =
  'w-full rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200'

export function ResourceForm({
  resource,
  onSaved,
  onCancel,
}: ResourceFormProps) {
  const [nombre, setNombre] = useState(resource?.nombre ?? '')
  const [descripcion, setDescripcion] = useState(resource?.descripcion ?? '')
  const [tipo, setTipo] = useState(resource?.tipo ?? '')
  const [capacidad, setCapacidad] = useState(
    resource ? String(resource.capacidad) : '',
  )
  const [estado, setEstado] = useState<ResourceStatus>(
    resource?.estado ?? 'DISPONIBLE',
  )
  const [saved, setSaved] = useState(false)
  const createMutation = useCreateResource()
  const updateMutation = useUpdateResource()
  const isEditing = resource !== null
  const activeMutation = isEditing ? updateMutation : createMutation

  function resetForm() {
    setNombre('')
    setDescripcion('')
    setTipo('')
    setCapacidad('')
    setEstado('DISPONIBLE')
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSaved(false)

    const request = {
      nombre: nombre.trim(),
      descripcion: descripcion.trim() || null,
      tipo: tipo.trim(),
      capacidad: Number(capacidad),
      estado,
    }

    if (resource) {
      updateMutation.mutate(
        { resourceId: resource.id, request },
        {
          onSuccess: (updatedResource) => {
            setSaved(true)
            onSaved?.(updatedResource)
          },
        },
      )
      return
    }

    createMutation.mutate(request, {
      onSuccess: (createdResource) => {
        resetForm()
        setSaved(true)
        onSaved?.(createdResource)
      },
    })
  }

  return (
    <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <h2 className="text-lg font-semibold text-slate-900">
        {isEditing ? 'Editar recurso' : 'Registrar recurso'}
      </h2>
      <p className="mt-1 text-sm text-slate-600">
        {isEditing
          ? 'Actualiza los datos y guarda los cambios.'
          : 'Registra un espacio o equipo para reservarlo después.'}
      </p>

      <form onSubmit={handleSubmit} className="mt-4 space-y-4">
        <div>
          <label
            htmlFor="resource-name"
            className="mb-1 block text-sm font-medium text-slate-700"
          >
            Nombre del recurso
          </label>
          <input
            id="resource-name"
            type="text"
            required
            maxLength={100}
            value={nombre}
            onChange={(event) => setNombre(event.target.value)}
            className={inputClass}
          />
        </div>

        <div>
          <label
            htmlFor="resource-description"
            className="mb-1 block text-sm font-medium text-slate-700"
          >
            Descripción
          </label>
          <textarea
            id="resource-description"
            rows={3}
            maxLength={255}
            value={descripcion}
            onChange={(event) => setDescripcion(event.target.value)}
            className={inputClass}
          />
        </div>

        <div>
          <label
            htmlFor="resource-type"
            className="mb-1 block text-sm font-medium text-slate-700"
          >
            Tipo
          </label>
          <input
            id="resource-type"
            type="text"
            required
            maxLength={50}
            value={tipo}
            onChange={(event) => setTipo(event.target.value)}
            className={inputClass}
          />
        </div>

        <div>
          <label
            htmlFor="resource-capacity"
            className="mb-1 block text-sm font-medium text-slate-700"
          >
            Capacidad
          </label>
          <input
            id="resource-capacity"
            type="number"
            required
            min={1}
            value={capacidad}
            onChange={(event) => setCapacidad(event.target.value)}
            className={inputClass}
          />
        </div>

        <div>
          <label
            htmlFor="resource-status"
            className="mb-1 block text-sm font-medium text-slate-700"
          >
            Estado del recurso
          </label>
          <select
            id="resource-status"
            value={estado}
            onChange={(event) => setEstado(event.target.value as ResourceStatus)}
            className={inputClass}
          >
            <option value="DISPONIBLE">Disponible</option>
            <option value="MANTENIMIENTO">Mantenimiento</option>
            <option value="FUERA_DE_SERVICIO">Fuera de servicio</option>
          </select>
        </div>

        {activeMutation.isError && (
          <ErrorAlert
            message={describeApiError(activeMutation.error)}
            title={
              isEditing
                ? 'No se pudo actualizar el recurso'
                : 'No se pudo registrar el recurso'
            }
          />
        )}

        {saved && (
          <p
            role="status"
            className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-800"
          >
            {isEditing
              ? 'Recurso actualizado correctamente.'
              : 'Recurso registrado correctamente.'}
          </p>
        )}

        <div className="flex flex-col gap-3">
          <button
            type="submit"
            disabled={activeMutation.isPending}
            className="inline-flex w-full items-center justify-center rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-300 disabled:cursor-not-allowed disabled:bg-indigo-300"
          >
            {activeMutation.isPending
              ? 'Guardando…'
              : isEditing
                ? 'Guardar cambios'
                : 'Registrar recurso'}
          </button>
          {isEditing && (
            <button
              type="button"
              onClick={onCancel}
              className="inline-flex w-full items-center justify-center rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-slate-300"
            >
              Cancelar edición
            </button>
          )}
        </div>
      </form>
    </section>
  )
}
