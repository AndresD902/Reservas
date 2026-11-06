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

export async function listReservationsByUser(
  userId: number,
): Promise<Reservation[]> {
  return apiFetch<Reservation[]>(`/reservas/usuario/${userId}`)
}

export async function cancelReservation(id: number): Promise<Reservation> {
  return apiFetch<Reservation>(`/reservas/${id}`, {
    method: 'DELETE',
  })
}
