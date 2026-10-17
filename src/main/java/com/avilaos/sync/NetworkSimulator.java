package com.avilaos.sync;

import com.avilaos.core.model.BlockReason;
import com.avilaos.core.model.ProcessControlBlock;
import com.avilaos.core.model.ProcessState;

/**
 * Simulador de latencia de red para ÁvilaOS en accesos a buffers remotos.
 * Permite simular el retardo de comunicación entre diferentes computadores del clúster.
 * 
 * Requisitos: D-11, BUF-14, BUF-15, Issue #25.
 */
public class NetworkSimulator {

    private int latencyCycles;

    public NetworkSimulator() {
        this(0);
    }

    public NetworkSimulator(int latencyCycles) {
        this.latencyCycles = Math.max(0, latencyCycles);
    }

    public int getLatencyCycles() {
        return latencyCycles;
    }

    public void setLatencyCycles(int latencyCycles) {
        this.latencyCycles = Math.max(0, latencyCycles);
    }

    public boolean isRemoteAccess(ProcessControlBlock pcb, Buffer buffer) {
        if (pcb == null || buffer == null) {
            return false;
        }
        return pcb.getComputerId() != buffer.getIdComp();
    }

    public boolean handleDelay(ProcessControlBlock pcb, Buffer buffer) {
        if (!isRemoteAccess(pcb, buffer) || latencyCycles <= 0) {
            return true; // Acceso local o sin latencia
        }

        if (pcb.getNetworkDelayRemaining() > 0) {
            pcb.decrementNetworkDelay();
            if (pcb.getNetworkDelayRemaining() == 0) {
                pcb.setNetworkDelayDone(true);
                pcb.setState(ProcessState.LISTO);
                pcb.setBlockReason(BlockReason.NONE);
            }
            return true;
        } else if (pcb.getSyncStage() == 0 && !pcb.isNetworkDelayDone()) {
            pcb.setNetworkDelayRemaining(this.latencyCycles);
            pcb.setNetworkDelayDone(true);
            pcb.setState(ProcessState.BLOQUEADO);
            pcb.setBlockReason(BlockReason.NETWORK_LATENCY);
            return false;
        }

        return true;
    }
}
