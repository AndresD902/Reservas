# language: es
@auth
@auth-acceptance
Característica: Inicio de sesión fallido y reintento exitoso

  Unas credenciales inválidas no crean sesión y el reintento válido sí la crea.

  @FR-018
  Escenario: Rechazar credenciales inválidas y permitir el reintento
    Dado un administrador registrado con correo "admin@example.com" y contraseña "secreta"
    Y un token CSRF disponible
    Cuando inicio sesión con el correo "admin@example.com" y la contraseña "incorrecta"
    Entonces la respuesta tiene estado HTTP 401 con error "CREDENCIALES_INVALIDAS"
    Y la consulta de sesión responde 401
    Cuando reintento el inicio de sesión con la contraseña "secreta"
    Entonces la respuesta tiene estado HTTP 200
    Y la sesión devuelve al administrador "admin@example.com"
