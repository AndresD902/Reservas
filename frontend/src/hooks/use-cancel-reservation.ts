import { useMutation, useQueryClient } from '@tanstack/react-query'
import { cancelReservation } from '../api/reservations'

export function useCancelReservation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: cancelReservation,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['reservas'] })
      void queryClient.invalidateQueries({ queryKey: ['disponibilidad'] })
    },
  })
}
