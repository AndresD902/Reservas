import { useQuery } from '@tanstack/react-query'
import { listResources } from '../api/resources'

export function useRecursos() {
  return useQuery({
    queryKey: ['recursos'],
    queryFn: () => listResources(),
    retry: false,
  })
}
