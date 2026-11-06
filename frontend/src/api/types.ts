export type UserStatus = 'ACTIVO' | 'INACTIVO'

export type ResourceStatus = 'DISPONIBLE' | 'MANTENIMIENTO' | 'FUERA_DE_SERVICIO'

export type ReservationStatus = 'ACTIVA' | 'CANCELADA' | 'FINALIZADA'

export interface Administrator {
  id: number
  nombre: string
  correo: string
}

export interface LoginRequest {
  correo: string
  password: string
}

export interface User {
  id: number
  nombre: string
  documento: string
  correo: string
  estado: UserStatus
}

export interface Resource {
  id: number
  nombre: string
  descripcion: string | null
  tipo: string
  capacidad: number
  estado: ResourceStatus
}

export interface Reservation {
  id: number
  usuarioId: number
  usuarioNombre: string
  recursoId: number
  recursoNombre: string
  fechaInicio: string
  fechaFin: string
  estado: ReservationStatus
  fechaCreacion: string
}

export interface Availability {
  recursoId: number
  fechaInicio: string
  fechaFin: string
  disponible: boolean
  reservasSolapadas: Reservation[]
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface ApiErrorBody {
  status: number
  error: string
  message: string
}

export interface CreateReservationRequest {
  usuarioId: number
  recursoId: number
  fechaInicio: string
  fechaFin: string
}

export interface CreateUserRequest {
  nombre: string
  documento: string
  correo: string
  estado: UserStatus
}

export interface CreateResourceRequest {
  nombre: string
  descripcion: string | null
  tipo: string
  capacidad: number
  estado: ResourceStatus
}
