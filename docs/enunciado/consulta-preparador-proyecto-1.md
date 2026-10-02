# Consulta al preparador — Proyecto 1 ÁvilaOS

**Estado:** texto dirigido a Ares Ramirez, listo para que el estudiante lo copie y envíe manualmente. No enviado por Codex. Los códigos D corresponden al registro de dudas del equipo y permiten anotar luego las respuestas. Copiar el asunto y el cuerpo desde «Hola Ares»; esta nota es interna.

**Asunto sugerido:** Proyecto 1 ÁvilaOS — aclaraciones de alcance y reglas de simulación

---

Hola Ares, ¿cómo estás? Estamos revisando el enunciado del Proyecto 1 antes de cerrar los diagramas y la arquitectura. Agrupamos estas dudas para confirmar las reglas de evaluación y distinguirlas de las decisiones que podemos tomar y justificar como equipo.

### Reglas que necesitamos precisar

1. **Políticas (D-01).** Dice “mínimo 3”, pero enumera FCFS, EDF, Round Robin y prioridades apropiativas. ¿Se requieren las cuatro o podemos elegir tres? En ese caso, ¿hay alguna obligatoria?

2. **Instrucciones y E/S (D-05, D-16).** Para CPU bound e I/O bound, ¿qué relación hay entre cantidad de instrucciones y “ciclos necesarios para satisfacer el proceso”? ¿Debemos configurar cada cuánto solicita E/S y cuánto dura esa espera? Para la visualización, ¿basta representar instrucciones abstractas de cómputo/E/S/buffer? ¿Se exige un DMA explícito o basta modelar operaciones de E/S pendientes con duración y finalización?

3. **Avance por ciclo (D-04).** Entendemos que PC/MAR avanzan cuando el proceso ejecuta una instrucción, incluidas las primitivas en modo SO, y no durante su espera. ¿Es correcto? Si un `semWait` bloquea, ¿consume su instrucción y se continúa después de esa espera al despertar? ¿Hay ciclos adicionales obligatorios para despacho, cambio de proceso o atención de interrupciones?

4. **Fin de productores/consumidores (D-06).** Se configuran instrucciones, intervalo entre operaciones y cantidad objetivo de elementos. ¿Cuál determina la terminación normal si los límites no coinciden? ¿El intervalo se mide en ciclos globales o en ciclos de ejecución del propio proceso? ¿Las instrucciones de semáforos e inserción/extracción forman parte del presupuesto de instrucciones ingresado?

5. **Deadline (D-07).** ¿Se mide en ciclos globales desde la creación o desde la admisión? ¿Disminuye también mientras el proceso está Nuevo, Listo o Bloqueado? Si completa su última instrucción justo en el ciclo en que vence, ¿cuenta como cumplimiento o vencimiento?

6. **Latencia remota (D-11).** ¿Se paga una vez por operación lógica de producir/consumir, o por cada primitiva remota de semáforo/acceso al buffer? ¿La espera de red ocurre antes de adquirir los semáforos, o existe una secuencia específica esperada?

7. **Métricas (D-09).** ¿Hay fórmulas indicadas para equidad y agregación global? ¿Tiempo de respuesta se mide desde creación hasta primera ejecución, utilización incluye ciclos en modo SO y throughput cuenta sólo terminaciones normales? Para cumplimiento de deadlines, ¿cómo se tratan los procesos vencidos y los que siguen activos al consultar la métrica?

### Decisiones que podríamos documentar como equipo

8. **Asignación y bloqueo (D-02, D-03).** ¿Basta ofrecer asignación manual de computador, dejando balanceo automático como opcional? ¿Se aceptan los cinco estados indicados y un motivo de bloqueo separado, en lugar de subestados adicionales?

9. **RAM y admisión (D-08, D-12).** ¿Podemos usar unidades abstractas de memoria, reservar para cada buffer su capacidad completa al crearlo y rechazar buffers que no caben? ¿La selección entre procesos nuevos que caben en RAM queda a nuestro criterio documentado, y podemos rechazar procesos que pidan más que la RAM total de su computador?

10. **Cambios en ejecución (D-10).** ¿Hay una regla obligatoria sobre cuándo se aplica un cambio de política/quantum al proceso actual, o podemos definirla y justificarla? Por ejemplo, aplicar el cambio en el próximo ciclo o conceder el nuevo quantum a partir del siguiente despacho.

11. **Vencimiento durante sincronización (D-13).** Si vence el deadline después de reservar un espacio/elemento o adquirir el mutex, ¿se espera terminación inmediata con cancelación consistente según el avance de la operación, o existe una simplificación permitida? Queremos evitar dejar permisos retenidos o contadores inconsistentes.

### Entrega

12. **UML (D-14).** ¿Un diagrama de clases y uno de secuencia cumplen el requisito de los dos UML?

13. **Fecha y registro (D-15).** ¿Qué fecha de calendario corresponde al viernes de Semana 7 antes de las 7:00 AM? ¿Ya está disponible el spreadsheet para registrar la entrega?

Si alguna de estas reglas queda expresamente a criterio del equipo, nos sirve saberlo para documentar el supuesto y avanzar. Puedes responder por número y priorizar las primeras siete, que afectan más el comportamiento de la simulación. Muchas gracias.
