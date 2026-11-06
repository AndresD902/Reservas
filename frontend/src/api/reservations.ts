import { apiFetch } from './client'
import type { CreateReservationRequest, Reservation } from './types'

export async function createReservation(
  request: CreateReservationRequest,
): Promise<Reservation> {
  return apiFetch<Reservation>('/reservas', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}
