# Integración local de correcciones de Luis

Fecha: 2026-10-18. PR existente: https://github.com/Luis55-cmd/Proyecto1.SistemasOperativos/pull/40

## Trabajo realizado

1. Restaurado `SimulationController.getCycleDurationMs()` desde HEAD. La única edición local era su eliminación. No se atribuye autor ni causa; se restaura el contrato usado por la GUI.
2. Revisados y probados en copia aislada los commits 4ffe23a (reserva de buffers), 23e785a (red) y 9d9ad73 (desbloqueos).
3. Integrados en feat/gui mediante merge local aa81e9d. Sin conflictos; preservados nombre único de computadores y consulta de intervalo.

No se hizo push, merge del PR ni cambio de estado de issues. El remoto feat/gui sigue en 93dc05e en el momento de esta revisión.

## Evidencia

Compilación Maven de NetBeans/JDK 25 correcta. Ejecutados por separado los doce main de pruebas existentes: todos pasan (ocho del motor y cuatro de GUI). La suite de primitivas mantiene sus tres observaciones conocidas sobre null.

`ReviewFixesProbe.java` pasa antes y después de integrar:

- Buffer: RAM insuficiente, capacidad cero/negativa, host inexistente, ajuste exacto, exceso posterior y falta de rango contiguo. Los fallos no registran buffer ni dejan reserva contable; fallo previo no consume ID.
- Sincronización: consumidor esperando elementos, productor esperando espacio y espera de mutex entre computadores completan después de liberar el recurso.
- Red: latencias 0/1/2, dos solicitudes remotas simultáneas que descuentan una vez por tick, y vencimiento de deadline sin reactivación posterior.

Las cinco reproducciones originales ahora dan:

| Caso | Resultado |
|---|---|
| Round Robin | Sigue fallando: tras 8 ticks A=12 restantes, B=20 |
| Doble terminación | Sigue fallando: ended=2, RAM usada=15 en vez de 35 |
| Consumidor bloqueado | Corregido en la reproducción: termina, buffer vacío, consumidos=1 |
| Latencia congelada | Corregido: proceso termina, demora restante=0 |
| Buffer sin RAM | Corregido: rechazado, sin registro, RAM usada=0 |

El probe antiguo presupone que createBuffer devuelve un objeto incluso al fallar. Para repetirlo con el nuevo contrato se cambió únicamente su comprobación final, de getBufferById(buf.getId()) a buf != null, en la copia temporal. No se interpreta ese antiguo NullPointerException como regresión del motor.

## Límites y observaciones

- No implica que el motor completo esté correcto: RR y liberación duplicada permanecen abiertos.
- `ProducerConsumerDriver.wakeUpProcess` accede al singleton SimulationController para localizar el kernel. Introduce una dependencia desde sincronización hacia control, contraria a la separación planteada. Además captura Exception sin reportarla y tiene un fallback que cambia el estado a LISTO sin garantizar encolado. No se reprodujo ese fallback en las pruebas del controlador; queda como riesgo de diseño a revisar.
- La sobrecarga nueva de createProcess ya acepta targetBufferId, cyclesPerOperation y requiredElements. Esto facilita el contrato productor/consumidor, pero todavía no recibe la secuencia CPU/E/S de nuestro editor.
- No se corrigieron otros comportamientos del equipo ni se implementó creación de procesos desde GUI.

## Prueba manual antes del push

- [ ] Abrir en NetBeans y crear computadores con nombre, RAM y política.
- [ ] Rechazar nombre duplicado con aviso rojo.
- [ ] Confirmar destinos reales en Procesos.
- [ ] Abrir Resumen y detalle por computador.
- [ ] Probar iniciar/pausar/reanudar, paso manual e intervalo aplicado.

## Archivos relevantes

- src/main/java/com/avilaos/simulation/SimulationController.java
- src/main/java/com/avilaos/core/kernel/ComputerKernel.java
- src/main/java/com/avilaos/sync/Buffer.java
- src/main/java/com/avilaos/sync/NetworkSimulator.java
- src/main/java/com/avilaos/sync/ProducerConsumerDriver.java
- src/test/java/com/avilaos/simulation/SimulationControllerTest.java
- docs/revisiones/2026-10-18/ReviewFixesProbe.java
