<!-- Archivo: specs/001-sistema-reservas-recursos/spec.md -->
# Feature Specification: Sistema de Reserva de Espacios y Recursos

## Overview
El propósito de esta función es automatizar y gestionar centralizadamente la reserva de espacios físicos (laboratorios, auditorios, salas) y recursos (equipos audiovisuales), eliminando procesos manuales y previniendo conflictos como reservas duplicadas, asignación de recursos en mantenimiento o exceso de reservas activas por usuario.

## User Scenarios

### Escenario 1: Gestión de Usuarios y Recursos
- **Como** Administrador del Sistema.
- **Quiero** registrar y actualizar el estado de los usuarios y recursos compartidos.
- **Para** asegurar que solo los usuarios activos y recursos disponibles participen en el proceso de reserva.

### Escenario 2: Creación de Reserva Exitosa
- **Como** Usuario Activo.
- **Quiero** solicitar la reserva de un recurso disponible especificando la fecha y hora de inicio y fin.
- **Para** asegurar el uso exclusivo del espacio o equipo durante el intervalo reservado.

### Escenario 3: Intento de Reserva Rechazado por Conflicto o Inactivación
- **Como** Usuario (inactivo o activo).
- **Quiero** recibir un mensaje claro de rechazo al intentar reservar un recurso no disponible, en conflicto de horario, en mantenimiento o cuando haya superado el límite permitido.
- **Para** conocer inmediatamente la razón de la denegación y tomar acciones correctivas.

### Escenario 4: Inicio de sesión del administrador
- **Como** Administrador del Sistema.
- **Quiero** autenticarme con mis credenciales en un módulo de login.
- **Para** acceder a la gestión de reservas, usuarios y recursos solo con identidad verificada.

## Functional Requirements

### Gestión de Usuarios
- **FR-001**: El sistema debe permitir registrar usuarios con nombre, documento, correo electrónico y estado (`ACTIVO`, `INACTIVO`).
- **FR-002**: El sistema debe permitir consultar usuarios individualmente o en listado.
- **FR-003**: El sistema debe permitir cambiar el estado de un usuario entre `ACTIVO` e `INACTIVO`.

### Gestión de Recursos
- **FR-004**: El sistema debe permitir registrar recursos con nombre, descripción, tipo, capacidad y estado (`DISPONIBLE`, `MANTENIMIENTO`, `FUERA_DE_SERVICIO`).
- **FR-005**: El sistema debe permitir actualizar la información y cambiar el estado de un recurso. El cambio a `MANTENIMIENTO` se rechaza con el error `RECURSO_CON_RESERVAS_FUTURAS` mientras el recurso tenga al menos una reserva `ACTIVA` cuya `fecha_fin` sea posterior al momento del cambio, incluidas las que están en curso; el administrador debe cancelarlas antes.
- **FR-006**: El sistema debe permitir consultar la lista de recursos y el detalle de un recurso por su identificador.

### Gestión de Reservas
- **FR-007**: El sistema debe permitir crear una reserva asociando un usuario, un recurso, una fecha/hora de inicio y una fecha/hora de fin.
- **FR-008**: El sistema debe validar que solo usuarios con estado `ACTIVO` puedan crear reservas.
- **FR-009**: El sistema debe validar que solo recursos con estado `DISPONIBLE` puedan ser reservados (rechazando solicitudes para recursos en `MANTENIMIENTO` o `FUERA_DE_SERVICIO`).
- **FR-010**: El sistema debe validar que la fecha y hora final de la reserva sea estrictamente posterior a la fecha y hora inicial.
- **FR-011**: El sistema debe rechazar cualquier reserva cuyas fechas de inicio o fin sean anteriores al momento exacto de la transacción (fechas pasadas).
- **FR-012**: El sistema debe impedir el registro de reservas cuyo intervalo `[fechaInicio, fechaFin)` se solape parcial o totalmente con una reserva existente activa para el mismo recurso. El intervalo es semiabierto: el extremo de inicio cuenta y el de fin no, de modo que una reserva que empieza exactamente cuando otra termina no se considera solapada.
- **FR-013**: El sistema debe limitar a un máximo de **3 reservas activas simultáneas** por cada usuario. Si el usuario ya posee 3 reservas en estado `ACTIVA`, la creación debe ser rechazada.
- **FR-014**: El sistema debe permitir cancelar reservas que se encuentren en estado `ACTIVA`.
- **FR-015**: El sistema debe rechazar la cancelación de reservas que se encuentren en estado `FINALIZADA` o ya `CANCELADA`.
- **FR-016**: El sistema debe permitir la consulta de reservas por usuario, por recurso, por fecha y la verificación de disponibilidad de un recurso en un rango especificado. La consulta por fecha devuelve las reservas cuyo intervalo se solapa con el rango solicitado, con la misma semántica semiabierta de FR-012, y rechaza un rango cuyo fin no sea estrictamente posterior al inicio.
- **FR-017**: El sistema debe permitir listar usuarios, recursos y reservas con paginación y filtros. Los usuarios se filtran por estado; los recursos por estado y tipo, sin distinguir mayúsculas; las reservas por estado, usuario y recurso. La paginación usa los parámetros `page`, `size` y `sort`, con 20 elementos por defecto y 100 como máximo.

### Autenticación del administrador
- **FR-018**: El sistema debe permitir iniciar sesión a un administrador registrado con correo y contraseña. Unas credenciales inválidas se rechazan con 401 y el cuerpo de error común.
- **FR-019**: La sesión iniciada debe conservarse entre peticiones mediante una cookie de sesión `HttpOnly`; el cierre de sesión debe invalidarla y exigir un nuevo inicio para volver a entrar.
- **FR-020**: Las operaciones de escritura de usuarios y recursos (`POST /usuarios`, `PUT/PATCH /usuarios/{id}/estado`, `POST /recursos`, `PUT /recursos/{id}`, `PUT/PATCH /recursos/{id}/estado`) deben exigir una sesión de administrador. Las lecturas y el flujo de reserva (`GET` de usuarios, recursos y reservas, `POST /reservas` y `DELETE /reservas/{id}`) siguen abiertos sin autenticación.
- **FR-021**: Las contraseñas de los administradores deben almacenarse con un hash BCrypt; ninguna credencial se guarda ni se registra en texto plano.

### Ciclo de vida de la reserva
- **Finalización automática**: Una reserva en estado `ACTIVA` pasa a `FINALIZADA` cuando su `fecha_fin` es anterior al momento de la ejecución de un proceso programado con frecuencia configurable; el valor por defecto es de 5 minutos. Las reservas en estado `CANCELADA` o `FINALIZADA` no cambian.
- **Estados terminales**: `CANCELADA` y `FINALIZADA` son terminales; ninguna transición posterior los modifica.

## Success Criteria

- **Cero Duplicados**: El 100% de las solicitudes concurrentes de reserva para el mismo recurso en el mismo bloque horario deben resultar en como máximo 1 reserva confirmada y n-1 rechazadas con el error adecuado.
- **Integridad de Negocio**: Cero reservas registradas para usuarios en estado `INACTIVO` o recursos en `MANTENIMIENTO`/`FUERA_DE_SERVICIO`.
- **Cumplimiento de Límite**: 0% de usuarios con más de 3 reservas en estado `ACTIVA` de forma simultánea.
- **Finalización de Reservas Vencidas**: El 100% de las reservas `ACTIVA` con `fecha_fin` vencida pasan a `FINALIZADA` en la siguiente ejecución del proceso programado.
- **Manejo Estructurado de Errores**: El 100% de los fallos de validación de negocio deben devolver un código HTTP de error consistente (ej. 400 Bad Request, 409 Conflict, 404 Not Found) acompañado del cuerpo JSON estandarizado.
- **Acceso Administrativo Restringido**: El 0% de las operaciones de escritura de usuarios y recursos se ejecutan sin una sesión de administrador válida; los intentos sin sesión reciben 401 con el cuerpo de error común.

## Edge Cases

- **Solicitudes Concurrentes Simultáneas**: Dos peticiones HTTP idénticas recibidas exactamente en el mismo milisegundo para el mismo recurso y horario.
- **Solapamiento en Límites Exactos**: Solicitud de reserva cuyo `fechaInicio` es exactamente igual al `fechaFin` de una reserva previa. El intervalo es semiabierto `[inicio, fin)`, así que las dos reservas no se solapan y la solicitud se permite.
- **Transición de Estado del Recurso durante Reserva Activa**: Modificar un recurso a `MANTENIMIENTO` cuando ya existen reservas `ACTIVA` cuya `fecha_fin` es posterior al momento del cambio. La transición se bloquea con el error `RECURSO_CON_RESERVAS_FUTURAS`; las reservas no se cancelan automáticamente. El administrador debe cancelarlas antes de cambiar el estado.
- **Credenciales Inválidas**: Un intento de login con correo inexistente o contraseña incorrecta se rechaza con 401 y no revela cuál de los dos datos falló.
- **Sesión Ausente o Vencida**: Una operación de escritura administrativa sin sesión válida se rechaza con 401 y el cuerpo de error común; el frontend redirige al login.
- **Token CSRF Ausente**: Una operación de escritura administrativa con sesión válida pero sin el token CSRF exigido se rechaza con 403.

## Dependencies & Assumptions

- Se asume que la zona horaria del sistema está estandarizada en UTC o en la hora local del servidor definida institucionalmente.
- La autenticación del administrador se incorpora en esta fase con sesión de servidor (cookie `HttpOnly`) y CSRF con token; no se adopta JWT/OAuth2. El flujo de reserva del usuario final sigue sin autenticación, como se definió en las historias HU-19 y HU-20.
- Los administradores viven en su propia tabla y se dan de alta por operación; no existe registro público de administradores en esta fase.