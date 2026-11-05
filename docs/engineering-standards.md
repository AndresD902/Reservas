---
description: "Estándares de desarrollo de software: ciclo de Test Driven Development, tipado, convenciones de código y estructura de pruebas. Aplica únicamente a los proyectos que tienen código."
---
# Estándares de ingeniería

> **Alcance.** Este documento aplica **solo si el proyecto tiene código**. Un proyecto de proceso, de configuración de un entorno o sin componente técnico no lo usa y puede eliminarlo, quitando además su fila del mapa de estándares en `base-standards.md`.

Los principios transversales de los que este documento es la instancia para software están en `base-standards.md`. Aquí está la mecánica.

## Test Driven Development

Es la forma que toma la disciplina de verificación cuando el trabajo es código: el criterio observable previo es **una prueba que falla**.

| Fase | Qué haces |
|---|---|
| **RED** | Escribes la prueba que expresa el comportamiento deseado y la ves fallar. Una prueba que pasa de inmediato no está probando lo que crees |
| **GREEN** | Escribes el código mínimo que la hace pasar. Nada extra para casos hipotéticos |
| **REFACTOR** | Mejoras la estructura manteniendo las pruebas en verde |

**Ninguna funcionalidad nueva sin una prueba fallida primero.** Aplica también a las correcciones: un bug se reproduce con una prueba que falla antes de arreglarlo, porque una corrección sin prueba de regresión no impide que el bug vuelva.

## Tipado

Todo el código debe estar completamente tipado. Sin tipos implícitos ni escapes al sistema de tipos (`any`, `# type: ignore` y equivalentes) salvo con una justificación escrita en el propio código explicando por qué no hay alternativa.

## Convenciones de código

- **Nombres en inglés**, descriptivos, que digan qué es la cosa y no cómo está implementada.
- **Comentarios y docstrings en español**, según la política de idioma de `base-standards.md`.
- Comentar el **porqué**, no el qué: si el código necesita un comentario para explicar qué hace, casi siempre el arreglo es el código, no el comentario.
- **Funciones cortas y con una sola responsabilidad**, antes que funciones largas con banderas de comportamiento.

## Estructura de pruebas

- Los nombres de las pruebas describen el comportamiento verificado, no el nombre del método que ejercitan.
- Una prueba verifica una cosa. Una prueba que falla debe señalar sin ambigüedad qué se rompió.
- Se prueban los casos límite y las condiciones de error, no solo el camino feliz.
- Nada de dependencias entre pruebas ni de orden de ejecución implícito.

## Estándares por dominio

Cuando el proyecto crezca, el detalle específico se separa a su propio documento siguiendo la convención de nombres de `documentation-standards.md`: `backend-standards.md` para patrones de API, base de datos y seguridad; `frontend-standards.md` para componentes, arquitectura de UI y accesibilidad. Este documento conserva lo transversal a todo el código del proyecto.
