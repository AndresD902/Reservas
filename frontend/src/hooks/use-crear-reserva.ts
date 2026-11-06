import { useMutation, useQueryClient } from '@tanstack/react-query'
import { createReservation } from '../api/reservations'

export function useCrearReserva() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: createReservation,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['disponibilidad'] })
      void queryClient.invalidateQueries({ queryKey: ['reservas'] })
    },
  })
}
