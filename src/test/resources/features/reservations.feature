# language: es
@acceptance
Característica: Creación y cancelación de reservas

  Los escenarios cubren la reserva exitosa y los rechazos de las reglas de
  negocio de FR-007 a FR-015.

  @FR-007
  Escenario: Crear una reserva para un usuario activo y un recurso disponible
    Dado un usuario activo registrado
    Y un recurso disponible registrado
    Cuando reservo el recurso dentro de 2 días de "09:00" a "10:00"
    Entonces la respuesta tiene estado HTTP 201
    Y la reserva devuelta está en estado "ACTIVA"

  @FR-008
  Escenario: Rechazar la reserva de un usuario inactivo
    Dado un usuario inactivo registrado
    Y un recurso disponible registrado
    Cuando reservo el recurso dentro de 2 días de "09:00" a "10:00"
    Entonces la respuesta tiene estado HTTP 400 con error "USUARIO_INACTIVO"

  @FR-009
  Escenario: Rechazar la reserva de un recurso que no está disponible
    Dado un usuario activo registrado
    Y un recurso registrado en estado "MANTENIMIENTO"
    Cuando reservo el recurso dentro de 2 días de "09:00" a "10:00"
    Entonces la respuesta tiene estado HTTP 409 con error "RECURSO_NO_DISPONIBLE"

  @FR-010
  Escenario: Rechazar una reserva cuyo fin no es posterior al inicio
    Dado un usuario activo registrado
    Y un recurso disponible registrado
    Cuando reservo el recurso con fin anterior al inicio
    Entonces la respuesta tiene estado HTTP 400 con error "FECHAS_INVALIDAS"

  @FR-011
  Escenario: Rechazar una reserva con fechas pasadas
    Dado un usuario activo registrado
    Y un recurso disponible registrado
    Cuando reservo el recurso en una fecha pasada
    Entonces la respuesta tiene estado HTTP 400 con error "FECHA_PASADA"

  @FR-012
  Escenario: Rechazar una reserva que se solapa con otra activa
    Dado un usuario activo registrado
    Y un recurso disponible registrado
    Y una reserva activa del recurso dentro de 2 días de "09:00" a "11:00"
    Cuando reservo el recurso dentro de 2 días de "10:00" a "12:00"
    Entonces la respuesta tiene estado HTTP 409 con error "RECURSO_NO_DISPONIBLE"

  @FR-012
  Escenario: Permitir una reserva que empieza justo cuando termina otra
    Dado un usuario activo registrado
    Y un recurso disponible registrado
    Y una reserva activa del recurso dentro de 2 días de "09:00" a "10:00"
    Cuando reservo el recurso dentro de 2 días de "10:00" a "11:00"
    Entonces la respuesta tiene estado HTTP 201
    Y la reserva devuelta está en estado "ACTIVA"

  @FR-013
  Escenario: Rechazar la cuarta reserva activa de un usuario
    Dado un usuario activo registrado
    Y un recurso disponible registrado
    Y el usuario ya tiene 3 reservas activas
    Cuando reservo el recurso dentro de 5 días de "09:00" a "10:00"
    Entonces la respuesta tiene estado HTTP 400 con error "LIMITE_RESERVAS_ACTIVAS"

  @FR-014
  Escenario: Cancelar una reserva activa
    Dado un usuario activo registrado
    Y un recurso disponible registrado
    Y una reserva registrada en estado "ACTIVA"
    Cuando cancelo la reserva registrada
    Entonces la respuesta tiene estado HTTP 200
    Y la reserva devuelta está en estado "CANCELADA"

  @FR-015
  Escenario: Rechazar la cancelación de una reserva ya cancelada
    Dado un usuario activo registrado
    Y un recurso disponible registrado
    Y una reserva registrada en estado "CANCELADA"
    Cuando cancelo la reserva registrada
    Entonces la respuesta tiene estado HTTP 400 con error "RESERVA_CANCELADA"

  @FR-015
  Escenario: Rechazar la cancelación de una reserva finalizada
    Dado un usuario activo registrado
    Y un recurso disponible registrado
    Y una reserva registrada en estado "FINALIZADA"
    Cuando cancelo la reserva registrada
    Entonces la respuesta tiene estado HTTP 400 con error "RESERVA_FINALIZADA"
