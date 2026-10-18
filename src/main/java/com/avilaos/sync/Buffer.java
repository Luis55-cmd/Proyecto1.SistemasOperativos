/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.avilaos.sync;

import com.avilaos.core.model.BlockReason;
import com.avilaos.core.model.ProcessControlBlock;
import com.avilaos.core.model.ProcessState;
import com.avilaos.core.structures.*;
import java.util.concurrent.Semaphore;
/**
 *
 * @author rafr
 */
public class Buffer {
    Semaphore mutex;
    Semaphore vacios;
    Semaphore llenos;
    int idComp;
    int storage;
    int sizeInMemory;
    int id;
    MyQueue<Object> datos;
    private int produced;
    private int comsumed;

    private final MyQueue<ProcessControlBlock> waitingMutex = new MyQueue<>();
    private final MyQueue<ProcessControlBlock> waitingVacios = new MyQueue<>();
    private final MyQueue<ProcessControlBlock> waitingLlenos = new MyQueue<>();

    public Buffer(Semaphore mutex, Semaphore vacios, Semaphore llenos, int idComp, int storage, int id, MyQueue<Object> datos,  int sizeInMemory) {
        this.mutex = new Semaphore(1, true);
        this.vacios = new Semaphore(storage, true);
        this.llenos = new Semaphore(0, true);
        this.idComp = idComp;
        this.storage = storage;
        this.sizeInMemory = sizeInMemory;
        this.id = id;
        this.datos = datos;
        this.produced = 0;
        this.comsumed = 0;
    }

    public synchronized Object consume() {
        if (!datos.isEmpty()) {
            comsumed++;
            return datos.dequeue();
        }
        return null;
    }
    
     public synchronized void produce(Object item) {
        if (datos.size() < storage) {
            datos.enqueue(item);
            produced++;
        }
    }

    public Semaphore getMutex() {
        return mutex;
    }

    public void setMutex(Semaphore mutex) {
        this.mutex = mutex;
    }

    public Semaphore getVacios() {
        return vacios;
    }

    public void setVacios(Semaphore vacios) {
        this.vacios = vacios;
    }

    public Semaphore getLlenos() {
        return llenos;
    }

    public void setLlenos(Semaphore llenos) {
        this.llenos = llenos;
    }

    public int getIdComp() {
        return idComp;
    }

    public void setIdComp(int idComp) {
        this.idComp = idComp;
    }

    public int getStorage() {
        return storage;
    }

    public void setStorage(int storage) {
        this.storage = storage;
    }

    public int getSizeInMemory() {
        return sizeInMemory;
    }

    public void setSizeInMemory(int sizeInMemory) {
        this.sizeInMemory = sizeInMemory;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MyQueue<Object> getDatos() {
        return datos;
    }

    public void setDatos(MyQueue<Object> datos) {
        this.datos = datos;
    }

    public int getProduced() {
        return produced;
    }

    public void setProduced(int produced) {
        this.produced = produced;
    }

    public int getComsumed() {
        return comsumed;
    }

    public void setComsumed(int comsumed) {
        this.comsumed = comsumed;
    }

    public synchronized void registerWaitingMutex(ProcessControlBlock pcb) {
        if (pcb == null) return;
        this.waitingMutex.removeElement(pcb);
        this.waitingMutex.enqueue(pcb);
    }

    public synchronized ProcessControlBlock pollWaitingMutex() {
        while (!this.waitingMutex.isEmpty()) {
            ProcessControlBlock p = this.waitingMutex.dequeue();
            if (p != null
                    && p.getState() == ProcessState.BLOQUEADO
                    && p.getBlockReason() == BlockReason.WAITING_MUTEX) {
                return p;
            }
        }
        return null;
    }

    public synchronized void registerWaitingVacios(ProcessControlBlock pcb) {
        if (pcb == null) return;
        this.waitingVacios.removeElement(pcb);
        this.waitingVacios.enqueue(pcb);
    }

    public synchronized ProcessControlBlock pollWaitingVacios() {
        while (!this.waitingVacios.isEmpty()) {
            ProcessControlBlock p = this.waitingVacios.dequeue();
            if (p != null
                    && p.getState() == ProcessState.BLOQUEADO
                    && p.getBlockReason() == BlockReason.WAITING_FULL_BUFFER) {
                return p;
            }
        }
        return null;
    }

    public synchronized void registerWaitingLlenos(ProcessControlBlock pcb) {
        if (pcb == null) return;
        this.waitingLlenos.removeElement(pcb);
        this.waitingLlenos.enqueue(pcb);
    }

    public synchronized ProcessControlBlock pollWaitingLlenos() {
        while (!this.waitingLlenos.isEmpty()) {
            ProcessControlBlock p = this.waitingLlenos.dequeue();
            if (p != null
                    && p.getState() == ProcessState.BLOQUEADO
                    && p.getBlockReason() == BlockReason.WAITING_EMPTY_BUFFER) {
                return p;
            }
        }
        return null;
    }

    public synchronized void removeWaitingProcess(ProcessControlBlock pcb) {
        if (pcb == null) return;
        this.waitingMutex.removeElement(pcb);
        this.waitingVacios.removeElement(pcb);
        this.waitingLlenos.removeElement(pcb);
    }

    public MyQueue<ProcessControlBlock> getWaitingMutex() {
        return waitingMutex;
    }

    public MyQueue<ProcessControlBlock> getWaitingVacios() {
        return waitingVacios;
    }

    public MyQueue<ProcessControlBlock> getWaitingLlenos() {
        return waitingLlenos;
    }
}
