export function LoadingState({ message = 'Cargando…' }: { message?: string }) {
  return (
    <div
      role="status"
      className="flex items-center gap-3 rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-600 shadow-sm"
    >
      <span
        aria-hidden="true"
        className="size-4 animate-spin rounded-full border-2 border-slate-300 border-t-indigo-600"
      />
      {message}
    </div>
  )
}
