package com.avilaos.simulation;

import com.avilaos.core.kernel.ComputerKernel;
import com.avilaos.core.kernel.GlobalClock;
import com.avilaos.core.kernel.IClockTickable;
import com.avilaos.core.model.BlockReason;
import com.avilaos.core.model.ExecutionMode;
import com.avilaos.core.model.ProcessControlBlock;
import com.avilaos.core.model.ProcessState;
import com.avilaos.core.model.ProcessType;
import com.avilaos.core.model.SchedulingPolicyType;
import com.avilaos.core.structures.MyLinkedList;
import com.avilaos.core.structures.MyQueue;
import com.avilaos.hardware.CPU;
import com.avilaos.hardware.Computer;
import com.avilaos.hardware.DMA;
import com.avilaos.hardware.RAM;
import com.avilaos.sync.Buffer;
import com.avilaos.sync.ProducerConsumerDriver;

/**
 * Controlador y Fachada Central de la Simulación (Capa 3: Control y Simulación).
 * Coordina la simulación del clúster ÁvilaOS, orquesta el avance temporal
 * sincronizado con GlobalClock, y expone una API limpia y desacoplada para la GUI.
 * 
 * Requisitos: ARC-02, ARC-13, ARC-14, ARC-23, D-07, D-13, Issue #26.
 */
public class SimulationController implements IClockTickable {

    private static volatile SimulationController instance;

    private final MyLinkedList<Computer> computers;
    private final MyLinkedList<Buffer> buffers;
    private final GlobalClock clock;
    private final ProducerConsumerDriver syncDriver;

    private int nextComputerId = 1;
    private int nextBufferId = 1;

    private SimulationController() {
        this.computers = new MyLinkedList<>();
        this.buffers = new MyLinkedList<>();
        this.clock = GlobalClock.getInstance();
        this.syncDriver = new ProducerConsumerDriver(2); // Latencia por defecto: 2 ciclos
        this.clock.addSubscriber(this);
    }

    public static SimulationController getInstance() {
        if (instance == null) {
            synchronized (SimulationController.class) {
                if (instance == null) {
                    instance = new SimulationController();
                }
            }
        }
        return instance;
    }

    // =========================================================================
    // MÉTODOS PARA BOTONES DE LA GUI (Gestión de Clúster, Procesos y Buffers)
    // =========================================================================

    /**
     * Crea un nuevo computador en el clúster con su propio Hardware y Kernel.
     */
    public synchronized Computer createComputer(int ramSize, SchedulingPolicyType policy) {
        int id = nextComputerId++;
        RAM ram = new RAM(ramSize, 0, new int[ramSize]);
        CPU cpu = new CPU(0, 0, 0, null, ExecutionMode.USER);
        DMA dma = new DMA();

        ComputerKernel kernel = new ComputerKernel(
                new MyLinkedList<>(),
                new MyQueue<>(),
                new MyQueue<>(),
                new MyQueue<>(),
                null, null, null,
                new MyQueue<>(),
                this.clock
        );

        Computer comp = new Computer(id, cpu, ram, dma, kernel, policy != null ? policy : SchedulingPolicyType.FCFS);
        this.computers.append(comp);
        return comp;
    }

    /**
     * Crea y admite un proceso en el computador especificado.
     */
    public synchronized ProcessControlBlock createProcess(String name, int computerId, ProcessType type,
                                                          int instructions, int memory, int priority, int deadline) {
        Computer comp = getComputerById(computerId);
        if (comp == null) {
            return null;
        }

        ProcessControlBlock pcb = new ProcessControlBlock(
                name, computerId, type != null ? type : ProcessType.CPU_BOUND,
                instructions, memory, priority, deadline
        );

        comp.getKernel().gestionCreateProcess(comp, pcb);
        return pcb;
    }

    /**
     * Crea y aloja un Búfer acotado en la memoria RAM del computador anfitrión.
     */
    public synchronized Buffer createBuffer(int hostComputerId, int storage) {
        Computer host = getComputerById(hostComputerId);
        if (host == null) {
            return null;
        }

        int id = nextBufferId++;
        Buffer buffer = new Buffer(null, null, null, hostComputerId, storage, id, new MyQueue<>(), storage);
        host.getKernel().placeBufferRAM(buffer, host);
        this.buffers.append(buffer);
        return buffer;
    }

    // =========================================================================
    // CONTROL DEL RELOJ GLOBAL (Play, Pausa, Step, Velocidad)
    // =========================================================================

    public void startSimulation() {
        this.clock.start();
    }

    public void pauseSimulation() {
        this.clock.pause();
    }

    public void stepSimulation() {
        this.clock.step();
    }

    public void setCycleDurationMs(int ms) {
        this.clock.setCycleDurationMs(ms);
    }

    public int getCurrentCycle() {
        return this.clock.getCycleCounter();
    }

    public boolean isRunning() {
        return this.clock.isRunning();
    }

    public boolean isPaused() {
        return this.clock.isPaused();
    }

    public void setNetworkLatency(int latencyCycles) {
        this.syncDriver.setLatencia(latencyCycles);
    }

    public int getNetworkLatency() {
        return this.syncDriver.getLatencia();
    }

    // =========================================================================
    // AVANCE SINCRONIZADO POR CICLO (IClockTickable)
    // =========================================================================

    @Override
    public synchronized void tick(int currentCycle) {
        for (int i = 0; i < this.computers.size(); i++) {
            Computer comp = this.computers.get(i);
            if (comp != null) {
                stepComputerCycle(comp, currentCycle);
            }
        }
    }

    /**
     * Ejecuta exactamente un ciclo temporal sobre el computador dado.
     */
    private void stepComputerCycle(Computer comp, int currentCycle) {
        ComputerKernel kernel = comp.getKernel();
        CPU cpu = comp.getCpu();
        if (kernel == null || cpu == null) {
            return;
        }

        // 1. Notificar al planificador el paso del tick (e.g. quantum en Round Robin)
        if (kernel.getActiveScheduler() != null) {
            kernel.getActiveScheduler().onClockTick();
        }

        // 2. Decrementar deadlines y verificar abortos (D-07, D-13)
        checkDeadlines(comp, currentCycle);

        // 3. Despachar si la CPU está libre
        ProcessControlBlock currentRunning = cpu.getPCB();
        if (currentRunning == null || currentRunning.getState() != ProcessState.EJECUTANDO) {
            kernel.dispatch(comp, currentRunning);
            currentRunning = cpu.getPCB();
        }

        // 4. Ejecutar 1 ciclo si hay un proceso en CPU
        if (currentRunning != null && currentRunning.getState() == ProcessState.EJECUTANDO) {
            if (currentRunning.getType() == ProcessType.PRODUCER || currentRunning.getType() == ProcessType.CONSUMER) {
                Buffer targetBuf = getBufferById(parseBufferId(currentRunning.getTargetBufferId()));
                if (targetBuf != null) {
                    boolean continueRunning = syncDriver.executeCycle(currentRunning, targetBuf);
                    if (!continueRunning) {
                        if (currentRunning.getState() == ProcessState.BLOQUEADO) {
                            kernel.blockProcess(currentRunning);
                            cpu.setPCB(null);
                        } else if (currentRunning.getState() == ProcessState.SALIENTE) {
                            kernel.terminateProcess(comp, currentRunning);
                            cpu.setPCB(null);
                        }
                    }
                } else {
                    currentRunning.executeOneCycle(currentCycle);
                }
            } else {
                currentRunning.executeOneCycle(currentCycle);
            }

            // Sincronizar registros hardware en la CPU
            cpu.setPC(currentRunning.getProgramCounter());
            cpu.setMAR(currentRunning.getMemoryAddressRegister());
            cpu.setMode(currentRunning.getMode());

            // Verificar terminación natural
            if (currentRunning.getState() == ProcessState.SALIENTE || currentRunning.getRemainingTime() <= 0) {
                kernel.terminateProcess(comp, currentRunning);
                cpu.setPCB(null);
            }
        }
    }

    /**
     * Actualiza y verifica deadlines de procesos.
     * Si vence el deadline (D-07, D-13), fuerza la terminación y libera recursos.
     */
    private void checkDeadlines(Computer comp, int currentCycle) {
        ComputerKernel kernel = comp.getKernel();
        if (kernel == null) return;

        MyLinkedList<ProcessControlBlock> list = kernel.getProcessList();
        for (int i = 0; i < list.size(); i++) {
            ProcessControlBlock p = list.get(i);
            if (p != null && p.getState() != ProcessState.SALIENTE && p.getState() != ProcessState.NUEVO) {
                p.updateDeadlineOnTick(currentCycle);
                if (p.getRemainingDeadline() <= 0) {
                    cleanupProcessResources(p);
                    kernel.terminateProcess(comp, p);
                    if (comp.getCpu().getPCB() != null && comp.getCpu().getPCB().getId() == p.getId()) {
                        comp.getCpu().setPCB(null);
                    }
                }
            }
        }
    }

    /**
     * D-13 / Issue #26: Libera recursos retenidos si un proceso vence deadline en sección crítica.
     */
    private void cleanupProcessResources(ProcessControlBlock pcb) {
        if (pcb == null) return;
        if (pcb.getSyncStage() == 2 || pcb.getSyncStage() == 3) {
            Buffer buf = getBufferById(parseBufferId(pcb.getTargetBufferId()));
            if (buf != null && buf.getMutex() != null) {
                buf.getMutex().release();
            }
        }
    }

    private int parseBufferId(String targetId) {
        if (targetId == null || targetId.trim().isEmpty()) {
            return 1;
        }
        try {
            return Integer.parseInt(targetId.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    // =========================================================================
    // GETTERS PARA CONSULTA DE ESTADO DESDE LA GUI (Observabilidad)
    // =========================================================================

    public MyLinkedList<Computer> getComputers() {
        return computers;
    }

    public MyLinkedList<Buffer> getBuffers() {
        return buffers;
    }

    public Computer getComputerById(int id) {
        for (int i = 0; i < this.computers.size(); i++) {
            Computer c = this.computers.get(i);
            if (c != null && c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    public Buffer getBufferById(int id) {
        for (int i = 0; i < this.buffers.size(); i++) {
            Buffer b = this.buffers.get(i);
            if (b != null && b.getId() == id) {
                return b;
            }
        }
        return null;
    }

    public synchronized void reset() {
        this.clock.reset();
        this.clock.addSubscriber(this);
        while (!this.computers.isEmpty()) {
            this.computers.deletePos(0);
        }
        while (!this.buffers.isEmpty()) {
            this.buffers.deletePos(0);
        }
        this.nextComputerId = 1;
        this.nextBufferId = 1;
    }
}
