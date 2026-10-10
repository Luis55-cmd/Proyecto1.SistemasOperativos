/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.avilaos;

/**
 *
 * @author rafr
 */
public class CPU {
    private int PC;
    private int MAR;
    private int IR;
    private int PSW;

    public CPU(int PC, int MAR, int IR, int PSW) {
        this.PC = PC;
        this.MAR = MAR;
        this.IR = IR;
        this.PSW = PSW;
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

    public int getPSW() {
        return PSW;
    }

    public void setPSW(int PSW) {
        this.PSW = PSW;
    }
    
    
}
