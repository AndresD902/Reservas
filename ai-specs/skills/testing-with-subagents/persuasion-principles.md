# Principios de persuasión para el diseño de artefactos

## Contenido

| Sección | Qué trae |
|---|---|
| Los siete principios | Autoridad, compromiso, escasez, prueba social, unidad, reciprocidad y simpatía, cada uno con cuándo usarlo y un ejemplo |
| Combinaciones de principios por tipo de artefacto | Qué principios usar y cuáles evitar según el artefacto |
| Por qué funciona: la psicología | Reglas de línea clara, intenciones de implementación y el modelo parahumano |
| Uso ético | El límite entre asegurar una práctica y manipular |
| Citas de investigación | Cialdini (2021) y Meincke et al. (2025) |
| Referencia rápida | Cinco preguntas para diseñar |

Los ejemplos hablan de *skills* porque de ahí salieron, pero **los principios valen igual para un agent**: lo que cambia es dónde se escribe la contramedida, no cómo funciona la persuasión.

## Resumen

Los LLM responden a los mismos principios de persuasión que las personas. Entender esta psicología ayuda a diseñar instrucciones más efectivas — no para manipular, sino para que las prácticas críticas se cumplan incluso bajo presión.

**Base de investigación:** Meincke et al. (2025) probaron 7 principios de persuasión con N=28,000 conversaciones con IA. Las técnicas de persuasión más que duplicaron la tasa de cumplimiento (33% → 72%, p < .001).

## Los siete principios

### 1. Autoridad

**Qué es:** Deferencia hacia la experiencia, las credenciales o las fuentes oficiales.

**Cómo funciona en las skills:**
- Lenguaje imperativo: "DEBES", "Nunca", "Siempre"
- Formulación no negociable: "Sin excepciones"
- Elimina la fatiga de decisión y la racionalización

**Cuándo usarlo:**
- Skills que imponen disciplina (TDD, requisitos de verificación)
- Prácticas críticas de seguridad
- Buenas prácticas ya establecidas

**Ejemplo:**
```markdown
✅ ¿Escribiste código antes de la prueba? Bórralo. Empieza de nuevo. Sin excepciones.
❌ Considera escribir las pruebas primero cuando sea posible.
```

### 2. Compromiso

**Qué es:** Consistencia con acciones, declaraciones o compromisos previos.

**Cómo funciona en las skills:**
- Exigir anuncios explícitos: "Anuncia el uso de la skill"
- Forzar elecciones explícitas: "Elige A, B o C"
- Usar seguimiento: TodoWrite para checklists

**Cuándo usarlo:**
- Para asegurar que las skills realmente se sigan
- Procesos de varios pasos
- Mecanismos de rendición de cuentas

**Ejemplo:**
```markdown
✅ Cuando encuentres una skill, DEBES anunciar: "Estoy usando [nombre de la skill]"
❌ Considera avisarle a tu compañero qué skill estás usando.
```

### 3. Escasez

**Qué es:** Urgencia por límites de tiempo o disponibilidad limitada.

**Cómo funciona en las skills:**
- Requisitos acotados en el tiempo: "Antes de continuar"
- Dependencias secuenciales: "Inmediatamente después de X"
- Previene la procrastinación

**Cuándo usarlo:**
- Requisitos de verificación inmediata
- Flujos de trabajo sensibles al tiempo
- Prevenir el "lo hago después"

**Ejemplo:**
```markdown
✅ Al terminar una tarea, pide revisión de código INMEDIATAMENTE antes de continuar.
❌ Puedes revisar el código cuando te resulte conveniente.
```

### 4. Prueba social

**Qué es:** Conformidad con lo que otros hacen o lo que se considera normal.

**Cómo funciona en las skills:**
- Patrones universales: "Siempre", "Cada vez"
- Modos de falla: "X sin Y = fallo"
- Establece normas

**Cuándo usarlo:**
- Documentar prácticas universales
- Advertir sobre fallos comunes
- Reforzar estándares

**Ejemplo:**
```markdown
✅ Checklists sin seguimiento en TodoWrite = pasos que se saltan. Siempre.
❌ Algunas personas encuentran útil TodoWrite para los checklists.
```

### 5. Unidad

**Qué es:** Identidad compartida, sentido de pertenencia a un mismo grupo.

**Cómo funciona en las skills:**
- Lenguaje colaborativo: "nuestro código base", "somos colegas"
- Objetivos compartidos: "ambos queremos calidad"

**Cuándo usarlo:**
- Flujos de trabajo colaborativos
- Establecer cultura de equipo
- Prácticas no jerárquicas

**Ejemplo:**
```markdown
✅ Somos colegas trabajando juntos. Necesito tu juicio técnico honesto.
❌ Probablemente deberías decirme si me equivoco.
```

### 6. Reciprocidad

**Qué es:** Obligación de devolver un beneficio recibido.

**Cómo funciona:**
- Usar con moderación — puede sentirse manipulador
- Rara vez necesario en skills

**Cuándo evitarlo:**
- Casi siempre (otros principios son más efectivos)

### 7. Simpatía

**Qué es:** Preferencia por cooperar con quienes nos agradan.

**Cómo funciona:**
- **NO usar** para forzar cumplimiento
- Entra en conflicto con una cultura de retroalimentación honesta
- Genera adulación

**Cuándo evitarlo:**
- Siempre, para imponer disciplina

## Combinaciones de principios por tipo de artefacto

| Tipo de artefacto | Usar | Evitar |
|---|---|---|
| Impone disciplina | Autoridad + Compromiso + Prueba social | Simpatía, Reciprocidad |
| Guía / técnica | Autoridad moderada + Unidad | Autoridad excesiva |
| Colaborativa | Unidad + Compromiso | Autoridad, Simpatía |
| Referencia | Solo claridad | Toda persuasión |

## Por qué funciona: la psicología

**Las reglas de línea clara reducen la racionalización:**
- "DEBES" elimina la fatiga de decisión
- El lenguaje absoluto elimina las preguntas de "¿esto es una excepción?"
- Las anti-racionalizaciones explícitas cierran vacíos específicos

**Las intenciones de implementación crean comportamiento automático:**
- Disparador claro + acción requerida = ejecución automática
- "Cuando X, haz Y" es más efectivo que "generalmente haz Y"
- Reduce la carga cognitiva del cumplimiento

**Los LLM son parahumanos:**
- Entrenados con texto humano que contiene estos patrones
- El lenguaje de autoridad precede al cumplimiento en los datos de entrenamiento
- Las secuencias de compromiso (declaración → acción) están frecuentemente modeladas
- Los patrones de prueba social (todos hacen X) establecen normas

## Uso ético

**Legítimo:**
- Asegurar que se sigan prácticas críticas
- Crear documentación efectiva
- Prevenir fallos predecibles

**Ilegítimo:**
- Manipular para beneficio personal
- Crear urgencia falsa
- Cumplimiento basado en culpa

**La prueba:** ¿esta técnica serviría a los intereses genuinos del usuario si los entendiera por completo?

## Citas de investigación

**Cialdini, R. B. (2021).** *Influence: The Psychology of Persuasion (New and Expanded).* Harper Business.
- Los siete principios de la persuasión
- Fundamento empírico para la investigación sobre influencia

**Meincke, L., Shapiro, D., Duckworth, A. L., Mollick, E., Mollick, L., & Cialdini, R. (2025).** Call Me A Jerk: Persuading AI to Comply with Objectionable Requests. University of Pennsylvania.
- Probaron 7 principios con N=28,000 conversaciones con LLM
- El cumplimiento subió de 33% a 72% con técnicas de persuasión
- Autoridad, compromiso y escasez fueron los más efectivos
- Valida el modelo parahumano del comportamiento de los LLM

## Referencia rápida

Al diseñar una skill, pregúntate:

1. **¿Qué tipo es?** (Disciplina vs. guía vs. referencia)
2. **¿Qué comportamiento intento cambiar?**
3. **¿Qué principio(s) aplican?** (Usualmente autoridad + compromiso para disciplina)
4. **¿Estoy combinando demasiados?** (No uses los siete)
5. **¿Es esto ético?** (¿Sirve a los intereses genuinos del usuario?)
