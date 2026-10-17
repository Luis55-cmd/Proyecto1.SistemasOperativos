package com.avilaos.simulation;

import com.avilaos.core.model.ProcessControlBlock;
import com.avilaos.core.model.ProcessState;
import com.avilaos.core.model.ProcessType;
import com.avilaos.core.model.SchedulingPolicyType;
import com.avilaos.hardware.Computer;
import com.avilaos.sync.Buffer;

/**
 * Pruebas unitarias y de integración para SimulationController (Capa 3).
 */
public class SimulationControllerTest {

    public static void main(String[] args) {
        testClusterCreationAndProcess();
        testSimulationTickExecution();
        testBufferCreation();
        System.out.println(">>> TODAS LAS PRUEBAS DE SimulationControllerTest PASARON EXITOSAMENTE. <<<");
    }

    private static void testClusterCreationAndProcess() {
        SimulationController controller = SimulationController.getInstance();
        controller.reset();

        Computer comp1 = controller.createComputer(1024, SchedulingPolicyType.FCFS);
        Computer comp2 = controller.createComputer(512, SchedulingPolicyType.ROUND_ROBIN);

        if (controller.getComputers().size() != 2) throw new AssertionError("Deben haber 2 computadores");
        if (comp1.getId() != 1) throw new AssertionError("comp1 debe tener id 1");
        if (comp2.getId() != 2) throw new AssertionError("comp2 debe tener id 2");

        ProcessControlBlock p1 = controller.createProcess("TestProc", 1, ProcessType.CPU_BOUND, 5, 200, 1, 50);
        if (p1 == null) throw new AssertionError("El proceso debe crearse");
        if (p1.getState() != ProcessState.LISTO) throw new AssertionError("p1 debe estar en estado LISTO");
    }

    private static void testSimulationTickExecution() {
        SimulationController controller = SimulationController.getInstance();
        controller.reset();

        Computer comp = controller.createComputer(1024, SchedulingPolicyType.FCFS);
        ProcessControlBlock p = controller.createProcess("CpuProc", comp.getId(), ProcessType.CPU_BOUND, 3, 100, 1, 50);

        // Ciclo 1: Despacha a CPU y ejecuta 1 instrucción
        controller.stepSimulation();
        if (comp.getCpu().getPCB() == null) throw new AssertionError("Debe haber proceso en CPU");
        if (comp.getCpu().getPCB().getId() != p.getId()) throw new AssertionError("El proceso en CPU debe ser p");
        if (p.getRemainingTime() != 2) throw new AssertionError("Debe restar 1 instruccion (quedan 2)");

        // Ciclo 2
        controller.stepSimulation();
        if (p.getRemainingTime() != 1) throw new AssertionError("Queda 1 instruccion");

        // Ciclo 3: Termina el proceso
        controller.stepSimulation();
        if (p.getState() != ProcessState.SALIENTE) throw new AssertionError("El proceso debe pasar a SALIENTE");
        if (comp.getKernel().getEndedQueue().size() != 1) throw new AssertionError("endedQueue debe tener al proceso terminado");
    }

    private static void testBufferCreation() {
        SimulationController controller = SimulationController.getInstance();
        controller.reset();

        Computer comp = controller.createComputer(1024, SchedulingPolicyType.FCFS);
        Buffer buf = controller.createBuffer(comp.getId(), 10);

        if (buf == null) throw new AssertionError("El buffer debe crearse");
        if (controller.getBuffers().size() != 1) throw new AssertionError("Debe haber 1 buffer registrado");
        if (buf.getIdComp() != comp.getId()) throw new AssertionError("El buffer anfitrion debe coincidir");
    }
}
