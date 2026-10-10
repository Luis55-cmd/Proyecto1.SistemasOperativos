package com.avilaos.core.scheduler;

import com.avilaos.core.model.ProcessControlBlock;
import com.avilaos.core.model.SchedulingPolicyType;
import com.avilaos.core.structures.MyQueue;

/**
 * Planificador Round Robin con quantum de tiempo configurable.
 * Desaloja al proceso cuando consume su cuota de tiempo y lo envía al final de la cola.
 */
public class RoundRobinScheduler implements IScheduler {
    int quantum;

    @Override
    public ProcessControlBlock scheduleNext(MyQueue<ProcessControlBlock> readyQueue, ProcessControlBlock currentRunning) {
        // TODO: Selección Round Robin
        return null;
    }

    @Override
    public void addProcess(ProcessControlBlock process) {
        // TODO: Inserción al final de la cola
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
        return SchedulingPolicyType.ROUND_ROBIN;
    }

    @Override
    public void onClockTick() {
        // TODO: Decremento del quantum en curso
    }
}
