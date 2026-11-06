import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  getCurrentAdministrator,
  login,
  logout,
} from '../api/auth'
import type { Administrator, LoginRequest } from '../api/types'

export const SESSION_QUERY_KEY = ['auth', 'session'] as const

export function useSession() {
  return useQuery({
    queryKey: SESSION_QUERY_KEY,
    queryFn: getCurrentAdministrator,
    retry: false,
    staleTime: 5 * 60 * 1000,
  })
}

export function useLogin() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: LoginRequest) => login(request),
    onSuccess: (administrator: Administrator) => {
      queryClient.setQueryData(SESSION_QUERY_KEY, administrator)
    },
  })
}

export function useLogout() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: logout,
    onSuccess: () => {
      queryClient.setQueryData(SESSION_QUERY_KEY, null)
      queryClient.removeQueries({ queryKey: SESSION_QUERY_KEY })
    },
  })
}
