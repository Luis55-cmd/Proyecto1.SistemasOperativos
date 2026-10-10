/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.avilaos;
import com.avilaos.core.kernel.ComputerKernel;
/**
 *
 * @author rafr
 */
public class Computer {
    private int id;
    private CPU cpu;
    private RAM ram;
    private DMA dma;
    private ComputerKernel kernel;

    public Computer(int id, CPU cpu, RAM ram, DMA dma, ComputerKernel kernel) {
        this.id = id;
        this.cpu = cpu;
        this.ram = ram;
        this.dma = dma;
        this.kernel = kernel;
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
    
}
