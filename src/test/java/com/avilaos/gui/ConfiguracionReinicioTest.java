package com.avilaos.gui;
import com.avilaos.simulation.SimulationController;
import java.io.File;
import java.nio.file.Files;

public class ConfiguracionReinicioTest {
    public static void main(String[] args) throws Exception {
        var c = SimulationController.getInstance();
        var m = new MonitorSimulacion(c, () -> {});
        if (!m.cargarEstado(new File(args[0]))) throw new AssertionError("Fallo al cargar escenario");
        for (int i=0; i<9; i++) c.stepSimulation();
        if (!c.getAllProcesses().get(0).isTerminated()) throw new AssertionError("No terminó el proceso");
        File salida = Files.createTempFile("avilaos-config-", ".json").toFile();
        try {
            if (!m.guardarEstado(salida)) throw new AssertionError("Falló el guardado");
            if (!m.cargarEstado(salida)) throw new AssertionError("Falló la recarga");
            if (c.getCurrentCycle()!=0 || c.getAllProcesses().size()!=3 || c.getBuffers().size()!=1)
                throw new AssertionError("Configuración inicial incompleta");
            var p=c.getAllProcesses().get(0);
            if (p.getProgramCounter()!=0 || p.getAssignedMemory()!=9 || p.isTerminated())
                throw new AssertionError("Proceso terminado no reinició con su memoria original");
            if (!"NUEVO".equals(c.getAllProcesses().get(1).getState().name()))
                throw new AssertionError("El proceso que no cabe debe esperar en nuevos");
            System.out.println("PASS: guardar tras terminar y reiniciar configuración");
        } finally { salida.delete(); }
    }
}
