package com.avilaos.core.kernel;

import com.avilaos.hardware.Computer;
import com.avilaos.hardware.CPU;
import com.avilaos.core.structures.*;
import com.avilaos.core.model.BlockReason;
import com.avilaos.core.model.ExecutionMode;
import com.avilaos.core.model.ProcessControlBlock;
import com.avilaos.core.model.ProcessState;
import com.avilaos.core.model.ProcessType;
import com.avilaos.core.model.SchedulingPolicyType;
import com.avilaos.core.scheduler.*;
import com.avilaos.sync.Buffer;
import com.avilaos.sync.ProducerConsumerDriver;

/**
 * Núcleo del computador simulado (Capa 1: Kernel).
 * Administra las colas de estados de procesos, admisión a largo plazo,
 * despacho a CPU según política de planificación y control de ciclo de vida.
 * 
 * Requisitos: ARC-06 a ARC-10, PLA-01 a PLA-10, MEM-03 a MEM-07, Issues #19, #20, #21.
 */
public class ComputerKernel {

    private final MyLinkedList<ProcessControlBlock> processList;
    private final MyQueue<ProcessControlBlock> blockedQueue;
    private final MyQueue<ProcessControlBlock> newQueue;
    private final MyQueue<ProcessControlBlock> readyQueue;
    private final MyQueue<ProcessControlBlock> endedQueue;
    private final GlobalClock clock;

    private IScheduler activeScheduler;
    private SchedulingPolicyType currentPolicy;

    public ComputerKernel(MyLinkedList<ProcessControlBlock> processList,
                          MyQueue<ProcessControlBlock> blockedQueue,
                          MyQueue<ProcessControlBlock> newQueue,
                          MyQueue<ProcessControlBlock> readyQueue,
                          MyQueue<ProcessControlBlock> readyQueue2,
                          MyQueue<ProcessControlBlock> readyQueue3,
                          MyQueue<ProcessControlBlock> readyQueue4,
                          MyQueue<ProcessControlBlock> endedQueue,
                          GlobalClock clock) {
        this.processList = (processList != null) ? processList : new MyLinkedList<>();
        this.blockedQueue = (blockedQueue != null) ? blockedQueue : new MyQueue<>();
        this.newQueue = (newQueue != null) ? newQueue : new MyQueue<>();
        this.readyQueue = (readyQueue != null) ? readyQueue : new MyQueue<>();
        this.endedQueue = (endedQueue != null) ? endedQueue : new MyQueue<>();
        this.clock = clock;
    }

    /**
     * Admisión de un proceso al computador (Planificador a largo plazo).
     * Si hay memoria suficiente en RAM, se admite a READY; de lo contrario,
     * permanece en NEW en espera de liberación de memoria.
     */
    public boolean admitProcess(Computer com, ProcessControlBlock p) {
        if (p == null || com == null) {
            return false;
        }

        p.setComputerId(com.getId());

        if (com.getMemoryManager() != null && com.getMemoryManager().hasAvailableMemory(p.getAssignedMemory())) {
            com.getMemoryManager().allocateMemory(p.getAssignedMemory());
            p.setState(ProcessState.LISTO);
            p.setBlockReason(BlockReason.NONE);
            this.newQueue.removeElement(p);
            this.readyQueue.enqueue(p);
            return true;
        } else {
            p.setState(ProcessState.NUEVO);
            this.newQueue.enqueue(p);
            return false;
        }
    }

    /**
     * Evalúa la admisión de procesos en espera en la cola de nuevos (FIFO estricto).
     * Se invoca automáticamente tras la liberación de memoria al terminar un proceso.
     * 
     * @param com Computador anfitrión.
     * @return Cantidad de procesos admitidos a la cola de listos.
     */
    public int evaluateNewQueueAdmission(Computer com) {
        if (com == null || this.newQueue == null || this.newQueue.isEmpty()) {
            return 0;
        }

        int admittedCount = 0;
        while (!this.newQueue.isEmpty()) {
            ProcessControlBlock nextNew = this.newQueue.peek();
            if (com.getMemoryManager() != null && com.getMemoryManager().hasAvailableMemory(nextNew.getAssignedMemory())) {
                this.newQueue.dequeue();
                com.getMemoryManager().allocateMemory(nextNew.getAssignedMemory());
                nextNew.setState(ProcessState.LISTO);
                nextNew.setBlockReason(BlockReason.NONE);
                this.readyQueue.enqueue(nextNew);
                admittedCount++;
            } else {
                // FIFO estricto: si el primer proceso en espera no cabe, se detiene la admisión
                break;
            }
        }
        return admittedCount;
    }

    /**
     * Crea un proceso y gestiona su admisión formal a través del planificador a largo plazo.
     */
    public String gestionCreateProcess(Computer idComp, boolean asignacionManual, int memoryPt) {
        ProcessControlBlock p = new ProcessControlBlock();
        p.setMemoryPointer(memoryPt);
        return gestionCreateProcess(idComp, p);
    }

    public String gestionCreateProcess(Computer idComp, ProcessControlBlock p) {
        if (idComp == null || p == null) {
            return "Error: Computador o proceso inválido.";
        }

        this.processList.append(p);

        boolean admitido = admitProcess(idComp, p);
        if (admitido) {
            return "Se ha creado el proceso y admitido a la cola de listos.";
        } else {
            return "Memoria insuficiente: Proceso en cola de NUEVOS en espera de RAM.";
        }
    }

    public boolean isMemoryTotallyUsed(Computer com, int memoria) {
        if (com == null || com.getMemoryManager() == null) {
            return false;
        }
        return com.getMemoryManager().hasAvailableMemory(memoria);
    }

    public void verificateRAMSpaceForNew(Computer com, ProcessControlBlock p) {
        if (com != null && p != null && com.getMemoryManager() != null) {
            if (com.getMemoryManager().hasAvailableMemory(p.getAssignedMemory())) {
                com.getMemoryManager().allocateMemory(p.getAssignedMemory());
                this.newQueue.removeElement(p);
                this.readyQueue.enqueue(p);
                p.setState(ProcessState.LISTO);
            }
        }
    }

    /**
     * Despacha el siguiente proceso de la cola de listos a la CPU usando la política activa.
     */
    public void dispatch(Computer com, ProcessControlBlock p) {
        if (com == null) {
            return;
        }

        if (this.readyQueue.isEmpty()) {
            if (com.getCpu() != null) {
                com.getCpu().setPCB(null);
            }
            return;
        }

        IScheduler scheduler = getOrUpdateScheduler(com);
        ProcessControlBlock currentRunning = (p != null && p.getState() == ProcessState.EJECUTANDO) ? p : null;
        ProcessControlBlock next = scheduler.scheduleNext(this.readyQueue, currentRunning);

        if (next != null) {
            next.setState(ProcessState.EJECUTANDO);
            if (com.getCpu() != null) {
                com.getCpu().setPCB(next);
                com.getCpu().setPC(next.getProgramCounter());
                com.getCpu().setMAR(next.getMemoryAddressRegister());
                com.getCpu().setMode(next.getMode() != null ? next.getMode() : ExecutionMode.USER);
            }
        }
    }

    private IScheduler getOrUpdateScheduler(Computer com) {
        SchedulingPolicyType policy = (com != null && com.getPolitica() != null)
                ? com.getPolitica()
                : SchedulingPolicyType.FCFS;

        if (this.activeScheduler == null || this.currentPolicy != policy) {
            this.currentPolicy = policy;
            switch (policy) {
                case FCFS -> this.activeScheduler = new FcfsScheduler();
                case ROUND_ROBIN -> this.activeScheduler = new RoundRobinScheduler(3);
                case EDF -> this.activeScheduler = new EdfScheduler();
                case PRIORITY_PREEMPTIVE -> this.activeScheduler = new PreemptivePriorityScheduler();
                default -> this.activeScheduler = new FcfsScheduler();
            }
        }
        return this.activeScheduler;
    }

    public void terminateFatherProcess(int fatherId) {
        if (this.processList == null) return;
        for (int i = 0; i < this.processList.size(); i++) {
            ProcessControlBlock proceso = this.processList.get(i);
            if (proceso != null && proceso.getIdFather() == fatherId) {
                ProcessState estadoActual = proceso.getState();
                if (estadoActual == ProcessState.BLOQUEADO) {
                    this.blockedQueue.removeElement(proceso);
                } else if (estadoActual == ProcessState.LISTO) {
                    this.readyQueue.removeElement(proceso);
                } else if (estadoActual == ProcessState.NUEVO) {
                    this.newQueue.removeElement(proceso);
                }

                proceso.setState(ProcessState.SALIENTE);
                this.endedQueue.enqueue(proceso);

                this.processList.deletePos(i);
                i--;
            }
        }
    }

    public void terminateProcess(ProcessControlBlock p) {
        terminateProcess(null, p);
    }

    public void terminateProcess(Computer com, ProcessControlBlock p) {
        if (p == null) {
            return;
        }

        if (p.getState() == ProcessState.BLOQUEADO) {
            this.blockedQueue.removeElement(p);
        } else if (p.getState() == ProcessState.LISTO) {
            this.readyQueue.removeElement(p);
        } else if (p.getState() == ProcessState.NUEVO) {
            this.newQueue.removeElement(p);
        }

        p.setState(ProcessState.SALIENTE);
        p.setBlockReason(BlockReason.NONE);
        this.endedQueue.enqueue(p);

        // Desalojar CPU si el proceso terminado estaba corriendo en ella
        if (com != null && com.getCpu() != null
                && com.getCpu().getPCB() != null
                && com.getCpu().getPCB().getId() == p.getId()) {
            com.getCpu().setPCB(null);
        }

        // Liberar la memoria RAM asignada al proceso
        if (com != null && com.getMemoryManager() != null) {
            com.getMemoryManager().freeMemory(p.getAssignedMemory());
            // Disparar admisión automática de la cola de NUEVOS
            evaluateNewQueueAdmission(com);
        }

        terminateFatherProcess(p.getId());
    }

    /**
     * Mueve un proceso a la cola de bloqueados y actualiza su estado.
     * Versión sin Computer: usada internamente por ProducerConsumerDriver.
     */
    public void blockProcess(ProcessControlBlock p) {
        if (p == null) {
            return;
        }
        p.setState(ProcessState.BLOQUEADO);
        this.readyQueue.removeElement(p);
        this.blockedQueue.enqueue(p);
    }

    /**
     * Mueve un proceso a bloqueados y desaloja la CPU si el proceso estaba corriendo ahí.
     * El kernel es el único responsable de vaciar la CPU — no debe hacerlo la capa superior.
     *
     * @param com Computador anfitrión (para acceder a su CPU).
     * @param p   Proceso a bloquear.
     */
    public void blockProcess(Computer com, ProcessControlBlock p) {
        blockProcess(p); // lógica de colas
        // Desalojar CPU si el proceso bloqueado estaba corriendo en ella
        if (com != null && com.getCpu() != null
                && com.getCpu().getPCB() != null
                && com.getCpu().getPCB().getId() == p.getId()) {
            com.getCpu().setPCB(null);
        }
    }

    /**
     * Desbloquea un proceso y lo retorna a la cola de listos.
     */
    public void blockedToReady(ProcessControlBlock p) {
        if (p == null) {
            return;
        }
        this.blockedQueue.removeElement(p);
        p.setState(ProcessState.LISTO);
        p.setBlockReason(BlockReason.NONE);
        this.readyQueue.enqueue(p);
    }

    public boolean placeBufferRAM(Buffer buffer, Computer com) {
        if (buffer == null || com == null) {
            return false;
        }
        int cantidad = buffer.getStorage();
        if (cantidad <= 0) {
            return false;
        }
        if (com.getRam() == null || com.getRam().getSpace() == null) {
            return false;
        }

        // 1. Validar disponibilidad contable previa
        if (com.getMemoryManager() != null && !com.getMemoryManager().hasAvailableMemory(cantidad)) {
            return false;
        }

        // 2. Validar disponibilidad física de bloques contiguos en RAM
        int[] space = com.getRam().getSpace();
        int ramTotalSize = space.length;
        int consecutiveFreeBlocks = 0;
        int startIndex = -1;
        for (int i = 0; i < ramTotalSize; i++) {
            if (space[i] == 0) {
                if (consecutiveFreeBlocks == 0) {
                    startIndex = i;
                }
                consecutiveFreeBlocks++;
                if (consecutiveFreeBlocks == cantidad) {
                    break;
                }
            } else {
                consecutiveFreeBlocks = 0;
                startIndex = -1;
            }
        }

        if (consecutiveFreeBlocks < cantidad || startIndex < 0) {
            return false;
        }

        // 3. Realizar reserva contable (solo tras verificar espacio contiguo)
        if (com.getMemoryManager() != null) {
            if (!com.getMemoryManager().allocateMemory(cantidad)) {
                return false;
            }
        } else {
            com.getRam().setUsedSize(com.getRam().getUsedSize() + cantidad);
        }

        // 4. Ubicar físicamente el búfer en RAM con reversión en caso de fallo
        try {
            for (int i = startIndex; i < startIndex + cantidad; i++) {
                space[i] = buffer.getId() + 1000;
            }
            return true;
        } catch (Exception e) {
            for (int i = startIndex; i < startIndex + cantidad; i++) {
                if (space[i] == buffer.getId() + 1000) {
                    space[i] = 0;
                }
            }
            if (com.getMemoryManager() != null) {
                com.getMemoryManager().freeMemory(cantidad);
            } else {
                com.getRam().setUsedSize(Math.max(0, com.getRam().getUsedSize() - cantidad));
            }
            return false;
        }
    }

    // =========================================================================
    // CICLO DE EJECUCIÓN — único punto de acceso al hardware de CPU (ARC-06)
    // =========================================================================

    /**
     * Ejecuta exactamente un ciclo temporal sobre el computador dado.
     * Centraliza todo acceso a CPU en la capa del kernel:
     * tick del scheduler, deadlines, despacho, ejecución y sync de registros.
     */
    public void executeCycle(Computer comp, int currentCycle,
                             ProducerConsumerDriver syncDriver,
                             MyLinkedList<Buffer> buffers) {
        CPU cpu = comp.getCpu();
        if (cpu == null) return;

        // 1. Notificar al planificador (quantum Round Robin, etc.)
        if (this.activeScheduler != null) {
            this.activeScheduler.onClockTick();
        }

        // 2. Verificar y vencer deadlines (D-07, D-13)
        checkDeadlines(comp, currentCycle, buffers);

        // 3. Despachar si la CPU está libre
        ProcessControlBlock currentRunning = cpu.getPCB();
        if (currentRunning == null || currentRunning.getState() != ProcessState.EJECUTANDO) {
            dispatch(comp, currentRunning);
            currentRunning = cpu.getPCB();
        }

        // 4. Ejecutar 1 ciclo si hay un proceso en CPU
        if (currentRunning != null && currentRunning.getState() == ProcessState.EJECUTANDO) {
            if (currentRunning.getType() == ProcessType.PRODUCER
                    || currentRunning.getType() == ProcessType.CONSUMER) {
                Buffer targetBuf = findBufferById(
                        parseBufferId(currentRunning.getTargetBufferId()), buffers);
                if (targetBuf != null) {
                    boolean continueRunning = syncDriver.executeCycle(currentRunning, targetBuf);
                    if (!continueRunning) {
                        if (currentRunning.getState() == ProcessState.BLOQUEADO) {
                            blockProcess(comp, currentRunning);
                        } else if (currentRunning.getState() == ProcessState.SALIENTE) {
                            terminateProcess(comp, currentRunning);
                        }
                    }
                } else {
                    currentRunning.executeOneCycle(currentCycle);
                }
            } else {
                currentRunning.executeOneCycle(currentCycle);
            }

            // Sincronizar registros hardware — solo el kernel toca la CPU
            if (cpu.getPCB() != null) {
                cpu.setPC(currentRunning.getProgramCounter());
                cpu.setMAR(currentRunning.getMemoryAddressRegister());
                cpu.setMode(currentRunning.getMode());
            }

            // Verificar terminación natural
            if (currentRunning.getState() == ProcessState.SALIENTE
                    || currentRunning.getRemainingTime() <= 0) {
                terminateProcess(comp, currentRunning);
            }
        }
    }

    private void checkDeadlines(Computer comp, int currentCycle, MyLinkedList<Buffer> buffers) {
        for (int i = 0; i < this.processList.size(); i++) {
            ProcessControlBlock p = this.processList.get(i);
            if (p != null
                    && p.getState() != ProcessState.SALIENTE
                    && p.getState() != ProcessState.NUEVO) {
                p.updateDeadlineOnTick(currentCycle);
                if (p.getRemainingDeadline() <= 0) {
                    cleanupProcessResources(p, buffers);
                    terminateProcess(comp, p);
                }
            }
        }
    }

    private void cleanupProcessResources(ProcessControlBlock pcb, MyLinkedList<Buffer> buffers) {
        if (pcb == null) return;
        if (pcb.getSyncStage() == 2 || pcb.getSyncStage() == 3) {
            Buffer buf = findBufferById(parseBufferId(pcb.getTargetBufferId()), buffers);
            if (buf != null && buf.getMutex() != null) {
                buf.getMutex().release();
            }
        }
    }

    private Buffer findBufferById(int id, MyLinkedList<Buffer> buffers) {
        if (buffers == null) return null;
        for (int i = 0; i < buffers.size(); i++) {
            Buffer b = buffers.get(i);
            if (b != null && b.getId() == id) return b;
        }
        return null;
    }

    private int parseBufferId(String targetId) {
        if (targetId == null || targetId.trim().isEmpty()) return 1;
        try {
            return Integer.parseInt(targetId.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    // --- GETTERS DE COLAS ---
    public MyLinkedList<ProcessControlBlock> getProcessList() {
        return processList;
    }

    public MyQueue<ProcessControlBlock> getBlockedQueue() {
        return blockedQueue;
    }

    public MyQueue<ProcessControlBlock> getNewQueue() {
        return newQueue;
    }

    public MyQueue<ProcessControlBlock> getReadyQueue() {
        return readyQueue;
    }

    public MyQueue<ProcessControlBlock> getEndedQueue() {
        return endedQueue;
    }

    public GlobalClock getClock() {
        return clock;
    }

    public IScheduler getActiveScheduler() {
        return activeScheduler;
    }
}
