package com.avilaos.core.scheduler;

import java.util.Comparator;

import com.avilaos.core.model.ProcessControlBlock;
import com.avilaos.core.model.ProcessState;
import com.avilaos.core.model.SchedulingPolicyType;
import com.avilaos.core.structures.MyLinkedList;
import com.avilaos.core.structures.MyQueue;

/**
 * Planificador EDF (Earliest Deadline First).
 * Ordena la cola de listos de menor a mayor remainingDeadline y admite desalojo
 * si llega un proceso con un deadline más cercano que el que está en CPU.
 * 
 * Requisitos: RF §1, RF §3, PLA-02.
 */
public class EdfScheduler implements IScheduler {

    private final MyQueue<ProcessControlBlock> readyQueue;

    private static final Comparator<ProcessControlBlock> EDF_COMPARATOR = (p1, p2) -> {
        int cmp = Integer.compare(p1.getRemainingDeadline(), p2.getRemainingDeadline());
        if (cmp == 0) {
            return Integer.compare(p1.getId(), p2.getId());
        }
        return cmp;
    };

    public EdfScheduler() {
        this.readyQueue = new MyQueue<>();
    }

    @Override
    public ProcessControlBlock scheduleNext(MyQueue<ProcessControlBlock> queue, ProcessControlBlock currentRunning) {
        MyQueue<ProcessControlBlock> targetQueue = (queue != null) ? queue : this.readyQueue;

        // Si hay un proceso en CPU, verificamos si hay algún candidato con deadline más urgente (desalojo)
        if (currentRunning != null && currentRunning.getState() == ProcessState.RUNNING) {
            if (!targetQueue.isEmpty()) {
                ProcessControlBlock candidate = targetQueue.peek();
                if (candidate.getRemainingDeadline() < currentRunning.getRemainingDeadline()) {
                    // Desalojo: el actual vuelve a la cola y se reordena
                    currentRunning.setState(ProcessState.READY);
                    targetQueue.enqueue(currentRunning);
                    sortQueue(targetQueue);

                    ProcessControlBlock next = targetQueue.dequeue();
                    next.setState(ProcessState.RUNNING);
                    return next;
                }
            }
            return currentRunning;
        }

        // Si la CPU está libre
        if (!targetQueue.isEmpty()) {
            ProcessControlBlock next = targetQueue.dequeue();
            next.setState(ProcessState.RUNNING);
            return next;
        }

        return null;
    }

    @Override
    public void addProcess(ProcessControlBlock process) {
        if (process != null) {
            process.setState(ProcessState.READY);
            this.readyQueue.enqueue(process);
            sortQueue(this.readyQueue);
        }
    }

    @Override
    public void removeProcess(ProcessControlBlock process) {
        if (process != null && !readyQueue.isEmpty()) {
            int count = readyQueue.size();
            for (int i = 0; i < count; i++) {
                ProcessControlBlock current = readyQueue.dequeue();
                if (current.getId() != process.getId()) {
                    readyQueue.enqueue(current);
                }
            }
        }
    }

    @Override
    public MyQueue<ProcessControlBlock> getReadyQueue() {
        return this.readyQueue;
    }

    @Override
    public SchedulingPolicyType getType() {
        return SchedulingPolicyType.EDF;
    }

    @Override
    public void onClockTick() {
        // Mantiene la cola ordenada tras el decremento de deadlines
        sortQueue(this.readyQueue);
    }

    private void sortQueue(MyQueue<ProcessControlBlock> queue) {
        if (queue == null || queue.size() <= 1) {
            return;
        }
        MyLinkedList<ProcessControlBlock> list = new MyLinkedList<>();
        while (!queue.isEmpty()) {
            list.add(queue.dequeue());
        }
        list.sort(EDF_COMPARATOR);
        for (ProcessControlBlock p : list) {
            queue.enqueue(p);
        }
    }
}
