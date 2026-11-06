import { useQuery } from '@tanstack/react-query'
import { listReservationsByUser } from '../api/reservations'

export function useUserReservations(userId: number | null) {
  return useQuery({
    queryKey: ['reservas', 'usuario', userId],
    queryFn: () => listReservationsByUser(userId!),
    enabled: userId !== null,
    retry: false,
  })
}
