import { useQuery } from '@tanstack/react-query'
import { listActiveUsers } from '../api/users'

export function useUsuarios() {
  return useQuery({
    queryKey: ['usuarios', 'ACTIVO'],
    queryFn: listActiveUsers,
    retry: false,
  })
}
