import { apiFetch } from './client'
import type { Page, User } from './types'

export async function listActiveUsers(): Promise<Page<User>> {
  const params = new URLSearchParams({
    estado: 'ACTIVO',
    size: '100',
    sort: 'nombre,asc',
  })

  return apiFetch<Page<User>>(`/usuarios?${params.toString()}`)
}
