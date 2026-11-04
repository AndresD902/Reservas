<!-- Archivo: specs/001-sistema-reservas-recursos/research.md -->
# Investigación Técnica y Decisiones de Diseño

## Decisiones Técnicas

### 1. Control de Concurrencia y Prevención de Doble Reserva (500 Usuarios Simultáneos)
- **Decisión**: Utilizar **Bloqueo Pesimista (Pessimistic Locking)** a nivel de base de datos (`PESSIMISTIC_WRITE`) sobre el recurso al momento de validar y registrar la reserva en la transacción del servicio (`ReservaService`).
- **Justificación**: Ante el escenario de carga futura de 500 usuarios intentando reservar simultáneamente el mismo recurso limitado, la estrategia pesimista previene el fenómeno de "Lost Update" o reservas duplicadas en base de datos sin obligar al cliente a reintentar fallos de concurrencia optimista (`OptimisticLockException`).

### 2. Gestión de Migraciones con Flyway
- **Decisión**: Integración de Flyway para la administración de esquemas de base de datos.
- **Justificación**: Garantiza que el esquema de la base de datos sea declarativo, reproducible y rastreable en el control de versiones, requisito fundamental para las pruebas automatizadas y pipelines de CI/CD.

### 3. Estandarización de Respuestas de Error mediante `@RestControllerAdvice`
- **Decisión**: Crear un controlador global de excepciones con `@RestControllerAdvice` que retorne objetos DTO homogéneos con código HTTP, clave de error y mensaje descriptivo.
- **Justificación**: Cumple estrictamente con el requerimiento de manejo global de excepciones y facilita las validaciones automatizadas desde herramientas como Postman, JMeter o pruebas de integración.

## Alternativas Consideradas

- **Bloqueo Optimista (`@Version`)**:
  - *Descartado porque*: En escenarios de alta contención (500 usuarios sobre un mismo recurso al mismo tiempo), la tasa de colisión sería extremadamente alta, causando múltiples excepciones de concurrencia que degradarían la experiencia de usuario y el rendimiento.
- **Acceso a Repositorio desde Controlador**:
  - *Descartado porque*: Viola explícitamente los principios de la constitución del proyecto y la arquitectura por capas requerida.

## Riesgos y Mitigaciones

- **Riesgo**: *Deadlocks* (Bloqueos mutuos) bajo alta concurrencia.
  - *Mitigación*: Mantener las transacciones de reserva extremadamente cortas y ordenar el acceso a los recursos de manera uniforme.
- **Riesgo**: Solapamiento por diferencias en microsegundos o zonas horarias.
  - *Mitigación*: Persistir todas las fechas en tiempo UTC y aplicar consultas con intervalos semihabiertos (`fecha_inicio < :nuevaFin AND fecha_fin > :nuevaInicio`).