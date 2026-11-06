# language: es
@acceptance
Característica: Gestión de recursos

  Los escenarios cubren el registro, la consulta y el cambio de estado de
  recursos descritos en FR-004, FR-005 y FR-006. El bloqueo de la transición a
  MANTENIMIENTO con reservas futuras (FR-005) todavía no está implementado y
  queda como escenario pendiente.

  @FR-004
  Escenario: Registrar un recurso disponible
    Cuando registro el recurso "Sala Magna" de tipo "SALA" con capacidad 20 en estado "DISPONIBLE"
    Entonces la respuesta tiene estado HTTP 201
    Y el recurso registrado tiene nombre "Sala Magna", tipo "SALA" y estado "DISPONIBLE"

  @FR-005
  Escenario: Cambiar el estado de un recurso sin reservas futuras
    Dado un recurso registrado en estado "DISPONIBLE"
    Cuando cambio el estado del recurso registrado a "MANTENIMIENTO"
    Entonces la respuesta tiene estado HTTP 200
    Y el recurso devuelto tiene estado "MANTENIMIENTO"

  @FR-005 @pending
  Escenario: Rechazar mantenimiento cuando el recurso tiene reservas activas futuras
    Dado un usuario activo registrado
    Y un recurso disponible registrado
    Y una reserva activa del recurso dentro de 2 días de "09:00" a "10:00"
    Cuando intento cambiar el estado del recurso registrado a "MANTENIMIENTO"
    Entonces la respuesta tiene estado HTTP 409 con error "RECURSO_CON_RESERVAS_FUTURAS"

  @FR-006
  Escenario: Consultar un recurso por su identificador
    Dado un recurso disponible registrado
    Cuando consulto el recurso registrado
    Entonces la respuesta tiene estado HTTP 200
    Y el recurso devuelto tiene nombre "Recurso de aceptación" y tipo "SALA"

  @FR-006
  Escenario: Listar los recursos registrados
    Dado un recurso disponible registrado
    Cuando listo los recursos
    Entonces la respuesta tiene estado HTTP 200
    Y el listado de recursos contiene el recurso registrado
