import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { createUser, listUsers, updateUserStatus } from '../api/users'
import type { UserStatus } from '../api/types'

export function useAdminUsers() {
  return useQuery({
    queryKey: ['usuarios', 'admin'],
    queryFn: () => listUsers(),
    retry: false,
  })
}

export function useCreateUser() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: createUser,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['usuarios'] })
    },
  })
}

export function useUpdateUserStatus() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ userId, estado }: { userId: number; estado: UserStatus }) =>
      updateUserStatus(userId, estado),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['usuarios'] })
    },
  })
}
