package com.avilaos.gui;

import com.avilaos.simulation.SimulationController;
import com.avilaos.core.model.SchedulingPolicyType;
import java.awt.*;
import javax.swing.*;

/** Prueba integrada del clic, registro real y tabla; ejecutable como main sin pantalla. */
public class CreacionComputadoresGuiTest {
    static Component buscar(Container raiz, Class<?> tipo, String texto) {
        for (Component c : raiz.getComponents()) {
            if (tipo.isInstance(c) && (texto == null || c instanceof AbstractButton b && texto.equals(b.getText()))) return c;
            if (c instanceof Container contenedor) {
                Component encontrado = buscar(contenedor, tipo, texto);
                if (encontrado != null) return encontrado;
            }
        }
        return null;
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            SimulationController controller = SimulationController.getInstance();
            controller.reset();
            PanelPrincipal panel = new PanelPrincipal();
            panel.mostrarPanel(PanelPrincipal.Destino.COMPUTADOR);
            JButton crear = (JButton) buscar(panel, JButton.class, "Crear computador");
            JTextField nombre = (JTextField) buscar(panel, JTextField.class, null);
            crear.doClick();
            if (!controller.getComputers().isEmpty()) throw new AssertionError("Nombre vacío creó un computador");
            nombre.setText("  Avila Norte  ");
            ((JToggleButton) buscar(panel, JToggleButton.class, "EDF")).doClick();
            crear.doClick(); crear.doClick();
            if (controller.getComputers().size() != 2) throw new AssertionError("Faltan instancias");
            var a = controller.getComputers().get(0);
            var b = controller.getComputers().get(1);
            if (a.getId() == b.getId() || !a.getName().equals("Avila Norte")) throw new AssertionError("Identidad/nombre");
            if (a.getRam().getTotalSize() != 128 || a.getPolitica() != SchedulingPolicyType.EDF) throw new AssertionError("Configuración");
            JTable tabla = (JTable) buscar(panel, JTable.class, null);
            if (tabla.getRowCount() != 2 || !tabla.getValueAt(0, 1).equals(a.getName())) throw new AssertionError("Tabla");
            panel.mostrarPanel(PanelPrincipal.Destino.MENU);
            panel.mostrarPanel(PanelPrincipal.Destino.COMPUTADOR);
            if (tabla.getRowCount() != 2) throw new AssertionError("Navegación");
            if (controller.getCurrentCycle() != 0 || controller.isRunning()) throw new AssertionError("Reloj arrancó");
            System.out.println("PASS: validación, dos instancias, nombre, RAM, política, tabla y reloj detenido");
        });
    }
}
