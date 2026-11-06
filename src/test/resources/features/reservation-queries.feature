# language: es
@acceptance
Característica: Consultas de reservas y disponibilidad

  Los escenarios cubren la consulta de reservas por usuario, por recurso y por
  rango de fechas, además de la verificación de disponibilidad de FR-016.

  @FR-016
  Escenario: Consultar las reservas de un usuario
    Dado un usuario activo registrado
    Y un recurso disponible registrado
    Y una reserva activa del recurso dentro de 2 días de "09:00" a "10:00"
    Cuando consulto las reservas del usuario registrado
    Entonces la respuesta tiene estado HTTP 200
    Y el listado de reservas contiene la reserva registrada

  @FR-016
  Escenario: Consultar las reservas de un recurso
    Dado un usuario activo registrado
    Y un recurso disponible registrado
    Y una reserva activa del recurso dentro de 2 días de "09:00" a "10:00"
    Cuando consulto las reservas del recurso registrado
    Entonces la respuesta tiene estado HTTP 200
    Y el listado de reservas contiene la reserva registrada

  @FR-016
  Escenario: Consultar las reservas que se solapan con un rango de fechas
    Dado un usuario activo registrado
    Y un recurso disponible registrado
    Y una reserva activa del recurso dentro de 2 días de "09:00" a "11:00"
    Cuando consulto las reservas dentro de 2 días de "10:00" a "12:00"
    Entonces la respuesta tiene estado HTTP 200
    Y el listado de reservas contiene la reserva registrada

  @FR-016
  Escenario: Consultar la disponibilidad de un recurso libre
    Dado un usuario activo registrado
    Y un recurso disponible registrado
    Cuando consulto la disponibilidad del recurso dentro de 2 días de "09:00" a "10:00"
    Entonces la respuesta tiene estado HTTP 200
    Y el recurso está disponible
    Y la disponibilidad no reporta reservas solapadas

  @FR-016
  Escenario: Consultar la disponibilidad de un recurso con una reserva activa
    Dado un usuario activo registrado
    Y un recurso disponible registrado
    Y una reserva activa del recurso dentro de 2 días de "09:00" a "10:00"
    Cuando consulto la disponibilidad del recurso dentro de 2 días de "09:00" a "10:00"
    Entonces la respuesta tiene estado HTTP 200
    Y el recurso no está disponible
    Y la disponibilidad reporta reservas solapadas

  @FR-016
  Escenario: Rechazar la consulta por un rango inválido
    Cuando consulto las reservas en un rango inválido
    Entonces la respuesta tiene estado HTTP 400 con error "FECHAS_INVALIDAS"
