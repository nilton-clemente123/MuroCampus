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

## Mejora 2: búsqueda local por título

- Propuesta del plan aprobado: filtrar por título las publicaciones ya descargadas.
- Adaptación: se combinan el estado remoto y la consulta mediante `combine` y `stateIn` en el
  ViewModel, conservando la lista original sin mutarla. La UI solo envía el texto y observa estados.
- Se ignoran mayúsculas y espacios en los extremos. Vaciar la consulta restaura los resultados;
  la búsqueda se mantiene al cambiar el filtro de usuario y se aplica a su nueva respuesta.
- No se hace una petición por cada carácter ni se añade debounce sin necesidad: el filtrado es local
  sobre un conjunto pequeño. Los estados de carga/error no se ocultan con una lista vacía.
- Pruebas: coincidencia solo por título, mayúsculas, consulta vacía, sin coincidencias, preservación
  del error, combinación con usuario y ausencia de llamadas extra de red.

## Mejora 3: encabezado Accept con interceptor

- Propuesta del plan aprobado: un interceptor OkHttp agrega `Accept: application/json` a las peticiones.
- Se utiliza `header`, no `addHeader`, para sustituir valores previos sin acumular duplicados.
  El interceptor pertenece al cliente HTTP; ninguna pantalla ni ViewModel conoce ese detalle.
- Se mantiene HTTPS y el logging BODY únicamente en debug. El encabezado Accept indica el formato
  de respuesta deseado, no sustituye el Content-Type del cuerpo enviado por el conversor.
- Se prueba el cliente de producción con MockWebServer en GET, POST y DELETE; además se verifica
  que dos encabezados Accept previos se sustituyen por uno solo.
- Se añadieron pruebas instrumentadas de búsqueda sin resultados y borrado con confirmación/cancelación.

## Revisión crítica

- No se usaron catches vacíos, `GlobalScope`, llamadas HTTP desde Composables ni HTTP en producción.
- Las URLs HTTP de MockWebServer se usan exclusivamente en pruebas JVM locales.
- La extracción de capturas por coordenadas falló; se descartó esa captura y se sustituyó por una
  prueba Compose por etiquetas, comprobando primero «Publicado con id 101».
- Las pruebas instrumentadas reales dependen de la conectividad. Una repetición justo después de
  restablecer la red falló por timeout; con la conexión recuperada pasó. Las pruebas JVM de contrato
  no dependen de ese servicio externo.

## Resultado final verificado

- `assembleDebug`, `testDebugUnitTest`, `lintDebug` y `connectedDebugAndroidTest`: correctos.
- 27 pruebas JVM y 4 instrumentadas: cero fallos; lint sin errores.
- La prueba instrumentada final confirmó el flujo original y las mejoras de búsqueda/eliminación.
- Las cinco capturas del README se mantienen como evidencia de main, sin reemplazarlas por
  pantallas de mejora-ia. No se configuró remoto ni se publicó en GitHub.