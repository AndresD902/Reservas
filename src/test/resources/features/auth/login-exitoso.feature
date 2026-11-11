# language: es
@auth
@auth-acceptance
Característica: Inicio de sesión exitoso del administrador

  El flujo completo crea la sesión, la reconoce y la invalida al cerrar sesión.

  @FR-018
  @FR-019
  Escenario: Iniciar sesión, reconocerla y cerrar sesión
    Dado un administrador registrado con correo "admin@example.com" y contraseña "secreta"
    Y un token CSRF disponible
    Cuando inicio sesión con el correo "admin@example.com" y la contraseña "secreta"
    Entonces la respuesta tiene estado HTTP 200
    Y la sesión devuelve al administrador "admin@example.com"
    Cuando cierro la sesión
    Entonces la respuesta tiene estado HTTP 204
    Y la consulta de sesión responde 401 con error "CREDENCIALES_INVALIDAS"
