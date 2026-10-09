package com.avilaos.core.model;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bloque de Control de Proceso (PCB) de ÁvilaOS.
 * 
 * Contiene toda la metadata, métricas temporales, registros simulados
 * y parámetros de ejecución de un proceso en el clúster.
 * 
 * Requisitos: RF §1, RF §2, PCB-01 a PCB-11, D-04, D-06, D-07.
 */
public class ProcessControlBlock {

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);

    // Identificación y ubicación
    private int id;
    private String name;
    private int computerId;

    // Estados y métricas
    private ProcessState state;
    private ProcessType type;
    private BlockReason blockReason;
    private int priority;
    private int assignedMemory;

    // Tiempos e instrucciones
    private int totalInstructions;
    private int remainingTime;
    private int deadline;
    private int remainingDeadline;
    private int arrivalCycle;
    private int startCycle;
    private int completionCycle;

    // Registros hardware simulados
    private int programCounter;
    private int memoryAddressRegister;

    // Atributos para Productor / Consumidor
    private String targetBufferId;
    private int cyclesPerOperation;
    private int requiredElements;
    private int processedElements;

    /**
     * Constructor por defecto: asigna ID único generado automáticamente.
     */
    public ProcessControlBlock() {
        this.id = ID_GENERATOR.getAndIncrement();
        this.name = "Process-" + this.id;
        this.computerId = 0;
        this.state = ProcessState.NEW;
        this.type = ProcessType.CPU_BOUND;
        this.blockReason = BlockReason.NONE;
        this.priority = 1;
        this.assignedMemory = 0;
        this.totalInstructions = 10;
        this.remainingTime = 10;
        this.deadline = 100;
        this.remainingDeadline = 100;
        this.arrivalCycle = 0;
        this.startCycle = -1;
        this.completionCycle = -1;
        this.programCounter = 0;
        this.memoryAddressRegister = 0;
        this.targetBufferId = null;
        this.cyclesPerOperation = 1;
        this.requiredElements = 0;
        this.processedElements = 0;
    }

    /**
     * Constructor para procesos estándar (CPU-bound o I/O-bound).
     */
    public ProcessControlBlock(String name, int computerId, ProcessType type,
                               int totalInstructions, int assignedMemory,
                               int priority, int deadline) {
        this();
        this.name = (name != null && !name.trim().isEmpty()) ? name : "Process-" + this.id;
        this.computerId = computerId;
        this.type = (type != null) ? type : ProcessType.CPU_BOUND;
        this.totalInstructions = totalInstructions;
        this.remainingTime = totalInstructions;
        this.assignedMemory = assignedMemory;
        this.priority = priority;
        this.deadline = deadline;
        this.remainingDeadline = deadline;
    }

    /**
     * Constructor para procesos de sincronización (Productor / Consumidor).
     */
    public ProcessControlBlock(String name, int computerId, ProcessType type,
                               int totalInstructions, int assignedMemory,
                               int priority, int deadline,
                               String targetBufferId, int cyclesPerOperation,
                               int requiredElements) {
        this(name, computerId, type, totalInstructions, assignedMemory, priority, deadline);
        this.targetBufferId = targetBufferId;
        this.cyclesPerOperation = cyclesPerOperation;
        this.requiredElements = requiredElements;
        this.processedElements = 0;
    }

    /**
     * Actualiza el deadline del proceso ante un tick del reloj global.
     * En cada ciclo, si remainingDeadline <= 0, el PCB transiciona a TERMINATED inmediatamente.
     *
     * @param currentCycle Ciclo actual del reloj global.
     */
    public void updateDeadlineOnTick(int currentCycle) {
        if (this.state != ProcessState.TERMINATED) {
            this.remainingDeadline--;
            if (this.remainingDeadline <= 0) {
                this.state = ProcessState.TERMINATED;
                this.blockReason = BlockReason.NONE;
                if (this.completionCycle == -1) {
                    this.completionCycle = currentCycle;
                }
            }
        }
    }

    /**
     * Ejecuta un ciclo de procesamiento sobre la CPU simulada.
     * Si está en RUNNING, se incrementan linealmente programCounter++ y memoryAddressRegister++,
     * se decrementa remainingTime y se verifica si finalizó.
     *
     * @param currentCycle Ciclo actual del reloj global.
     */
    public void executeOneCycle(int currentCycle) {
        if (this.state == ProcessState.RUNNING) {
            if (this.startCycle == -1) {
                this.startCycle = currentCycle;
            }
            this.programCounter++;
            this.memoryAddressRegister++;
            this.remainingTime--;

            if (this.remainingTime <= 0) {
                this.state = ProcessState.TERMINATED;
                this.blockReason = BlockReason.NONE;
                this.completionCycle = currentCycle;
            }
        }
    }

    /**
     * Registra la producción o consumo de un elemento.
     * Si alcanza los elementos requeridos, pasa a TERMINATED.
     *
     * @param currentCycle Ciclo actual del reloj global.
     */
    public void registerElementProcessed(int currentCycle) {
        this.processedElements++;
        if (this.requiredElements > 0 && this.processedElements >= this.requiredElements) {
            this.state = ProcessState.TERMINATED;
            this.blockReason = BlockReason.NONE;
            this.completionCycle = currentCycle;
        }
    }

    /**
     * Finaliza forzadamente el proceso (por ejemplo, por aborto de deadline o cancelación).
     *
     * @param currentCycle Ciclo actual del reloj global.
     */
    public void terminate(int currentCycle) {
        this.state = ProcessState.TERMINATED;
        this.blockReason = BlockReason.NONE;
        if (this.completionCycle == -1) {
            this.completionCycle = currentCycle;
        }
    }

    public static void resetIdGenerator(int initialValue) {
        ID_GENERATOR.set(initialValue);
    }

    // --- GETTERS Y SETTERS ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getComputerId() {
        return computerId;
    }

    public void setComputerId(int computerId) {
        this.computerId = computerId;
    }

    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

    public ProcessType getType() {
        return type;
    }

    public void setType(ProcessType type) {
        this.type = type;
    }

    public BlockReason getBlockReason() {
        return blockReason;
    }

    public void setBlockReason(BlockReason blockReason) {
        this.blockReason = blockReason;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getAssignedMemory() {
        return assignedMemory;
    }

    public void setAssignedMemory(int assignedMemory) {
        this.assignedMemory = assignedMemory;
    }

    public int getTotalInstructions() {
        return totalInstructions;
    }

    public void setTotalInstructions(int totalInstructions) {
        this.totalInstructions = totalInstructions;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public int getDeadline() {
        return deadline;
    }

    public void setDeadline(int deadline) {
        this.deadline = deadline;
    }

    public int getRemainingDeadline() {
        return remainingDeadline;
    }

    public void setRemainingDeadline(int remainingDeadline) {
        this.remainingDeadline = remainingDeadline;
    }

    public int getArrivalCycle() {
        return arrivalCycle;
    }

    public void setArrivalCycle(int arrivalCycle) {
        this.arrivalCycle = arrivalCycle;
    }

    public int getStartCycle() {
        return startCycle;
    }

    public void setStartCycle(int startCycle) {
        this.startCycle = startCycle;
    }

    public int getCompletionCycle() {
        return completionCycle;
    }

    public void setCompletionCycle(int completionCycle) {
        this.completionCycle = completionCycle;
    }

    public int getProgramCounter() {
        return programCounter;
    }

    public void setProgramCounter(int programCounter) {
        this.programCounter = programCounter;
    }

    public int getMemoryAddressRegister() {
        return memoryAddressRegister;
    }

    public void setMemoryAddressRegister(int memoryAddressRegister) {
        this.memoryAddressRegister = memoryAddressRegister;
    }

    public String getTargetBufferId() {
        return targetBufferId;
    }

    public void setTargetBufferId(String targetBufferId) {
        this.targetBufferId = targetBufferId;
    }

    public int getCyclesPerOperation() {
        return cyclesPerOperation;
    }

    public void setCyclesPerOperation(int cyclesPerOperation) {
        this.cyclesPerOperation = cyclesPerOperation;
    }

    public int getRequiredElements() {
        return requiredElements;
    }

    public void setRequiredElements(int requiredElements) {
        this.requiredElements = requiredElements;
    }

    public int getProcessedElements() {
        return processedElements;
    }

    public void setProcessedElements(int processedElements) {
        this.processedElements = processedElements;
    }

    @Override
    public String toString() {
        return "PCB{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", pc=" + computerId +
                ", state=" + state +
                ", type=" + type +
                ", remTime=" + remainingTime +
                ", remDeadline=" + remainingDeadline +
                ", PC=" + programCounter +
                ", MAR=" + memoryAddressRegister +
                '}';
    }
}