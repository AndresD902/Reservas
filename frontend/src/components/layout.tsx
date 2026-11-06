import { Link, NavLink, Outlet } from 'react-router-dom'
import { UserSelector } from './user-selector'

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `rounded-lg px-3 py-2 text-sm font-semibold transition ${
    isActive
      ? 'bg-indigo-50 text-indigo-700'
      : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
  }`

export function Layout() {
  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex w-full max-w-6xl flex-col gap-4 px-4 py-4 lg:flex-row lg:items-center lg:justify-between sm:px-6">
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
          <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between lg:items-center">
            <nav aria-label="Principal" className="flex flex-wrap gap-1">
              <NavLink to="/reservar" className={navLinkClass}>
                Reservar
              </NavLink>
              <NavLink to="/admin" className={navLinkClass}>
                Administración
              </NavLink>
            </nav>
            <UserSelector />
          </div>
        </div>
      </header>
      <main className="mx-auto w-full max-w-6xl px-4 py-8 sm:px-6">
        <Outlet />
      </main>
    </div>
  )
}
