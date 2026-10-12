/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.avilaos;
import com.avilaos.core.model.ExecutionMode;
import com.avilaos.core.model.ProcessControlBlock;

/**
 *
 * @author rafr
 */
public class CPU {
    private int PC;
    private int MAR;
    private int IR;
    private ProcessControlBlock PCB;
    private ExecutionMode mode;

    public CPU(int PC, int MAR, int IR,ProcessControlBlock PCB ,ExecutionMode mode) {
        this.PC = PC;
        this.MAR = MAR;
        this.IR = IR;
        this.PCB = null;
        this.mode = ExecutionMode.USER;
        
    }

    public ExecutionMode getMode() {
        return mode;
    }

    public void setMode(ExecutionMode mode) {
        this.mode = mode;
    }

    public int getPC() {
        return PC;
    }

    public void setPC(int PC) {
        this.PC = PC;
    }

    public int getMAR() {
        return MAR;
    }

    public void setMAR(int MAR) {
        this.MAR = MAR;
    }

    public int getIR() {
        return IR;
    }

    public void setIR(int IR) {
        this.IR = IR;
    }

    public ProcessControlBlock getPCB() {
        return PCB;
    }

    public void setPCB(ProcessControlBlock PCB) {
        this.PCB = PCB;
    }

    
    
    
}
