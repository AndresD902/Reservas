import { ApiError } from '../api/client'

const ERROR_MESSAGES: Record<string, string> = {
  CREDENCIALES_INVALIDAS: 'El correo o la contraseña no son correctos.',
  NO_AUTENTICADO: 'Debes iniciar sesión para realizar esta operación.',
  ACCESO_DENEGADO: 'No tienes permisos para realizar esta operación.',
  USUARIO_INACTIVO: 'El usuario seleccionado no está activo y no puede reservar.',
  RECURSO_NO_DISPONIBLE:
    'El recurso ya está reservado en ese horario. Elige otro intervalo.',
  FECHAS_INVALIDAS: 'La fecha de fin debe ser posterior a la fecha de inicio.',
  FECHA_PASADA: 'No se puede reservar en el pasado.',
  LIMITE_RESERVAS_ACTIVAS:
    'El usuario ya tiene 3 reservas activas. Cancela una antes de crear otra.',
  USUARIO_NO_ENCONTRADO:
    'El usuario seleccionado no existe. Actualiza la página y vuelve a intentarlo.',
  RECURSO_NO_ENCONTRADO: 'El recurso no existe o fue eliminado.',
  DOCUMENTO_DUPLICADO: 'Ya existe un usuario con ese documento.',
  CORREO_DUPLICADO: 'Ya existe un usuario con ese correo electrónico.',
  RECURSO_CON_RESERVAS_FUTURAS:
    'No se puede poner el recurso en mantenimiento porque tiene reservas activas futuras.',
  VALIDACION: 'Revisa los datos del formulario e inténtalo de nuevo.',
  NETWORK_ERROR:
    'No se pudo conectar con la API. Verifica que el backend esté en ejecución.',
}

export function describeApiError(error: unknown): string {
  if (error instanceof ApiError) {
    return ERROR_MESSAGES[error.code] ?? error.message
  }

  return 'No se pudo completar la operación. Inténtalo de nuevo.'
}
