# Revisión de avances — 18 de octubre de 2026

## Alcance y comunicación

El equipo avisa que agregó SimulationController como intermediario entre UI y otras capas, y pide que los botones lo invoquen. La captura compartida describe presentación, control/simulación, memoria/sincronización y núcleo/hardware. Se revisó código, no sólo esa descripción.

Fetch realizado. Nuestra GUI y cambios locales se preservaron; no hubo merge, push ni publicación de issues.

- origin/develop: 419bc7b, incluye PR #30 de buffer.
- origin/feat/buffer: aa8e8cd.
- origin/feat/kernel_components: 4408b2f, corrección de suma de RAM.
- origin/fix/kernel-memory-sync: f9fdebb.
- origin/feat/simulation-controller: 034856d, revisión principal.

## Qué agregaron

Ricardo: productor/consumidor por fases, buffer, ampliaciones de CPU/PCB/kernel y formularios NetBeans. Luis: controlador singleton, separación de paquetes hardware/memory/sync/simulation/ui, gestor contable de RAM, admisión de nuevos, simulación de latencia y ejecución por ciclos centralizada en el kernel.

SimulationController expone createComputer, createProcess, createBuffer, start/pause/step/reset, velocidad, latencia y consultas de computadores/buffers. Se suscribe al reloj y llama executeCycle de cada kernel secuencialmente. No implementa por ello un hilo por computador.

Mejoras sobre revisión anterior: una cola de listos, scheduler persistente, switch sin caída entre políticas, despacho del resultado del scheduler, buffer con ID/datos inicializados y admisión pendiente por falta de RAM. FIFO aquí describe admisión de NUEVOS, no política universal de CPU.

## Verificación

Copia aislada en /tmp/avilaos-review-oct18 desde git archive de 034856d. Compila con Maven de NetBeans y JDK 25 (pom del equipo declara source/target 17). Se ejecutaron los ocho main de pruebas: todos pasan. Primitivas conserva tres observaciones de null sin contrato.

ReviewProbe.java contiene cinco reproducciones adicionales, usando el controlador con avance manual. No forma parte del motor ni cambia código del equipo.

| Hallazgo | Reproducción | Causa observada |
|---|---|---|
| Alta: RR no rota | Dos procesos de 20 ticks, quantum kernel 3; tras 8 ticks A tiene 12 y B 20 | executeCycle sólo llama dispatch cuando CPU está libre o proceso no ejecutando; no consulta desalojo mientras sigue ejecutando. También requiere revisar prioridades expropiativas. |
| Alta: productor termina dos veces | Buffer 5, productor 20 RAM, otro proceso 30 RAM; tras terminar productor ended=2 y RAM=15, debería 35 | executeCycle llama terminateProcess en rama del driver y otra vez en comprobación final; libera la misma memoria dos veces. |
| Alta: consumidor no despierta | Consumidor espera buffer vacío; productor inserta; tras 15 ticks sigue BLOQUEADO, hay 1 elemento y 0 consumidos | release de Semaphore no mueve PCB simulado de bloqueados a listos; falta integración de despertar. |
| Alta: latencia remota no avanza | Productor en computador 2 con buffer en 1; tras 10 ticks demora sigue 2 y PCB BLOQUEADO | handleDelay sólo corre al ejecutar el proceso, que ya salió de CPU; no hay avance de red sobre bloqueados. |
| Alta: buffer registrado sin RAM | RAM 32, crear buffer 64 devuelve buffer registrado y RAM usada 0 | createBuffer ignora mensaje de fallo de placeBufferRAM y agrega buffer de todas formas. |

Referencias en 034856d: core/kernel/ComputerKernel.java executeCycle (353), llamadas terminateProcess (386 y 406); simulation/SimulationController.java createBuffer (107); sync/ProducerConsumerDriver.java y sync/NetworkSimulator.java.

## Diferencias con nuestro diseño

- createProcess recibe name, computerId, type, instructions, memory, priority, deadline. No recibe secuencia CPU/E/S, buffer destino ni cantidad de elementos.
- ProducerConsumerDriver tiene protocolo fijo de cinco fases y PCB con syncStage/requiredElements/processedElements. Por tanto, ya modelaron múltiples elementos; antes de seguir nuestra propuesta hay que conciliar esto.
- El modelo actual avanza contadores; no hay imagen ejecutable de instrucciones CPU/E/S. DMA continúa vacío. IO_BOUND cae en el avance genérico del PCB.
- MemoryManager reserva cantidades, no rangos contiguos por proceso. RAM usa int[], no el boolean[] acordado. Los buffers sí marcan posiciones; procesos no. No tratarlo aún como modelo espacial consistente.
- El controlador devuelve listas/objetos mutables. Para observabilidad concurrente falta acordar acceso/instantáneas y actualización de Swing en EDT.
- Nuestra GUI sigue en com.avilaos.gui; los formularios del equipo viven en com.avilaos.ui. Evitar mantener dos flujos; adoptar un Main y conectar nuestra GUI al controlador.

## Orden recomendado

1. Compartir hallazgos como issues, con estas reproducciones (pendiente publicar).
2. Reutilizar SimulationController, no crear otro.
3. Integrar primero crear computador: RAM + política -> Computer -> lista visible.
4. Cerrar contrato de programa CPU/E/S y rol productor/consumidor antes de conectar creación de procesos.
5. Corregir fallos del ciclo/recursos y después habilitar ejecución integral desde GUI.
