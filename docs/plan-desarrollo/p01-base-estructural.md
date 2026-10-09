# P01 — Base estructural y clasificación de acuerdos

**Estado: base estructural cerrada por César el 03-10-2026 como versión inicial revisable.** No significa aprobación de todo el equipo, cierre completo de P01, implementación o validación mediante pruebas. No añade elecciones sobre clases concretas. El cierre aprobado comprende alcance y responsabilidades y se revisará cuando aparezca evidencia.

[P01](paquetes/P01.md) · [Catálogo de alternativas](p01-alternativas.md) · [Capas discutidas](../diagrama-capas.md) · [Checklist → paquetes](trazabilidad.md)

## Criterio de clasificación

- **Estructural:** qué piezas y datos existen, quién es responsable de ellos y cómo se relacionan. Cambiarlo suele afectar a varios componentes.
- **Comportamiento:** qué hace el simulador ante una situación; es observable y debe ser consistente entre componentes.
- **Implementación:** cómo se representa o programa lo anterior: campos exactos, estructuras de datos, firmas, mecanismos de coordinación y algoritmos internos.

Una decisión puede contener las tres cosas. No se traslada una regla observable a “implementación” sólo porque vayamos a resolverla más adelante.

## Lo que ya definimos, separado por naturaleza

Los IDs siguientes son requisitos relacionados, no casillas cumplidas. Los paquetes indican dónde se desarrollará el acuerdo; la ubicación principal de cada requisito sigue en trazabilidad.md.

| Acuerdo | Parte estructural definida | Comportamiento acordado o pendiente | Implementación diferida | Requisitos relacionados / paquetes |
|---|---|---|---|---|
| A01 — Preparar P2 | Separar admisión, gestión de memoria y estados; liberar memoria no debe estar inseparablemente ligado a destruir el proceso. No implementar P2 ahora | Suspensión y memoria virtual futuras sin definir | Contratos y clases que materializan esa separación | ARC-15, MEM-03, MEM-06 / P03, P05, P08. Extensibilidad es decisión de diseño, no requisito nuevo |
| A02 — Motivo de bloqueo | PCB conserva estado y motivo/recurso esperado; quien controla la transición mantiene su coherencia | Entrar/salir del bloqueo mantiene consistentes información y pertenencia a cola | Formato del dato o referencia, campos y operaciones coordinadas | EST-06, EST-07, PCB-06, GUI-13 / P05, P09, P16, P19, P23 |
| A03 — Destino del proceso | Selección de computador separada de admisión local y selección de CPU; extensión automática muy opcional | Usuario elige el computador, sin migración posterior | Interfaz concreta y futura política automática | PRO-20, PRO-21 / P03, P22 |
| A04 — E/S | Un controlador por computador, con operación activa y solicitudes pendientes | Una operación activa, FIFO; duración de servicio empieza al atenderla | Estructura propia de la cola y representación de progreso | ARC-22, PRO-09, PRO-13 / P03, P09. Controlador único/FIFO son simplificaciones elegidas |
| A05 — Tick | Reloj global compartido; avance discreto coordinado, ya exigido | Determinismo como intención; precedencias y visibilidad de efectos aún pendientes | Fases concretas, almacenamiento de eventos y coordinación | ARC-13, ARC-14, CIC-01 / P07 |
| A06 — Costes del SO | CPU representa ejecución de usuario y trabajo del SO, sin doble CPU oculta | Costes separados y fijos con base mínima de un tick; desglose sin duplicaciones pendiente | Representación del trabajo pendiente del SO y contadores | ARC-19, CIC-01, CIC-06, CIC-08, CIC-10, CIC-12 / P07, P10, P16, P23. Costes adicionales son convención elegida |
| A07 — PC siguiente | Contexto del proceso conserva PC; la espera pendiente es distinta de la próxima instrucción | PC apunta a la siguiente instrucción, pero el bloqueo impide ejecutarla; convención MAR y cierre de espera por concretar | Registro concreto de la continuación y permisos; propuesta de ubicarlo junto al motivo del PCB aún por detallar | PRO-23, CIC-03, CIC-04, CIC-05 / P05, P07, P16 |
| A08 — Hilos por computador | Un hilo Java por computador, coordinados por reloj global | Un paso por computador por tick; arbitraje de recursos compartidos pendiente | Mecanismo de coordinación, exclusión mutua y estructuras auxiliares | ARC-11, ARC-12, ARC-13, ARC-14 / P04, P07, P16 |
| A09 — Imagen completa | Programa/imagen con secuencia completa, separado conceptualmente del contexto PCB | Tamaño fijo una vez generado | Contenedor propio, representación de cada instrucción y acceso mediante PC | PRO-23, PRO-24, CIC-02 / P05, P09, P18 |
| A10 — Creador por bloques | Responsabilidad de generar el programa a partir de CPU/E/S y parámetros; protocolos automáticos; plantillas | Usuario configura trabajo y confirma; límites de ticks por definir, sin editar protocolos arbitrariamente | Formularios, expansión de bloques y validaciones concretas | PRO-01, PRO-04, PRO-07, PRO-12, PRO-13, PRO-14, PRO-15, PRO-18, PRO-19 / P09, P18, P22, P25 |
| A11 — Trabajo por elemento | No añade piezas nuevas | N significa trabajo útil CPU por elemento; protocolo y esperas aparte. Interpretación documentada, no confirmación de Ares | Generación de repeticiones en la secuencia | PRO-16, PRO-17, CIC-05, CIC-07, CIC-09, CIC-11 / P18, P22 |
| A16.1 — Memoria calculada | Generación produce una imagen cuyo tamaño se comunica a admisión/memoria; usuario no edita directamente ese consumo | Memoria calculada visible y aceptada antes de crear. Factor uniforme, propuesta 1:1 y coste de contexto aún pendientes | Cálculo y validaciones a partir de la representación elegida | PRO-05, PCB-09, MEM-01, MEM-03, MEM-04 / P05, P08, P09, P18, P22, P25 |

## Base estructural cerrada

### Computadores y coordinación

- Clúster de instancias de un mismo computador: CPU, RAM, SO, planificador y colas propios. Es obligación del enunciado (ARC-02, ARC-04–ARC-10), no una nueva elección pendiente.
- Reloj global coordina; un hilo Java por computador es la elección A08. El reloj no selecciona procesos.
- Un controlador de E/S por computador (A04). Conserva operaciones; el SO gestiona las transiciones de procesos al recibir finalizaciones.

### Procesos, programa y recursos

- Imagen/programa almacena la secuencia completa; PCB conserva contexto y motivo de bloqueo (A02/A07/A09). No implica una clase por concepto.
- Creador traduce la configuración CPU/E/S y parámetros aplicables a una secuencia con protocolos automáticos; la imagen creada queda fija (A10).
- Asignar computador, admitir en memoria y seleccionar CPU son responsabilidades distintas (A01/A03).
- El consumo de proceso se deriva de la imagen y se muestra antes de confirmar; la fórmula pertenece al contrato de memoria (A16.1).
- Buffers con anfitrión y residencia en su RAM, compartidos por procesos locales/remotos, sincronizados con semáforos: obligación del enunciado (BUF-05–BUF-07, BUF-08–BUF-11). Esto no decide aún costes de red ni orden de concesión.

### Separación de responsabilidades y extensión

- GUI configura y observa; la lógica de simulación permanece fuera de las ventanas (ARC-23 y diagrama ya discutido). Log/métricas observan, no toman decisiones de planificación.
- Políticas intercambiables mediante interfaz y componentes reactivos al reloj con interfaz común: obligaciones ARC-20–ARC-22. Sus firmas se diseñan en P03, no ahora.
- Semáforos del modelo representan permisos/esperas; semáforos Java protegen estructuras frente a hilos reales (ARC-12 y enunciado). No convertir la espera de un proceso simulado en la detención de toda su CPU.
- P1 conserva límites que permitan extender P2; no se incorpora suspensión/disco/memoria virtual ni asignación automática obligatoria (A01/A03).

**Resultado y cierre:** César aprobó cerrar esta base estructural y continuar con P02/P03; no es necesario resolver A12–A35 previamente. Esta base sirve para diseñar el primer conjunto de contratos. No afirma que la arquitectura esté validada ni impide modificarla razonadamente después de probar.

## Pendientes que no bloquean ese cierre estructural

| Pendiente | Naturaleza | Dónde resolverlo antes de depender de él |
|---|---|---|
| Precedencias del tick, desglose de costes SO y solicitud E/S | Comportamiento | P07/P09; comenzar con el recorrido CPU/E/S y ampliar casos |
| Fin normal, deadline y cancelación con recursos | Comportamiento | P05/P12/P18; A12–A15, sin aplazar la regla después de implementar su función |
| Unidad de memoria, coste de imagen/buffer y admisión | Comportamiento | P08/P17; A16–A19 |
| Concesión de permisos, arbitraje y latencia | Comportamiento; representación interna es implementación | P16/P18/P19; A20–A24 |
| Cantidad de políticas exigidas (contradicción 3/4) | Alcance del enunciado, no forma de la arquitectura | Mantener las cuatro nombradas visibles en el plan; resolver D-01/A25 antes de descartar alguna. Interfaz común permite avanzar |
| Desempates, apropiación EDF y cambios en ejecución | Comportamiento | P11/P13/P14/P15; A26–A29 |
| Definiciones y agregación de métricas | Comportamiento observable | P21; A30–A35. P20 registra lo necesario para el recorrido en construcción |
| Clases, campos, estructuras propias, coordinación y límites numéricos de entrada | Implementación o parámetros del contrato | En cada paquete con el mapa de función del estudiante y pruebas pequeñas |

La memoria derivada (PRO-05), instrucciones generadas desde bloques (PRO-04) y ciclos útiles por elemento (PRO-16/17) son interpretaciones que deben permanecer visibles. No se da por confirmado por Ares lo que sólo hemos justificado nosotros. No cambian silenciosamente el checklist.

## Control a partir de aquí

1. P01 conserva esta base y el registro de decisiones, sin entrevista obligatoria de 35 puntos.
2. P02 comprueba el entorno/proyecto existente. P03 detalla con el estudiante los contratos del siguiente recorrido, no todo el producto de una vez.
3. Cada cambio referencia su paquete Pxx, los IDs originales que atiende y, si corresponde, la decisión A que aplica. Los IDs relacionados de esta revisión no cambian sus propietarios originales.
4. Acordar diseño no equivale a requisito cumplido. Implementar, comprobar y poder explicar el resultado siguen siendo necesarios.

No se cierra ningún Issue, PR ni requisito mediante esta revisión documental. El cierre de la base estructural se distingue del cierre completo del paquete P01 y del acuerdo colectivo del equipo.
