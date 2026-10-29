package com.avilaos.gui;

import com.avilaos.core.model.ProcessControlBlock;
import com.avilaos.core.model.BlockReason;
import com.avilaos.core.model.ProcessType;
import com.avilaos.core.model.SchedulingPolicyType;
import com.avilaos.simulation.SimulationController;
import java.lang.reflect.Field;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.SwingUtilities;

/** Comprueba que los datos visibles llegan al PCB sin secuencias ficticias. */
public class EditorProcesoFormularioTest {
    private static final SimulationController c = SimulationController.getInstance();
    private static EditorProceso editor;
    private static Object campo(String nombre) {
        try { Field f = EditorProceso.class.getDeclaredField(nombre); f.setAccessible(true); return f.get(editor); }
        catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }
    private static void numero(String nombre, int valor) { ((JSpinner) campo(nombre)).setValue(valor); }
    private static void check(boolean valor, String mensaje) { if (!valor) throw new AssertionError(mensaje); }
    @SuppressWarnings("unchecked")
    private static void enviar(ProcessType tipo, String nombre) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ((JComboBox<ProcessType>) campo("comboTipoProceso")).setSelectedItem(tipo);
            ((JTextField) campo("campoNombre")).setText(nombre);
            numero("spinnerMemoria", 17);
            numero("spinnerDeadline", 90);
            numero("spinnerInstrucciones", 12);
            numero("spinnerIoInterval", 4);
            numero("spinnerIoDuration", 3);
            numero("spinnerCiclosOperacion", 2);
            numero("spinnerElementos", 3);
            editor.generarPCB();
        });
        GuiTestDrain.await();
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            c.reset();
            var pc = c.createComputer("Equipo", 128, SchedulingPolicyType.FCFS);
            c.createBuffer(pc.getId(), 2);
            editor = new EditorProceso(() -> {});
        });
        enviar(ProcessType.CPU_BOUND, "CPU");
        enviar(ProcessType.IO_BOUND, "IO");
        enviar(ProcessType.PRODUCER, "Productor");
        enviar(ProcessType.CONSUMER, "Consumidor");
        check(c.getAllProcesses().size() == 4, "Se esperaban cuatro procesos");
        for (int i = 0; i < 4; i++) {
            ProcessControlBlock p = c.getAllProcesses().get(i);
            check(p.getRequiredMemory() == 17 && p.getDeadline() == 90, "Memoria o deadline " + p.getName());
            check(p.getTotalInstructions() == (i < 2 ? 12 : 21), "Instrucciones " + p.getName());
            if (i == 1) check(p.getIoInterval() == 4 && p.getIoDuration() == 3, "Parámetros E/S");
            if (i >= 2) check("1".equals(p.getTargetBufferId()) && p.getCyclesPerOperation() == 2
                    && p.getRequiredElements() == 3, "Parámetros buffer");
        }
        SwingUtilities.invokeAndWait(() -> {
            var tabla = (DefaultTableModel) campo("procesosVisibles");
            check(tabla.getRowCount() == 4, "Tabla no muestra los cuatro procesos");
            check(tabla.getValueAt(0, 1).equals("CPU") && tabla.getValueAt(0, 5).equals(17), "Fila CPU o memoria");
            check(tabla.getValueAt(1, 9).toString().contains("cada 4"), "Faltan parámetros E/S");
            check(tabla.getValueAt(2, 9).toString().contains("3 elementos"), "Faltan parámetros de productor");
            ((JTextField) campo("campoNombre")).setText("   ");
            editor.generarPCB();
        });
        check(c.getAllProcesses().size() == 4, "Aceptó nombre vacío");
        SwingUtilities.invokeAndWait(() -> {
            c.reset();
            c.createComputer("E/S", 128, SchedulingPolicyType.FCFS);
            editor = new EditorProceso(() -> {});
            ((JComboBox<ProcessType>) campo("comboTipoProceso")).setSelectedItem(ProcessType.IO_BOUND);
            ((JTextField) campo("campoNombre")).setText("Prueba E/S");
            numero("spinnerDeadline", 100);
            numero("spinnerInstrucciones", 1);
            numero("spinnerIoInterval", 1);
            editor.generarPCB();
            check(((JLabel) campo("mensaje")).getText().contains("al menos 2"), "Mensaje de mínimo");
            numero("spinnerInstrucciones", 10);
            numero("spinnerIoInterval", 10);
            editor.generarPCB();
            check(((JLabel) campo("mensaje")).getText().contains("menor"), "Mensaje de igualdad");
            numero("spinnerIoInterval", 15);
            editor.generarPCB();
            check(((JLabel) campo("mensaje")).getText().contains("menor"), "Mensaje de intervalo excesivo");
            check(c.getAllProcesses().size() == 0, "Se creó un I/O bound inválido");
            numero("spinnerIoInterval", 9);
            editor.generarPCB();
        });
        GuiTestDrain.await();
        check(c.getAllProcesses().size() == 1, "No creó el caso de frontera válido");
        ProcessControlBlock io = c.getAllProcesses().get(0);
        for (int i = 0; i < 9; i++) c.stepSimulation();
        check(io.getProgramCounter() == 9 && io.getBlockReason() == BlockReason.WAITING_IO,
                "El proceso válido no solicitó E/S tras nueve instrucciones");
        System.out.println("PASS: cuatro tipos y validación I/O bound, incluido bloqueo en el límite");
        c.reset();
        System.exit(0);
    }
}
