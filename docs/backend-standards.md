---
description: "Estándares del backend del sistema de reservas: stack, estructura de paquetes, base de datos, migraciones, configuración local y pruebas, con un puntero a los artefactos de spec-kit que fijan los principios de arquitectura. Se consulta antes de tocar código bajo src/ o el esquema de la base de datos."
---
# Estándares de backend

## Propósito y alcance

Este documento gobierna la forma concreta del backend: con qué se construye, dónde va cada pieza y cómo se cambia el esquema. Es un documento delegado desde `docs/base-standards.md` y especializa `docs/engineering-standards.md`, que conserva lo transversal a todo el código —Test Driven Development, tipado, convenciones de nombres y estructura de pruebas—. Lo que dice ese documento aplica aquí sin repetirse.

## Fuentes de verdad

Los principios de arquitectura y el diseño de cada funcionalidad viven en los artefactos de Spec-Driven Development, no aquí. Este documento los referencia; no reproduce sus reglas.

| Artefacto | Gobierna |
|---|---|
| `.specify/memory/constitution.md` | Principios no negociables: separación de capas, invariantes de negocio, control de concurrencia, DTOs en la API, manejo centralizado de excepciones, formato de error y *mutation testing* |
| `specs/<nnn>-<funcionalidad>/` | Especificación, plan técnico, investigación, modelo de datos y tareas de cada funcionalidad |
| `build.gradle` | Versiones exactas del stack y dependencias |

Si este documento y la constitución se contradicen, manda la constitución y este documento se corrige.

## Stack

Las versiones no se copian aquí: las fija `build.gradle`, y una copia queda desactualizada en el primer *upgrade*.

| Pieza | Tecnología |
|---|---|
| Lenguaje | Java, con la versión fijada por el *toolchain* de Gradle |
| Build | Gradle, siempre a través del *wrapper* (`./gradlew`) |
| Framework | Spring Boot: Web MVC, Data JPA, Validation |
| Base de datos | PostgreSQL |
| Migraciones | Flyway |
| Reducción de *boilerplate* | Lombok |
| Pruebas | JUnit Platform, con los *starters* de prueba de Spring Boot |

## Estructura de paquetes

Todo el código vive bajo `com.institucion.reservas`. La regla de dependencia entre capas la fija la constitución, en su principio 1; la tabla dice solo dónde va cada cosa.

| Paquete | Contenido |
|---|---|
| `controller` | Endpoints REST. Reciben y devuelven DTOs |
| `service` | Interfaces de los servicios de negocio |
| `service.impl` | Implementaciones: reglas de negocio y límites transaccionales |
| `repository` | Interfaces de Spring Data JPA |
| `entity` | Entidades JPA y enums de estado |
| `dto` | Objetos de request y response de la API |
| `mapper` | Conversión entre entidades y DTOs, escrita a mano como componentes de Spring |
| `exception` | Excepciones del dominio y el manejador global |

## Base de datos y migraciones

- **El esquema solo cambia con una migración de Flyway.** Hibernate corre con `ddl-auto: validate`: verifica el esquema contra las entidades, pero no lo modifica.
- Las migraciones viven en `src/main/resources/db/migration/` y siguen la convención de Flyway `V<n>__<descripcion>.sql`.
- **Una migración aplicada no se edita.** Cualquier corrección va en una migración nueva.
- Un cambio de esquema dispara la actualización del modelo de datos de la funcionalidad afectada, según el catálogo de `docs/documentation-standards.md`.

## Configuración local

- `application.yml` activa el perfil `local` y toma las credenciales de la base de datos de las variables de entorno `DB_USERNAME` y `DB_PASSWORD`.
- `src/main/resources/application-local.example.yml` es la plantilla versionada del perfil local. La copia con credenciales reales va en la raíz del repositorio como `application-local.yml`: Spring Boot la carga desde el directorio de trabajo, y `.gitignore` la excluye solo en esa ruta.

## Pruebas

La mecánica es la de `docs/engineering-standards.md`. Lo que se exige a la suite —aislamiento de la lógica de negocio, pruebas unitarias y de integración, *mutation testing* sobre los servicios— lo fija la constitución, en su principio 4 y en la sección "Estándares de Calidad".

## Desviaciones conocidas

Brechas entre el código actual y los estándares, detectadas al adoptar el harness. Cada una es trabajo pendiente, no una excepción aceptada: se resuelve con su propia historia en el backlog y se quita de esta tabla cuando se cierra.

| Desviación | Estándar que incumple |
|---|---|
| Los identificadores del código están en español (`RecursoController`, `fechaInicio`, `EstadoReserva`) | Política de idioma de `docs/base-standards.md`: código e identificadores en inglés |
| El directorio `specs/001-sistema-reservas-recursos/` está nombrado en español | Política de idioma de `docs/base-standards.md`: nombres de archivos y directorios en inglés |
| `specs/001-sistema-reservas-recursos/plan.md` declara Spring Boot 3.x y `build.gradle` usa 4.x | La especificación es la fuente de verdad (`docs/base-standards.md`): el plan quedó desactualizado o la versión cambió sin actualizarlo. Indeterminado cuál de los dos refleja la decisión |
| No hay plugin de *mutation testing* configurado en `build.gradle` | Constitución, sección "Estándares de Calidad" |
| La única prueba es `ReservasApplicationTests`; las reglas de negocio no tienen pruebas | Test Driven Development, en `docs/engineering-standards.md` |