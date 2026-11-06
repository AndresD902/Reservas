import { apiFetch } from './client'
import type { CreateUserRequest, Page, User, UserStatus } from './types'

export async function listUsers(estado?: UserStatus): Promise<Page<User>> {
  const params = new URLSearchParams({
    size: '100',
    sort: 'nombre,asc',
  })

  if (estado) {
    params.set('estado', estado)
  }

  return apiFetch<Page<User>>(`/usuarios?${params.toString()}`)
}

export async function listActiveUsers(): Promise<Page<User>> {
  return listUsers('ACTIVO')
}

export async function createUser(request: CreateUserRequest): Promise<User> {
  return apiFetch<User>('/usuarios', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export async function updateUserStatus(
  userId: number,
  estado: UserStatus,
): Promise<User> {
  return apiFetch<User>(`/usuarios/${userId}/estado`, {
    method: 'PATCH',
    body: JSON.stringify({ estado }),
  })
}
