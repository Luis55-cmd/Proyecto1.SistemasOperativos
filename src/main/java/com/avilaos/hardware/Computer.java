/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.avilaos.hardware;

import com.avilaos.core.kernel.ComputerKernel;
import com.avilaos.core.model.SchedulingPolicyType;
import com.avilaos.memory.MemoryManager;
/**
 *
 * @author rafr
 */
public class Computer {
    private int id;
    private String name;
    private CPU cpu;
    private RAM ram;
    private DMA dma;
    private ComputerKernel kernel;
    private SchedulingPolicyType politica;
    private MemoryManager memoryManager;

    public Computer(int id, CPU cpu, RAM ram, DMA dma, ComputerKernel kernel, SchedulingPolicyType politica) {
        this.id = id;
        this.name = "Computador " + id;
        this.cpu = cpu;
        this.ram = ram;
        this.dma = dma;
        this.kernel = kernel;
        this.politica = politica;
        this.memoryManager = (ram != null) ? new com.avilaos.memory.MemoryManager(ram) : null;
    }

    /** Nombre visible; el ID sigue siendo la identidad. No cambia hardware ni reloj. */
    public String getName() { return name; }

    /** Entrada nombre no vacío; elimina espacios externos. Error sin mutación. Sin ciclos. */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El computador necesita un nombre.");
        }
        this.name = name.trim();
    }

    public SchedulingPolicyType getPolitica() {
        return politica;
    }

    public void setPolitica(SchedulingPolicyType politica) {
        this.politica = politica;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CPU getCpu() {
        return cpu;
    }

    public void setCpu(CPU cpu) {
        this.cpu = cpu;
    }

    public RAM getRam() {
        return ram;
    }

    public void setRam(RAM ram) {
        this.ram = ram;
    }

    public DMA getDma() {
        return dma;
    }

    public void setDma(DMA dma) {
        this.dma = dma;
    }

    public ComputerKernel getKernel() {
        return kernel;
    }

    public void setKernel(ComputerKernel kernel) {
        this.kernel = kernel;
    }

    public com.avilaos.memory.MemoryManager getMemoryManager() {
        if (this.memoryManager == null && this.ram != null) {
            this.memoryManager = new com.avilaos.memory.MemoryManager(this.ram);
        }
        return this.memoryManager;
    }

    public void setMemoryManager(com.avilaos.memory.MemoryManager memoryManager) {
        this.memoryManager = memoryManager;
        if (memoryManager != null && memoryManager.getRam() != null) {
            this.ram = memoryManager.getRam();
        }
    }
}
