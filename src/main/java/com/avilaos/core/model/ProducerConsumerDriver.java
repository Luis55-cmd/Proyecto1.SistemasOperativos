/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.avilaos.core.model;

/**
 *
 * @author rafr
 */
public class ProducerConsumerDriver {
    private int latencia; 
    
    public ProducerConsumerDriver(int latencia) {
        this.latencia = latencia;
    }

    public int getLatencia() {
        return latencia;
    }
    
    public void setLatencia(int latencia){
        this.latencia = latencia;
    }
    
    public void blockProcess(ProcessControlBlock pcb, BlockReason reason) {
        pcb.setState(ProcessState.BLOQUEADO);
        pcb.setBlockReason(reason);
    }
    
    public boolean executeCycle(ProcessControlBlock pcb, Buffer buffer) {
        if (pcb.getComputerId() != buffer.getIdComp()) {
            if (pcb.getNetworkDelayRemaining() > 0) {
                pcb.decrementNetworkDelay();
                return true; 
            } else if (pcb.getSyncStage() == 0 && pcb.getNetworkDelayRemaining() == 0 && !pcb.isNetworkDelayDone()) {
                pcb.setNetworkDelayRemaining(latencia);
                pcb.setNetworkDelayDone(true);
                blockProcess(pcb, BlockReason.NETWORK_LATENCY);
                return false; 
            }
        }
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
                if (pcb.getProcessedElements() >= pcb.getRequiredElements() || pcb.getRemainingTime() <= 0) {
                    pcb.setState(ProcessState.SALIENTE);
                    pcb.setBlockReason(BlockReason.NONE);
                    return false;
                }
                return true;
            }
        }
        return true;
    }
    
}
