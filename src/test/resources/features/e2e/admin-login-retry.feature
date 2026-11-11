# language: es
@e2e-gherkin @HU-32 @FR-018
Característica: Inicio de sesión fallido con reintento exitoso

  Como administrador del sistema
  Quiero que unas credenciales inválidas no creen sesión y que el reintento válido sí
  Para verificar el rechazo y la recuperación del login

  Escenario: El administrador falla, no obtiene sesión y reintenta
    Dado que abro la pantalla de inicio de sesión
    Cuando inicio sesión con una contraseña incorrecta
    Entonces permanezco en la ruta "/login"
    Y veo el mensaje de error "El correo o la contraseña no son correctos."
    Cuando abro la ruta "/admin/usuarios"
    Entonces soy redirigido a la ruta "/login"
    Cuando reintento iniciar sesión con la contraseña correcta
    Entonces veo el encabezado "Administración de usuarios"
    Y estoy en la ruta "/admin/usuarios"
