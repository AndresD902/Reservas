import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <div className="rounded-2xl border border-dashed border-slate-300 bg-white px-6 py-12 text-center">
      <h1 className="text-2xl font-semibold text-slate-900">Página no encontrada</h1>
      <p className="mt-2 text-slate-600">
        La dirección que abriste no corresponde a ninguna pantalla de la interfaz.
      </p>
      <Link
        to="/reservar"
        className="mt-6 inline-flex items-center justify-center rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-indigo-700"
      >
        Volver a reservar
      </Link>
    </div>
  )
}
