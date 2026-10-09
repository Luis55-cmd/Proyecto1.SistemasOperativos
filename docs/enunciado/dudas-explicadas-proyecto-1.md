# Proyecto 1 — Entender las dudas antes de consultarlas

**Base:** [enunciado](transcripcion.md) y D-01–D-16 del [checklist original](checklist-requerimientos-proyecto-1.md#contradicciones-y-decisiones-pendientes). El checklist no se modifica. Este documento explica las dudas y revisa su importancia; no convierte posibles soluciones en decisiones aprobadas.

No tenemos 16 contradicciones. Tenemos una contradicción clara —cantidad de políticas—, varias reglas incompletas de simulación y varias decisiones que probablemente pueda tomar el equipo justificándolas. Conviene preguntar también cuáles quedan a nuestro criterio, para no pedir al preparador que diseñe la solución.

## Primero: distinguir los tiempos

Un **ciclo global** transcurre para el clúster entero. Un **ciclo de CPU de P1** ocurre cuando P1 obtiene CPU y ejecuta. Un **tiempo de espera** transcurre aunque P1 no ejecute. El **deadline** es un límite para completar trabajo y el **quantum** limita una ocupación de CPU. No son contadores intercambiables.

Ejemplo: P1 espera una lectura durante tres ciclos globales. Pasan tres ciclos, pero P1 no ejecuta tres instrucciones. Esos ciclos pueden afectar su deadline, según la regla del proyecto, sin aumentar su PC. Esta distinción es el centro de varias preguntas.

## D-01 — ¿Tres políticas o cuatro?

**Qué ocurre:** la misma frase dice «mínimo 3» y nombra FCFS, EDF, RR y prioridades apropiativas: cuatro políticas.

**Por qué importa:** elegir sólo tres podría dejar fuera una exigida; implementar cuatro aumenta trabajo y pruebas. Aquí no basta una preferencia de diseño.

**Pregunta:** ¿se requieren las cuatro enumeradas o podemos elegir tres? Si son tres, ¿hay alguna obligatoria?

**Clasificación:** contradicción real. Consultar pronto.

## D-02 — ¿Asignación manual, automática o ambas?

**Qué ocurre:** se describe que el usuario elige el computador y luego se admite asignación manual o automática, con balanceo opcional.

**Ejemplo:** seleccionar «Computador 2» en el formulario es asignación manual. Que ÁvilaOS elija el de menor carga es automática. En ambos casos, después no puede migrar el proceso.

**Interpretación razonable:** la selección manual parece suficiente; no hace falta construir balanceo sólo por esta mención. La duda aumenta si se pretende ofrecer únicamente asignación automática.

**Pregunta:** ¿basta ofrecer asignación manual, dejando la automática como ampliación opcional?

**Clasificación:** confirmación de alcance de bajo costo, no un bloqueo general del diseño.

## D-03 — ¿Más estados o motivos de bloqueo?

**Qué ocurre:** se piden cinco estados y se solicita identificar y justificar los estados de los bloqueados.

**Ejemplo:** un proceso espera porque el buffer está vacío y otro porque hay latencia de red. Ambos pueden tener estado `Bloqueado` y diferentes motivos. Otra representación usaría subestados como «bloqueado por E/S» o «bloqueado por semáforo».

**Por qué importa:** cambia cómo representamos y mostramos las esperas, pero no cambia la obligación de explicar por qué no puede continuar cada proceso.

**Pregunta:** ¿se acepta conservar los cinco estados y distinguir las causas mediante un motivo de bloqueo, o exigen subestados explícitos?

**Clasificación:** confirmar representación aceptada. La propuesta no está aprobada por plantearla aquí.

## D-04 — ¿Qué avanza en cada ciclo?

**Qué ocurre:** el enunciado dice que PC y MAR aumentan por ciclo y que cada primitiva ocupa una instrucción en modo SO.

**Lo que no es una duda teórica:** un proceso bloqueado no está ejecutando instrucciones. No tiene sentido preguntar si todos los procesos deben avanzar su PC estando en las colas.

**Lo que sí falta concretar:** cómo se contabiliza la instrucción `semWait` que bloquea, dónde continúa cuando despierta y si existen ciclos de SO adicionales para despacho o interrupciones.

**Ejemplo:** P1 ejecuta `semWait` en el ciclo 10 y no hay permiso. Ese intento puede consumir la instrucción del ciclo; después espera. Al despertar debe estar claro si continúa tras la espera o si el simulador vuelve a ejecutar la misma operación. Repetir una reserva sin cuidado puede alterar dos veces el contador.

**Pregunta:** ¿PC/MAR avanzan al ejecutar cada instrucción, incluyendo primitivas en modo SO; la espera no agrega avances, y deben modelarse costos adicionales de despacho/interrupción?

**Clasificación:** regla de contabilización y reanudación, relevante para secuencias y pruebas.

## D-05 — ¿Qué instrucciones simula un proceso?

**Qué ocurre:** hay cantidades de instrucciones y perfiles CPU/I/O, y hay que mostrar la instrucción del PC. No se define un repertorio ni cuándo un I/O bound solicita E/S.

**Ejemplo:** «20 instrucciones y 5 ciclos» no aclara si esos cinco ciclos describen una espera de E/S, una ráfaga de CPU o la duración total. Un contador que disminuye hasta cero no basta por sí solo para diferenciar CPU bound de I/O bound.

**Por qué importa:** antes de simular ejecución tenemos que saber qué operaciones se alternan, qué causa la espera y qué significa trabajo restante.

**Pregunta:** ¿cómo se relacionan cantidad de instrucciones y ciclos en CPU/I/O bound? ¿Debemos configurar frecuencia y duración de E/S? ¿Basta mostrar operaciones abstractas —cómputo, E/S, primitivas de buffer— o se espera otro formato?

**Clasificación:** precisión importante del comportamiento esperado. No supone construir un intérprete de ensamblador.

## D-06 — ¿Cuándo termina un productor o consumidor?

**Qué ocurre:** conviven instrucciones, intervalo de producción/consumo y cantidad objetivo de elementos. El `while(true)` del anexo muestra repetición, pero el texto limita la ejecución.

**Ejemplo:** un productor tiene 30 instrucciones, produce cada 4 ciclos y necesita producir 10 elementos. Las operaciones de semáforo e inserción también consumen instrucciones. Los valores pueden ser incompatibles si todos son límites independientes.

**Otra diferencia importante:** «cada 4 ciclos» puede significar cuatro ciclos globales, cuatro instrucciones propias o cuatro ciclos de trabajo entre operaciones. Un proceso bloqueado no tiene el mismo resultado bajo esas tres interpretaciones.

**Pregunta:** ¿manda la cantidad objetivo de elementos, el presupuesto de instrucciones o el primero que se agote? ¿Cómo se mide el intervalo entre operaciones y cómo se cuentan las primitivas dentro del presupuesto?

**Clasificación:** regla necesaria para construir cargas coherentes. Deadline es una causa adicional de terminación, no una respuesta a esta duda.

## D-07 — ¿Cómo corre el deadline?

**Qué ocurre:** está claro que llegar a cero termina el proceso. Falta precisar cuándo empieza el contador y cuándo disminuye.

**Ejemplo:** P1 tiene deadline 10 y pasa seis ciclos en Nuevo por falta de RAM. Si cuenta desde creación, al admitirse quedan cuatro. Si cuenta desde admisión, aún quedan diez. EDF y las métricas cambian mucho.

**Caso frontera:** P1 completa su última instrucción en el mismo ciclo en que el deadline vence. Hay que decidir si terminó a tiempo. El orden de procesar esos eventos cambia la clasificación.

**Pregunta:** ¿es un presupuesto de ciclos globales desde creación o desde admisión; sigue corriendo en Listo/Bloqueado; y completar justo en el límite cuenta como éxito?

**Clasificación:** regla central para todos los procesos y las métricas, no sólo para EDF.

## D-08 — ¿Cuánta RAM ocupa un buffer?

**Qué ocurre:** capacidad mide cantidad de elementos; RAM debe medir alguna unidad de memoria. El texto no conecta ambas.

**Ejemplo:** un buffer de capacidad 10 podría reservar diez unidades desde su creación, o diez veces el tamaño del elemento, quizá con espacio adicional. No conviene confundir el tamaño real de objetos Java con la RAM simulada.

**Por qué importa:** admitir un buffer puede quitar espacio a procesos. También debe definirse qué mostrar si el usuario pide uno que no cabe.

**Pregunta:** ¿podemos usar unidades abstractas, reservar la capacidad completa al crear el buffer y rechazar su creación si no cabe, documentando la equivalencia?

**Clasificación:** propuesta de simplificación a validar, no fórmula oculta que debamos adivinar.

## D-09 — ¿Qué fórmulas esperan en las métricas?

**Qué ocurre:** se enumeran métricas, pero algunas tienen variantes o necesitan acuerdos sobre la población medida.

**Ejemplos:** tiempo hasta la primera ejecución no es tiempo hasta terminar. Un proceso terminado por deadline no necesariamente cuenta como trabajo completado para throughput. «Equidad» no indica una fórmula única. Utilización podría incluir CPU en modo SO o medir sólo trabajo de usuario.

**Por qué importa:** dos simuladores con la misma ejecución pueden mostrar números diferentes si sus definiciones difieren. Para el agregado global también hay que explicar cómo se combinan computadores y procesos.

**Pregunta:** ¿hay fórmulas/rúbrica para equidad y agregados? ¿Respuesta se mide desde creación hasta primera ejecución, CPU ocupada incluye modo SO y throughput cuenta únicamente terminaciones normales? ¿Cómo se contabilizan los deadlines vencidos?

**Clasificación:** confirmar convenciones evaluables. No estamos preguntando qué es una media, sino qué eventos y casos entran en ella.

## D-10 — ¿Cómo se aplica un cambio de política o quantum?

**Qué ocurre:** se exige cambiar ambos en ejecución, pero no se fija el instante efectivo ni qué sucede con el turno actual.

**Ejemplo:** P1 lleva cinco ciclos de un quantum 8 y el usuario lo reduce a 3. Podría agotarse el turno en el siguiente punto de decisión o conservarse el turno ya concedido hasta el próximo despacho. Ambas opciones requieren una regla explícita.

**Pregunta:** ¿los cambios se aplican en el siguiente ciclo y afectan al ejecutante, o podemos documentar que el nuevo quantum se aplica en su siguiente asignación de CPU? ¿Hay una regla obligatoria para reordenar al cambiar política?

**Clasificación:** generalmente decisión de simulación; preguntar si la evaluación impone una convención.

## D-11 — ¿Qué es exactamente un acceso remoto?

**Qué ocurre:** cada acceso remoto bloquea por latencia, pero una operación productor–consumidor incluye varias primitivas.

**Ejemplo:** con latencia 3, insertar un elemento podría añadir una espera total de tres ciclos, o varias esperas si cada operación sobre semáforos/datos se considera un acceso. Los tiempos dejan de parecerse.

**El orden también importa:** esperar red antes de adquirir un mutex no produce la misma contención que conservar el mutex durante esa espera. El costo se paga en tiempo simulado; no obliga a montar una red real.

**Pregunta:** ¿la latencia se cobra una vez por operación lógica de producir/consumir o por primitiva remota? ¿Debe aplicarse antes de adquirir los semáforos o hay otra secuencia exigida?

**Clasificación:** precisión importante de sincronización y tiempos.

## D-12 — ¿A quién admitir cuando se libera RAM?

**Qué ocurre:** revisar la cola no define cómo elegir entre solicitudes de distintos tamaños.

**Ejemplo:** quedan cuatro unidades libres. El primero espera seis y el segundo necesita dos. FIFO estricto no admite ninguno; buscar uno que quepa admite al segundo. Son políticas de admisión diferentes, independientes de la política de CPU.

**Caso adicional:** un proceso pide más memoria que la RAM total de su computador. Esperar no resolverá esa solicitud bajo las capacidades actuales.

**Pregunta:** ¿podemos definir y justificar la política de admisión —incluido adelantar a uno que quepa— y rechazar solicitudes que excedan la RAM total?

**Clasificación:** decisión del equipo si no existe regla de corrección específica.

## D-13 — ¿Qué pasa si vence el deadline dentro del protocolo de buffer?

**Qué ocurre:** la terminación puede interrumpir una secuencia que ya reservó un recurso.

**Ejemplo:** un productor reservó un espacio y obtuvo el mutex; vence su deadline antes de insertar. Si se elimina el proceso y sólo se libera su RAM, el mutex puede quedar retenido y el espacio perdido. Si ya insertó pero aún no señaló el elemento, el caso es distinto.

**Lo que debemos resolver nosotros:** conservar la consistencia y no dejar esperas o permisos huérfanos. No basta hacer `signal` sobre todos los semáforos indiscriminadamente; depende de qué se haya realizado.

**Lo que sí corresponde preguntar:** ¿se espera admitir vencimiento en cualquier instrucción, incluso dentro de esta secuencia, con cancelación consistente, o existe una simplificación autorizada para esos casos?

**Clasificación:** caso límite importante. El preparador fija el alcance; el equipo diseña y justifica la recuperación.

## D-14 — ¿Qué dos UML?

**Qué ocurre:** se piden dos, sin indicar tipos.

**Nuestra propuesta para consultar:** clases para estructura y secuencia para interacción. El dibujo de arquitectura que queremos hacer puede acompañarlos como herramienta de trabajo.

**Pregunta:** ¿clases y secuencia cumplen el requisito de los dos UML?

**Clasificación:** confirmación breve. No se desprende del texto que debamos entregar tres UML.

## D-15 — ¿Cuándo y dónde se registra la entrega?

**Qué ocurre:** se fija viernes de Semana 7 antes de las 7:00 AM y se anuncia un spreadsheet posterior.

**Pregunta:** ¿qué fecha de calendario corresponde y ya está disponible el spreadsheet?

**Clasificación:** logística. No impide discutir arquitectura hoy. Evitar deducir una fecha de otro documento posiblemente desactualizado.

## D-16 — ¿Es obligatoria una entidad DMA explícita?

**Qué ocurre:** DMA aparece como ejemplo de componente que reacciona al reloj, no como una especificación completa de E/S.

**Ejemplo:** representar una operación pendiente que concluye tras ciertos ciclos permite observar bloqueo y desbloqueo. Representar además un controlador DMA con estado propio agrega detalle. La mención por sí sola no define cuánto de ese detalle se evalúa.

**Pregunta:** ¿se exige un DMA explícito o es aceptable representar E/S mediante operaciones pendientes con duración y evento de finalización?

**Clasificación:** confirmar fidelidad esperada; conviene unirla a D-05 en el mensaje.

## Qué enviaría primero

El [mensaje preparado](consulta-preparador-proyecto-1.md) agrupa las dudas en tres partes:

1. Reglas que afectan alcance y comportamiento antes de diseñar: D-01, D-04–07, D-09, D-11 y D-16.
2. Decisiones que podemos tomar si autorizan supuestos documentados: D-02–03, D-08, D-10, D-12–13.
3. Confirmaciones de entrega: D-14–15.

Se pueden responder por número. Ninguna interpretación de este documento se registra todavía como respuesta oficial.
