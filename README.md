# 🏔️ ÁvilaOS — Simulador de Sistema Operativo Distribuido

> **Proyecto 1 — Sistemas Operativos**  
> Simulador en tiempo real de un sistema operativo distribuido monohilo sobre un clúster de computadores sincronizados por un reloj global, con estructuras de datos propias, 4 políticas de planificación intercambiables en caliente y sincronización Productor-Consumidor (local y remota) mediante semáforos de Stallings.

---

## 📋 Tabla de Contenidos
1. [Características Principales](#-características-principales)
2. [Requisitos y Ejecución](#-requisitos-y-ejecución)
3. [Arquitectura por Capas](#-arquitectura-por-capas)
4. [Diagrama UML de Clases](#-diagrama-uml-de-clases)
5. [Diagrama UML de Secuencia](#-diagrama-uml-de-secuencia)
6. [Estructura de Paquetes](#-estructura-de-paquetes)
7. [Informe Técnico Completo](#-informe-técnico-completo)

---

## ✨ Características Principales

* **Clúster Distribuido Reutilizable:** Soporte para $N \ge 2$ computadores instanciados a partir de una única clase `Computer` y `ComputerKernel`, cada uno con su propia `CPU`, `RAM`, `DMA`, `MemoryManager` y colas independientes (`Nuevos`, `Listos`, `Bloqueados`, `Terminados`).
* **Estructuras de Datos 100% Propias:** Implementación desde cero de `Node<T>`, `MyLinkedList<T>`, `MyQueue<T>` y `MyStack<T>` sin utilizar ninguna colección de `java.util.*`.
* **4 Políticas de Planificación en Caliente (`IScheduler`):**
  * **FCFS** (*First-Come, First-Served*)
  * **Round Robin** (con *quantum* configurable en vivo por computador)
  * **EDF** (*Earliest Deadline First* apropiativo)
  * **Prioridades Apropiativas** (*Preemptive Priority*)
* **Sincronización Productor-Consumidor (Algoritmo de Stallings):** Buffers acotados alojados en la RAM de un computador anfitrión, protegidos con tres semáforos (`mutex`, `vacios`, `llenos`), soportando acceso **local** y **remoto** con **latencia de red** configurable en ciclos.
* **Concurrencia Real y Seguridad de Hilos:** Reloj global (`GlobalClock`) en hilo daemon independiente, cola de comandos (`GuiCommands`), exclusión mutua del modelo (`StateAccess`) y publicación de instantáneas inmutables (`SimulationSnapshot`) hacia la GUI Swing.
* **Métricas y Gráficas en Tiempo Real (`JFreeChart`):** Visualización por computador y global de **Utilización de CPU**, **Throughput**, **Tasa de éxito de Deadlines**, **Tiempo de Espera**, **Tiempo de Respuesta** y **Equidad**, además de persistencia **JSON** (`Gson`) y exportación de logs.

---

## 🚀 Requisitos y Ejecución

* **Java:** JDK 21 o superior (configurado para JDK 25 en `pom.xml`).
* **IDE:** Apache NetBeans 23+ (proyecto Maven nativo).
* **Librerías autorizadas (vía Maven):** `org.jfree:jfreechart:1.5.4` y `com.google.code.gson:gson:2.11.0`.

### Ejecución desde Apache NetBeans
1. Abrir el proyecto `Proyecto1.SistemasOperativos`.
2. Verificar en *Project Properties → Libraries* que `Java Platform` apunte a **JDK 25** (o ajustar `<maven.compiler.source>` / `<maven.compiler.target>` en `pom.xml` a tu versión de JDK 21+).
3. Ejecutar **Clean and Build** y luego **Run Project (F6)**.

### Ejecución desde Terminal (PowerShell)
```powershell
cd Proyecto1.SistemasOperativos
& "C:\Program Files\NetBeans-23\netbeans\java\maven\bin\mvn.cmd" compile exec:java
```

---

## 🏛️ Arquitectura por Capas

```mermaid
flowchart TB
    subgraph presentacion["Capa 3: Presentación (Swing GUI — Observador Desacoplado)"]
        gui["PanelPrincipal · MonitorSimulacion · EditorProceso · PanelMetricas"]
    end

    subgraph aplicacion["Capa 2: Control, Concurrencia y Sincronización Distribuida"]
        control["SimulationController · GuiCommands · StateAccess"]
        observacion["SimulationSnapshot (Inmutable) · Log y Métricas"]
        sync["Buffer (Semáforos: mutex, vacíos, llenos) · ProducerConsumerDriver · NetworkSimulator"]
    end

    subgraph computador["Capa 1: Computador Simulado (Instancia repetible en el clúster)"]
        subgraph software["Núcleo del Sistema Operativo"]
            procesos["ProcessControlBlock (PCB — Modelo Monohilo 1:1)"]
            so["ComputerKernel · IScheduler (FCFS, RR, EDF, Prioridades)"]
            estructuras["Estructuras Propias: MyLinkedList · MyQueue · MyStack · Node"]
        end
        subgraph hardware["Hardware Simulado"]
            cpu["CPU (PC, MAR, IR, ExecutionMode: USER / KERNEL)"]
            ram["RAM & MemoryManager (Admisión y Buffers)"]
            es["DMA (Controlador E/S)"]
        end
    end

    reloj["GlobalClock (Thread Daemon) — Sincronización por Ticks"]

    gui -->|"Comandos asíncronos (GuiRequest)"| control
    control -->|"Creación, políticas y configuración"| so
    control -->|"Play / Pause / Step / Intervalo"| reloj
    procesos -->|"Fases de ciclo y bloqueos"| so
    so -->|"dispatch() y cambio de contexto"| cpu
    so -->|"semWait / semSignal / Red"| sync
    so -->|"allocateMemory / releaseMemory"| ram
    reloj -.->|"tick(currentCycle) — IClockTickable"| control
    control -.->|"executeCycle()"| so
    so -.->|"tick(currentCycle)"| cpu
    so -.->|"tick(currentCycle)"| es
    control -->|"Publica estado tras cada ciclo"| observacion
    observacion -->|"Refresco en EDT"| gui
```

---

## 📐 Diagrama UML de Clases

```mermaid
classDiagram
    direction TB

    class IClockTickable {
        <<interface>>
        +tick(int currentCycle) void
    }

    class IScheduler {
        <<interface>>
        +scheduleNext(MyQueue~ProcessControlBlock~ queue, ProcessControlBlock currentRunning) ProcessControlBlock
        +addProcess(ProcessControlBlock process) void
        +removeProcess(ProcessControlBlock process) void
        +getReadyQueue() MyQueue~ProcessControlBlock~
        +getType() SchedulingPolicyType
        +onClockTick() void
    }

    class ProcessState {
        <<enumeration>>
        NUEVO
        LISTO
        EJECUTANDO
        BLOQUEADO
        SALIENTE
    }

    class ProcessType {
        <<enumeration>>
        CPU_BOUND
        IO_BOUND
        PRODUCER
        CONSUMER
    }

    class ExecutionMode {
        <<enumeration>>
        USER
        KERNEL
    }

    class SchedulingPolicyType {
        <<enumeration>>
        FCFS
        ROUND_ROBIN
        EDF
        PRIORITY_PREEMPTIVE
    }

    class BlockReason {
        <<enumeration>>
        NONE
        WAITING_MUTEX
        WAITING_EMPTY_BUFFER
        WAITING_FULL_BUFFER
        NETWORK_LATENCY
        WAITING_IO
    }

    class GlobalClock {
        -GlobalClock instance$
        -MyLinkedList~IClockTickable~ subscribers
        -int cycleCounter
        -int cycleDurationMs
        -boolean running
        -boolean paused
        -Thread clockThread
        +getInstance()$ GlobalClock
        +start() void
        +pause() void
        +resume() void
        +step() void
        +reset() void
        +run() void
    }

    class SimulationController {
        -SimulationController instance$
        -MyLinkedList~Computer~ computers
        -MyLinkedList~Buffer~ buffers
        -GlobalClock clock
        -ProducerConsumerDriver syncDriver
        -SimulationSnapshot snapshot
        +getInstance()$ SimulationController
        +createComputer(String name, int ramSize, SchedulingPolicyType policy, int quantum) Computer
        +createProcess(...) ProcessControlBlock
        +createBuffer(int hostId, int storage, int memory) Buffer
        +selectLeastLoadedComputer() Computer
        +tick(int currentCycle) void
        +reset() void
        +snapshot() SimulationSnapshot
    }

    class Computer {
        -int id
        -String name
        -CPU cpu
        -RAM ram
        -DMA dma
        -ComputerKernel kernel
        -MemoryManager memoryManager
        -SchedulingPolicyType politica
        -int quantum
        -int ciclosActivos
        -int ciclosObservados
        -int procesosCompletados
        -int procesosFinalizados
        -int deadlinesCumplidos
        -int totalTiempoRespuesta
        -int totalTiempoEspera
        +registrarCicloObservado() void
        +incrementarCicloActivo() void
        +registrarTerminacion(ProcessControlBlock pcb, int cicloActual) void
    }

    class ComputerKernel {
        -MyLinkedList~ProcessControlBlock~ processList
        -MyQueue~ProcessControlBlock~ newQueue
        -MyQueue~ProcessControlBlock~ readyQueue
        -MyQueue~ProcessControlBlock~ blockedQueue
        -MyQueue~ProcessControlBlock~ endedQueue
        -IScheduler activeScheduler
        +admitProcess(Computer com, ProcessControlBlock p) boolean
        +dispatch(Computer com, ProcessControlBlock p) void
        +executeCycle(Computer comp, int currentCycle, ProducerConsumerDriver syncDriver, MyLinkedList~Buffer~ buffers) void
        +blockProcess(Computer com, ProcessControlBlock p) void
        +blockedToReady(ProcessControlBlock p) void
        +terminateProcess(Computer com, ProcessControlBlock p) void
        +placeBufferRAM(Buffer buffer, Computer com) boolean
    }

    class ProcessControlBlock {
        -int id
        -String name
        -int computerId
        -ProcessState state
        -ProcessType type
        -ExecutionMode mode
        -BlockReason blockReason
        -int priority
        -int assignedMemory
        -int totalInstructions
        -int remainingTime
        -int deadline
        -int remainingDeadline
        -int arrivalCycle
        -int startCycle
        -int completionCycle
        -int waitingTime
        -int programCounter
        -int memoryAddressRegister
        +executeOneCycle(int currentCycle) void
        +updateDeadlineOnTick(int currentCycle) void
        +incrementWaitingTime() void
        +resetIdGenerator(int initialValue)$ void
    }

    class CPU {
        -int PC
        -int MAR
        -int IR
        -ProcessControlBlock PCB
        -ExecutionMode mode
        +tick(int currentCycle) void
    }

    class MemoryManager {
        -RAM ram
        +hasAvailableMemory(int amount) boolean
        +allocateMemory(int amount) boolean
        +releaseMemory(int amount) void
    }

    class Buffer {
        -int id
        -int idComp
        -int storage
        -int sizeInMemory
        -Semaphore mutex
        -Semaphore vacios
        -Semaphore llenos
        -MyQueue~Object~ datos
        -MyQueue~ProcessControlBlock~ waitingMutex
        -MyQueue~ProcessControlBlock~ waitingVacios
        -MyQueue~ProcessControlBlock~ waitingLlenos
        +semWait(ProcessControlBlock pcb, SemaphoreKind kind) boolean
        +semSignal(ProcessControlBlock emitter, SemaphoreKind kind) ProcessControlBlock
        +produce(Object item) void
        +consume() Object
        +cleanupPermits(ProcessControlBlock pcb) void
    }

    class ProducerConsumerDriver {
        -NetworkSimulator networkSimulator
        +executeCycle(ProcessControlBlock pcb, Buffer buffer) boolean
        +stepNetworkDelay(ProcessControlBlock pcb) boolean
    }

    class NetworkSimulator {
        -int latencyCycles
        +isRemoteAccess(ProcessControlBlock pcb, Buffer buffer) boolean
        +handleDelay(ProcessControlBlock pcb, Buffer buffer) boolean
        +stepDelay(ProcessControlBlock pcb) boolean
    }

    class FcfsScheduler
    class RoundRobinScheduler
    class EdfScheduler
    class PreemptivePriorityScheduler

    IClockTickable <|.. SimulationController
    IClockTickable <|.. CPU
    IScheduler <|.. FcfsScheduler
    IScheduler <|.. RoundRobinScheduler
    IScheduler <|.. EdfScheduler
    IScheduler <|.. PreemptivePriorityScheduler

    SimulationController --> GlobalClock : se suscribe
    SimulationController "1" *-- "0..*" Computer : gestiona
    SimulationController "1" *-- "0..*" Buffer : gestiona
    SimulationController --> ProducerConsumerDriver : coordina sync
    Computer "1" *-- "1" CPU
    Computer "1" *-- "1" MemoryManager
    Computer "1" *-- "1" ComputerKernel
    ComputerKernel --> IScheduler : delega selección
    ComputerKernel "1" *-- "0..*" ProcessControlBlock : administra colas
    ProducerConsumerDriver --> NetworkSimulator : aplica latencia
    ProducerConsumerDriver --> Buffer : ejecuta Stallings
    ProcessControlBlock --> ProcessState
    ProcessControlBlock --> ProcessType
    ProcessControlBlock --> ExecutionMode
    ProcessControlBlock --> BlockReason
    Computer --> SchedulingPolicyType
```

---

## 🔄 Diagrama UML de Secuencia

Interacción completa durante un ciclo del reloj global (`tick`), incluyendo planificación, acceso remoto de un proceso Productor/Consumidor con latencia de red, protocolo de semáforos de Stallings (`semWait` / `semSignal`) y publicación hacia la GUI:

```mermaid
sequenceDiagram
    autonumber
    participant Clock as GlobalClock (Thread)
    participant Ctrl as SimulationController
    participant Kernel as ComputerKernel
    participant Sched as IScheduler
    participant Driver as ProducerConsumerDriver
    participant Net as NetworkSimulator
    participant Buf as Buffer (Semáforos)
    participant CPU as CPU
    participant Snap as SimulationSnapshot
    participant GUI as MonitorSimulacion / PanelMetricas (EDT)

    Clock->>Ctrl: tick(currentCycle)
    activate Ctrl
    loop Por cada Computer del clúster
        Ctrl->>Kernel: executeCycle(comp, currentCycle, syncDriver, buffers)
        activate Kernel
        Kernel->>Sched: onClockTick()
        Kernel->>Kernel: checkDeadlines() · advanceNetworkDelays() · advanceIOBlocks() · flushWokenProcesses()
        Kernel->>Sched: scheduleNext(readyQueue, currentRunning)
        Sched-->>Kernel: nextProcess (PCB)
        Kernel->>CPU: setPCB(nextProcess) · setPC() · setMAR() · setMode()

        alt Proceso es PRODUCER o CONSUMER
            Kernel->>Driver: executeCycle(pcb, targetBuffer)
            activate Driver
            alt Fase WAIT_SLOT y Buffer en otro computador (Acceso Remoto)
                Driver->>Net: handleDelay(pcb, targetBuffer)
                alt Latencia pendiente
                    Net-->>Driver: false (PCB pasa a BLOQUEADO por NETWORK_LATENCY)
                    Driver-->>Kernel: false
                    Kernel->>Kernel: blockProcess(comp, pcb) y libera CPU
                else Latencia completada o acceso local
                    Net-->>Driver: true
                end
            end
            alt Fase de Semáforos (WAIT_SLOT / WAIT_MUTEX / ACCESS / SIGNAL_MUTEX / SIGNAL_SLOT)
                Driver->>Buf: semWait(pcb, VACIOS/LLENOS/MUTEX) o semSignal(...) (Modo KERNEL)
                alt Permiso disponible
                    Buf-->>Driver: true (concede permiso y avanza PC/MAR)
                else Semáforo en 0 (sin permiso)
                    Buf-->>Driver: false (encola en waitingQueue y marca BLOQUEADO)
                    Kernel->>Kernel: blockProcess(comp, pcb) y libera CPU (Modo KERNEL)
                end
            else Fase WORK (Producción / Consumo)
                Driver->>Driver: Avanza ciclo en modo USER (PC++, MAR++)
            end
            deactivate Driver
        else Proceso es CPU_BOUND o IO_BOUND
            Kernel->>Kernel: pcb.executeOneCycle(currentCycle) (Modo USER, PC++, MAR++)
        end

        Kernel->>CPU: tick(currentCycle)
        Kernel->>Kernel: Incrementa waitingTime a procesos en LISTO o BLOQUEADO
        deactivate Kernel
    end

    Ctrl->>Snap: publishSnapshot() (Copia inmutable del ciclo)
    Ctrl->>GUI: SwingUtilities.invokeLater(actualizarVista / actualizarMetricas)
    deactivate Ctrl
```

---

## 📦 Estructura de Paquetes

```text
src/main/java/com/avilaos/
├── Main.java                              # Punto de entrada (lanza Swing en el EDT)
├── concurrency/
│   ├── GuiCommands.java                   # Hilo trabajador asíncrono para comandos de la GUI
│   └── StateAccess.java                   # Semáforo reentrante de exclusión mutua del modelo
├── core/
│   ├── kernel/
│   │   ├── ComputerKernel.java            # Núcleo del SO: admisión, colas, despacho y ciclo
│   │   ├── GlobalClock.java               # Reloj global Singleton multihilo
│   │   └── IClockTickable.java            # Interfaz para componentes sincronizados por reloj
│   ├── model/
│   │   ├── BlockReason.java               # Enum de motivos de bloqueo
│   │   ├── ExecutionMode.java             # Enum de modo de procesador (USER / KERNEL)
│   │   ├── ProcessControlBlock.java       # PCB: contexto, registros, tiempos y permisos
│   │   ├── ProcessState.java              # Enum de los 5 estados del proceso
│   │   ├── ProcessType.java               # Enum de tipos de proceso
│   │   └── SchedulingPolicyType.java      # Enum de las 4 políticas de planificación
│   ├── scheduler/
│   │   ├── IScheduler.java                # Interfaz común de planificación
│   │   ├── FcfsScheduler.java             # Política First-Come, First-Served
│   │   ├── RoundRobinScheduler.java       # Política Round Robin con quantum dinámico
│   │   ├── EdfScheduler.java              # Política Earliest Deadline First apropiativa
│   │   └── PreemptivePriorityScheduler.java # Política de Prioridades Apropiativas
│   └── structures/
│       ├── Node.java                      # Nodo genérico enlazado
│       ├── MyLinkedList.java              # Lista enlazada genérica propia
│       ├── MyQueue.java                   # Cola FIFO genérica propia
│       └── MyStack.java                   # Pila LIFO genérica propia
├── hardware/
│   ├── Computer.java                      # Unidad de computador reutilizable y métricas locales
│   ├── CPU.java                           # Procesador simulado (PC, MAR, IR, Mode)
│   ├── RAM.java                           # Memoria física simulada
│   └── DMA.java                           # Controlador de acceso directo a memoria / E/S
├── memory/
│   └── MemoryManager.java                 # Gestor contable de memoria usada y libre
├── sync/
│   ├── Buffer.java                        # Buffer acotado con 3 semáforos y colas de espera
│   ├── ProducerConsumerDriver.java        # Motor por fases del algoritmo de Stallings
│   └── NetworkSimulator.java              # Simulador de latencia de red entre nodos
├── simulation/
│   ├── SimulationController.java          # Orquestador del clúster y balanceador de carga
│   ├── SimulationSnapshot.java            # Foto inmutable de estado e historial para la GUI
│   └── ILogListener.java                  # Listener para eventos del log global
└── gui/
    ├── PanelPrincipal.java                # Ventana principal y navegación por módulos (01 a 06)
    ├── EditorProceso.java                 # Constructor interactivo de procesos con validación
    ├── MonitorSimulacion.java             # Monitor en tiempo real, colas, buffers y JSON
    ├── PanelMetricas.java                 # Gráfica JFreeChart multi-métrica y tablas de KPIs
    ├── ValidarCarga.java                  # Validador estricto de esquemas JSON
    ├── GuiRequest.java                    # Puente seguro de solicitudes EDT -> Modelo
    └── LogoAvila.java                     # Identidad gráfica vectorial
```

---

## 📄 Informe Técnico Completo

El documento detallado con la explicación exhaustiva de cada clase, método, enumeración, interfaz y análisis de políticas se encuentra en:  
👉 **[`docs/INFORME_TECNICO.md`](docs/INFORME_TECNICO.md)**
