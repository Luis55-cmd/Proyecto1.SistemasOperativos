# Mapa de recorridos y protección entre hilos Java

Estado: implementación local; ver evidencia de pruebas al final.
Rama: fix/java-thread-synchronization.

## Objetivo

El EDT atiende la interfaz sin esperar un permiso del modelo ni ejecutar lectura/escritura de archivos o ticks completos. El intervalo configurado entre ticks es una espera de reloj, no la duración de la sección crítica. No se promete una duración imperceptible sin medirla bajo carga.

Implementación: conservar el hilo del reloj, el EDT y añadir un único trabajador de solicitudes de GUI. La GUI captura entradas y encola una solicitud; el trabajador aplica las modificaciones completas bajo el semáforo de estado. Una cola propia preserva orden y evita crear un hilo por clic. La espera de solicitudes requiere coordinación adicional, distinta del permiso de estado. Implementado en `GuiCommands` con una cola propia limitada a 128 solicitudes.

Lecturas visuales: copiar valores del modelo bajo protección; dibujar después de liberar. No entregar referencias mutables a Computer, PCB, Buffer o colas como supuesto snapshot. Agrupar refrescos para no encolar uno por tick indefinidamente; las métricas históricas deben conservar sus muestras aunque se omitan cuadros visuales.

## Recorridos inspeccionados (riesgos de partida y tratamiento)

| ID | Entrada y funciones principales actuales | Estado / resultado | Riesgo actual | Tratamiento propuesto |
|---|---|---|---|---|
| R01 | GlobalClock.run → tick → SimulationController.tick → ComputerKernel.executeCycle | Ciclo, CPU, PCBs, colas, RAM, buffers y métricas | Contador avanza antes de completar el modelo; notificación bajo bloqueo de suscriptores | Una transición coherente por tick, contador incluido; liberar antes de dormir y notificar GUI |
| R02 | MonitorSimulacion.controlesReloj → stepSimulation → GlobalClock.step → tick | Mismo resultado que un tick automático | Se ejecuta en EDT; posible solapamiento con un tick en curso | Solicitud al trabajador; exigir pausa/detención y serializar con tick automático |
| R03 | PanelPrincipal.crearComputadorDesdeFormulario → createComputer (sobrecargas) | Instancia, ID y lista de computadores | Sobrecargas anidadas; validación y alta deben ser indivisibles | Capturar campos en EDT; trabajador valida y crea bajo permiso; resultado vuelve al EDT |
| R04 | EditorProceso.generarPCB → selectSenderInitiatedComputer/getComputerById → createProcess → gestionCreateProcess → admisión | ID, PCB, reserva de RAM o cola de nuevos | Elección de destino separada de alta; acceso a objetos vivos | Seleccionar/validar destino y admitir dentro de la misma operación; actualizar formulario fuera |
| R05 | PanelPrincipal.crearBufferDesdeFormulario → createBuffer → placeBufferRAM | Buffer, ID, reserva RAM y lista global | Lectura del anfitrión y alta requieren coherencia | Validación final y reserva/registro en una operación; sin pintar dentro |
| R06 | MonitorSimulacion.VistaComputador: listeners → Computer.setPolitica / setQuantum | Política/quantum; posterior actualización del planificador | Modificación directa que evita el bloqueo del controlador | Enviar ID y valor al controlador a través del trabajador; confirmar solo cuando esté aplicado |
| R07 | notifyGUI → invokeLater → MonitorSimulacion.actualizarVista / PanelMetricas.actualizarMetricas / actualizar listas y destinos | Tablas, tarjetas, gráficos, opciones | Dibujo bajo synchronized(controller), otras lecturas sin esa protección | Snapshot breve y coherente; liberar permiso antes de tocar Swing; actualizar datos visibles en EDT |
| R08 | Botones reloj → startSimulation/pauseSimulation/setCycleDurationMs → GlobalClock.start/pause/resume/setCycleDurationMs | Estado de ejecución y velocidad | pause cambia bandera pero puede quedar un tick en curso | Pausa efectiva al terminar el tick en curso; no interrumpir una instrucción; comandos de ciclo de vida ordenados |
| R09 | MonitorSimulacion.guardarEstado(File) / exportar log | Archivo | Escritura y lectura del modelo desde EDT; snapshot inconsistente | Elegir archivo en EDT; obtener copia protegida; escribir fuera de permiso y fuera de EDT |
| R10 | MonitorSimulacion.cargarEstado(File) → reset → GlobalClock.reset/stop → createComputer/createBuffer/restoreProcess | Reemplazo del estado completo | Destruye antes de validar; bucles de reconstrucción; reloj y controlador toman bloqueos en orden inverso | Leer y validar fuera del permiso; detener/quiescer reloj sin retener estado; instalar estado validado, publicar y descartar refrescos viejos |
| R11 | SimulationController.logEvent → invokeLater → listeners → MonitorSimulacion.agregarLog | Log y ciclo asociado | Se obtiene timestamp cuando se dibuja, no necesariamente cuando ocurrió el evento | Capturar evento/ciclo al producirse; añadir texto en EDT fuera del permiso; impedir acumulación ilimitada de tareas visuales |
| R12 | ProcessControlBlock.allocateId/resetIdGenerator | Contador de IDs | Reinicio durante una sesión viva puede reutilizar IDs | Mantener semáforo propio ya implementado; reinicio solo dentro de reemplazo controlado de sesión |

## Contrato de las operaciones propuestas

- Responsabilidad: aplicar una modificación completa o tomar una copia coherente del estado.
- Entradas: datos capturados en EDT, identificadores y generación de sesión; nunca leer Swing desde el trabajador.
- Salidas: éxito/error y valores para actualizar la interfaz, publicados en EDT.
- Estado/efectos: los descritos en R01–R12, bajo el permiso correspondiente; archivos fuera del permiso.
- Conexiones: GUI → cola propia → trabajador → controlador → kernel; reloj → controlador; copias/resultados → EDT.
- Condiciones: revalidar existencia de destino y parámetros al ejecutar; rechazar solicitudes de una sesión reemplazada; evitar envíos duplicados mientras una operación está pendiente.
- Iteraciones: trabajador espera solicitudes mientras vive la JVM; cada operación termina al completar su recorrido; recorridos de listas limitados por sus elementos. No hacer espera activa.
- Caso normal: se aplica una solicitud, se libera en finally y se comunica resultado.
- Caso de error: liberar permiso y reportar sin tocar Swing desde otro hilo; evitar modificaciones parciales, particularmente al importar.

## Límites de bloqueo

No retener permiso de estado al dormir entre ticks, esperar solicitudes, abrir diálogos, pintar, leer/escribir disco, esperar la terminación del reloj ni esperar una tarea del EDT. `StateAccess` reconoce al hilo propietario y cuenta entradas anidadas. Las sobrecargas internas no vuelven a esperar el mismo permiso; solo la salida exterior publica y libera.

Revisar especialmente GlobalClock.tick (bloquea subscribers y llama al controlador) frente a SimulationController.reset (bloquea controlador y llama a clock.reset). Es un orden inverso visible en código; no se afirma haber reproducido el interbloqueo.

Los semáforos simulados de buffers son independientes: el PCB puede conservarlos entre ticks. El permiso de estado de Java se libera al completar cada operación.

## Alcance y validación

Medir duración de tick, tiempo de espera de solicitudes y demora del EDT con carga representativa; verificar que crear/cambiar política durante ejecución mantiene invariantes. Probar pausa/paso/carga con un tick en curso, errores de archivo, múltiples clics y cierre. La responsividad del EDT no garantiza por sí sola que una operación larga termine rápido: deberá mostrar estado pendiente y mantener habilitada la navegación.

No se modifica aquí la política de planificación ni se propone un hilo por computador simulado. La persistencia completa del protocolo continúa siendo otro pendiente.

## Cuántos hilos hay y quién hace qué

Hay **tres roles de ejecución de la aplicación**, no tres hilos totales en la JVM:

| Hilo | Origen / vida | Responsabilidad | Cuándo espera |
|---|---|---|---|
| `AWT-EventQueue-…` (EDT) | Lo gestiona Swing | Leer campos, encolar solicitudes, navegar y dibujar valores de snapshots; abrir selector de archivos | Cola de eventos Swing. No adquiere el permiso de estado en los recorridos normales de GUI |
| `AvilaOS-GlobalClock` | Un `Thread` propio daemon, creado en el primer arranque y reutilizado | Ejecutar un tick completo: contador, computadores, kernel, E/S, buffers y estadísticas | Entre ticks o mientras está detenido/pausado; espera sin retener el permiso de estado |
| `AvilaOS-Commands` | Un `Thread` propio daemon, creado con la primera solicitud | Ejecutar comandos de GUI por orden de aceptación y trabajo de archivos | Espera solicitudes; puede esperar al modelo sin bloquear el EDT |

No hay un hilo Java por computador, proceso o DMA simulado. Los computadores avanzan dentro del mismo tick del reloj. El hilo `main` prepara el arranque; la JVM también tiene hilos de GC, renderizado y otros servicios que no forman parte del protocolo del modelo. Por eso el conteo de un monitor de JVM será mayor. Si aún no se pulsa iniciar o no se manda ningún comando, los trabajadores correspondientes todavía no existen.

Los dos trabajadores son daemon: no impiden salir al cerrar la aplicación. No hay garantía de terminar una exportación si se cierra la JVM durante esa exportación; esperar su confirmación antes de cerrar.

## Permisos reales y recorrido de un clic

1. El EDT captura nombre, ID, política, quantum u otros campos en valores locales.
2. `GuiRequest.model` captura la generación de sesión y manda el trabajo a `GuiCommands.submit`.
3. La cola propia usa un semáforo para exclusión y otro para avisar que hay trabajo. Si no puede aceptar inmediatamente o está llena, devuelve un error; nunca espera espacio desde EDT.
4. El trabajador adquiere `StateAccess`, un semáforo binario justo. Revalida la sesión y ejecuta el controlador. Un tick automático adquiere exactamente el mismo permiso.
5. Un `finally` publica una copia inmutable del estado y libera el permiso. Las entradas anidadas incrementan/decrementan profundidad; no conceden acceso a otro hilo.
6. El resultado/error vuelve al EDT mediante `invokeLater`. La creación de procesos deshabilita su botón mientras está pendiente. Las altas de computador siguen validando nombre único dentro del permiso.

`StateAccess` es global porque tick y operaciones del controlador recorren varias EDD a la vez. Poner un candado independiente en cada lista no hace atómica la transición entre lista de listos, CPU y PCB.

Los `wait/signal` simulados del productor/consumidor siguen perteneciendo al PCB y pueden durar varios ticks. **Nunca son el permiso `StateAccess`**. Este se devuelve al terminar cada operación Java, aunque un PCB continúe bloqueado.

También quedan monitores `synchronized` breves para registrar/notificar observadores y para inicializar el singleton, además de sincronización local previa en componentes. La exclusión entre tick y comandos sí usa el semáforo real compartido. No se afirma haber sustituido todos los monitores existentes por semáforos.

## Lecturas, archivos y sesiones

- `SimulationSnapshot`: registros inmutables, arrays encapsulados, textos de colas y valores numéricos. El EDT no recorre las listas vivas del modelo. Un refresco toma una copia publicada y trabaja sobre ella.
- Los logs pendientes se limitan a 2000 eventos y se entregan en lotes de 100; ante saturación se omiten los más antiguos y se informa cuántos. El archivo de log no sustituye las pruebas de invariantes.
- Los refrescos visuales se agrupan; el historial guarda hasta 2000 muestras de CPU para no depender de cuántas veces alcanzó a dibujar el EDT. El mismo ciclo actualiza su muestra y un reset separa sesiones.
- Guardar: copia JSON a memoria dentro del permiso; escritura al archivo fuera del permiso y fuera del EDT.
- Cargar: lectura del archivo fuera del permiso; reconstrucción completa bajo el permiso desde el trabajador. Mientras se reconstruye, se retrasa el siguiente tick y la GUI conserva el snapshot anterior.
- Una solicitud pendiente de una sesión anterior se rechaza. Las tarjetas se reconstruyen al cambiar la sesión aunque se reutilicen IDs.
- Los getters que devuelven objetos vivos se conservan para el núcleo y pruebas. No usarlos desde nuevas pantallas: usar `snapshot()` y enviar comandos.

**Límites que permanecen:** el cargador existente todavía no ofrece validación transaccional ni rollback ante un JSON inválido; puede dejar una carga parcial. La persistencia completa de permisos/fase del protocolo productor-consumidor tampoco queda resuelta aquí. El parser/reconstrucción se ejecuta bajo el permiso (fuera del EDT); una carga grande puede detener el avance del reloj durante más tiempo que un tick normal. No confundir exclusión correcta con carga completa y reversible.

## Validación automática y mutaciones

Archivo: `src/test/java/com/avilaos/gui/ThreadCoordinationTest.java`. Ejecutable como `main`, con un argumento por caso. Barreras `Semaphore` fuerzan los intercalados; las esperas tienen límites. Ninguna comprobación depende de interpretar texto del log de simulación.

| Caso | Funciones probadas | Evidencia de contrato | Mutación compilada que lo hace fallar |
|---|---|---|---|
| `edt` | `GuiCommands.submit/run`, `GuiRequest.model` | Con permiso retenido, el EDT atiende otro evento; el comando espera y su respuesta llega a EDT | Ejecutar el trabajo mediante `invokeAndWait` en EDT: timeout de barrera |
| `exclusion` | `StateAccess.enter/exit` | Otro hilo no entra; la salida de una entrada anidada no libera el permiso exterior | Cambiar semáforo de 1 a 2 permisos: dos propietarios simultáneos |
| `snapshot` | `publishSnapshot`, `SimulationSnapshot` | Copias anteriores no cambian; cada alta/política publica nueva vista | Omitir publicaciones después de la inicial: falta el snapshot intermedio |
| `pause` | `GlobalClock.run/pause/reset/step` | Pausar espera a terminar un tick retenido por una barrera; luego no avanza hasta paso/reanudación | Quitar permiso de `pause`: confirma con tick incompleto |
| `session` | `requireSession`, `GuiRequest.model`, `reset` | Comando encolado antes del reset no crea objetos en la nueva sesión | Desactivar validación de sesión: comando viejo contamina estado |
| `failure` | `GuiRequest.model` y trabajador | Excepción comunicada; una solicitud posterior funciona y publica | Omitir `StateAccess.exit`: se pierde la publicación/permiso exterior |
| `history` | `SimulationSnapshot.history` | Se conservan muestras de ciclos sin refrescar GUI; reset separa historia | Descartar snapshot anterior al construir: se pierden muestras |
| `clock` | `GlobalClock.start/pause/reset` | Cinco arranques/reset reutilizan el mismo objeto Thread daemon | Crear Thread en cada start: cambia el trabajador |

| `log` | `logEvent/flushLogs` | Timestamp del evento, entrega en EDT y descarte de otra sesión | Quitar filtro de sesión: se muestra un evento anterior |

Resultado: **9/9 bases pasan y 9/9 mutaciones compiladas son detectadas**. Las mutaciones se ejecutaron en clases separadas en `/tmp/avilaos-thread-mutations`; no se modificó el código de trabajo para introducirlas. Cada carpeta tiene `base.log` y `mutation.log`. Estos archivos temporales no son parte del repositorio.

Para repetir la base, compilar con Maven `test-compile` y ejecutar `com.avilaos.gui.ThreadCoordinationTest` con cada argumento de la tabla, usando `target/classes:target/test-classes` y JFreeChart en el classpath. Maven por sí solo no ejecuta estos `main`.

Las pruebas GUI previas de creación de computador, buffer y reloj usan ahora `GuiTestDrain.await` fuera del EDT para esperar la respuesta asincrónica. Mantienen sus comprobaciones de negocio/UI. No se volvió síncrona la implementación para hacer pasar las pruebas.

## Qué revisar manualmente y qué queda por dominar

1. Iniciar reloj rápido; crear computadores/procesos y navegar mientras se aplican solicitudes. Confirmar mensajes de resultado, sin duplicados accidentales.
2. Pausar y pulsar +1 tick: aumenta una vez. Reanudar conserva la sesión.
3. Cargar otro escenario con iguales IDs: política/quantum deben operar sobre la nueva sesión.
4. Exportar un archivo y comprobar la confirmación antes de cerrar la aplicación.

La prueba del EDT demuestra que no espera el permiso de estado; no garantiza que dibujar miles de filas sea imperceptible. El costo de pintar Swing y el tamaño de una importación requieren evaluación con cargas representativas.

Para defenderlo: explicar reentrada y `finally`, por qué `volatile` publica la referencia pero no vuelve segura una EDD mutable, diferencia entre ambos tipos de semáforo, por qué una pausa espera el tick completo, y por qué la generación de sesión evita reutilizar accidentalmente IDs de otra simulación. La comprensión de estos mecanismos aún debe validarse con un recorrido oral del estudiante.

## Cierre de verificación local — 28/10/2026

- Maven `test-compile`: correcto.
- 27 ejecutables `*Test.java`: pasan (26 previos más el caso EDT por defecto del nuevo ejecutable).
- Los 9 modos de `ThreadCoordinationTest`: pasan por separado y cada uno detecta una mutación compilada.
- Los 7 escenarios JSON: pasan, con terminación y liberación de RAM esperadas; incluyen E/S, admisión, prioridades y productor/consumidor remoto con RR.
- `git diff --check`: correcto.
- Medición puntual del pulso EDT mientras otro hilo retenía el permiso: 6,87 ms. Es evidencia de esa ejecución local, no una garantía de latencia para cualquier carga.
- No se ejecutó una revisión visual manual de NetBeans en este cierre. Validación original realizada en `fix/java-thread-synchronization`. PR preparado en `fix/java-threads-pr`, basado en `fix/metrics-chart-scale` tras integrar el PR #56. El usuario confirmó que la GUI mantiene su comportamiento visual.

### Evidencia resumida de las mutaciones

- `edt`: `Timeout de barrera`.
- `exclusion`: `Consultar log temporal`.
- `snapshot`: `Snapshot intermedio cambió/no se publicó`.
- `pause`: `Pausa confirmó durante tick incompleto`.
- `session`: `Comando viejo contaminó nueva sesión`.
- `failure`: `Excepción perdió permiso/trabajador`.
- `history`: `Se perdieron muestras sin refrescar GUI`.
- `clock`: `Se creó otro trabajador de reloj`.
- `log`: `Log de sesión anterior se mostró`.
