import { Link } from 'react-router-dom'
import type { Resource } from '../api/types'
import { StatusBadge } from './status-badge'

export function ResourceList({ resources }: { resources: Resource[] }) {
  return (
    <ul className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
      {resources.map((resource) => {
        const isAvailable = resource.estado === 'DISPONIBLE'

        return (
          <li key={resource.id}>
            <article
              aria-label={resource.nombre}
              className="flex h-full flex-col rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:shadow-md"
            >
              <div className="flex items-start justify-between gap-3">
                <div>
                  <h3 className="text-lg font-semibold text-slate-900">{resource.nombre}</h3>
                  <p className="mt-1 text-sm text-slate-500">
                    {resource.tipo} · Capacidad {resource.capacidad}
                  </p>
                </div>
                <StatusBadge status={resource.estado} />
              </div>
              <p className="mt-3 flex-1 text-sm text-slate-600">
                {resource.descripcion ?? 'Sin descripción.'}
              </p>
              <div className="mt-5">
                {isAvailable ? (
                  <Link
                    to={`/recursos/${resource.id}`}
                    aria-label={`Reservar ${resource.nombre}`}
                    className="inline-flex items-center justify-center rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-300"
                  >
                    Reservar
                  </Link>
                ) : (
                  <span className="text-sm font-medium text-slate-500">
                    No disponible para reservar
                  </span>
                )}
              </div>
            </article>
          </li>
        )
      })}
    </ul>
  )
}
