<!-- Archivo: checklists/requirements.md -->
# Lista de Verificación de Calidad de Requisitos (Quality Checklist)

## Validaciones de Especificación y Diseño

- [x] **Capa de Controladores Desacoplada**: Ningún controlador accede directamente al repositorio.
- [x] **Regla de Negocio 1**: Usuarios inactivos no pueden realizar reservas (Validado en `ReservaService`).
- [x] **Regla de Negocio 2 y 3**: Recursos en mantenimiento o fuera de servicio no se pueden reservar (Validado en `ReservaService`).
- [x] **Regla de Negocio 4**: `fechaFin` estrictamente mayor que `fechaInicio` (Validado en `ReservaService`).
- [x] **Regla de Negocio 5**: Control anti-solapamiento de reservas en el mismo recurso (Validado en `ReservaService` + Bloqueo Pesimista en BD).
- [x] **Regla de Negocio 6**: Incapacidad de cancelar reservas `FINALIZADA` (Validado en `ReservaService`).
- [x] **Regla de Negocio 7**: Prohibición de reservas en fechas pasadas a la hora actual (Validado en `ReservaService`).
- [x] **Regla de Negocio 8**: Límite estricto de máximo 3 reservas activas simultáneas por usuario (Validado en `ReservaService`).
- [x] **Estructura Standard de Errores**: Objeto JSON global configurado en `@RestControllerAdvice` con código, tipo de error y mensaje explicativo.
- [x] **Preparado para Alta Concurrencia**: Estrategia de bloqueo pesimista definida para soportar la prueba futura de 500 usuarios simultáneos sin reservas duplicadas.