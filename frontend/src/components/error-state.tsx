import { describeApiError } from '../utils/error-messages'

interface ErrorStateProps {
  message: string
  error?: unknown
  onRetry?: () => void
}

export function ErrorState({ message, error, onRetry }: ErrorStateProps) {
  const detail = error ? describeApiError(error) : null

  return (
    <div role="alert" className="rounded-2xl border border-rose-200 bg-rose-50 px-6 py-8 text-center">
      <h2 className="text-lg font-semibold text-rose-800">{message}</h2>
      {detail && detail !== message && <p className="mt-2 text-sm text-rose-700">{detail}</p>}
      {onRetry && (
        <button
          type="button"
          onClick={onRetry}
          className="mt-5 inline-flex items-center justify-center rounded-lg bg-rose-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-rose-700 focus:outline-none focus:ring-2 focus:ring-rose-300"
        >
          Reintentar
        </button>
      )}
    </div>
  )
}
