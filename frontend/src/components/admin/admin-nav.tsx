import { NavLink } from 'react-router-dom'

const linkClass = ({ isActive }: { isActive: boolean }) =>
  `rounded-lg px-3 py-2 text-sm font-semibold transition ${
    isActive
      ? 'bg-indigo-600 text-white'
      : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
  }`

export function AdminNav() {
  return (
    <nav
      aria-label="Secciones de administración"
      className="flex flex-wrap gap-2 rounded-xl border border-slate-200 bg-white p-1.5 shadow-sm"
    >
      <NavLink to="/admin/usuarios" className={linkClass}>
        Usuarios
      </NavLink>
      <NavLink to="/admin/recursos" className={linkClass}>
        Recursos
      </NavLink>
    </nav>
  )
}
