import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createResource,
  listResources,
  updateResource,
  updateResourceStatus,
} from '../api/resources'
import type { CreateResourceRequest, ResourceStatus } from '../api/types'

export function useAdminResources() {
  return useQuery({
    queryKey: ['recursos', 'admin'],
    queryFn: () => listResources(),
    retry: false,
  })
}

export function useCreateResource() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: createResource,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['recursos'] })
    },
  })
}

export function useUpdateResource() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({
      resourceId,
      request,
    }: {
      resourceId: number
      request: CreateResourceRequest
    }) => updateResource(resourceId, request),
    onSuccess: (resource) => {
      void queryClient.invalidateQueries({ queryKey: ['recursos'] })
      void queryClient.invalidateQueries({ queryKey: ['recurso', resource.id] })
    },
  })
}

export function useUpdateResourceStatus() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({
      resourceId,
      estado,
    }: {
      resourceId: number
      estado: ResourceStatus
    }) => updateResourceStatus(resourceId, estado),
    onSuccess: (resource) => {
      void queryClient.invalidateQueries({ queryKey: ['recursos'] })
      void queryClient.invalidateQueries({ queryKey: ['recurso', resource.id] })
      void queryClient.invalidateQueries({ queryKey: ['disponibilidad'] })
    },
  })
}
