package com.avilaos.gui;

import com.avilaos.simulation.SimulationController;
import com.avilaos.core.model.*;
import java.awt.*;
import javax.swing.*;

/** Fixture de prueba, no carga procesos en la aplicación normal. */
public class MonitorSimulacionTest {
    static boolean contiene(Container padre, String texto) {
        for (Component c : padre.getComponents()) {
            if (c instanceof JLabel l && texto.equals(l.getText())) return true;
            if (c instanceof Container hijo && contiene(hijo, texto)) return true;
        }
        return false;
    }
    static void distribuir(Container c) {
        c.doLayout();
        for (Component hijo : c.getComponents()) if (hijo instanceof Container contenedor) distribuir(contenedor);
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var controller = SimulationController.getInstance(); controller.reset();
            var panel = new PanelPrincipal(); panel.mostrarPanel(PanelPrincipal.Destino.SIMULACION);
            ((JButton) CreacionComputadoresGuiTest.buscar(panel, JButton.class, "06   Métricas")).doClick();
            if (panel.getDestinoActual() != PanelPrincipal.Destino.METRICAS) throw new AssertionError("MENU: acceso directo a métricas");
            panel.mostrarPanel(PanelPrincipal.Destino.SIMULACION);
            var monitor = (MonitorSimulacion) CreacionComputadoresGuiTest.buscar(panel, MonitorSimulacion.class, null);
            var pestanas = (JTabbedPane) CreacionComputadoresGuiTest.buscar(monitor, JTabbedPane.class, null);
            if (pestanas.getTabCount() != 2 || !contiene(monitor, "Todavía no hay computadores")) throw new AssertionError("Vacío");
            ((JButton) CreacionComputadoresGuiTest.buscar(monitor, JButton.class, "Crear un computador →")).doClick();
            if (panel.getDestinoActual() != PanelPrincipal.Destino.COMPUTADOR) throw new AssertionError("Navegación");
            var norte = controller.createComputer("Norte", 128, SchedulingPolicyType.FCFS);
            controller.createComputer("Sur", 256, SchedulingPolicyType.EDF);
            panel.mostrarPanel(PanelPrincipal.Destino.SIMULACION);
            if (pestanas.getTabCount() != 4 || !contiene(monitor, "CPU libre")) throw new AssertionError("Computadores");
            ((JButton) CreacionComputadoresGuiTest.buscar(monitor, JButton.class, "Ver detalle →")).doClick();
            if (pestanas.getSelectedIndex() != 1) throw new AssertionError("Abrir detalle");
            controller.createComputer("Este", 512, SchedulingPolicyType.ROUND_ROBIN); monitor.actualizarVista();
            if (pestanas.getTabCount() != 5 || pestanas.getSelectedIndex() != 1) throw new AssertionError("Conservar pestaña");
            pestanas.setSelectedIndex(pestanas.indexOfTab("Métricas"));
            controller.createComputer("Oeste", 128, SchedulingPolicyType.FCFS); monitor.actualizarVista();
            if (!"Métricas".equals(pestanas.getTitleAt(pestanas.getSelectedIndex()))) throw new AssertionError("METRICAS: conservar selección al agregar computador");
            if (controller.getCurrentCycle() != 0) throw new AssertionError("Monitor avanzó reloj");
            controller.createProcess("Prueba CPU", norte.getId(), ProcessType.CPU_BOUND, 2, 32, 1, 100);
            monitor.actualizarVista();
            if (!contiene(monitor, "RAM 32 / 128")) throw new AssertionError("RAM");
            controller.stepSimulation(); monitor.actualizarVista();
            if (!contiene(monitor, "CPU ocupada · USER")) throw new AssertionError("CPU ocupada");
            controller.stepSimulation(); monitor.actualizarVista();
            if (!contiene(monitor, "Nuevos: 0  ·  Listos: 0  ·  Bloqueados: 0  ·  Terminados: 1")) throw new AssertionError("Fin");
            if (!contiene(monitor, "Reloj global · ciclo 2")) throw new AssertionError("Reloj global");
            pestanas.setSelectedIndex(0);
            panel.setSize(1180, 780); distribuir(panel);
            try {
                var imagen = new java.awt.image.BufferedImage(1180, 780, java.awt.image.BufferedImage.TYPE_INT_RGB);
                Graphics2D g = imagen.createGraphics(); panel.printAll(g); g.dispose();
                javax.imageio.ImageIO.write(imagen, "png", new java.io.File("/tmp/avilaos-monitor.png"));
            } catch (java.io.IOException e) { throw new RuntimeException(e); }
            System.out.println("PASS: vacío, navegación, pestañas, selección, RAM, CPU, terminación y monitor sin ticks propios");
        });
    }
}
