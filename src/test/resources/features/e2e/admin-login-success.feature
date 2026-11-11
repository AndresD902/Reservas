# language: es
@e2e-gherkin @HU-31 @FR-018 @FR-019
Característica: Inicio de sesión exitoso del administrador

  Como administrador del sistema
  Quiero iniciar y cerrar sesión desde la interfaz
  Para verificar que la sesión se crea, se reconoce y se invalida

  Escenario: El administrador llega al panel y cierra sesión
    Dado que no tengo una sesión iniciada
    Cuando abro la ruta "/admin/usuarios"
    Entonces soy redirigido a la ruta "/login"
    Cuando inicio sesión como administrador
    Entonces veo el encabezado "Administración de usuarios"
    Y estoy en la ruta "/admin/usuarios"
    Cuando cierro la sesión
    Entonces soy redirigido a la ruta "/login"
    Cuando abro la ruta "/admin/usuarios"
    Entonces soy redirigido a la ruta "/login"
