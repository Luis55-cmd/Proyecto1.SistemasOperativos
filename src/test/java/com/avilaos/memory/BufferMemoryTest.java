package com.avilaos.memory;

import com.avilaos.simulation.SimulationController;
import com.avilaos.core.model.SchedulingPolicyType;

public class BufferMemoryTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        var c = SimulationController.getInstance();
        c.reset();
        var pc = c.createComputer("RAM", 32, SchedulingPolicyType.FCFS);
        var buffer = c.createBuffer(pc.getId(), 2, 9);
        check(buffer != null && buffer.getStorage() == 2, "CAPACIDAD: conserva dos elementos");
        check(pc.getMemoryManager().getUsedMemory() == 9, "RAM: reserva tamaño declarado");
        int ocupadas = 0;
        for (int celda : pc.getRam().getSpace()) if (celda != 0) ocupadas++;
        check(ocupadas == 9, "FISICA: nueve celdas");
        var rechazado = c.createBuffer(pc.getId(), 1, 24);
        check(rechazado == null && c.getBuffers().size() == 1, "RECHAZO: tamaño no cabe");
        check(pc.getMemoryManager().getUsedMemory() == 9, "ERROR: sin reserva parcial");
        var grande = c.createBuffer(pc.getId(), 100, 3);
        check(grande != null && pc.getMemoryManager().getUsedMemory() == 12, "INDEPENDENCIA: capacidad no es RAM");
        System.out.println("PASS BufferMemoryTest");
    }
}
