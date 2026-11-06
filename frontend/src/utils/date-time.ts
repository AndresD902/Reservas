const DATE_TIME_LOCAL_PATTERN = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/

export function toDateTimeLocalValue(date: Date): string {
  const pad = (value: number) => String(value).padStart(2, '0')

  return [
    `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`,
    `${pad(date.getHours())}:${pad(date.getMinutes())}`,
  ].join('T')
}

export function toApiDateTime(value: string): string {
  return DATE_TIME_LOCAL_PATTERN.test(value) ? `${value}:00` : value
}

export function validateReservationRange(
  start: string,
  end: string,
  now: Date = new Date(),
): string | null {
  if (!start || !end) {
    return 'Selecciona la fecha y hora de inicio y de fin.'
  }

  const startDate = new Date(start)
  const endDate = new Date(end)

  if (Number.isNaN(startDate.getTime()) || Number.isNaN(endDate.getTime())) {
    return 'Las fechas no son válidas.'
  }

  if (endDate.getTime() <= startDate.getTime()) {
    return 'La fecha de fin debe ser posterior a la fecha de inicio.'
  }

  if (startDate.getTime() < now.getTime()) {
    return 'No se puede reservar en el pasado.'
  }

  return null
}

export function formatDateTime(value: string): string {
  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat('es-CO', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date)
}
