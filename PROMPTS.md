# Registro de asistencia de IA

Asistente utilizado: **Cline/ChatGPT**, no Gemini. Fecha: 25 de noviembre de 2026.
Según la aclaración del estudiante, el docente autorizó empezar desde cero y usar IA en ambas
fases. No se atribuye a Gemini ninguna respuesta ni se presenta este trabajo como realizado sin IA.

Los textos entre comillas son extractos reales de la conversación. Las mejoras concretas fueron
propuestas por el agente en el plan aprobado; no son prompts adicionales inventados del estudiante.

| Prompt que escribí (extracto real) | Resumen de la respuesta del asistente | Lo que se adaptó | Criterio técnico aplicado |
| --- | --- | --- | --- |
| «en base a este archivo,crees poder hacer los commits que se piden ?» | Revisó el PDF y el proyecto, identificó 11 commits en main y al menos 3 mejoras en mejora-ia. | Se conservó el paquete real `com.clemente.murocampus` y las versiones de la plantilla, en lugar de copiar literalmente la estructura supuesta del PDF. | Cada commit debe ser un avance compilable y la arquitectura no debe depender del nombre de paquete del ejemplo. |
| «el profesor dijo que nosotros debemos hacerlo desde cero» y «el profesor dijo que si podemos usarlo» | Adaptó la planificación para construir la base y desarrollar las dos fases con asistencia declarada. | Se crearon los componentes y la fábrica que la guía suponía entregados. | Transparencia sobre el origen del código, sin simular un proyecto base del docente. |
| «The user approved switching to act mode. Continue with the approved plan now.» y «Please continue where you left off.» | Implementó el plan aprobado y retomó desde Git sin reescribir los commits existentes. | Las tres mejoras elegidas se desarrollan en commits independientes en mejora-ia. | Trazabilidad de cambios, pruebas por capa y separación del estado final de main. |

## Mejora 1: eliminar publicación

- Propuesta del agente aprobada: botón Eliminar en detalle, `@DELETE`, `Response<Unit>`, Repository,
  ViewModel y UI; se mantiene MVVM.
- Adaptación: confirmación explícita y aviso de simulación; no se retira permanentemente la fila
  ni se afirma que el servidor conserva el borrado.
- El Repository revisa `isSuccessful`: una respuesta HTTP fallida con `Response<Unit>` no se trata
  como éxito solo porque haya llegado una respuesta. Se traduce el error en la capa de datos.
- `CancellationException` se relanza. El ViewModel no importa Retrofit ni excepciones HTTP.
- Validación automatizada: DELETE con ruta correcta, códigos 200/204/404/500, error de red,
  cancelación, estados del ViewModel, reintento y protección frente a envío duplicado.