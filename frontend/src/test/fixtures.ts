import type {
  Availability,
  Page,
  Reservation,
  Resource,
  User,
} from '../api/types'

export const activeUser: User = {
  id: 1,
  nombre: 'Ana Torres',
  documento: '100000001',
  correo: 'ana.torres@example.com',
  estado: 'ACTIVO',
}

export const availableResource: Resource = {
  id: 10,
  nombre: 'Sala 101',
  descripcion: 'Sala de reuniones con videoconferencia',
  tipo: 'SALA',
  capacidad: 8,
  estado: 'DISPONIBLE',
}

export const maintenanceResource: Resource = {
  id: 11,
  nombre: 'Equipo audiovisual',
  descripcion: 'Proyector y sistema de sonido móvil',
  tipo: 'EQUIPO',
  capacidad: 1,
  estado: 'MANTENIMIENTO',
}

export const reservation: Reservation = {
  id: 100,
  usuarioId: 1,
  usuarioNombre: 'Ana Torres',
  recursoId: 10,
  recursoNombre: 'Sala 101',
  fechaInicio: '2026-11-07T10:00:00',
  fechaFin: '2026-11-07T11:00:00',
  estado: 'ACTIVA',
  fechaCreacion: '2026-11-06T09:00:00',
}

export const availability: Availability = {
  recursoId: 10,
  fechaInicio: '2026-11-07T10:00:00',
  fechaFin: '2026-11-07T11:00:00',
  disponible: true,
  reservasSolapadas: [],
}

export function pageOf<T>(content: T[]): Page<T> {
  return {
    content,
    totalElements: content.length,
    totalPages: 1,
    number: 0,
    size: 100,
  }
}
