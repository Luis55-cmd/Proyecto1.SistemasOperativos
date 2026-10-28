# P01 — Alternativas para acordar las reglas del simulador

**Estado: revisión parcial; César eligió A01-B, A02-A, A03-A con extensión automática muy opcional, A04-B y A08-B con arbitraje pendiente. A05 sigue en discusión; A06 queda con costes separados y fijos (base de un tick por acción); A07 adopta PC siguiente, con representación de espera por concretar. A09-A elegida: secuencia completa en la imagen del proceso/programa; A10 acordada: editor CPU/E/S, protocolos automáticos y límites de ticks por definir; A11-A elegida como interpretación documentada de los ciclos útiles por elemento; A12–A35 siguen pendientes. Fecha: 03-10-2026.**

[P01](paquetes/P01.md) · [Mapa de desarrollo](README.md) · [Enunciado](../enunciado/transcripcion.md) · [Dudas D-xx](../enunciado/dudas-explicadas-proyecto-1.md)

Los siete grupos se descomponen aquí en **35 decisiones principales**, con una subdecisión A16.1 sobre memoria de la imagen del proceso. Cada una compara tres caminos y recomienda uno por su relación entre claridad, esfuerzo y alcance del Proyecto 1. Las recomendaciones son análisis de la IA, no decisiones aprobadas ni respuestas de Ares. La libertad de justificar simplificaciones que el estudiante reportó de la preparaduría no resuelve por sí sola ninguna duda específica.

Los códigos A01–A35 identifican decisiones de este documento: **no sustituyen los IDs del checklist**. El checklist permanece intacto. Este documento tampoco define todavía contratos de funciones ni autoriza implementación.

**Cierre estructural aprobado por César el 03-10-2026.** El catálogo mantiene abiertos los comportamientos y detalles pendientes, a resolver por paquete.

**Síntesis vigente:** [base estructural y clasificación por acuerdo, con requisitos relacionados](p01-base-estructural.md). Consultarla para distinguir lo elegido, lo pendiente y qué puede avanzar. Las propuestas anteriores que contradigan una elección posterior no están vigentes.

## Cómo usar este catálogo: no es una puerta de 35 decisiones

**Corrección de enfoque acordada con César:** no resolver A01–A35 en serie antes de programar. Conservar las elecciones hechas como línea base revisable y contrastarlas con recorridos y pruebas. Los pendientes sólo condicionan el paquete que los necesita; no bloquean el proyecto completo.

| Naturaleza | Decisiones relacionadas | Cuándo resolver |
|---|---|---|
| Alcance y separación de responsabilidades | A01–A04, A08–A10; aspecto de entrada de A16.1 | Línea base inicial de P01/P03. Ya hay elecciones registradas; no cerrar anticipadamente clases y estructuras internas |
| Contratos de ejecución | A05–A07, A11–A15 | Lo mínimo necesario para el recorrido que se está construyendo; validar y refinar en P05/P07/P09/P12/P18 |
| Reglas de memoria y admisión | A16–A19, incluida fórmula A16.1 | Al diseñar P08/P17 y su conexión con creación de procesos |
| Reglas de sincronización y comunicación | A20–A24 | Al diseñar P16/P18/P19, con casos concretos de espera y colisión |
| Reglas de planificación | A25–A29 | A25 define alcance; las demás al diseñar las políticas y P15 |
| Definiciones de medición | A30–A35 | Al diseñar P21; anticipar en P20 sólo los eventos que el recorrido actual necesita |

Una regla observable (por ejemplo cuándo vence un proceso) no es sólo un detalle de código, pero tampoco tiene que decidirse antes de trabajar un componente que no depende de ella. En una misma A puede haber un acuerdo estructural y detalles todavía abiertos: A08 elige hilos por computador; deja el arbitraje concreto para el recurso que se vaya a implementar.

No se añaden tickets por cada A. El control sigue siendo requisito original → paquete Pxx → contrato/cambio/prueba. Las A documentan decisiones que afectan ese trabajo. Elegir una alternativa no marca requisitos como cumplidos ni cierra paquetes.

## Límites que ya vienen dados

- Mínimo dos computadores de la misma clase; reloj global; cada computador avanza un ciclo por tick; Threads y Semaphores de Java obligatorios.
- Una cola de listos por computador; sin migración de procesos.
- Estados Nuevo, Listo, Ejecución, Bloqueado y Terminado; motivos de bloqueo visibles.
- Cada instrucción dura un ciclo. Cada `semWait`, `semSignal`, inserción y extracción es una instrucción en modo SO. No se puede comprimir todo el protocolo en una instrucción.
- La falta de RAM mantiene al proceso en Nuevo; terminar libera su memoria. El buffer también ocupa RAM.
- Deadline en cero termina el proceso. No podemos mantenerlo vivo indefinidamente para que libere un mutex.
- El algoritmo del anexo y el orden de sus operaciones deben respetarse; un bloqueo simulado debe liberar la CPU simulada.

Una alternativa marcada **no recomendable para P1** amplía el alcance o introduce una dificultad concreta. Las interpretaciones que necesitan aclaración se señalan expresamente; no se presentan como hechos del enunciado.

## Índice

1. [Alcance y simplificaciones — A01–A04](#1-alcance-y-simplificaciones)
2. [Reglas de los ciclos — A05–A08](#2-reglas-de-los-ciclos)
3. [Trabajo de cada tipo de proceso — A09–A11](#3-trabajo-de-cada-tipo-de-proceso)
4. [Terminación y deadlines — A12–A15](#4-terminación-y-deadlines)
5. [Admisión y memoria de buffers — A16–A19](#5-admisión-y-memoria-de-buffers)
6. [Sincronización y acceso remoto — A20–A24](#6-sincronización-y-acceso-remoto)
7. [Planificación y métricas — A25–A35](#7-planificación-y-métricas)

## 1. Alcance y simplificaciones

### A01 — ¿Cuánto adelantar del Proyecto 2?

| Camino | Pros | Contras |
|---|---|---|
| A. Construir únicamente lo requerido por P1 | Menos trabajo inmediato | Si se mezclan responsabilidades, costará extenderlo |
| B. Implementar P1 separando admisión, memoria y estados | Permite extender sin simular funciones futuras | Exige cuidar los límites del diseño |
| C. Implementar ya suspensión, disco y memoria virtual | Adelanta parte de P2 | Multiplica transiciones, pruebas y decisiones todavía desconocidas |

**Recomiendo B.** Preparar responsabilidades separadas, sin añadir estados ni mecanismos que todavía no tienen uso. “Extensible” no significa programar P2 hoy. Afecta P03/P05/P08.

**Elección de César: B (03-10-2026).** Falta concretar juntos los límites del diseño. Ejemplo de separación propuesta: la admisión decide cuándo admitir; la gestión de memoria informa disponibilidad y reserva/libera; la planificación de CPU elige entre procesos elegibles. En P1, liberar RAM ocurre al terminar. En P2 podría ocurrir también al suspender, conservando el PCB: por eso liberar memoria y destruir/terminar un proceso no deben ser una única responsabilidad inseparable. Esto ilustra la extensión; no aprueba todavía contratos ni el mecanismo de suspensión de P2.

### A02 — ¿Cómo representar por qué está bloqueado? · D-03

| Camino | Pros | Contras |
|---|---|---|
| A. Estado Bloqueado + motivo y recurso esperado | Conserva cinco estados; GUI clara | Hay que mantener coherentes estado y motivo |
| B. Un estado distinto por cada bloqueo | Lectura directa de cada variante | Crece el enum y se duplican transiciones |
| C. Deducir el motivo según la cola donde aparezca | Evita guardar el motivo dos veces | Consultas más complejas; errores de pertenencia confunden la GUI |

**Recomiendo A:** E/S, red o semáforo, identificando el recurso concreto. Esperar RAM sigue siendo Nuevo, no Bloqueado. Afecta P05/P09/P16/P19.

**Elección de César: A (03-10-2026).** Extender el PCB para guardar el motivo de bloqueo y permitir identificar el recurso esperado. Ejemplo conceptual: estado=Bloqueado; causa=Semáforo; recurso=B1.elementosDisponibles. El contador y la cola pertenecen al semáforo; no se copian al PCB. La responsabilidad que controla la transición a Bloqueado debe incluir la actualización de esa información y su coherencia con la cola correspondiente; no dejar que cada llamador cambie el estado y olvide el motivo. Al salir del bloqueo, retirar la información de espera vigente. La representación concreta y los contratos se definirán con el estudiante en P05/P09/P16/P19.

### A03 — ¿Cómo asignar el computador? · D-02

| Camino | Pros | Contras |
|---|---|---|
| A. Elección manual del usuario | Fácil; permite preparar experimentos controlados | El usuario puede repartir mal la carga |
| B. Automático por RAM libre | Regla sencilla y visible | RAM libre no implica CPU desocupada; requiere justificar balanceo |
| C. Automático por carga estimada de CPU | Atiende mejor el trabajo pendiente | Estimar carga con E/S y bloqueos añade ambigüedad |

**Recomiendo A para P1.** El enunciado permite la elección manual; no necesitamos un balanceador. La asignación queda fija. Afecta P22.

**Elección de César: A, con extensión automática muy opcional (03-10-2026).** Inicialmente el usuario elige el computador donde se ejecutará cada proceso. Mantener separada la selección del destino de la creación/admisión local para poder añadir posteriormente una interfaz única de carga con un selector manual/automático, tipo nube. En modo automático, una política elegiría el computador del clúster. No se ha elegido esa política ni se exige implementar ahora el selector, balanceador o infraestructura de nube; sólo conservar la posibilidad de extensión. Esta ampliación sigue siendo MUY OPCIONAL salvo que Ares indique lo contrario.

La asignación responde **en cuál computador nace el proceso**; luego vienen admisión a su RAM (A18) y selección para su CPU (A25–A29). Elegir el computador 2 no concede RAM ni CPU inmediatamente. El modo automático tampoco supondría migración posterior: P1 la prohíbe. La suspensión/memoria virtual de P2 es una responsabilidad distinta y sus requisitos adicionales aún no se conocen.

### A04 — ¿Qué representa el controlador de E/S? · D-16

| Camino | Pros | Contras |
|---|---|---|
| A. Operaciones pendientes con temporizadores independientes | Muy simple; hace visible CPU/E/S simultáneas | Equivale a capacidad de E/S sin límite práctico |
| B. Un controlador por computador, una operación activa y cola FIFO | Modelo acotado; distingue esperar servicio de recibirlo | Añade cola y regla de inicio del servicio |
| C. Varios dispositivos, canales y transferencias detalladas | Mayor variedad de escenarios | Mucho alcance que el enunciado no exige |

**Recomiendo B**, como simplificación explícita. Su duración empieza al recibir servicio, no al entrar en la cola. Llamarlo DMA no obliga a emular transferencias byte a byte. Afecta P03/P09.

**Elección de César: B (03-10-2026).** Un controlador por computador, una operación activa y cola FIFO; espera en cola separada del tiempo de servicio. Pendiente definir contratos al trabajar P03/P09.

## 2. Reglas de los ciclos

### A05 — ¿En qué orden ocurren las cosas dentro del tick? · D-04

| Camino | Pros | Contras |
|---|---|---|
| A. Fronteras de ciclo con fases fijas | Resultados reproducibles y recorridos explicables | Hay que escribir el orden y respetarlo |
| B. Cada hilo procesa eventos en el orden en que los recibe | Menos coordinación inicial | El mismo escenario puede cambiar con la ejecución de Java |
| C. Agenda de eventos con tiempos y prioridades | Orden muy explícito y flexible | Motor adicional innecesario para pasos de duración fija |

**Recomiendo A.** Propuesta: en la frontera se aplican comandos y finalizaciones pendientes; se admite y planifica; cada CPU dispone de un único intervalo de instrucción; al cerrar se registran resultados, terminaciones y vencimientos. Una finalización de E/S producida al cerrar permite competir en el siguiente intervalo. A14 fija el empate con deadline. No hay dos instrucciones porque el proceso anterior se bloquee. Afecta P07 y todos los componentes por ciclo.

**En discusión con César (03-10-2026).** Coincide con avance discreto y posibilidad de determinismo; el orden exacto no está elegido. Cada componente puede avanzar su paso durante el mismo tick (CPU y E/S en paralelo), pero la CPU no ejecuta una instrucción de usuario y otra de SO en ese único ciclo. Con la preferencia A06-B, las fases anteriores son puntos de coordinación: atender una interrupción o despachar consume los ciclos de CPU acordados, no sucede gratis por nombrarlo como fase. Falta ordenar sucesos de la misma frontera y definir desde cuándo sus efectos habilitan otra acción.

**Ampliación de A05 solicitada por César:** preparar un mapa/tabla de precedencia de eventos y sus coincidencias. Debe distinguir orden de atención, acciones que se combinan o quedan anuladas y eventos recibidos mientras el SO está ocupado. Ejemplo: quantum agotado y deadline del mismo proceso no deben producir una reinserción en Listo de un proceso terminado. CPU atiende de forma secuencial el trabajo que cueste ciclos; queda por acordar cómo registrar inmediatamente un vencimiento sin permitir ejecución adicional mientras su limpieza espera CPU. El mapa todavía no está definido.

### A06 — ¿Cuánto cuesta entrar al SO y cambiar de proceso? · D-04

| Camino | Pros | Contras |
|---|---|---|
| A. Sólo cuestan ciclos las instrucciones explícitas del modelo | Conteos simples; respeta los costes expresamente definidos | Despacho e interrupción no consumen un ciclo exclusivo |
| B. Cada despacho o interrupción cuesta un ciclo adicional de SO | Hace visible su sobrecarga | Cambia todas las duraciones y exige resolver eventos simultáneos |
| C. Costes configurables por tipo de evento | Permite experimentar | Más parámetros y casos sin exigencia explícita |

**Recomiendo A**, documentándolo como convención pendiente de aceptación. Las primitivas obligatorias siguen costando un ciclo SO cada una. La GUI registra modo por instrucción y eventos del núcleo; no inventa un ciclo extra para mostrar una transición. Si el equipo quiere esa sobrecarga, B es defendible, pero debe recalcular tiempos y métricas. Afecta P07/P10/P16/P23.

**Preferencia de César: B (03-10-2026); sustituye la recomendación inicial como dirección de trabajo.** Quiere que despacho y atención de interrupciones ocupen CPU en modo SO. Pendiente confirmar costes y agrupación: propuesta para discutir, un tick por atención y otro por despacho si hay cambio de proceso; no crear además un coste de retorno sin acordarlo. Las instrucciones duran un tick, por lo que una interrupción no corta una instrucción en cuartos. Si el 1/4 se refiere a instrucciones del proceso o a consumo del quantum, sí puede interrumpirse entre instrucciones. Los eventos simultáneos incluyen fin de E/S, fin de quantum y vencimiento de deadline en la misma frontera; falta fijar precedencias y tratamiento de múltiples interrupciones. El reloj y la E/S siguen avanzando durante ticks de SO. A05/A14 y los contratos afectados deberán respetar esta elección cuando se cierre.

**Revisión de A06 por César:** no se acepta todavía retorno gratuito al mismo proceso. Separar tres conceptos antes de fijar costes: cambio de modo usuario↔SO; trabajo de atención de interrupción/llamada; cambio de contexto entre procesos. Comparar costes agrupados por episodio, un tick por cada transición/trabajo, o costes configurables. Si el SO ya está ejecutando, atender otro evento no implica otra entrada desde usuario. Volver al mismo proceso sí cambia de modo, pero no cambia de identidad de proceso. Debe evitarse cobrar dos veces el mismo trabajo dentro de despacho y cambio de contexto. Actualización posterior: César acepta costes separados y fijos, con base mínima de un tick por acción. Cada cambio de modo, atención y cambio de proceso consume tiempo y CPU; no puede coexistir con una instrucción de usuario en ese mismo tick de esa CPU. El desglose exacto de acciones se concretará al definir contratos, evitando duplicar despacho/cambio de contexto. Esta elección sustituye la propuesta anterior de retorno gratuito.

### A07 — ¿Qué hacen PC y MAR cuando un semWait bloquea? · D-04

| Camino | Pros | Contras |
|---|---|---|
| A. Avanzar al emitir la instrucción y guardar una continuación pendiente | No cobra de nuevo la misma instrucción | Hay que distinguir PC siguiente de operación todavía pendiente |
| B. Mantener PC en semWait y avanzarlo cuando recibe el permiso | La espera queda asociada a su dirección | Avanza al despertar sin nueva instrucción; hay que explicar esa convención |
| C. Reintentar semWait al volver a CPU, con permiso reservado | Traza explícita del reintento | Añade un ciclo y exige impedir doble adquisición |

**Recomiendo A como interpretación**, no como aclaración recibida: el intento consume un ciclo; al despertar se completa la espera sin volver a descontar el permiso. PC/MAR no avanzan mientras está listo, bloqueado o nuevo. El texto “por cada ciclo del reloj” requiere esta precisión para que los procesos no ejecuten mientras esperan. La GUI debe separar instrucción ejecutada, siguiente PC y operación pendiente. Afecta P05/P07/P16.

**Explicación de contexto (elección de PC siguiente registrada debajo).** `semWait` solicita un permiso: si existe lo adquiere, y si no, el proceso queda bloqueado en la cola del semáforo y libera la CPU. `semSignal` libera/notifica un permiso y puede habilitar a un esperador. Ejemplo: un consumidor pide un elemento de un buffer vacío; espera hasta que un productor inserte y señalice. A07 decide cómo representar el avance de esa instrucción en PC/MAR y su continuación, no si el proceso debe quedarse gastando CPU mientras espera. La representación concreta de la espera sigue pendiente.

**Elección de César: PC apunta a la siguiente instrucción (A), 03-10-2026.** Falta acordar dónde y cómo representar la operación pendiente. En A, PC=21 identifica la instrucción posterior a semWait en 20, no un espacio vacío ni el código del manejador del SO. El estado Bloqueado impide ejecutar 21 hasta obtener el permiso y ser seleccionado. Si 20 era la última instrucción, 21 puede representar fin de secuencia: no se debe acceder allí como instrucción ni terminar con una espera aún pendiente. En B, conservar 20 exige distinguir semWait pendiente de una instrucción todavía no iniciada, para no adquirir dos veces. La dirección de la última instrucción ejecutada y la próxima deben etiquetarse claramente en la GUI; MAR requiere su propia convención, no se deduce automáticamente del PC.

**Propuesta de representación, todavía no elegida:** ampliar la información de bloqueo de A02 en el PCB con una referencia a la espera pendiente: clase de operación (semWait) y semáforo esperado. El PCB identifica qué le impide continuar; la cola del semáforo registra a quién debe conceder permiso. No hace falta duplicar el contador ni inventar un segundo PC. Al conceder el permiso, la transición coordinada retira la espera, registra la adquisición y pasa a Listo; el PC conserva la dirección siguiente. La adquisición debe quedar registrada para cancelación/limpieza (A15), aunque ya no exista espera pendiente. El formato concreto y quién realiza cada paso quedan para contratos de funciones.

### A08 — ¿Cómo hacer reproducible la concurrencia entre computadores?

| Camino | Pros | Contras |
|---|---|---|
| A. Un hilo simulador avanza computadores en orden fijo, separado de la GUI | Coordinación sencilla | Menos paralelismo; posible sesgo estable en accesos simultáneos |
| B. Hilo por computador, fases coordinadas y arbitraje explícito de recursos | Representa concurrencia; cada máquina cumple su tick | Coordinación y pruebas más difíciles |
| C. Hilo por proceso simulado, además de computadores | Correspondencia visual con procesos | Mucha coordinación; fácil confundir espera Java con bloqueo simulado |

**Recomiendo B**, sujeto a definir sus contratos juntos en P07. Las fases por sí solas no resuelven dos accesos al mismo buffer: también hay que acordar su orden (A24). Los semáforos Java protegen estructuras; no deben dejar detenida una CPU simulada porque un proceso espera un elemento. A es una simplificación posible si se acepta que cumple el uso de hilos exigido. Afecta P04/P07/P16.

**Elección de César: B (03-10-2026), con arbitraje pendiente.** A05 describe el orden lógico del mundo simulado; A08 trata cómo los hilos Java ejecutan ese mundo. El reloj coordina que cada computador complete su paso antes de avanzar al siguiente tick. Si dos o más computadores solicitan el mismo permiso en ese tick, la exclusión mutua evita corromper el estado pero no garantiza siempre el mismo ganador: hace falta una regla explícita de arbitraje (A24). César propone evaluar la prioridad del PCB; no está elegida y también necesitaría un desempate para prioridades iguales. La espera Java de coordinación es distinta de la espera de un proceso simulado: esta última debe permitir que su CPU atienda otro proceso.

**Alcance acordado para A08:** César difiere el detalle de combinaciones y colisiones al trabajo de implementación. No se exige enumerarlas todas ahora para avanzar en P01. Al abordar cada recurso compartido, definir su arbitraje y los casos simultáneos relevantes antes de implementar sus funciones; incluir nuevos casos a medida que aparezcan. La prioridad del PCB es sólo una candidata.

## 3. Trabajo de cada tipo de proceso

### A09 — ¿Cómo representar las instrucciones? · D-05

| Camino | Pros | Contras |
|---|---|---|
| A. Lista explícita generada a partir de parámetros | PC identifica claramente cada instrucción; fácil mostrarla | Hay que construir y guardar la secuencia con estructuras permitidas |
| B. Generar la siguiente operación desde contadores y fase | Menos almacenamiento; patrones compactos | Relacionar fase, PC y próxima instrucción requiere más cuidado |
| C. Lenguaje de instrucciones escrito por el usuario | Muy flexible | Añade parser, validación y herramienta de edición |

**Recomiendo A para cargas acotadas de P1.** Generar patrones, no pedir al usuario que programe. Si se permiten cargas enormes, B gana sentido. La linealidad puede representarse desplegando las repeticiones de productor/consumidor. Afecta P05/P09/P18/P22.

**Elección de César: A (03-10-2026).** La imagen del proceso/programa almacena la secuencia completa de instrucciones. El PCB conserva el contexto, incluido el PC que identifica la próxima instrucción de esa secuencia; no necesita contener la secuencia en sí. Se prioriza claridad del simulador y de su defensa sobre ahorrar memoria mediante generación dinámica de instrucciones. Esto no obliga a simular bytes físicos ni cambia la contabilidad de RAM declarada; las reglas de memoria siguen siendo decisiones separadas. No se ha aprobado todavía una estructura concreta de clases o funciones.

### A10 — ¿Qué distingue CPU bound de I/O bound? · D-05/D-16

| Camino | Pros | Contras |
|---|---|---|
| A. Perfiles fijos: CPU puro frente a ráfagas cortas con E/S | Pocos campos; comparación repetible | Menos variedad; constantes elegidas por el equipo |
| B. Total de instrucciones, intervalo entre E/S y duración configurables | Expresa cargas distintas sin lenguaje nuevo | Más validaciones; combinaciones pueden contradecir la etiqueta del tipo |
| C. Ráfagas y duraciones aleatorias | Cargas variadas | Semillas, distribución y reproducibilidad complican la defensa |

**Recomiendo B con valores predeterminados por tipo.** Explicar qué significa “ciclos necesarios”: instrucciones consumen CPU; duraciones de E/S consumen tiempo de espera; el tiempo total real emerge del escenario. Acordar si la instrucción que solicita E/S pertenece al total y su modo antes de generar la secuencia; no tratar ambos contadores como equivalentes. Afecta P09/P22/P25.

**Preferencia de César (03-10-2026): creador por bloques y plantillas.** Poder componer bloques CPU y E/S de duración elegida y disponer de procesos preguardados para pruebas, desde cargas pequeñas hasta grandes. La secuencia completa de A09 tendrá longitud variable. Queda abierta cuánta libertad ofrecer y qué instrucciones insertar automáticamente.

**Propuesta para revisar:** editor de bloques para CPU/I/O y generador guiado para productor/consumidor. El usuario elige trabajo útil, buffer y meta; el simulador expande el protocolo obligatorio de semáforos sin permitir reordenarlo arbitrariamente. Un bloque CPU de N ticks genera N instrucciones de trabajo; una E/S de L ticks expresa duración del servicio y no L instrucciones de CPU ni L avances de PC durante bloqueo. Falta acordar la representación/coste de solicitar E/S en coordinación con A06. Conservar el tipo explícito requerido y validar coherencia del perfil; no inventar todavía un umbral de clasificación automática.

**Límites por decidir:** máximo de instrucciones tras expandir bloques y repeticiones, límites de duraciones y conteos, relación de memoria declarada con longitud del programa (sin asumir un tick = una unidad de RAM). Propuesta: editar antes de crear, congelar la secuencia al crear el proceso y generar otros procesos durante la ejecución; edición de un proceso ya en marcha no está solicitada. El presupuesto de instrucciones generado debe hacerse visible y coherente con A12. Plantillas de prueba son una ayuda de entrada, no otra política del SO. Estas propuestas no aprueban todavía la interfaz ni sus contratos.

**Acuerdo de César (03-10-2026):** de cara al editor de trabajo, el usuario sólo compone consumo de CPU y E/S; los protocolos de semáforos se agregan automáticamente según el tipo y los parámetros del proceso. El formulario conserva los atributos requeridos (tipo, buffer/meta cuando corresponda, etc.). Validar límites de ticks para acotar programas; el valor y si se limita por bloque, por total o ambos quedan pendientes. Una vez generado, el tamaño del proceso queda fijo. Conservar las plantillas previamente solicitadas. La relación entre instrucciones y RAM simulada se desarrolla en A16.1; la decisión posterior exige memoria derivada y no editable, con factor exacto pendiente.

### A11 — ¿“Cada N ciclos produce/consume” mide qué? · D-06

| Camino | Pros | Contras |
|---|---|---|
| A. N instrucciones propias de trabajo útil entre operaciones | No progresa mientras espera; fácil asociar a CPU | El intervalo global puede ser mucho mayor que N |
| B. N ticks globales desde la operación anterior | Modela una cadencia externa | Debe definirse qué hacer con operaciones atrasadas |
| C. N ciclos de CPU incluyendo primitivas SO | Cuenta todo el servicio recibido | Mezcla trabajo útil con protocolo; intervalos pequeños son problemáticos |

**Recomiendo A.** En productor, preparar el elemento precede al protocolo; en consumidor, el trabajo de consumir sigue a la extracción y señales, como en el anexo. Los cinco pasos de sincronización/mutación añaden sus propios ciclos. No prometer una producción cada N ticks globales. Afecta P18/P22.

**Elección de César: A (03-10-2026), contrastada con el enunciado.** N representa instrucciones de trabajo útil por elemento, no un periodo garantizado de reloj global. El anexo sitúa producir() antes del protocolo y consumir() después; desarrollar ese trabajo como N instrucciones respeta ese orden. Las cinco operaciones de semáforos/mutación conservan su coste de una instrucción cada una (transcripción, líneas 102–104 y 178–196). Esperas y costes de A06 se añaden aparte. Las instrucciones de trabajo son consecutivas en el programa, pero su ejecución puede ser interrumpida por el planificador.

**Ambigüedad que permanece:** la línea 7 dice “cada cuántos ciclos produce o consume un elemento”, sin precisar si son ciclos útiles, todo el servicio CPU o reloj global. Por ello A es una interpretación coherente con el algoritmo, no una definición explícitamente confirmada por el enunciado o Ares. En GUI/informe denominar el parámetro “ciclos de trabajo por elemento” y explicar el coste adicional del protocolo. Para N=5, una repetición contiene 10 instrucciones (5 útiles + 5 del protocolo), sin contar otras instrucciones que eventualmente se acuerden; no prometer un elemento cada cinco ticks globales. Conservar esta precisión en D-06 y en la validación de P18/P22.

## 4. Terminación y deadlines

### A12 — ¿Cómo conviven cantidad de instrucciones y meta de elementos? · D-06

| Camino | Pros | Contras |
|---|---|---|
| A. Validar que ambos describan un programa completo coherente | Evita cortes normales a mitad del protocolo | Restringe combinaciones de entrada; requiere fórmula transparente |
| B. Terminar al alcanzar cualquiera de los dos límites | Conserva entradas independientes | Puede incumplir la meta de elementos; necesita limpieza anticipada |
| C. Considerar instrucciones un presupuesto máximo y exigir la meta dentro de él | Distingue éxito de agotamiento | Introduce un motivo adicional de fracaso no precisado en el texto |

**Recomiendo A**, explicitando la tensión del enunciado. Para un patrón con N instrucciones útiles y cinco primitivas por elemento, M elementos requieren M×(N+5) instrucciones, si no se añaden otras instrucciones. Mostrar el cálculo y validar el número declarado; no borrar silenciosamente ese campo. Terminar normalmente sólo al completar el último protocolo y trabajo útil correspondiente. Deadline conserva precedencia como causa forzada. Afecta P18/P22/P25.

### A13 — ¿Desde cuándo corre el deadline? · D-07

| Camino | Pros | Contras |
|---|---|---|
| A. Desde creación, en ticks globales | Incluye toda la espera y presión de RAM | Puede expirar sin ser admitido |
| B. Desde admisión en RAM | Compara procesos una vez admitidos | Oculta esperas arbitrarias en Nuevo |
| C. Fecha absoluta elegida por el usuario | Comparación directa para EDF | Crear procesos durante la ejecución resulta menos intuitivo |

**Recomiendo A:** el usuario declara duración D; al crear en t se obtiene vencimiento t+D. El restante se deriva del reloj y disminuye también en Nuevo/Listo/Bloqueado. D debe ser positivo. El almacenamiento de una fecha absoluta no convierte la experiencia de entrada en C. Afecta P05/P12/P13/P22.

### A14 — ¿Qué ocurre si termina justo al vencer? · D-07

| Camino | Pros | Contras |
|---|---|---|
| A. Aceptar finalización en la frontera exacta; después cancelar lo inconcluso | Permite usar todos los intervalos concedidos | Hay que fijar el orden de cierre |
| B. Vencer antes de aceptar el resultado de esa frontera | Límite estricto fácil de describir | Puede quitar el último intervalo que el usuario creía disponible |
| C. Contar fin y vencimiento simultáneos como categoría propia | Conserva ambos hechos | Complica la tasa de cumplimiento sin gran beneficio |

**Recomiendo A.** Creado en t=0 con D=1 puede completar una instrucción entre 0 y 1; si sigue inconcluso al llegar a 1, termina por deadline. Nunca ejecuta entre 1 y 2. Afecta P07/P12/P21.

### A15 — ¿Qué hacer si vence con permisos o mutex adquiridos? · D-13

| Camino | Pros | Contras |
|---|---|---|
| A. Registrar fase y recursos; el SO completa la limpieza pertinente | Conserva operaciones ya realizadas; corrección local | Es la decisión con más casos límite |
| B. Deshacer la operación en curso completamente | Apariencia de cancelación uniforme | Deshacer una inserción ya visible o consumo concurrente puede ser imposible sin más aislamiento |
| C. Registrar reservas como transacciones que sólo se publican al confirmar | Cancelación ordenada antes de confirmar | Introduce un modelo transaccional adicional al algoritmo de clase |

**Recomiendo A.** Antes de mutar, devolver reservas; después de mutar, conservar el efecto y reconciliar señales pendientes; liberar el mutex si lo posee y retirar esperas/eventos del proceso. La limpieza pertenece al SO, no permite al proceso vencido seguir ejecutando. Su coste debe concordar con A06. No basta con “liberar todos los semáforos”: podría inventar espacios o elementos. Esta decisión requiere tabla de fases y casos concretos antes de programar P12/P16/P18/P19.

## 5. Admisión y memoria de buffers

### A16 — ¿En qué unidad se mide la RAM? · D-08

| Camino | Pros | Contras |
|---|---|---|
| A. Unidades abstractas enteras | Aritmética simple y ninguna falsa precisión | Hay que explicar que no son bytes reales |
| B. Bytes o KiB simulados con contadores | Presentación familiar | Sugiere realismo que el modelo no proporciona |
| C. Marcos/páginas de tamaño fijo | Facilita una futura paginación | Adelanta decisiones de P2 y fragmentación interna |

**Recomiendo A.** Misma unidad para RAM, procesos y buffers; sin direcciones físicas ni particiones. Afecta P08/P17/P22.

#### A16.1 — ¿Cómo se relacionan instrucciones y RAM del proceso?

**Decisión de César (03-10-2026): consumo de proceso derivado y no editable.** El usuario configura la capacidad de RAM de cada computador y la carga CPU/E/S del proceso; el simulador calcula y muestra la memoria requerida a partir de la imagen completa generada, incluidas las instrucciones automáticas. No se permite introducir un consumo arbitrario ni agregar un excedente manual. Se descartan las propuestas previas de memoria declarada independiente o de un mínimo ampliable por el usuario.

La relación entre instrucciones y memoria se modelará mediante una simplificación documentada; tamaños variables por tipo de instrucción quedan fuera del alcance elegido. Tres variantes de cálculo consideradas:

| Camino | Pros | Contras |
|---|---|---|
| A. Una unidad de RAM por instrucción almacenada | Conteo transparente y fácil de comprobar | Abstrae datos, contexto y codificación física |
| B. Un tamaño uniforme K por instrucción almacenada | Permite expresar otra escala de memoria | K sólo reescala el modelo si no hay otros costes |
| C. Tamaño uniforme por instrucción más coste fijo de contexto | Hace visible que el proceso tiene contexto además de programa | Añade una constante y obliga a definir cuándo/dónde se cobra |

**Recomiendo A; factor exacto y coste de contexto todavía pendientes de confirmación.** Memoria de imagen = número de instrucciones generadas × una unidad abstracta. No es una afirmación sobre tamaños de instrucciones reales. La duración de E/S es un parámetro de su operación, no una instrucción nueva por cada tick de espera; los ticks de cambio de modo/atención tampoco añaden por sí solos instrucciones a la imagen. La representación de solicitud de E/S y las instrucciones automáticas deben fijarse antes de cerrar ejemplos numéricos. El tamaño queda fijo al crear el proceso; la ocupación efectiva de RAM ocurre al admitirlo según las reglas de memoria.

**Tiempo y memoria se mantienen separados:** trabajo de CPU y servicio de E/S describen demanda temporal; instrucciones almacenadas determinan el tamaño bajo esta convención. El tiempo hasta terminar incluye además espera en colas, semáforos, red y costes del SO, por lo que no debe mostrarse la suma de bloques como una duración total garantizada.

**Confirmación previa acordada por César (03-10-2026):** antes de crear el proceso, mostrar la memoria calculada de la imagen completa, incluidas las instrucciones automáticas, y solicitar confirmación como parte del formulario. El usuario define la carga CPU/E/S y los demás parámetros aplicables; revisa el tamaño resultante y acepta o vuelve a editar el programa. No hay campo de memoria del proceso editable ni se permite declarar un valor que contradiga el cálculo. La confirmación pertenece al flujo del producto, no implica solicitar autorización adicional para documentar o desarrollar cada paso.

**Interpretación del enunciado que se documentará:** la memoria requerida se define indirectamente mediante la carga configurada y se acepta explícitamente antes de crear el proceso. Esto mantiene coherencia entre imagen y consumo: por ejemplo, 500 instrucciones de CPU no pueden declararse como dos unidades de RAM bajo la propuesta 1:1. Los ticks de espera de E/S no equivalen a instrucciones almacenadas. El enunciado presenta la memoria como una característica definida por el usuario; este mecanismo es la interpretación elegida por César, no una confirmación atribuible a Ares. Factor exacto por instrucción y tratamiento del contexto siguen pendientes. El checklist permanece intacto. Afecta P05/P08/P09/P18/P22/P25.


### A17 — ¿Cuánta memoria ocupa un buffer? · D-08

| Camino | Pros | Contras |
|---|---|---|
| A. Una unidad por casilla, reservada al crearlo | Relación capacidad/RAM clara | Omite metadatos y tamaños variables |
| B. Capacidad × tamaño de elemento configurable | Permite comparar elementos distintos | Añade un parámetro sin utilidad central |
| C. Cabecera fija + capacidad × tamaño | Separa estructura de contenido | Más constantes arbitrarias que defender |

**Recomiendo A:** un buffer de capacidad 8 reserva 8 unidades aunque esté vacío. No reservar sólo por elementos presentes: la capacidad debe seguir disponible. Afecta P17/P22.

### A18 — ¿A quién admitir cuando se libera RAM? · D-12

| Camino | Pros | Contras |
|---|---|---|
| A. FIFO estricto; si la cabeza no cabe, nadie pasa | Fácil; respeta antigüedad | Puede dejar RAM libre mientras otros sí caben |
| B. Recorrer por antigüedad y admitir los que quepan | Aprovecha RAM disponible | Un proceso grande puede esperar indefinidamente |
| C. B con reserva progresiva o envejecimiento | Reduce postergación de procesos grandes | Añade política, umbral y nuevas pruebas |

**Recomiendo B para P1**, admitiendo sucesivamente mientras quede capacidad, y haciendo visible la espera de los grandes. La posibilidad de inanición debe aparecer en las conclusiones; no prometer equidad de admisión. Afecta P08.

### A19 — ¿Qué hacer con solicitudes que no pueden reservar RAM? · D-08/D-12

| Camino | Pros | Contras |
|---|---|---|
| A. Rechazar procesos mayores que RAM total y buffers sin espacio libre | Errores claros; no crea objetos imposibles o buffers pendientes | No admite reservar un buffer para más tarde |
| B. Permitir todo y dejar solicitudes esperando | Flujo uniforme | Algunas nunca podrán satisfacerse; añade cola de creación de buffers |
| C. Simular la carga inicial completa y exigir que todo quepa | Escenarios iniciales previsibles | Impide explorar procesos en Nuevo por falta de RAM |

**Recomiendo A.** Un proceso que cabe en la RAM total pero no en la libre entra en Nuevo. Si buffers residentes hacen imposible admitirlo en la configuración actual, mostrar advertencia concreta; no cambiarle de computador. Definir el orden de carga inicial: primero reservar buffers y después crear procesos. Afecta P08/P17/P22/P25.

## 6. Sincronización y acceso remoto

### A20 — ¿Cómo expresar permisos y espera de un semáforo simulado?

| Camino | Pros | Contras |
|---|---|---|
| A. Conteo no negativo de permisos libres + cola de espera | GUI intuitiva; distingue permisos de procesos esperando | Entregar a un esperador requiere transferencia explícita |
| B. Contador firmado; valor negativo representa espera | Modelo compacto si se mantiene su invariante | Explicación de valores menos intuitiva en GUI |
| C. Permisos como objetos de reserva individuales + cola | Propiedad y cancelación muy visibles | Más objetos y bookkeeping para un semáforo sencillo |

**Recomiendo A**, más registro de permisos adquiridos por cada proceso para A15. Al señalizar con esperadores se concede a uno, sin dejar que otro recién llegado robe ese permiso. Esto describe semáforos de ÁvilaOS; los Semaphores de Java siguen protegiendo el acceso concurrente a sus estructuras. Afecta P16.

### A21 — ¿En qué orden despiertan los que esperan un semáforo?

| Camino | Pros | Contras |
|---|---|---|
| A. FIFO por llegada a ese semáforo | Simple; espera local predecible | No favorece deadlines cercanos |
| B. Según prioridad del proceso | Atiende urgencia declarada | Puede postergar prioridades bajas; cambia la comparación de políticas CPU |
| C. Según deadline más próximo | Favorece urgencia temporal | Añade otra política EDF independiente de la CPU |

**Recomiendo A.** Despertar significa pasar a Listo, no recibir inmediatamente CPU: eso depende del planificador del computador de origen. Afecta P16/P18.

### A22 — ¿Cuántas veces se cobra latencia por operación remota? · D-11

| Camino | Pros | Contras |
|---|---|---|
| A. Una vez por operación lógica de producir/consumir | Fácil de configurar y comparar | Abstrae todos los mensajes del protocolo |
| B. Una vez por primitiva remota | Distingue cada interacción con el anfitrión | Multiplica bloqueos y coste respecto a A |
| C. Solicitud y respuesta por cada primitiva | Mayor detalle de comunicación | Mucho estado de red para un objetivo de sincronización |

**Recomiendo A como interpretación explícita de “acceso”.** Las primitivas siguen siendo instrucciones separadas; sólo se agrupa su coste de red. Si Ares exige “acceso” por primitiva, cambia a B y revisa P19 y métricas. Afecta P19/P22/P25.

### A23 — ¿En qué momento se aplica esa latencia? · D-11

| Camino | Pros | Contras |
|---|---|---|
| A. Antes del primer semWait de la operación | No retiene permisos mientras espera red | Abstrae el regreso de la respuesta |
| B. Tras adquirir permisos, antes de mutar | Representa transferencia con recurso reservado | Ocupa mutex/cupo durante red y aumenta bloqueos |
| C. Tras completar el protocolo, como espera de respuesta | Libera pronto los recursos del buffer | El efecto ya existe aunque el proceso no haya recibido confirmación |

**Recomiendo A junto con A22-A.** Productor: prepara elemento, espera red, comienza protocolo. Consumidor: espera red, comienza protocolo, luego consume. Después de la espera conserva una marca de operación habilitada para no cobrarla otra vez. Latencia L significa L intervalos completos de bloqueo; L=0 no añade espera. Afecta P19.

### A24 — ¿Quién gana dos accesos al mismo recurso en el mismo tick?

| Camino | Pros | Contras |
|---|---|---|
| A. Orden fijo por ID de computador/proceso | Determinista y fácil de reproducir | Sesgo repetido hacia IDs pequeños |
| B. Turno rotativo entre computadores; orden estable dentro de cada uno | Reproducible; reparte oportunidades | Necesita un cursor de arbitraje |
| C. Orden real de llegada de los hilos Java | Menos lógica de desempate | Resultados dependientes del equipo y de su carga |

**Recomiendo B para solicitudes nuevas simultáneas.** No reemplaza el FIFO de quienes ya estaban esperando (A21) ni permite más de una instrucción por CPU/tick. Este orden arbitra efectos compartidos, no decide la planificación local. Afecta P07/P16/P19.

## 7. Planificación y métricas

### A25 — ¿Planificar tres o cuatro políticas? · D-01

| Camino | Pros | Contras |
|---|---|---|
| A. Preparar las cuatro nombradas | Cubre la enumeración completa | Mayor trabajo |
| B. Implementar sólo tres, eligiendo cuáles con confirmación | Reduce alcance si lo autorizan | Sin respuesta explícita puede omitir una obligación |
| C. Diseñar contrato para cuatro y priorizar tres provisionalmente | Permite arrancar sin esperar | La cuarta sigue pendiente; no permite declarar proyecto completo |

**Recomiendo A como alcance y C como orden de trabajo si hace falta.** El texto dice “mínimo 3” pero enumera cuatro; eso no lo resolvemos cambiando el checklist. Afecta P10/P11/P13/P14.

### A26 — ¿EDF puede interrumpir al proceso actual?

| Camino | Pros | Contras |
|---|---|---|
| A. EDF apropiativo en fronteras de tick | Reacciona a deadlines más próximos | Más cambios de CPU |
| B. EDF no apropiativo | Menos cambios; implementación sencilla | Un proceso largo puede demorar a uno urgente |
| C. Ambas variantes seleccionables | Permite comparación | Amplía configuración y pruebas |

**Recomiendo A**, con vencimiento absoluto (A13) y sin cortar una instrucción a la mitad. No atribuir esta elección al texto: sólo especifica apropiación expresamente para prioridades. Afecta P13.

### A27 — ¿Cómo desempatar y representar prioridad?

| Camino | Pros | Contras |
|---|---|---|
| A. Empates por entrada a Listo y luego ID; prioridad entera menor = mayor urgencia | Determinista; trata por orden a equivalentes | Hay que explicar el sentido numérico y cuándo se renueva la llegada |
| B. Empates por creación y luego ID; prioridad mayor = mayor urgencia | Favorece procesos antiguos | Un recién despertado antiguo puede adelantarse a quien esperaba |
| C. Empates rotativos/aleatorios con semilla y niveles nominales | Reparte oportunidades o simplifica etiquetas | Más estado o menos granularidad de prioridad |

**Recomiendo A**, con rango positivo documentado y finito. Reingresar a Listo obtiene un nuevo orden de llegada; empatar con el proceso en CPU no lo expulsa por sí solo. FCFS y RR conservan sus reglas de cola: no se reemplazan por un orden universal. Si el equipo prefiere prioridad alta = número alto, puede combinar esa convención con el mismo desempate. Afecta P10/P13/P14/P22.

### A28 — ¿Cuándo surte efecto cambiar la política? · D-10

| Camino | Pros | Contras |
|---|---|---|
| A. En la próxima frontera, reevaluando también al proceso actual | Efecto rápido y coherente con la nueva política | Hay que conservar el contexto y decidir su posición de reinserción |
| B. En el próximo despacho natural | Cambio sencillo | GUI muestra política nueva mientras continúa una decisión de la anterior |
| C. En un punto seguro elegido al pausar/reanudar | Revisión visual fácil | Añade intervención del usuario y retrasa cambios |

**Recomiendo A.** Cambiar no implica expulsar siempre: se aplica la regla nueva al contexto actual. Definir por transición el tratamiento del proceso en CPU; por ejemplo, al entrar a RR iniciar un quantum nuevo. No reiniciar PC ni trabajo realizado. Afecta P15/P23.

### A29 — ¿Qué ocurre con el quantum en curso al cambiar su valor? · D-10

| Camino | Pros | Contras |
|---|---|---|
| A. El turno actual conserva su límite; nuevo valor desde el siguiente | Sencillo; no altera un turno ya iniciado | El cambio no se observa inmediatamente |
| B. Comparar consumo actual contra el nuevo límite en la siguiente frontera | Reacción rápida | Reducirlo puede causar expulsión inmediata |
| C. Reiniciar el turno con el quantum nuevo | Regla fácil de mostrar | Cambios repetidos pueden prolongar artificialmente al proceso actual |

**Recomiendo A.** La GUI diferencia valor configurado y restante del turno vigente. Contar las instrucciones de ese proceso en CPU, incluidas sus primitivas SO; bloquearse acaba el turno. Afecta P11/P15/P23.

### A30 — ¿Qué cuenta como throughput? · D-09

| Camino | Pros | Contras |
|---|---|---|
| A. Completados con éxito / ticks transcurridos | Coincide con “procesos completados”; fórmula sencilla | Promedio acumulado tarda en reflejar cambios |
| B. Completados con éxito / ticks de una ventana reciente | Refleja cambios de carga | Elegir ventana cambia el aspecto del resultado |
| C. Mostrar acumulado y ventana | Visión global y reciente | Más cálculos y explicaciones |

**Recomiendo A.** Deadline vencido no es trabajo completado: mostrarlo aparte. Global = suma de éxitos de todos los computadores / ticks globales, no promedio de throughputs. En tick cero mostrar “sin datos”. Afecta P21.

### A31 — ¿Cómo medir utilización de CPU? · D-09

| Camino | Pros | Contras |
|---|---|---|
| A. Ciclos ocupados en usuario o SO / ciclos disponibles | Mide ocupación total | Oculta cuánto es trabajo útil frente a protocolo |
| B. Sólo ciclos de usuario / ciclos disponibles | Muestra trabajo de usuario | Subestima ocupación; debería llamarse utilización útil |
| C. Total y desglose usuario/SO/ocioso | Explica sobrecarga sin ocultarla | Más contadores y presentación |

**Recomiendo C**, graficando utilización total acumulada por computador inicialmente. Global = suma de ciclos ocupados / suma de ciclos disponibles; con N máquinas presentes desde inicio y T ticks, denominador N×T. No contar espera de E/S/red como CPU ocupada. Afecta P21/P24.

### A32 — ¿Qué significa tiempo de respuesta? · D-09

| Camino | Pros | Contras |
|---|---|---|
| A. Creación → primera asignación de CPU | Incluye espera de admisión; cálculo simple | No describe cuánto tarda en terminar |
| B. Admisión → primera asignación de CPU | Aísla espera posterior a RAM | Oculta demora en Nuevo; debe etiquetarse claramente |
| C. Mostrar ambos, separando demora de admisión | Permite explicar el origen de la espera | Más indicadores para leer |

**Recomiendo A como indicador principal.** Promediar sólo procesos que hayan recibido CPU y mostrar cuántos aún no respondieron; no asignarles cero. Creación → finalización es otra métrica, tiempo de retorno. Global se calcula con suma de tiempos y cantidad de procesos, no promedio simple de promedios por máquina. Afecta P21.

### A33 — ¿Qué denominador usar para cumplimiento de deadline? · D-09

| Camino | Pros | Contras |
|---|---|---|
| A. Éxitos a tiempo / procesos ya finalizados | No penaliza procesos cuyo resultado aún no se conoce | Con pocas finalizaciones fluctúa mucho |
| B. Éxitos a tiempo / todos los creados | Progreso visible contra toda la carga | Confunde pendientes con incumplimientos |
| C. Medir sólo una cohorte cuando todos sus miembros terminen | Comparación de experimentos cerrados | No da una tasa final durante la ejecución |

**Recomiendo A** durante la simulación, mostrando también pendientes y tamaño de muestra. Con A14, completar en la frontera del deadline cuenta como éxito. Sin finalizados: “sin datos”. Global agrega numeradores y denominadores. Afecta P12/P21.

### A34 — ¿Qué entender por equidad? · D-09

| Camino | Pros | Contras |
|---|---|---|
| A. Comparar CPU recibida por cada proceso | Datos sencillos y visibles | Penaliza a quien necesita poca CPU o pasa tiempo en E/S |
| B. Comparar servicio recibido respecto a tiempo elegible para CPU | Distingue espera por planificación de bloqueo | Debe definirse elegibilidad y población medida |
| C. Comparar espera media, máxima y dispersión en Listo | Fácil de relacionar con inanición | Es un conjunto de indicadores, no un único índice |

**Recomiendo C para P1**, documentando “equidad observada mediante distribución de espera en Listo”, sin prometer igualdad. Medir espera acumulada por proceso en una misma cohorte/escenario y mostrar media, máximo y dispersión; entre escenarios usar la misma carga. Prioridades busca trato desigual deliberadamente: su dispersión no demuestra por sí sola un error. Si exigen un índice único, acordar fórmula y población antes de implementar. Afecta P21/informe.

### A35 — ¿Cómo promediar espera en semáforos? · D-09

| Camino | Pros | Contras |
|---|---|---|
| A. Tiempo total / episodios de bloqueo cerrados | Responde cuánto dura una espera típica | Procesos que esperan muchas veces pesan más |
| B. Tiempo total / procesos que han esperado en ese buffer | Responde cuánto acumula cada proceso afectado | Mezcla cargas de duración distinta |
| C. Mostrar promedio por episodio y acumulado por proceso | Evita confundir frecuencia y duración | Añade un desglose |

**Recomiendo C.** Separar espera por semáforo de red y E/S. Un episodio termina al recibir permiso o al cancelarse, guardando su causa. Las esperas abiertas se muestran con duración actual y no se hacen pasar por cerradas. Contar elementos producidos/consumidos cuando ocurre inserción/extracción efectiva, incluso si después vence el proceso: A15 conserva ese efecto. Afecta P16/P18/P19/P21.

## Cómo convertir esto en acuerdos

Consultar las decisiones según el paquete y recorrido en curso, usando la clasificación inicial. No continuar una entrevista lineal A01–A35. Registrar lo necesario para construir y verificar la siguiente parte, y revisar las hipótesis cuando las pruebas aporten evidencia.

Para registrar cada selección:

| Decisión | Elección registrada | Justificación o cambio | Quién/fecha | Paquetes afectados |
|---|---|---|---|---|
| A01 | B | Considerar P2 separando responsabilidades; concretar cómo | César / 03-10-2026 | P03/P05/P08 |
| A02 | A | Motivo en PCB; la transición a Bloqueado mantiene su coherencia | César / 03-10-2026 | P05/P09/P16/P19 |
| A03 | A con extensión opcional | Asignación manual inicial; permitir futura selección automática desde carga unificada, MUY OPCIONAL salvo indicación de Ares | César / 03-10-2026 | P22; límite de responsabilidades en P03 |
| A04 | B | Controlador por computador con operación activa y FIFO | César / 03-10-2026 | P03/P09 |
| A05 | En discusión | Avance discreto y determinismo; orden concreto pendiente | César / 03-10-2026 | P07 y componentes por ciclo |
| A06 | Costes separados y fijos | Base mínima de un tick por acción; cambios de modo, atención y cambios de proceso consumen CPU | César / 03-10-2026 | P07/P10/P16/P23 |
| A07 | A; representación de espera pendiente | PC siguiente; concretar registro de semWait pendiente en PCB | César / 03-10-2026 | P05/P07/P16 |
| A08 | B | Hilo por computador y coordinación por tick; arbitraje pendiente, evaluar prioridad del PCB | César / 03-10-2026 | P04/P07/P16/A24 |
| A09 | A | Secuencia completa en la imagen del proceso/programa; PC en PCB | César / 03-10-2026 | P05/P09/P18/P22 |
| A10 | Acordada: bloques CPU/E/S, plantillas y protocolos automáticos | Validar ticks; tamaño fijo tras generar; umbrales y fórmula RAM pendientes | César / 03-10-2026 | P09/P18/P22/P25 |
| A16.1 | Memoria derivada, no editable | Memoria calculada visible y aceptada antes de crear; tamaño uniforme, factor 1:1 recomendado y contexto pendientes; interpretación documentada | César / 03-10-2026 | P05/P08/P09/P18/P22/P25 |
| A11 | A | Trabajo útil por elemento; compatible con orden y costes del anexo, significado de ciclos documentado como interpretación | César / 03-10-2026 | P18/P22 |
| A12–A35 | Pendientes | Salvo acuerdo parcial A16.1 registrado arriba | — | Según cada ficha |

P01 entrega una línea base de alcance, responsabilidades y un recorrido inicial entendible, con pendientes asignados a sus paquetes. No exige resolver previamente todos los recorridos de buffers, red, deadlines y cambios de política. Estos se detallan y verifican al trabajar sus paquetes. Antes de cada implementación se acuerda con el estudiante el contrato de la función correspondiente; las pruebas pueden motivar revisiones explícitas del diseño. Este catálogo no prueba cumplimiento ni cierra por sí solo P01.
