package com.avilaos.sync;

import com.avilaos.core.model.BlockReason;
import com.avilaos.core.model.ExecutionMode;
import com.avilaos.core.model.ProcessControlBlock;
import com.avilaos.core.model.ProcessState;
import com.avilaos.core.model.ProcessType;

/**
 * Controlador de sincronización para procesos Productor y Consumidor.
 * Implementa el algoritmo clásico de Stallings por fases de ciclo
 * e integra el cambio de modo SYSTEM / USER y latencia de red.
 * 
 * Requisitos: D-05, D-06, D-11, SEM-02 a SEM-09, Issue #24, Issue #25.
 */
public class ProducerConsumerDriver {

    private final NetworkSimulator networkSimulator;

    public ProducerConsumerDriver(int latencia) {
        this.networkSimulator = new NetworkSimulator(latencia);
    }

    public int getLatencia() {
        return networkSimulator.getLatencyCycles();
    }

    public void setLatencia(int latencia) {
        this.networkSimulator.setLatencyCycles(latencia);
    }

    public NetworkSimulator getNetworkSimulator() {
        return networkSimulator;
    }

    public void blockProcess(ProcessControlBlock pcb, BlockReason reason) {
        pcb.setState(ProcessState.BLOQUEADO);
        pcb.setBlockReason(reason);
        pcb.setMode(ExecutionMode.USER);
    }

    public boolean executeCycle(ProcessControlBlock pcb, Buffer buffer) {
        if (pcb == null || buffer == null) {
            return false;
        }

        // Manejo de latencia de red en accesos remotos
        if (!networkSimulator.handleDelay(pcb, buffer)) {
            return false; // Proceso bloqueado esperando latencia de red
        }

        // Operaciones de sincronización de semáforos se ejecutan en modo SYSTEM (llamada al sistema)
        pcb.setMode(ExecutionMode.SYSTEM);

        switch (pcb.getSyncStage()) {
            case 0 -> {
                if (pcb.getType() == ProcessType.PRODUCER) {
                    if (!buffer.getVacios().tryAcquire()) {
                        blockProcess(pcb, BlockReason.WAITING_FULL_BUFFER);
                        return false;
                    }
                } else {
                    if (!buffer.getLlenos().tryAcquire()) {
                        blockProcess(pcb, BlockReason.WAITING_EMPTY_BUFFER);
                        return false;
                    }
                }
                pcb.setSyncStage(1);
                return true;
            }
            case 1 -> {
                if (!buffer.getMutex().tryAcquire()) {
                    blockProcess(pcb, BlockReason.WAITING_MUTEX);
                    return false;
                }
                pcb.setSyncStage(2);
                return true;
            }
            case 2 -> {
                if (pcb.getType() == ProcessType.PRODUCER) {
                    buffer.produce(pcb.getId());
                } else {
                    buffer.consume();
                }
                pcb.setSyncStage(3);
                return true;
            }
            case 3 -> {
                buffer.getMutex().release();
                pcb.setSyncStage(4);
                return true;
            }
            case 4 -> {
                if (pcb.getType() == ProcessType.PRODUCER) {
                    buffer.getLlenos().release();
                } else {
                    buffer.getVacios().release();
                }
                pcb.incrementProcessedElements();
                pcb.decrementRemainingTime();
                pcb.setSyncStage(0);
                pcb.setNetworkDelayDone(false);
                pcb.setMode(ExecutionMode.USER); // Regresa a modo usuario

                if (pcb.getProcessedElements() >= pcb.getRequiredElements() || pcb.getRemainingTime() <= 0) {
                    pcb.setState(ProcessState.SALIENTE);
                    pcb.setBlockReason(BlockReason.NONE);
                    return false;
                }
                return true;
            }
        }
        pcb.setMode(ExecutionMode.USER);
        return true;
    }
}
