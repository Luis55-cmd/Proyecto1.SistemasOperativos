# Proyecto 1 — Checklist atómico de requisitos de ÁvilaOS

**Fuente única de extracción:** [transcripción del enunciado](transcripcion.md).  
**Fecha de extracción:** 02-10-2026.  
**Estado:** primera extracción documental; no es una auditoría del código ni un diseño aprobado.

Cada casilla corresponde a una obligación o condición verificable. Todas comienzan sin marcar: no se ha comprobado implementación ni cumplimiento. Los IDs permiten vincular después requisitos con decisiones, funciones, pruebas o evidencia. Las repeticiones del enunciado se consolidan; una entrada de datos, su conservación y su visualización se separan porque pueden cumplirse de forma independiente.

Las fuentes se indican por grupo: **RF §n** significa la sección numerada de «Requerimientos funcionales». Las obligaciones que vienen del planteamiento, los supuestos, las consideraciones o el anexo también se incluyen. No se agregan funciones por costumbre —por ejemplo, botones de pausa o paso a paso— si el texto no las exige.

**Importante:** las políticas enumeradas se conservan individualmente, pero hay una contradicción pendiente en su cantidad (D-01). Las opciones, recomendaciones y exclusiones aparecen aparte, para que no se cuenten como funcionalidad obligatoria.

## Índice de bloques

- **ARC:** Arquitectura, diseño y reloj (23 casillas).
- **PRO:** Creación y modelo de procesos (26 casillas).
- **EST:** Estados y terminación (8 casillas).
- **PCB:** Campos mínimos del bloque de control (11 casillas).
- **PLA:** Políticas y colas de planificación (10 casillas).
- **BUF:** Buffers, distribución y latencia (15 casillas).
- **SEM:** Semáforos y comportamiento bloqueante (25 casillas).
- **MEM:** Admisión y memoria principal (9 casillas).
- **CIC:** Reglas de ejecución de la simulación (12 casillas).
- **GUI:** Interfaz gráfica y observabilidad (32 casillas).
- **CFG:** Configuración y persistencia (13 casillas).
- **MET:** Métricas de rendimiento (13 casillas).
- **TEC:** Equipo, tecnología y restricciones (8 casillas).
- **GIT:** Desarrollo y colaboración en GitHub (11 casillas).
- **DOC:** Informe y documentación (9 casillas).
- **ENT:** Entrega y defensa (11 casillas).

## Requisitos extraídos

### ARC — Arquitectura, diseño y reloj

**Fuente:** Planteamiento del Problema; RF §1; ¿Por dónde empezar?.

- [ ] **ARC-01** Construir un simulador de ÁvilaOS, no un sistema operativo real.
- [ ] **ARC-02** Soportar un clúster de al menos dos computadores simulados.
- [ ] **ARC-03** Permitir indicar la cantidad de computadores desde una vista de configuración.
- [ ] **ARC-04** Dotar a cada computador de su propia CPU simulada.
- [ ] **ARC-05** Dotar a cada computador de su propia RAM simulada.
- [ ] **ARC-06** Dotar a cada computador de su propio núcleo de sistema operativo.
- [ ] **ARC-07** Dotar a cada computador de su propio planificador.
- [ ] **ARC-08** Mantener las colas de procesos de cada computador separadas de las de los demás.
- [ ] **ARC-09** Representar todos los computadores como instancias de una misma clase.
- [ ] **ARC-10** Agregar computadores mediante nuevas instancias, sin duplicar el código del computador.
- [ ] **ARC-11** Usar threads de Java para la simulación.
- [ ] **ARC-12** Usar semáforos de Java para garantizar exclusión mutua sobre las estructuras compartidas entre threads reales.
- [ ] **ARC-13** Disponer de un reloj global que sincronice todos los computadores.
- [ ] **ARC-14** Hacer que cada computador avance exactamente un ciclo de ejecución por cada ciclo del reloj global.
- [ ] **ARC-15** Modelar la solución de forma estructurada y orientada a objetos.
- [ ] **ARC-16** Representar los estados de proceso mediante un enum.
- [ ] **ARC-17** Representar los tipos de proceso mediante un enum —exigido en el planteamiento, aunque no se repita en la lista mínima de RF §1—.
- [ ] **ARC-18** Representar las políticas de planificación mediante un enum.
- [ ] **ARC-19** Representar el modo del procesador —usuario o sistema operativo— mediante un enum.
- [ ] **ARC-20** Definir el comportamiento de las políticas de planificación mediante una interfaz.
- [ ] **ARC-21** Permitir agregar una política implementando/extendiéndose sobre esa abstracción, sin modificar el código del planificador.
- [ ] **ARC-22** Definir una interfaz común para los componentes que reaccionan al reloj, como CPU y DMA cuando corresponda.
- [ ] **ARC-23** Mantener la lógica del sistema fuera de las ventanas: la GUI observa y permite operar el simulador.

### PRO — Creación y modelo de procesos

**Fuente:** Planteamiento del Problema; RF §2–3 y §7.

- [ ] **PRO-01** Permitir al usuario crear procesos desde la interfaz (Vista 1).
- [ ] **PRO-02** Permitir crear procesos durante la ejecución de la simulación.
- [ ] **PRO-03** Permitir que el usuario indique el nombre del proceso.
- [ ] **PRO-04** Permitir que el usuario indique la cantidad de instrucciones del proceso.
- [ ] **PRO-05** Permitir que el usuario indique la memoria requerida por el proceso.
- [ ] **PRO-06** Permitir que el usuario indique la prioridad del proceso.
- [ ] **PRO-07** Permitir que el usuario seleccione el tipo de proceso.
- [ ] **PRO-08** Soportar procesos de tipo CPU bound.
- [ ] **PRO-09** Soportar procesos de tipo I/O bound.
- [ ] **PRO-10** Soportar procesos de tipo productor.
- [ ] **PRO-11** Soportar procesos de tipo consumidor.
- [ ] **PRO-12** Para un proceso CPU bound, permitir especificar los ciclos necesarios para satisfacerlo.
- [ ] **PRO-13** Para un proceso I/O bound, permitir especificar los ciclos necesarios para satisfacerlo.
- [ ] **PRO-14** Para un productor, permitir especificar el buffer con el que trabaja.
- [ ] **PRO-15** Para un consumidor, permitir especificar el buffer con el que trabaja.
- [ ] **PRO-16** Para un productor, permitir especificar cada cuántos ciclos produce un elemento.
- [ ] **PRO-17** Para un consumidor, permitir especificar cada cuántos ciclos consume un elemento.
- [ ] **PRO-18** Para un productor, permitir especificar cuántos elementos requiere producir para terminar.
- [ ] **PRO-19** Para un consumidor, permitir especificar cuántos elementos requiere consumir para terminar.
- [ ] **PRO-20** Asignar cada proceso a un computador mediante una modalidad admitida por el enunciado; ver D-02 sobre manual/automática.
- [ ] **PRO-21** Conservar el computador asignado durante toda la vida del proceso: no hay migración.
- [ ] **PRO-22** Modelar un único hilo de ejecución por proceso simulado.
- [ ] **PRO-23** Conservar el contador de programa propio de cada proceso.
- [ ] **PRO-24** Mantener los recursos de memoria propios de cada proceso.
- [ ] **PRO-25** Asignar al procesador el proceso —equivalente a su único hilo— como unidad de planificación.
- [ ] **PRO-26** Integrar el TCB en el PCB o mantener una asociación 1:1 entre ambos; no exige dos clases separadas.

### EST — Estados y terminación

**Fuente:** RF §2; Nota y supuestos; planteamiento.

- [ ] **EST-01** Incluir el estado Nuevo en el modelo de procesos.
- [ ] **EST-02** Incluir el estado Listo en el modelo de procesos.
- [ ] **EST-03** Incluir el estado Ejecución en el modelo de procesos.
- [ ] **EST-04** Incluir el estado Bloqueado en el modelo de procesos.
- [ ] **EST-05** Incluir el estado Terminado en el modelo de procesos.
- [ ] **EST-06** Identificar las variantes o motivos de bloqueo que se implementarán; ver D-03 sobre la redacción de estados bloqueados.
- [ ] **EST-07** Justificar las variantes o motivos de bloqueo implementados.
- [ ] **EST-08** Terminar un proceso cuando su deadline llegue a cero.

### PCB — Campos mínimos del bloque de control

**Fuente:** RF §2; contexto monohilo del planteamiento.

- [ ] **PCB-01** Incluir el ID del proceso en su PCB.
- [ ] **PCB-02** Generar dinámicamente el ID de cada proceso.
- [ ] **PCB-03** Garantizar que el ID sea único en todo el sistema distribuido.
- [ ] **PCB-04** Incluir el nombre del proceso en su PCB.
- [ ] **PCB-05** Incluir el computador en el que se ejecuta el proceso en su PCB.
- [ ] **PCB-06** Incluir el estado del proceso en su PCB.
- [ ] **PCB-07** Incluir el tipo del proceso en su PCB.
- [ ] **PCB-08** Incluir la prioridad del proceso en su PCB.
- [ ] **PCB-09** Incluir la memoria asignada al proceso en su PCB.
- [ ] **PCB-10** Incluir el deadline del proceso en su PCB.
- [ ] **PCB-11** Incluir el tiempo restante de ejecución del proceso en su PCB.

### PLA — Políticas y colas de planificación

**Fuente:** RF §3; RF §6–7; Nota y supuestos.

- [ ] **PLA-01** Implementar FCFS, política enumerada expresamente; su obligatoriedad individual queda sujeta a resolver D-01.
- [ ] **PLA-02** Implementar EDF, política enumerada expresamente; su obligatoriedad individual queda sujeta a resolver D-01.
- [ ] **PLA-03** Implementar Round Robin, política enumerada expresamente; su obligatoriedad individual queda sujeta a resolver D-01.
- [ ] **PLA-04** Permitir configurar el quantum de Round Robin.
- [ ] **PLA-05** Implementar prioridades apropiativas, política enumerada expresamente; su obligatoriedad individual queda sujeta a resolver D-01.
- [ ] **PLA-06** Programar el ordenamiento de la cola posterior a cada selección.
- [ ] **PLA-07** Mantener una política de planificación propia por computador.
- [ ] **PLA-08** Permitir cambiar la política de un computador durante la ejecución.
- [ ] **PLA-09** Hacer que el cambio de política de un computador sea independiente de las políticas de los demás.
- [ ] **PLA-10** Mantener una única cola de listos para el procesador de cada computador.

### BUF — Buffers, distribución y latencia

**Fuente:** Planteamiento; RF §4 y §7.

- [ ] **BUF-01** Permitir al usuario crear uno o más buffers.
- [ ] **BUF-02** Permitir crear buffers durante la ejecución de la simulación.
- [ ] **BUF-03** Permitir indicar la capacidad de cada buffer.
- [ ] **BUF-04** Limitar los elementos almacenados a la capacidad del buffer.
- [ ] **BUF-05** Permitir indicar el computador anfitrión de cada buffer.
- [ ] **BUF-06** Ubicar cada buffer en la RAM de su computador anfitrión.
- [ ] **BUF-07** Contabilizar el espacio del buffer como memoria ocupada de su anfitrión.
- [ ] **BUF-08** Soportar varios productores asociados a un mismo buffer.
- [ ] **BUF-09** Soportar varios consumidores asociados a un mismo buffer.
- [ ] **BUF-10** Soportar productores que acceden a un buffer en su mismo computador.
- [ ] **BUF-11** Soportar consumidores que acceden a un buffer en su mismo computador.
- [ ] **BUF-12** Soportar productores que acceden a un buffer en otro computador.
- [ ] **BUF-13** Soportar consumidores que acceden a un buffer en otro computador.
- [ ] **BUF-14** Permitir configurar la latencia de red en ciclos.
- [ ] **BUF-15** Bloquear al proceso por cada acceso a un buffer remoto durante la cantidad de ciclos indicada por la latencia de red.

### SEM — Semáforos y comportamiento bloqueante

**Fuente:** RF §4; planteamiento; anexo productor–consumidor.

- [ ] **SEM-01** Incluir semáforos como parte del modelo de ÁvilaOS; su implementación concreta queda a elección del equipo.
- [ ] **SEM-02** Sincronizar cada buffer mediante el algoritmo productor–consumidor de buffer acotado con semáforos.
- [ ] **SEM-03** Bloquear al productor que no puede continuar porque el buffer está lleno.
- [ ] **SEM-04** Bloquear al consumidor que no puede continuar porque el buffer está vacío.
- [ ] **SEM-05** Bloquear al proceso que no puede entrar porque la región crítica está ocupada.
- [ ] **SEM-06** Hacer pasar a Bloqueado a quien ejecuta semWait sin un permiso disponible.
- [ ] **SEM-07** Encolar al proceso bloqueado por semWait en la cola del semáforo correspondiente.
- [ ] **SEM-08** Liberar la CPU cuando su proceso se bloquea por semWait.
- [ ] **SEM-09** Permitir que la CPU atienda a otro proceso elegible después de ese bloqueo.
- [ ] **SEM-10** Disponer de un semáforo mutex s por buffer, inicializado en 1.
- [ ] **SEM-11** Disponer de un semáforo de elementos n por buffer, inicializado en 0.
- [ ] **SEM-12** Disponer de un semáforo de espacios e por buffer, inicializado con su capacidad.
- [ ] **SEM-13** En el productor, reservar un espacio con semWait(e) antes de adquirir el mutex con semWait(s).
- [ ] **SEM-14** En el productor, añadir el elemento después de adquirir el mutex.
- [ ] **SEM-15** En el productor, liberar el mutex con semSignal(s) después de añadir el elemento.
- [ ] **SEM-16** En el productor, señalar la disponibilidad del elemento con semSignal(n) después de liberar el mutex.
- [ ] **SEM-17** En el consumidor, reservar un elemento con semWait(n) antes de adquirir el mutex con semWait(s).
- [ ] **SEM-18** En el consumidor, extraer el elemento después de adquirir el mutex.
- [ ] **SEM-19** En el consumidor, liberar el mutex con semSignal(s) después de extraer el elemento.
- [ ] **SEM-20** En el consumidor, señalar un espacio disponible con semSignal(e) después de liberar el mutex.
- [ ] **SEM-21** Representar la producción del elemento antes de la secuencia de reserva e inserción del anexo.
- [ ] **SEM-22** Representar el consumo del elemento después de la secuencia de extracción y señalización del anexo.
- [ ] **SEM-23** Repetir el ciclo de cada productor mientras corresponda según sus límites de ejecución; ver D-06.
- [ ] **SEM-24** Repetir el ciclo de cada consumidor mientras corresponda según sus límites de ejecución; ver D-06.
- [ ] **SEM-25** Permitir la ejecución concurrente de productores y consumidores, como expresa paralelos(...) en el anexo.

### MEM — Admisión y memoria principal

**Fuente:** Planteamiento; RF §5; Nota y supuestos.

- [ ] **MEM-01** Permitir configurar el tamaño de RAM de cada computador.
- [ ] **MEM-02** Representar la RAM de cada computador como una capacidad limitada.
- [ ] **MEM-03** Admitir un proceso de Nuevo a Listo sólo si dispone de suficiente RAM en el computador asignado.
- [ ] **MEM-04** Reservar la memoria requerida al admitir el proceso —consecuencia directa de contabilizar su ocupación y liberarla al terminar—.
- [ ] **MEM-05** Mantener en la cola de nuevos al proceso que no puede admitirse por falta de memoria.
- [ ] **MEM-06** Liberar la memoria del proceso cuando termina.
- [ ] **MEM-07** Revisar la cola de nuevos para admitir procesos después de liberar memoria por una terminación.
- [ ] **MEM-08** Controlar la cantidad de memoria usada de cada computador.
- [ ] **MEM-09** Controlar la cantidad de memoria libre de cada computador.

### CIC — Reglas de ejecución de la simulación

**Fuente:** Nota y supuestos de simulación.

- [ ] **CIC-01** Hacer que cada instrucción se ejecute en un único ciclo de instrucción.
- [ ] **CIC-02** Modelar la ejecución de los procesos de manera lineal.
- [ ] **CIC-03** Incrementar el PC en una unidad por ciclo según el supuesto del enunciado; ver D-04 sobre el proceso al que se aplica.
- [ ] **CIC-04** Incrementar el MAR en una unidad por ciclo según el supuesto del enunciado; ver D-04.
- [ ] **CIC-05** Contabilizar cada semWait como una instrucción.
- [ ] **CIC-06** Ejecutar cada semWait en modo sistema operativo.
- [ ] **CIC-07** Contabilizar cada semSignal como una instrucción.
- [ ] **CIC-08** Ejecutar cada semSignal en modo sistema operativo.
- [ ] **CIC-09** Contabilizar cada inserción en un buffer como una instrucción.
- [ ] **CIC-10** Ejecutar cada inserción en un buffer en modo sistema operativo.
- [ ] **CIC-11** Contabilizar cada extracción de un buffer como una instrucción.
- [ ] **CIC-12** Ejecutar cada extracción de un buffer en modo sistema operativo.

### GUI — Interfaz gráfica y observabilidad

**Fuente:** Planteamiento (Vista 2); RF §6 y §8; Consideraciones / GUI.

- [ ] **GUI-01** Entregar una interfaz gráfica funcional; una solución sólo por consola no cumple.
- [ ] **GUI-02** Proporcionar una interfaz intuitiva —criterio cualitativo del enunciado—.
- [ ] **GUI-03** Visualizar los cambios de estado de procesos en tiempo real.
- [ ] **GUI-04** Mostrar el proceso que está ejecutándose en la CPU de cada computador.
- [ ] **GUI-05** Mostrar el valor del PC del proceso en ejecución en cada computador.
- [ ] **GUI-06** Mostrar la instrucción cuya dirección indica el PC, según el planteamiento; ver D-05 sobre su representación.
- [ ] **GUI-07** Mostrar la prioridad del proceso en ejecución en cada computador.
- [ ] **GUI-08** Mostrar el deadline del proceso en ejecución en cada computador.
- [ ] **GUI-09** Mostrar si cada CPU ejecuta en modo usuario o sistema operativo.
- [ ] **GUI-10** Mostrar la cola de nuevos de cada computador.
- [ ] **GUI-11** Mostrar la cola de listos de cada computador.
- [ ] **GUI-12** Mostrar la cola de bloqueados de cada computador.
- [ ] **GUI-13** Mostrar el motivo de bloqueo de cada proceso bloqueado.
- [ ] **GUI-14** Mostrar la cola de terminados de cada computador.
- [ ] **GUI-15** Reflejar inmediatamente en la GUI los cambios de ordenamiento de las colas.
- [ ] **GUI-16** Mostrar los elementos del PCB de cada proceso situado en las colas.
- [ ] **GUI-17** Mostrar los elementos del PCB del proceso situado en cada CPU.
- [ ] **GUI-18** Mostrar la memoria usada de cada computador.
- [ ] **GUI-19** Mostrar la memoria libre de cada computador.
- [ ] **GUI-20** Mostrar la política de planificación vigente en cada computador.
- [ ] **GUI-21** Proporcionar un selector de política para cada computador.
- [ ] **GUI-22** Mostrar globalmente el número de ciclo desde el inicio de la simulación.
- [ ] **GUI-23** Mostrar los elementos almacenados en cada buffer.
- [ ] **GUI-24** Mostrar la capacidad de cada buffer.
- [ ] **GUI-25** Mostrar los valores de los semáforos de cada buffer.
- [ ] **GUI-26** Mostrar los procesos bloqueados en cada semáforo de cada buffer.
- [ ] **GUI-27** Mostrar un log de eventos en formato de texto.
- [ ] **GUI-28** Registrar en el log cada decisión importante del sistema; los ejemplos del enunciado incluyen selección de CPU, bloqueo y acceso remoto.
- [ ] **GUI-29** Mostrar la utilización del procesador de cada computador con respecto al tiempo en un mismo gráfico.
- [ ] **GUI-30** Validar el tipo de dato en todos los campos de entrada.
- [ ] **GUI-31** Validar el rango en todos los campos de entrada.
- [ ] **GUI-32** Manejar entradas inválidas sin interrumpir el flujo del simulador.

### CFG — Configuración y persistencia

**Fuente:** RF §7; RF §1 y §5 para parámetros de la interfaz.

- [ ] **CFG-01** Permitir cambiar durante la ejecución la duración de un ciclo, expresada en milisegundos o segundos.
- [ ] **CFG-02** Permitir cambiar durante la ejecución el quantum de Round Robin.
- [ ] **CFG-03** Permitir introducir desde la GUI la duración del ciclo para la configuración persistente.
- [ ] **CFG-04** Permitir introducir desde la GUI el número de computadores para la configuración persistente.
- [ ] **CFG-05** Permitir introducir desde la GUI la RAM de cada computador para la configuración persistente.
- [ ] **CFG-06** Permitir introducir desde la GUI la política inicial de cada computador para la configuración persistente.
- [ ] **CFG-07** Permitir introducir desde la GUI el quantum para la configuración persistente.
- [ ] **CFG-08** Permitir introducir desde la GUI la latencia de red en ciclos para la configuración persistente.
- [ ] **CFG-09** Permitir introducir desde la GUI la carga inicial de procesos con todos sus atributos.
- [ ] **CFG-10** Permitir introducir desde la GUI la carga inicial de buffers con su capacidad.
- [ ] **CFG-11** Permitir introducir desde la GUI el anfitrión de cada buffer de la carga inicial.
- [ ] **CFG-12** Escribir la configuración indicada en un archivo CSV o JSON; basta uno de esos formatos.
- [ ] **CFG-13** Recuperar y utilizar la configuración guardada en simulaciones futuras.

### MET — Métricas de rendimiento

**Fuente:** Planteamiento; RF §8.

- [ ] **MET-01** Registrar el throughput por computador.
- [ ] **MET-02** Registrar el throughput del sistema completo.
- [ ] **MET-03** Registrar la utilización del procesador por computador.
- [ ] **MET-04** Registrar la utilización del procesador del sistema completo.
- [ ] **MET-05** Registrar el tiempo de respuesta promedio por computador.
- [ ] **MET-06** Registrar el tiempo de respuesta promedio del sistema completo.
- [ ] **MET-07** Registrar la tasa de cumplimiento de deadlines por computador.
- [ ] **MET-08** Registrar la tasa de cumplimiento de deadlines del sistema completo.
- [ ] **MET-09** Registrar la equidad por computador.
- [ ] **MET-10** Registrar la equidad del sistema completo.
- [ ] **MET-11** Registrar la cantidad de elementos producidos por buffer.
- [ ] **MET-12** Registrar la cantidad de elementos consumidos por buffer.
- [ ] **MET-13** Registrar el tiempo promedio que los procesos pasan bloqueados en semáforos.

### TEC — Equipo, tecnología y restricciones

**Fuente:** Consideraciones / Conformación de equipos y Tecnología y entorno.

- [ ] **TEC-01** Conformar un equipo de como máximo tres personas.
- [ ] **TEC-02** Desarrollar el proyecto en Java.
- [ ] **TEC-03** Usar una versión posterior a Java 21, según la redacción literal del enunciado.
- [ ] **TEC-04** Usar NetBeans como IDE requerido.
- [ ] **TEC-05** Garantizar la ejecución adecuada del proyecto en NetBeans.
- [ ] **TEC-06** Limitar las librerías externas a las categorías permitidas: gráficas, JSON/CSV, hilos y semáforos.
- [ ] **TEC-07** No utilizar colecciones del framework de Java, incluidas ArrayList, Queue, Stack y Vector.
- [ ] **TEC-08** Programar estructuras de datos propias para gestionar procesos y PCBs.

### GIT — Desarrollo y colaboración en GitHub

**Fuente:** Consideraciones / Estándares de desarrollo en GitHub.

- [ ] **GIT-01** Mantener el proyecto en un repositorio de GitHub.
- [ ] **GIT-02** No desarrollar únicamente sobre la rama main.
- [ ] **GIT-03** Contar con una rama develop.
- [ ] **GIT-04** Evidenciar el uso de ramas por funcionalidad.
- [ ] **GIT-05** Registrar las tareas pendientes mediante Issues de GitHub.
- [ ] **GIT-06** Registrar los errores encontrados mediante Issues de GitHub.
- [ ] **GIT-07** Realizar las fusiones de código entre ramas mediante Pull Requests.
- [ ] **GIT-08** Comentar los Pull Requests utilizados para integrar código.
- [ ] **GIT-09** Mantener una participación equilibrada de los integrantes reflejada en el historial de commits.
- [ ] **GIT-10** Escribir mensajes descriptivos en los commits.
- [ ] **GIT-11** Mantener un tamaño limitado de los commits; el enunciado no fija un umbral numérico.

### DOC — Informe y documentación

**Fuente:** Consideraciones / Documentación e informe.

- [ ] **DOC-01** Entregar un informe junto con el código.
- [ ] **DOC-02** Explicar en el informe la funcionalidad de las clases más importantes.
- [ ] **DOC-03** Explicar en el informe la funcionalidad de los métodos más importantes.
- [ ] **DOC-04** Describir en el informe los enums utilizados.
- [ ] **DOC-05** Describir en el informe las interfaces utilizadas.
- [ ] **DOC-06** Incluir dos diagramas UML que describan el sistema; el enunciado no identifica sus tipos.
- [ ] **DOC-07** Incluir conclusiones sobre el comportamiento del sistema con cada política de planificación implementada.
- [ ] **DOC-08** Incluir conclusiones sobre productores y consumidores con acceso local.
- [ ] **DOC-09** Incluir conclusiones sobre productores y consumidores con acceso remoto.

### ENT — Entrega y defensa

**Fuente:** Consideraciones / Entrega y evaluación; RF §1 y §3 para justificaciones condicionales.

- [ ] **ENT-01** Completar la entrega antes de las 7:00 AM del viernes de la Semana 7; no se infiere aquí una fecha de calendario.
- [ ] **ENT-02** Entregar el informe en formato PDF.
- [ ] **ENT-03** Enviar el informe a Gabriela Costa.
- [ ] **ENT-04** Enviar el informe a Ares Ramirez.
- [ ] **ENT-05** Enviar el enlace del repositorio GitHub a Gabriela Costa.
- [ ] **ENT-06** Enviar el enlace del repositorio GitHub a Ares Ramirez.
- [ ] **ENT-07** Registrar la entrega en el spreadsheet que se proporcionará.
- [ ] **ENT-08** Asistir todos los integrantes a la defensa presencial del viernes de la Semana 7.
- [ ] **ENT-09** Demostrar individualmente conocimientos sobre el proyecto durante la defensa.
- [ ] **ENT-10** Asegurar que cada integrante conozca el funcionamiento general de cada módulo de la solución.
- [ ] **ENT-11** Entregar un programa que se ejecute adecuadamente.

## Opciones y obligaciones condicionales

Estas casillas se evalúan sólo si el equipo elige la opción. No deben marcarse como incumplimiento cuando no aplica.

- [ ] **OPT-01 — Si se usa asignación automática:** investigar el criterio de asignación o balanceo elegido. Fuente: RF §3.
- [ ] **OPT-02 — Si se usa asignación automática:** justificar ese criterio en la defensa. Fuente: RF §3.
- [ ] **OPT-03 — Si se agregan otras interfaces:** justificar cada interfaz adicional en la defensa. Fuente: RF §1.

Permisos expresos: elegir CSV **o** JSON; elegir cómo implementar los semáforos de ÁvilaOS; utilizar librerías de gráficos permitidas; formar equipo con estudiantes de distintas secciones. Estos permisos no eliminan el uso obligatorio de threads y semáforos de Java ni las restricciones de librerías y colecciones.

## Recomendaciones del enunciado, separadas de la aceptación funcional

- **REC-01:** organizar el sistema por capas, ubicando mecanismos de hardware debajo de las decisiones del SO.
- **REC-02:** procurar que cada capa conozca únicamente la inferior, según la orientación de diseño del texto.
- **REC-03:** usar el computador como punto de ensamblaje de las piezas.
- **REC-04:** resolver primero un computador y después incorporar comunicación entre computadores.
- **REC-05:** distinguir semáforos de conteo y mutex; el anexo concreta sus funciones y orden.
- **REC-06:** preferir, cuando sea posible, compañeros de la misma sección; no es una prohibición de equipos mixtos.
- **REC-07:** usar la captura de GUI como referencia, sin obligación de copiar su estilo o distribución.

Fuente: ¿Por dónde empezar?, Conformación de equipos y Anexo de interfaz. Estas recomendaciones no prescriben aquí clases, métodos ni una arquitectura nueva.

## Exclusiones y alcance de la siguiente entrega

| ID | Alcance explícito | Fuente |
|---|---|---|
| EXC-01 | Proyecto 1 no exige representación de disco | Planteamiento |
| EXC-02 | Proyecto 1 no exige técnicas de memoria virtual | Planteamiento |
| EXC-03 | No se exige asignación contigua de memoria | Nota y supuestos |
| EXC-04 | No se exige implementar particiones de memoria | Nota y supuestos |
| EXC-05 | No hace falta documentar todo el código | Documentación e informe |
| EXC-06 | No se exige copiar el estilo de la GUI de referencia | Anexo de interfaz |
| FUT-01 | Proyecto 2 se construirá sobre el código entregado en Proyecto 1 | Continuidad con Proyecto 2 |
| FUT-02 | Proyecto 2 agregará gestión de disco, memoria virtual y suspensión | Continuidad con Proyecto 2 |

La ausencia de migración y la cola única de listos son restricciones obligatorias de Proyecto 1, no funcionalidades opcionales. No deben añadirse estados suspendidos a esta entrega sólo porque aparecen en las clases.

## Contradicciones y decisiones pendientes

Estas entradas no añaden requisitos. Registran lo que la transcripción no permite fijar sin interpretación o consulta. Resolverlas antes de que afecten contratos o pruebas.

| ID | Texto o problema | Qué falta resolver |
|---|---|---|
| D-01 | RF §3 dice «mínimo 3 políticas» y enumera FCFS, EDF, RR y prioridades apropiativas | Confirmar si deben implementarse las cuatro o se permite elegir tres. Las cuatro se conservan en PLA para evitar omisiones, sin resolver la contradicción silenciosamente |
| D-02 | El planteamiento permite al usuario elegir computador; RF §3 dice asignación manual o automática y llama opcional al balanceo | Confirmar si basta una modalidad o si el selector manual es obligatorio aun con asignación automática. No imponer ambas sin aclaración |
| D-03 | «Para los procesos bloqueados deben identificar y justificar los estados a implementar» | Precisar si se esperan subestados, motivos de bloqueo o estados adicionales; los motivos por semáforos y red sí aparecen expresamente |
| D-04 | PC y MAR incrementan «por cada ciclo del reloj»; procesos bloqueados/nuevos no ejecutan | Precisar a qué proceso se aplica el incremento, y cómo contar ciclos de SO, E/S y latencia; no incrementar automáticamente todos los procesos por una lectura literal aislada |
| D-05 | Se pide visualizar la instrucción cuya dirección contiene el PC, pero no se define repertorio completo | Acordar representación de instrucciones y perfiles CPU/I/O; no inventar un lenguaje de máquina como requisito |
| D-06 | Se indican cantidad de instrucciones, ciclos y cantidad de elementos para terminar; el anexo usa while(true) con límite descrito en prosa | Fijar qué límite termina productores/consumidores y cómo se relacionan trabajo, instrucciones e intervalos. También cómo actúa el deadline sobre esa terminación |
| D-07 | El deadline termina al llegar a cero | Fijar unidad, valor inicial, instantes de decremento, estados donde corre y orden cuando coincide con finalización normal; no está especificado completamente |
| D-08 | Buffers ocupan RAM, pero no hay fórmula de tamaño | Fijar unidad y costo por capacidad/elemento/metadatos, y comportamiento si no cabe un buffer nuevo |
| D-09 | Se enumeran métricas sin fórmulas completas | Definir respuesta, equidad, agregación global, denominadores y tratamiento de procesos abortados por deadline |
| D-10 | Se permite cambio de política y quantum en ejecución | Definir efecto sobre proceso actual, quantum en curso y orden de cola; no está descrito |
| D-11 | Cada acceso remoto implica latencia | Precisar qué cuenta como acceso: operación lógica sobre el buffer o cada primitiva; determinar orden de espera de red y adquisición de semáforos |
| D-12 | Debe revisarse admisión al liberar RAM | Fijar cómo elegir entre varios nuevos que caben y qué ocurre con solicitudes que nunca caben; no se establece orden de admisión |
| D-13 | Se pide terminar al vencer deadline incluso si usa recursos compartidos | Definir limpieza de esperas, permisos y recursos retenidos; el enunciado no detalla el protocolo |
| D-14 | Se exigen dos diagramas UML sin tipos concretos | Clases y secuencia son una elección del equipo. El diagrama de arquitectura adicional discutido no es un tercer UML obligatorio del texto |
| D-15 | «Viernes de Semana 7» y spreadsheet anunciado | Confirmar fecha de calendario y enlace/campos del spreadsheet; no suponerlos |
| D-16 | Se menciona DMA como ejemplo de componente reactivo | Aclarar el detalle esperado de E/S y si debe existir una entidad DMA explícita; no inferir una clase obligatoria sólo por el ejemplo |

## Consecuencias de evaluación declaradas

Se conservan como contexto de aceptación; no se duplican como nuevas funciones del simulador.

| Condición | Consecuencia indicada |
|---|---|
| Sin GUI o sólo consola | Calificación 0 |
| Sin repositorio GitHub | Calificación 0 |
| No ejecuta adecuadamente, incluido NetBeans | Calificación 0 |
| No realizado en Java | Calificación 0 |
| Integrante ausente en defensa | 0 para ese integrante |
| Integrante reprueba defensa | Nota máxima de 10 puntos para ese integrante, independientemente del código |
| Participación desequilibrada | Afecta la nota individual; no se indica fórmula |

El texto advierte que no se corregirá código para validar funcionamiento, aunque sí se revisará para comprobar comprensión. La ejecución y la defensa deben demostrar el trabajo.

## Cobertura de la extracción

| Parte del documento fuente | Destino en este checklist |
|---|---|
| Planteamiento del Problema | ARC, PRO, PCB, BUF, MEM, GUI, MET; exclusiones |
| RF §1: arquitectura | ARC; OPT-03 |
| RF §2: procesos | EST, PCB; D-03 y D-07 |
| RF §3: planificación | PLA, PRO; OPT-01–02; D-01–02 |
| RF §4: sincronización | BUF, SEM |
| RF §5: memoria | MEM |
| RF §6: interfaz | GUI |
| RF §7: configuración | PRO, BUF, PLA, CFG |
| RF §8: métricas | MET, GUI |
| Nota y supuestos | CIC, PLA, MEM; EXC-03–04 |
| ¿Por dónde empezar? | ARC, recomendaciones |
| Continuidad con Proyecto 2 | FUT-01–02 |
| Consideraciones: equipo y tecnología | TEC, permisos |
| Consideraciones: GitHub | GIT |
| Consideraciones: GUI | GUI |
| Consideraciones: documentación | DOC; EXC-05 |
| Consideraciones: entrega y evaluación | ENT, consecuencias; D-15 |
| Anexo productor–consumidor | SEM; D-06 |
| Anexo de interfaz | REC-07; EXC-06 |

## Cómo registrar cumplimiento

Marcar una casilla cuando exista evidencia suficiente para esa obligación. Al revisarla, se puede agregar al final de la línea una referencia a la prueba, captura, función, documento o acción de entrega que la demuestra. No marcar un requisito sólo porque esté nombrado en el diseño. Las decisiones pendientes pueden enlazarse por su ID sin alterar el texto fuente.
