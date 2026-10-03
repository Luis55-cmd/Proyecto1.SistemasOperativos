package com.avilaos.core.scheduler;

import com.avilaos.core.model.ProcessControlBlock;
import com.avilaos.core.model.SchedulingPolicyType;
import com.avilaos.core.structures.MyQueue;

/**
 * Planificador EDF (Earliest Deadline First).
 * Prioriza y ordena la cola según el proceso con el deadline más cercano.
 */
public class EdfScheduler implements IScheduler {
    // TODO: Cola de listos ordenada por remainingDeadline

    @Override
    public ProcessControlBlock scheduleNext(MyQueue<ProcessControlBlock> readyQueue, ProcessControlBlock currentRunning) {
        // TODO: Selección por menor deadline
        return null;
    }

    @Override
    public void addProcess(ProcessControlBlock process) {
        // TODO: Inserción ordenada por deadline
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
        return SchedulingPolicyType.EDF;
    }

    @Override
    public void onClockTick() {
        // TODO: Actualización de deadlines
    }
}
