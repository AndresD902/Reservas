<!-- Archivo: .specify/memory/constitution.md -->
# Constitución del Proyecto

## Principios Fundamentales

1. **Separación Estricta de Capas y Flujo Unidireccional**
   - El flujo de la aplicación debe seguir estrictamente el patrón Controller → Service → Repository → Data Access.
   - Queda explícitamente prohibido acceder a los repositorios o a la capa de persistencia directamente desde los controladores o cualquier componente externo a la capa de servicio.
   - La capa de controlador solo debe manejar el protocolo de entrada/salida (HTTP, DTOs y serialización) y delegar el 100% de la lógica de negocio a la capa de servicio.

2. **Invariantes de Negocio y Validación Defensiva**
   - El sistema debe garantizar la integridad de los datos en todo momento. Ninguna transacción debe dejar la base de datos en un estado inconsistente.
   - Las reglas de negocio (restricciones de estado, traslape de fechas, límites simultáneos) deben validarse a nivel de servicio y reforzarse a nivel de modelo/base de datos (restricciones de unicidad o bloqueos).
   - Queda prohibida la modificación mutada de entidades sin pasar por las validaciones de dominio correspondientes.

3. **Control de Concurrencia Robusto e Impermeable**
   - La arquitectura debe ser capaz de soportar alta concurrencia garantizando cero reservas duplicadas o solapadas para el mismo recurso.
   - Todo mecanismo de reserva o cambio de estado sensible debe aplicar estrategias de bloqueo (pesimista/optimista) o restricciones de integridad en base de datos para prevenir condiciones de carrera (*race conditions*).

4. **Diseño Orientado a Pruebas y Calidad Continua**
   - El código debe escribirse para ser altamente testeable, promoviendo alta cobertura de código mediante pruebas unitarias, de integración, análisis estático de código y pruebas de mutación.
   - La lógica de negocio debe aislarse completamente de dependencias externas para permitir mocking efectivo con frameworks de prueba.

## Restricciones de Diseño

- **Fases de Desarrollo**: No se permite iniciar la implementación de código de producción sin haber finalizado las fases de especificación, arquitectura de datos y plan técnico.
- **Acceso Directo**: Queda prohibido retornar entidades directas de la base de datos a través de la API REST; toda exposición pública debe realizarse a través de objetos de transferencia de datos (DTOs).
- **Manejo Centralizado de Excepciones**: No se permite capturar excepciones en controladores o servicios para retornar respuestas de error arbitrarias; toda excepción debe ser procesada por el manejador global de excepciones del sistema.

## Estándares de Calidad

- **Criterios de Aceptación**: Toda funcionalidad debe contar con criterios de aceptación claros y automatizables en pruebas unitarias y de integración.
- **Formato de Respuesta de Error**: Todos los errores HTTP de la API deben retornar una estructura homogénea con código de estado, identificador de error y mensaje explicativo.
- **Elegibilidad para Mutation Testing**: Todo servicio de negocio debe ser validado con pruebas de mutación, garantizando que cada condición lógica sea efectivamente verificada por la suite de pruebas.