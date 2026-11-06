interface SearchFieldProps {
  value: string
  onChange: (value: string) => void
}

export function SearchField({ value, onChange }: SearchFieldProps) {
  return (
    <div>
      <label htmlFor="resource-search" className="mb-1 block text-sm font-medium text-slate-700">
        Buscar recurso
      </label>
      <input
        id="resource-search"
        type="search"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder="Nombre, tipo o descripción"
        className="w-full rounded-xl border border-slate-300 bg-white px-4 py-2.5 text-sm shadow-sm placeholder:text-slate-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200"
      />
    </div>
  )
}
