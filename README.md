# ÁvilaOS — Simulador de Sistema Operativo Distribuido en Tiempo Real

Simulador académico de planificación de procesos de tiempo real, administración de memoria RAM y sincronización distribuida **Productor-Consumidor** mediante semáforos de Stallings y latencia de red, desarrollado en **Java** con interfaz gráfica en **Swing** y gráficas en tiempo real con **JFreeChart**.

📄 **[Ver Informe Técnico Completo (`docs/INFORME_TECNICO.md`)](docs/INFORME_TECNICO.md)**

---

## Características Principales

- **Estructuras de Datos 100% Propias (`com.avilaos.core.structures`):** Implementación desde cero de lista simplemente enlazada con ordenamiento in-situ (`MyLinkedList<T>`), cola FIFO (`MyQueue<T>`), pila LIFO (`MyStack<T>`) y nodo genérico (`Node<T>`), sin utilizar colecciones de `java.util`.
- **Modelo Monohilo 1:1 (`ProcessControlBlock`):** Cada proceso mantiene sus registros de hardware (`PC`, `MAR`), estado (`NUEVO`, `LISTO`, `EJECUTANDO`, `BLOQUEADO`, `SALIENTE`), modo de ejecución (`USER` / `KERNEL`), prioridad, *deadline*, tiempo de espera acumulado y titularidad de permisos semafóricos.
- **4 Políticas de Planificación Intercambiables en Caliente (`IScheduler`):**
  - **FCFS (`FcfsScheduler`):** *First-Come, First-Served* no apropiativo.
  - **Round Robin (`RoundRobinScheduler`):** Apropiativo por *quantum* configurable en tiempo real por computador.
  - **EDF (`EdfScheduler`):** *Earliest Deadline First* apropiativo por menor *deadline* restante.
  - **Prioridades Apropiativas (`PreemptivePriorityScheduler`):** Desalojo inmediato por mayor prioridad.
  - **Reordenamiento visual inmediato:** Al cambiar la política de cualquier computador desde la GUI, su cola de listos se reordena y actualiza al instante en pantalla.
- **Sincronización Distribuida Productor-Consumidor (`Buffer`, `ProducerConsumerDriver`, `NetworkSimulator`):**
  - Buffers acotados alojados en la RAM de un computador anfitrión con los 3 semáforos clásicos de Stallings (`mutex`, `llenos`, `vacios`).
  - Simulación de **latencia de red** configurable cuando un productor o consumidor opera sobre un buffer remoto.
- **Métricas en Tiempo Real con JFreeChart (`PanelMetricas`):**
  - Selector dinámico para graficar **Utilización de CPU (%)**, **Throughput (proc/ciclo)**, **Tasa de éxito de deadlines (%)** y **Tiempo promedio de espera (ticks)**.
- **Persistencia JSON Validada (`MonitorSimulacion`, `ValidarCarga`):**
  - Exportación e importación de escenarios completos en formato JSON y exportación de bitácoras `.log`.

---

## Requisitos y Ejecución

- **Java JDK:** 21 o superior (configurado para JDK 25 en `pom.xml`).
- **IDE recomendado:** Apache NetBeans 23+ o ejecución por línea de comandos con Maven.

### Compilar y ejecutar desde terminal
```powershell
mvn clean compile exec:java "-Dexec.mainClass=com.avilaos.Main"
```

---

## Diagrama de Arquitectura por Capas

```mermaid
flowchart TB
  subgraph GUI["Capa de Presentación (com.avilaos.gui)"]
    PP["PanelPrincipal"]
    EP["EditorProceso"]
    MS["MonitorSimulacion"]
    PM["PanelMetricas"]
    VC["ValidarCarga"]
    GR["GuiRequest"]
  end

  subgraph CTRL["Capa de Simulación y Concurrencia (com.avilaos.simulation / concurrency)"]
    SC["SimulationController"]
    SS["SimulationSnapshot"]
    GCmd["GuiCommands"]
    SA["StateAccess"]
  end

  subgraph SYNC_MEM["Capa de Memoria y Sincronización (com.avilaos.memory / sync)"]
    MM["MemoryManager"]
    BUF["Buffer"]
    PCD["ProducerConsumerDriver"]
    NS["NetworkSimulator"]
  end

  subgraph KERNEL_HW["Capa de Kernel, Hardware y Estructuras (com.avilaos.core / hardware)"]
    C["Computer"]
    CK["ComputerKernel"]
    GC["GlobalClock"]
    CPU["CPU · RAM · DMA"]
    SCH["IScheduler (FCFS, RR, EDF, Prioridades)"]
    PCB["ProcessControlBlock"]
    EDD["MyLinkedList · MyQueue · MyStack · Node"]
  end

  PP --> EP & MS & PM
  PP & EP & MS --> GR
  MS --> VC
  GR --> GCmd --> SA
  GR --> SC
  SC --> SS & GC & SA
  SC --> C & BUF & PCD
  PCD --> BUF & NS
  C --> CK & CPU & MM
  CK --> SCH & CPU & MM & PCD & BUF
  CK & SCH & BUF & PCD --> PCB
  CK & SCH & BUF & SC & GC & GCmd --> EDD
```

---

## Diagrama UML de Clases

```mermaid
classDiagram
    direction TB

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

    class MyLinkedList~T~ {
        +append(T data) void
        +get(int index) T
        +deletePos(int pos) void
        +sort(MyComparator~T~ comp) void
        +size() int
    }

    class MyQueue~T~ {
        +enqueue(T item) void
        +dequeue() T
        +peek() T
        +removeElement(T data) boolean
        +toArray() Object[]
    }

    class ProcessControlBlock {
        -int id
        -String name
        -ProcessState state
        -ProcessType type
        -ExecutionMode mode
        -BlockReason blockReason
        -int priority
        -int assignedMemory
        -int remainingTime
        -int remainingDeadline
        -int waitingTime
        -int programCounter
        -int memoryAddressRegister
        +executeOneCycle(int currentCycle) void
        +updateDeadlineOnTick(int currentCycle) void
        +incrementWaitingTime() void
    }

    class IScheduler {
        <<interface>>
        +scheduleNext(MyQueue~ProcessControlBlock~ queue, ProcessControlBlock currentRunning) ProcessControlBlock
        +addProcess(ProcessControlBlock process) void
        +removeProcess(ProcessControlBlock process) void
        +getType() SchedulingPolicyType
        +onClockTick() void
    }

    class FcfsScheduler
    class RoundRobinScheduler {
        -int quantum
        -int currentQuantumRemaining
        +setQuantum(int quantum) void
    }
    class EdfScheduler {
        +sortReadyQueue(MyQueue~ProcessControlBlock~ queue) void
    }
    class PreemptivePriorityScheduler {
        +sortReadyQueue(MyQueue~ProcessControlBlock~ queue) void
    }

    class Buffer {
        -int id
        -int idComp
        -int storage
        -int latency
        -MyQueue~Integer~ datos
        +semWait(ProcessControlBlock pcb, SemaphoreKind kind) boolean
        +semSignal(ProcessControlBlock pcb, SemaphoreKind kind) void
        +produce(int item) void
        +consume() Integer
        +cleanupPermits(ProcessControlBlock pcb) void
    }

    class ProducerConsumerDriver {
        +executeCycle(ProcessControlBlock pcb, Buffer buffer) boolean
        +stepNetworkDelay(ProcessControlBlock pcb) boolean
        +wakeUpProcess(ProcessControlBlock pcb)$ void
    }

    class CPU {
        -int PC
        -int MAR
        -ExecutionMode mode
        -ProcessControlBlock PCB
        +tick(int currentCycle) void
        +setPCB(ProcessControlBlock PCB) void
    }

    class ComputerKernel {
        -MyQueue~ProcessControlBlock~ newQueue
        -MyQueue~ProcessControlBlock~ readyQueue
        -MyQueue~ProcessControlBlock~ blockedQueue
        -MyQueue~ProcessControlBlock~ endedQueue
        -IScheduler activeScheduler
        +admitProcess(Computer com, ProcessControlBlock p) boolean
        +onPolicyChanged(Computer com) void
        +reorderReadyQueue(SchedulingPolicyType policy, boolean restoreFifo) void
        +dispatch(Computer com, ProcessControlBlock p) void
        +executeCycle(Computer comp, int currentCycle, ProducerConsumerDriver syncDriver, MyLinkedList~Buffer~ buffers) void
        +terminateProcess(Computer com, ProcessControlBlock p) void
    }

    class Computer {
        -int id
        -String name
        -CPU cpu
        -ComputerKernel kernel
        -SchedulingPolicyType politica
        -int quantum
        +setPolitica(SchedulingPolicyType politica) void
        +setQuantum(int quantum) void
    }

    class GlobalClock {
        -int cycleCounter
        -int cycleDurationMs
        +start() void
        +pause() void
        +step() void
        +reset() void
    }

    class SimulationController {
        -MyLinkedList~Computer~ computers
        -MyLinkedList~Buffer~ buffers
        -GlobalClock clock
        +tick(int currentCycle) void
        +createComputer(...) Computer
        +createProcess(...) ProcessControlBlock
        +createBuffer(...) Buffer
        +setComputerPolicy(int id, SchedulingPolicyType policy) void
        +reset() void
    }

    IScheduler <|.. FcfsScheduler
    IScheduler <|.. RoundRobinScheduler
    IScheduler <|.. EdfScheduler
    IScheduler <|.. PreemptivePriorityScheduler

    SimulationController *-- GlobalClock
    SimulationController *-- Computer
    SimulationController *-- Buffer
    SimulationController *-- ProducerConsumerDriver
    Computer *-- CPU
    Computer *-- ComputerKernel
    ComputerKernel --> IScheduler
    ComputerKernel --> ProducerConsumerDriver
    ComputerKernel --> Buffer
    CPU --> ProcessControlBlock
    ProcessControlBlock --> ProcessState
    ProcessControlBlock --> ProcessType
    ProcessControlBlock --> BlockReason
    CPU --> ExecutionMode
    Computer --> SchedulingPolicyType
```

---

## Diagrama UML de Secuencia

```mermaid
sequenceDiagram
    autonumber
    participant Clock as GlobalClock
    participant SA as StateAccess
    participant Ctrl as SimulationController
    participant Kernel as ComputerKernel
    participant Sched as IScheduler
    participant CPU as CPU
    participant Driver as ProducerConsumerDriver
    participant Net as NetworkSimulator
    participant Buf as Buffer
    participant GUI as MonitorSimulacion / PanelMetricas

    Clock->>SA: enter()
    Clock->>Ctrl: tick(currentCycle)
    activate Ctrl

    loop Por cada Computer en el clúster
        Ctrl->>Kernel: executeCycle(comp, currentCycle, syncDriver, buffers)
        activate Kernel

        Kernel->>Sched: onClockTick()
        Note over Kernel: checkDeadlines() y avance de red / E/S / flushWokenProcesses()
        Kernel->>Sched: scheduleNext(readyQueue, currentRunning)
        Sched-->>Kernel: nextProcess
        Kernel->>CPU: setPCB(nextProcess) (USER si != null, KERNEL si == null)

        alt Proceso Productor / Consumidor en CPU
            Kernel->>Driver: executeCycle(currentRunning, targetBuf)
            alt Requiere latencia de red remota
                Driver->>Net: handleDelay(currentRunning, targetBuf)
                Net-->>Driver: false (BLOQUEADO por NETWORK_LATENCY)
                Kernel->>Kernel: blockProcess(comp, currentRunning)
            else Operación sobre semáforos del Buffer (Modo KERNEL)
                Driver->>Buf: semWait / produce o consume / semSignal
                Buf->>Driver: wakeUpProcess(waitingPcb) -> LISTO
            end
            Kernel->>CPU: tick(currentCycle)
        else CPU libre
            Note over CPU: ExecutionMode.KERNEL
        end

        Note over Kernel: Incrementar waitingTime solo en procesos LISTO/BLOQUEADO que no ejecutaron en este ciclo
        deactivate Kernel
    end

    Ctrl->>SA: exit()
    SA->>Ctrl: publishSnapshot()
    Ctrl-->>GUI: SwingUtilities.invokeLater(actualizarVista / actualizarMetricas)
    deactivate Ctrl
```

---

## Estructura de Paquetes

```text
src/main/java/com/avilaos/
├── Main.java                        # Punto de entrada (inicia PanelPrincipal en el EDT)
├── concurrency/                     # StateAccess (exclusión mutua) y GuiCommands (hilo trabajador)
├── core/
│   ├── kernel/                      # ComputerKernel, GlobalClock e IClockTickable
│   ├── model/                       # ProcessControlBlock y los 5 Enums del dominio
│   ├── scheduler/                   # IScheduler, FcfsScheduler, RoundRobinScheduler, EdfScheduler, PreemptivePriorityScheduler
│   └── structures/                  # MyLinkedList, MyQueue, MyStack, Node y EstructuraVaciaException
├── gui/                             # PanelPrincipal, MonitorSimulacion, EditorProceso, PanelMetricas, ValidarCarga, GuiRequest
├── hardware/                        # Computer, CPU, RAM y DMA
├── memory/                          # MemoryManager (gestión contable de RAM)
├── simulation/                      # SimulationController y SimulationSnapshot
└── sync/                            # Buffer, ProducerConsumerDriver y NetworkSimulator
```
