import { useQuery } from '@tanstack/react-query'
import { getResource } from '../api/resources'

export function useRecurso(resourceId: number) {
  return useQuery({
    queryKey: ['recurso', resourceId],
    queryFn: () => getResource(resourceId),
    enabled: Number.isInteger(resourceId) && resourceId > 0,
    retry: false,
  })
}
