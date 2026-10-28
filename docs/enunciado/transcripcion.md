# Planteamiento del Problema

Una pequeña empresa de tecnología ha decidido construir **ÁvilaOS**, un sistema operativo distribuido sencillo que coordine un pequeño clúster de computadores. Ustedes han sido contratados como el equipo de diseño del sistema operativo: no tienen que programar un sistema operativo real, sino construir un simulador que modele con cierto grado de fidelidad cómo funcionaría un prototipo de ÁvilaOS sobre varias máquinas.

El objetivo de este proyecto es que los estudiantes desarrollen un simulador que les permita comprender y aplicar los conceptos de **procesos y sincronización** en un sistema compuesto por **al menos dos computadores**. Cada computador simulado tendrá sus propios componentes: **procesador (CPU), memoria principal (RAM) y su propio núcleo del sistema operativo** con su planificador y sus colas. Todos los computadores estarán sincronizados por un **reloj global** que marca el ritmo de la simulación, y podrán colaborar entre sí mediante mecanismos de comunicación como el problema del **productor-consumidor** implementado con **semáforos**.

El simulador deberá permitir la **creación de procesos por parte del usuario** (“Vista 1”), quien definirá características como el nombre del proceso, la cantidad de instrucciones, la cantidad de memoria que requiere, su prioridad y su tipo: **CPU bound** (muy consumidor del procesador), **I/O bound** (muy consumidor de E/S), **productor** o **consumidor**. Si un proceso es CPU bound o I/O bound, deberá especificarse cuántos ciclos se necesitan para satisfacer el proceso. Si es productor o consumidor, deberá indicarse el buffer con el que trabaja y cada cuántos ciclos produce o consume un elemento, y cuántos elementos requiere para terminar. Además, el usuario podrá decidir en qué computador se crea el proceso; también pueden implementar que sea ÁvilaOS quien lo asigne automáticamente según la carga de cada máquina.

En ÁvilaOS, cada proceso está compuesto por un único hilo de ejecución (modelo monohilo). Al tratarse de un esquema de un solo hilo por proceso, este gestiona de manera directa sus recursos de memoria, su propio contador de programa (PC) y su estado de ejecución. La unidad que el planificador asigna al procesador será el propio proceso (o su único hilo), por lo que el sistema operativo mantendrá el bloque de control del proceso (PCB) —el cual integra o se asocia de forma 1:1 con el TCB— y mostrará de manera clara su contexto de ejecución tanto en la CPU como en las distintas colas del sistema.

Los procesos productores y consumidores se comunicarán a través de **buffers de capacidad limitada** creados por el usuario. Cada buffer reside en la memoria de uno de los computadores (su computador anfitrión) y ocupa espacio en ella. Un productor o consumidor puede estar en el mismo computador que el buffer (**acceso local**) o en otro computador del clúster (**acceso remoto**); en este último caso, cada acceso al buffer tendrá un costo adicional de ciclos que representa la latencia de la red. El acceso al buffer deberá sincronizarse con el **algoritmo del productor-consumidor con semáforos** estudiado en clase, usando **semáforos**: cuando un proceso no pueda continuar (buffer lleno, buffer vacío o región crítica ocupada), deberá bloquearse en la cola del semáforo correspondiente y el procesador deberá atender a otro proceso.

Cada computador tiene una memoria principal de tamaño limitado. El sistema operativo de cada máquina sólo podrá admitir un proceso nuevo si hay espacio suficiente en su RAM; en caso contrario, el proceso deberá esperar en la cola de nuevos (planificación a largo plazo) hasta que se libere memoria. En esta primera versión de ÁvilaOS **no es necesario implementar una representación del disco ni las técnicas de memoria virtual**: estos componentes serán el objetivo del Proyecto 2.

La simulación debe proporcionar una **visualización clara** del estado de cada computador (“Vista 2”): qué proceso se está ejecutando en cada CPU y la instrucción cuya dirección se encuentra en el program counter, si en ese momento se está ejecutando el **sistema operativo o un programa de usuario**, el contenido de las colas de nuevos, listos, bloqueados y terminados, la ocupación de la memoria, y el estado de cada buffer con los valores de sus semáforos y los procesos que esperan en ellos. Asimismo, debe ser posible cambiar durante la ejecución la **política de planificación de cada computador** de forma independiente en cada computador.

El simulador deberá registrar **métricas de rendimiento básicas** por computador y del sistema completo, de manera que el equipo de diseño pueda comparar configuraciones: procesos completados por unidad de tiempo (throughput), utilización del procesador, tiempo de respuesta promedio y equidad, además de indicadores simples propios del productor-consumidor, como los elementos producidos y consumidos y el tiempo que los procesos pasan bloqueados en semáforos.

Finalmente, un aspecto fundamental del proyecto es el **diseño**. ÁvilaOS debe estar modelado de forma estructurada y orientada a objetos: los estados, tipos y políticas deben representarse con **enumeraciones (enums)**, los comportamientos intercambiables deben definirse mediante **interfaces**, y el computador debe ser una unidad reutilizable, de modo que agregar un computador más al clúster sea tan sencillo como crear otra instancia; esta lógica les simplificará el proyecto. Un buen diseño en este proyecto será la diferencia entre extender el simulador en el Proyecto 2 o tener que rehacerlo.

## Requerimientos funcionales

### 1. Arquitectura del sistema distribuido

- Deben hacer uso de **Hilos/Threads** de Java para la simulación y de **Semáforos/Semaphores** de Java para garantizar exclusión mutua sobre las estructuras compartidas entre hilos de Java (pilas, colas, tablas, buffers, etc.).
- El sistema debe simular **mínimo 2**. La cantidad se indica en una vista de configuración.
- Cada computador debe tener, como mínimo: **CPU, memoria principal (RAM), y su propio sistema operativo** (núcleo con planificador y demás que consideren necesario).
- Todos los computadores deben ser **instancias de una misma clase**. No se permite duplicar código para crear el segundo, tercer o cuarto computador.
- Debe existir un **reloj global** que sincronice el sistema distribuido y a los computadores: en cada ciclo de reloj, cada computador avanza exactamente un ciclo de ejecución.
- Uso obligatorio de **enums**. Como mínimo deben representarse con enumeraciones: los estados de un proceso, la política de planificación y el modo de ejecución del procesador (usuario / sistema operativo).
- Uso obligatorio de **interfaces**. Como mínimo:
  - La **política de planificación** debe definirse mediante una interfaz, de forma que agregar una política nueva no requiera modificar el código del planificador sino extenderlo.
  - Los **componentes que reaccionan al reloj** (CPU, DMA, etc.) deben compartir una interfaz común.
  - Cualquier otra interfaz que consideren conveniente deberá ser justificada el día de la defensa.

### 2. Procesos

- El sistema deberá implementar un modelo de estados que incluya: **Nuevo, Listo, Ejecución, Bloqueado y Terminado** para los procesos. Para los procesos bloqueados deben identificar y justificar los estados a implementar.
- Elementos mínimos del **PCB**: ID (generado dinámicamente y único en todo el sistema distribuido), nombre, computador en el que se ejecuta, estado, tipo, prioridad, memoria asignada, deadline y tiempo restante de ejecución.
- Al llegar a 0 el deadline de un proceso, este tiene que ser terminado. Esto es importante para las métricas de eficiencia de cada política.

### 3. Planificación

- Se deben desarrollar como mínimo **3 políticas de planificación**: **FCFS, EDF** (Earliest Deadline First), **Round Robin** (con quantum configurable) y **Prioridades apropiativas**. El ordenamiento de la cola posterior a cada selección deberá ser programado por ustedes.
- **Cada computador tiene su propia política**, que puede cambiarse en tiempo de ejecución de forma independiente de los demás.
- La asignación de un proceso a un computador puede ser **manual** (el usuario lo elige) o **automática** (ÁvilaOS lo asigna). El criterio de asignación automática (balanceo de carga) es opcional y queda a su elección. De aplicarse deberá ser investigado y justificado el día de la defensa.
- Una vez asignado, un proceso **no cambia de computador** (no hay migración de procesos para simplificar el sistema distribuido).

### 4. Sincronización: productor-consumidor

- Deben implementar **semáforos** en el proyecto. Su implementación queda a su elección pero deben ser parte de ÁvilaOS.
- El usuario puede crear **uno o más buffers** de capacidad limitada, indicando su capacidad y su computador anfitrión. El buffer ocupa espacio en la RAM de su anfitrión.
- La sincronización de cada buffer debe resolverse con el **algoritmo del productor-consumidor con buffer acotado y semáforos** (ver Anexo).
- Deben soportarse varios **productores y varios consumidores** por buffer, ubicados en el mismo computador que el buffer o en computadores distintos.
- Cada acceso a un buffer remoto bloquea al proceso durante una cantidad de ciclos igual a la **latencia de red** (parámetro configurable).
- Cuando un proceso hace `semWait` sobre un semáforo cuyo valor no le permite continuar, debe pasar al estado Bloqueado, entrar a la cola de ese semáforo y **liberar el procesador**.

### 5. Memoria principal

- Cada computador tiene una RAM de tamaño configurable. Cada proceso declara la memoria que requiere.
- Un proceso sólo puede pasar de Nuevo a Listo si hay memoria suficiente en su computador; si no, permanece en la **cola de nuevos**.
- La memoria de un proceso se libera cuando termina, y el sistema debe revisar entonces si algún proceso de la cola de nuevos puede ser admitido.

### 6. Interfaz gráfica

- Se debe hacer uso de una interfaz gráfica que permita observar durante la simulación, **para cada computador**:
  - El proceso en ejecución en el procesador, el valor de su program counter, su prioridad y su deadline.
  - Si se está ejecutando el sistema operativo o un programa de usuario.
  - Las colas de nuevos, listos, bloqueados (indicando el motivo) y terminados. Cualquier cambio en su ordenamiento debe ser visible inmediatamente.
  - Los elementos del PCB de cada proceso, tanto en las colas como en la CPU.
  - La ocupación de la memoria principal (usada / libre).
  - La política de planificación en uso y un selector para cambiarla.
- De forma **global** deben visualizarse:
  - El número de ciclo de reloj desde que se inicia la simulación.
  - El estado de cada buffer: elementos almacenados, capacidad, valores de sus semáforos y los procesos bloqueados en cada uno.
  - Un **log de eventos** de texto donde se registre cada decisión importante del sistema (ej.: "Computador 1 selecciona el proceso P3-H2", "Proceso P5-H1 se bloquea en el semáforo 'lleno' del buffer B1", "Proceso P2-H1 accede al buffer B2 de forma remota").

### 7. Parámetros y configuración

- La simulación debe permitir en tiempo de ejecución:
  - Cambiar la política de planificación de cada computador.
  - Cambiar la duración de un ciclo de ejecución (en ms o segundos) y el quantum de Round Robin.
  - Crear nuevos procesos y nuevos buffers.
- Desde la interfaz se le debe poder indicar al programa los siguientes parámetros, para que sean escritos en un archivo (**CSV o JSON**) y utilizados en futuras simulaciones:
  - Duración del ciclo de ejecución de una instrucción.
  - Número de computadores y tamaño de la RAM de cada uno.
  - Política de planificación inicial de cada computador y quantum.
  - Latencia de la red (en ciclos).
  - Carga inicial de procesos (con todos sus atributos) y de buffers (capacidad y anfitrión).

### 8. Métricas de rendimiento

- Por computador y del sistema completo: **throughput, utilización del procesador, tiempo de respuesta promedio, tasa de cumplimiento de los tiempos límites (deadlines) y equidad**.
- Del productor-consumidor: elementos producidos y consumidos por buffer y tiempo promedio que los procesos pasan bloqueados en semáforos.

- Mostrar en un **mismo gráfico** la utilización del procesador de cada computador con respecto al tiempo. Puede hacerse uso de librerías de gráficos para Java.

## Nota y supuestos de simulación

Con el fin de minimizar la complejidad del proyecto y estandarizar, se debe asumir que:

- Todas las instrucciones se ejecutan en un único ciclo de instrucción.
- Por simplicidad, todos los procesos se ejecutan de manera lineal: el PC y el MAR incrementan una unidad por cada ciclo del reloj.
- Cada operación `semWait`, `semSignal`, inserción o extracción de un elemento del buffer cuenta como **una instrucción ejecutada en modo sistema operativo**.
- Cada computador tiene una única cola de listos para su procesador.
- No es necesario implementar asignación contigua ni particiones de memoria: basta con controlar la memoria usada y la libre de cada computador.

## ¿Por dónde empezar?

Antes de escribir la primera línea de código, piensen en ÁvilaOS como lo que es: un sistema construido por capas. En la base está el hardware: un reloj que marca el paso del tiempo y unos componentes (procesador, memoria, controlador de E/S) que no toman decisiones, sólo obedecen. Por encima está el sistema operativo, que sí decide: qué proceso se admite, quién se bloquea y quién despierta. Si logran que cada capa conozca únicamente a la de abajo, y que el computador sea simplemente el lugar donde esas piezas se ensamblan, tener dos, tres o cuatro computadores dejará de ser un problema. Pregúntense también qué cosas del sistema son un conjunto fijo de valores y cuáles son comportamientos que podrían cambiar mañana: la respuesta les dirá dónde usar un enum y dónde una interfaz.

Resuelvan primero un solo computador ejecutando procesos, y sólo después piensen en la comunicación entre computadores para el buffer. Tengan presente que en este proyecto conviven **dos tipos de semáforos** con propósitos muy distintos: semáforos de conteo y semáforos binarios/mutex. Confundirlos es el error más común. Por último, recuerden que la interfaz gráfica debe **observar** al sistema, no ser el sistema: si la lógica de ÁvilaOS vive en las ventanas, el Proyecto 2 será mucho más difícil.

## Continuidad con el Proyecto 2

El Proyecto 2 extenderá ÁvilaOS con nuevas funciones del sistema operativo: **gestión de disco y memoria virtual** (incluyendo la suspensión de procesos). Este segundo proyecto se desarrollará **tomando como base el código entregado en el Proyecto 1**, por lo que la calidad del diseño, la separación en capas y el uso adecuado de enums e interfaces tendrán un impacto directo en el esfuerzo que requerirá la siguiente entrega.

## Consideraciones

### Conformación de equipos

- El proyecto puede ser elaborado máximo por 3 personas. Se permiten proyectos de compañeros de diferentes secciones pero de ser posible evitarlo.
- Solo se permite el uso de librerías para los hilos y los semáforos y para presentar la gráfica y leer el CSV, JSON, etc.

### Tecnología y entorno

- Se requiere que hagan el proyecto en versiones posteriores a **Java 21**, para poder asegurar un manejo adecuado del uso de los repositorios tanto entre los miembros del equipo como en la corrección.
- IDE: Estrictamente en NetBeans. Los programas que no se ejecuten adecuadamente en este entorno serán calificados con 0 (cero).
- Restricción de librerías: Sólo se permite el uso de librerías externas para la visualización de gráficas (ej. JFreeChart), el manejo de archivos JSON/CSV, y el uso de Hilos y Semáforos.
- **Estructuras de Datos:** Queda terminantemente prohibido el uso de `java.util.ArrayList`, `Queue`, `Stack`, `Vector` o cualquier colección del framework de Java. Los estudiantes deben programar sus propias estructuras (listas enlazadas, colas, etc.) para gestionar los procesos y el PCB.

### Estándares de desarrollo en GitHub (obligatorio)

- El uso de un repositorio en GitHub es obligatorio. Proyecto sin repositorio será calificado con 0 (cero).
- Uso de ramas (branches): No se permite trabajar únicamente en la rama `main`. Se debe evidenciar el uso de ramas por funcionalidad (ej.: `feat/scheduler`, `feat/gui`, `fix/interrupts`), y se debe contar con una rama `develop`.
- Gestión de tareas (Issues): El equipo debe registrar las tareas pendientes y errores encontrados mediante el sistema de Issues de GitHub.
- Integración (Pull Requests): La fusión de código entre ramas debe realizarse mediante Pull Requests comentados, simulando un entorno de trabajo profesional.
- Contribución equitativa: El historial de commits debe reflejar una participación equilibrada de todos los integrantes. Un desbalance significativo en los aportes afectará la nota individual.
- Commits: Los mensajes deben ser descriptivos (ej.: `fix: corrige puntero en cola de listos-suspendidos en lugar de update code`) y el tamaño debe ser limitado.

### Interfaz gráfica (GUI)

- Es un requisito indispensable. Proyectos sin interfaz gráfica o que solo funcionen por consola serán calificados con 0 (cero).
- La interfaz debe ser intuitiva y permitir visualizar el cambio de estados del proceso en tiempo real.
- Es obligatorio implementar validaciones de tipo de dato y rango en todos los campos de entrada. El sistema debe ser capaz de gestionar entradas inválidas sin interrumpir el flujo del simulador.

### Documentación e informe

Para la entrega, junto al código, se debe entregar un informe donde se detalle:

- La funcionalidad de las clases y métodos más importantes del proyecto, los enums e interfaces utilizados.
- **Dos diagramas UML** que describan el sistema y las conclusiones sobre el comportamiento del sistema con cada política de planificación y con productores/consumidores locales y remotos.
- No hace falta documentar todo el código.

### Entrega y evaluación

- Fecha límite: Viernes de Semana 7 antes de las 7:00 AM.
- Canal: Enviar informe PDF y link del repositorio GitHub a Gabriela Costa y Ares Ramirez y agregar al spreadsheet (el cual se les mandará más adelante) antes de la hora.
- Asignación presencial (defensa): El viernes de semana 7, los estudiantes deberán realizar la defensa en la cual cada uno demostrará sus conocimientos sobre el proyecto. La presencia de todos los integrantes del equipo es obligatoria.
  - Si un estudiante no asiste: 0 (cero) en el proyecto.
  - Si un estudiante reprueba la defensa: Su nota máxima será de 10 (diez) puntos, independientemente de la calidad del código.
- Es importante que cada uno de los miembros de cada equipo posea un buen conocimiento general del funcionamiento de cada módulo de la solución.
- Los proyectos sin interfaz gráfica serán calificados en base a 0 (cero). No se corregirá código para validar funcionamiento, más sí para verificar que los miembros del equipo lo comprendan.
- Los proyectos sin repositorio en GitHub serán calificados en base a 0 (cero).
- Los programas que no se ejecuten adecuadamente serán calificados en base a 0 (cero).
- Los proyectos que no sean realizados en Java serán calificados en base a 0 (cero).

## Anexo: productor-consumidor con buffer acotado

El algoritmo clásico (Stallings, capítulo 5) utiliza tres semáforos por buffer. Cada productor y cada consumidor repite su ciclo indefinidamente mientras le queden instrucciones:

```text
/* programa productor consumidor */
semaphore s = 1;
semaphore n = 0;
semaphore e = /* tamaño del buffer */;

void productor() {
    while (true) {
        producir();
        semWait(e);
        semWait(s);
        añadir();
        semSignal(s);
        semSignal(n);
    }
}

void consumidor() {
    while (true) {
        semWait(n);
        semWait(s);
        extraer();
        semSignal(s);
        semSignal(e);
        consumir();
    }
}

void main() {
    paralelos(productor, consumidor);
}
```

Observen el orden de las operaciones `semWait`: invertirlas en el productor (primero mutex y luego vacíos) puede llevar al sistema a un interbloqueo.

## Anexo: interfaz hecha con Gemini

No tienen que hacerla con este estilo, es para que puedan tener una idea de cómo debe quedar al final. Recuerden que es una interfaz hecha con IA y las formas en las que distribuyen los elementos pueden no ser las mejores.

La captura de referencia está guardada en [12.png](capturas/12.png). Muestra un ejemplo de panel global con reloj, eventos, buffers y métricas, junto con paneles por computador que presentan la CPU, política de planificación, memoria y colas de procesos.

---

*Transcripción de las capturas recibidas; se corrigieron únicamente saltos de línea y espaciado. La primera captura continúa en la segunda.*
