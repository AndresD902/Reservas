import { apiFetch } from './client'
import type { CreateUserRequest, Page, User, UserStatus } from './types'

const USERS_PAGE_SIZE = 100

async function listUsersPage(page: number, estado?: UserStatus): Promise<Page<User>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(USERS_PAGE_SIZE),
    sort: 'nombre,asc',
  })

  if (estado) {
    params.set('estado', estado)
  }

  return apiFetch<Page<User>>(`/usuarios?${params.toString()}`)
}

export async function listUsers(estado?: UserStatus): Promise<Page<User>> {
  return listUsersPage(0, estado)
}

// El selector de usuario debe ofrecer todos los usuarios activos, no solo la
// primera página: si no, un usuario fuera de las primeras 100 filas no puede
// seleccionarse para reservar.
export async function listActiveUsers(): Promise<Page<User>> {
  const first = await listUsersPage(0, 'ACTIVO')
  const remainingPages = Array.from({ length: Math.max(first.totalPages - 1, 0) }, (_, index) =>
    listUsersPage(index + 1, 'ACTIVO'),
  )
  const remaining = await Promise.all(remainingPages)

  return {
    ...first,
    content: [first, ...remaining].flatMap((page) => page.content),
  }
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
