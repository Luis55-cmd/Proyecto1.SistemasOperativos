package com.avilaos.core.scheduler;

import com.avilaos.core.model.ProcessControlBlock;
import com.avilaos.core.model.SchedulingPolicyType;
import com.avilaos.core.structures.MyQueue;

/**
 * Planificador FCFS (First-Come, First-Served).
 * Atiende a los procesos en orden estricto de llegada.
 */
public class FcfsScheduler implements IScheduler {
    // TODO: Atributos y cola de listos propia

    @Override
    public ProcessControlBlock scheduleNext(MyQueue<ProcessControlBlock> readyQueue, ProcessControlBlock currentRunning) {
        // TODO: Selección FCFS
        return null;
    }

    @Override
    public void addProcess(ProcessControlBlock process) {
        // TODO: Inserción FIFO
    }

    @Override
    public void removeProcess(ProcessControlBlock process) {
        // TODO: Eliminación
    }

    @Override
    public MyQueue<ProcessControlBlock> getReadyQueue() {
        return null;
    }

    @Override
    public SchedulingPolicyType getType() {
        return SchedulingPolicyType.FCFS;
    }

    @Override
    public void onClockTick() {
        // No requiere acción especial por tick en FCFS
    }
}
