package com.avilaos.core.model;

/**
 * Estados del ciclo de vida de un proceso en ÁvilaOS.
 * Requisito: RF §1, RF §2, ARC-16.
 */
public enum ProcessState {
    NEW,
    READY,
    RUNNING,
    BLOCKED,
    TERMINATED
}