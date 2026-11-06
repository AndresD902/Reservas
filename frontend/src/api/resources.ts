import { apiFetch } from './client'
import type { Availability, Page, Resource } from './types'

export async function listResources(): Promise<Page<Resource>> {
  const params = new URLSearchParams({
    size: '100',
    sort: 'nombre,asc',
  })

  return apiFetch<Page<Resource>>(`/recursos?${params.toString()}`)
}

export async function getResource(resourceId: number): Promise<Resource> {
  return apiFetch<Resource>(`/recursos/${resourceId}`)
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
