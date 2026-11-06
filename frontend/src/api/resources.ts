import { apiFetch } from './client'
import type {
  Availability,
  CreateResourceRequest,
  Page,
  Resource,
  ResourceStatus,
} from './types'

export interface ResourceFilters {
  estado?: ResourceStatus
  tipo?: string
}

export async function listResources(
  filters: ResourceFilters = {},
): Promise<Page<Resource>> {
  const params = new URLSearchParams({
    size: '100',
    sort: 'nombre,asc',
  })

  if (filters.estado) {
    params.set('estado', filters.estado)
  }

  if (filters.tipo?.trim()) {
    params.set('tipo', filters.tipo.trim())
  }

  return apiFetch<Page<Resource>>(`/recursos?${params.toString()}`)
}

export async function getResource(resourceId: number): Promise<Resource> {
  return apiFetch<Resource>(`/recursos/${resourceId}`)
}

export async function createResource(
  request: CreateResourceRequest,
): Promise<Resource> {
  return apiFetch<Resource>('/recursos', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export async function updateResource(
  resourceId: number,
  request: CreateResourceRequest,
): Promise<Resource> {
  return apiFetch<Resource>(`/recursos/${resourceId}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

export async function updateResourceStatus(
  resourceId: number,
  estado: ResourceStatus,
): Promise<Resource> {
  return apiFetch<Resource>(`/recursos/${resourceId}/estado`, {
    method: 'PATCH',
    body: JSON.stringify({ estado }),
  })
}

export async function checkAvailability(
  resourceId: number,
  start: string,
  end: string,
): Promise<Availability> {
  const params = new URLSearchParams({
    fechaInicio: start,
    fechaFin: end,
  })

  return apiFetch<Availability>(
    `/recursos/${resourceId}/disponibilidad?${params.toString()}`,
  )
}
