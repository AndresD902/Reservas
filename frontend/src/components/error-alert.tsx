interface ErrorAlertProps {
  message: string
  title?: string
}

export function ErrorAlert({ message, title = 'No se pudo completar la reserva' }: ErrorAlertProps) {
  return (
    <div role="alert" className="rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-800">
      <p className="font-semibold">{title}</p>
      <p className="mt-1">{message}</p>
    </div>
  )
}
