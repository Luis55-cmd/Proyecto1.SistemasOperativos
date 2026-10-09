package com.avilaos.core.model;

/**
 * Tipos de procesos admitidos en el simulador ÁvilaOS.
 * Requisito: RF §1 y RF §2.
 */
public class ProcessControlBlock{
    ProcessState state;
    

    public ProcessControlBlock(){
        this.state = ProcessState.NUEVO;
    }


}