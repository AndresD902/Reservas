import { Link, Outlet } from 'react-router-dom'
import { UserSelector } from './user-selector'

export function Layout() {
  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex w-full max-w-6xl flex-col gap-4 px-4 py-4 sm:flex-row sm:items-center sm:justify-between sm:px-6">
          <Link to="/reservar" className="flex items-center gap-3">
            <span className="flex size-10 items-center justify-center rounded-xl bg-indigo-600 text-lg font-bold text-white">
              R
            </span>
            <span>
              <span className="block text-lg font-semibold leading-tight text-slate-900">
                Reservas
              </span>
              <span className="block text-xs text-slate-500">
                Espacios y recursos institucionales
              </span>
            </span>
          </Link>
          <UserSelector />
        </div>
      </header>
      <main className="mx-auto w-full max-w-6xl px-4 py-8 sm:px-6">
        <Outlet />
      </main>
    </div>
  )
}
