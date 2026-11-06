import { useQuery } from '@tanstack/react-query'
import { checkAvailability } from '../api/resources'

export function useDisponibilidad(
  resourceId: number,
  start: string,
  end: string,
  enabled: boolean,
) {
  return useQuery({
    queryKey: ['disponibilidad', resourceId, start, end],
    queryFn: () => checkAvailability(resourceId, start, end),
    enabled: enabled && start.length > 0 && end.length > 0,
    retry: false,
  })
}
