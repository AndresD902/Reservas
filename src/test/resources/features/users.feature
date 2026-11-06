# language: es
@acceptance
Característica: Gestión de usuarios

  Los escenarios cubren el registro, la consulta y el cambio de estado de
  usuarios descritos en FR-001, FR-002 y FR-003.

  @FR-001
  Escenario: Registrar un usuario activo
    Cuando registro el usuario "Ana Torres" con documento "90000001" y correo "ana.torres@example.com" en estado "ACTIVO"
    Entonces la respuesta tiene estado HTTP 201
    Y el usuario registrado tiene documento "90000001", correo "ana.torres@example.com" y estado "ACTIVO"

  @FR-002
  Escenario: Consultar un usuario por su identificador
    Dado un usuario registrado con documento "90000002" y correo "bruno.diaz@example.com"
    Cuando consulto el usuario registrado
    Entonces la respuesta tiene estado HTTP 200
    Y el usuario devuelto tiene documento "90000002" y correo "bruno.diaz@example.com"

  @FR-002
  Escenario: Listar los usuarios registrados
    Dado un usuario registrado con documento "90000003" y correo "carla.gomez@example.com"
    Cuando listo los usuarios
    Entonces la respuesta tiene estado HTTP 200
    Y el listado de usuarios contiene el usuario registrado

  @FR-003
  Escenario: Inactivar un usuario activo
    Dado un usuario registrado con documento "90000004" y correo "diego.ruiz@example.com"
    Cuando cambio el estado del usuario registrado a "INACTIVO"
    Entonces la respuesta tiene estado HTTP 200
    Y el usuario devuelto tiene estado "INACTIVO"
