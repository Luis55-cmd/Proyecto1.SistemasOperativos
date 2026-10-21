package com.avilaos.gui;

import java.awt.*;
import com.avilaos.simulation.SimulationController;
import com.avilaos.hardware.Computer;
import com.avilaos.sync.Buffer;
import com.avilaos.core.model.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import static com.avilaos.gui.PanelPrincipal.*;

/** Borrador visual de programa. Incluye opciones avanzadas para Productor/Consumidor e inyección al Kernel. */
public class EditorProceso extends JPanel {
    // Arreglos propios del borrador
    private final String[] operaciones = new String[100];
    private final int[] duraciones = new int[100];
    private int cantidad;
    private final JComboBox<DestinoComputador> destino = new JComboBox<>();
    private final JButton crearComputador = accion("Crea un computador primero →", true);

    // --- COMPONENTES DEL ENCABEZADO Y CREACIÓN ---
    private final JTextField campoNombre = new JTextField();
    private final JSpinner spinnerPrioridad = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
    
    // Selector de tipo de proceso
    private final JComboBox<ProcessType> comboTipoProceso = new JComboBox<>(ProcessType.values());
    
    // Panel y campos exclusivos para Productor/Consumidor
    private final JPanel panelSync = new JPanel(new GridLayout(1, 3, 24, 0));
    private final JComboBox<String> comboBufferId = new JComboBox<>(); 
    private final JSpinner spinnerCiclosOperacion = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
    private final JSpinner spinnerElementos = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
    
    private final JButton botonCrear = accion("Crear y enviar al Kernel", true);
    // ---------------------------------------------

    private record DestinoComputador(int id, String nombre) {
        @Override public String toString() { return nombre + " · ID " + id; }
    }
    
    private final JComboBox<String> operacion = new JComboBox<>(new String[]{"CPU", "E/S"});
    private final JSpinner ticks = new JSpinner(new SpinnerNumberModel(5, 1, 10000, 1));
    private final JLabel resumen = texto("0 bloques  ·  CPU: 0 ticks  ·  E/S: 0 ticks", 13, TINTA, true);
    private final JLabel mensaje = texto("Agrega el primer bloque para comenzar.", 12, GRIS, false);
    private final ModeloBloques modelo = new ModeloBloques();
    private final JTable tabla = new JTable(modelo);

    public EditorProceso(Runnable irAComputadores) {
        super(new BorderLayout());
        JPanel pagina = pagina("PROCESOS", "Compón tu programa", "Define una secuencia de CPU y E/S, y el computador que la ejecutará.");
        JPanel identidad = tarjeta();
        identidad.setLayout(new BoxLayout(identidad, BoxLayout.Y_AXIS));
        
        // Fila 1: Datos generales
        JPanel camposGral = new JPanel(new GridLayout(1, 4, 16, 0));
        camposGral.setOpaque(false);
        camposGral.add(campo("Nombre del proceso", campoNombre));
        camposGral.add(campo("Computador destino", destino));
        camposGral.add(campo("Prioridad", spinnerPrioridad));
        camposGral.add(campo("Tipo de Proceso", comboTipoProceso));
        
        // Fila 2: Datos de Sincronización (Oculto por defecto)
        panelSync.setOpaque(false);
        panelSync.add(campo("ID del Buffer", comboBufferId)); 
        panelSync.add(campo("Ciclos x Operación", spinnerCiclosOperacion));
        panelSync.add(campo("Cant. Elementos", spinnerElementos));
        panelSync.setVisible(false);
        
        // Lógica para mostrar/ocultar las opciones de Productor/Consumidor
        comboTipoProceso.addActionListener(e -> {
            ProcessType tipo = (ProcessType) comboTipoProceso.getSelectedItem();
            boolean esSync = (tipo == ProcessType.PRODUCER || tipo == ProcessType.CONSUMER);
            panelSync.setVisible(esSync);
            identidad.revalidate();
            identidad.repaint();
        });

        identidad.add(camposGral);
        identidad.add(Box.createVerticalStrut(12));
        identidad.add(panelSync);
        
        crearComputador.addActionListener(e -> irAComputadores.run());
        crearComputador.setAlignmentX(Component.LEFT_ALIGNMENT);
        identidad.add(Box.createVerticalStrut(10));
        identidad.add(crearComputador);
        
        pagina.add(identidad);
        pagina.add(Box.createVerticalStrut(18));

        // --- RESTO DE LA INTERFAZ VISUAL ---
        JPanel editor = tarjeta();
        editor.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        editor.add(texto("Secuencia del programa", 19, TINTA, true));
        editor.add(Box.createVerticalStrut(8));
        editor.add(texto("CPU: trabajo de cálculo. E/S: duración de una operación de espera.", 12, GRIS, false));
        editor.add(Box.createVerticalStrut(18));
        
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        controles.setOpaque(false); controles.setAlignmentX(LEFT_ALIGNMENT);
        operacion.setPreferredSize(new Dimension(90, 36));
        ticks.setPreferredSize(new Dimension(90, 36));
        controles.add(operacion); controles.add(Box.createHorizontalStrut(12));
        controles.add(ticks); controles.add(texto("  ticks   ", 13, GRIS, false));
        JButton agregar = accion("+ Agregar bloque", true);
        agregar.addActionListener(e -> agregarBloque());
        controles.add(agregar);
        controles.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        editor.add(controles); editor.add(Box.createVerticalStrut(16));

        tabla.setRowHeight(36);
        tabla.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        tabla.setForeground(TINTA); tabla.setBackground(PAPEL);
        tabla.setSelectionBackground(new Color(225, 233, 209));
        tabla.setSelectionForeground(TINTA);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(new Color(235, 230, 215));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 32));
        tabla.setFillsViewportHeight(true);
        
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setColumnHeaderView(tabla.getTableHeader());
        scroll.setAlignmentX(LEFT_ALIGNMENT);
        scroll.setPreferredSize(new Dimension(500, 160));
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));
        editor.add(scroll); editor.add(Box.createVerticalStrut(12));
        
        JPanel acciones = new JPanel(new BorderLayout()); acciones.setOpaque(false);
        acciones.setAlignmentX(LEFT_ALIGNMENT);
        JButton quitar = accion("Quitar seleccionado", false);
        quitar.addActionListener(e -> quitarBloque());
        acciones.add(quitar, BorderLayout.WEST);
        
        JPanel panelCrear = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelCrear.setOpaque(false);
        botonCrear.addActionListener(e -> generarPCB());
        panelCrear.add(botonCrear);
        acciones.add(panelCrear, BorderLayout.CENTER);

        acciones.add(resumen, BorderLayout.EAST);
        acciones.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        mensaje.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        editor.add(acciones); editor.add(Box.createVerticalStrut(10)); editor.add(mensaje);
        pagina.add(editor); pagina.add(Box.createVerticalStrut(14));
        pagina.add(texto("Borrador local · Hasta 100 bloques de 1–10 000 ticks. No se envía al kernel.", 12, GRIS, false));
        pagina.add(Box.createVerticalStrut(6));
        pagina.add(texto("La memoria calculada y los protocolos de sincronización se conectarán después.", 12, GRIS, false));
        add(pagina);

        // --- CONEXIÓN AUTOMÁTICA AL CONTROLADOR ---
        SimulationController.getInstance().addGUIListener(this::actualizarListas);
        actualizarListas(); // Llamada inicial
    }

    /** Extrae los computadores y buffers disponibles directamente del controlador */
    public void actualizarListas() {
        SimulationController ctrl = SimulationController.getInstance();
        
        // 1. Actualizar Computadores
        Integer compSeleccionado = getComputadorDestinoId();
        destino.removeAllItems();
        for (int i = 0; i < ctrl.getComputers().size(); i++) {
            Computer c = ctrl.getComputers().get(i);
            DestinoComputador opcion = new DestinoComputador(c.getId(), c.getName());
            destino.addItem(opcion);
            if (compSeleccionado != null && compSeleccionado == c.getId()) destino.setSelectedItem(opcion);
        }
        boolean hayComputadores = destino.getItemCount() > 0;
        destino.setEnabled(hayComputadores);
        crearComputador.setVisible(!hayComputadores);

        // 2. Actualizar Buffers
        String bufferSeleccionado = (String) comboBufferId.getSelectedItem();
        comboBufferId.removeAllItems();
        boolean contieneBuffer = false;
        
        for (int i = 0; i < ctrl.getBuffers().size(); i++) {
            Buffer b = ctrl.getBuffers().get(i);
            String idStr = String.valueOf(b.getId());
            comboBufferId.addItem(idStr);
            if (bufferSeleccionado != null && bufferSeleccionado.equals(idStr)) {
                contieneBuffer = true;
            }
        }
        
        if (contieneBuffer) {
            comboBufferId.setSelectedItem(bufferSeleccionado);
        }
    }

    public Integer getComputadorDestinoId() {
        DestinoComputador seleccionado = (DestinoComputador) destino.getSelectedItem();
        return seleccionado == null ? null : seleccionado.id();
    }

    private void agregarBloque() {
        if (cantidad == operaciones.length) {
            mensaje.setText("Límite del prototipo: 100 bloques. Quita uno antes de agregar otro."); return;
        }
        try { ticks.commitEdit(); }
        catch (java.text.ParseException e) { mensaje.setText("Escribe un número entero entre 1 y 10 000."); return; }
        int valor = ((Number) ticks.getValue()).intValue();
        if (valor < 1 || valor > 10000) { mensaje.setText("Los ticks deben estar entre 1 y 10 000."); return; }
        operaciones[cantidad] = (String) operacion.getSelectedItem();
        duraciones[cantidad] = valor;
        cantidad++;
        modelo.fireTableRowsInserted(cantidad - 1, cantidad - 1);
        tabla.setRowSelectionInterval(cantidad - 1, cantidad - 1);
        actualizarResumen();
        mensaje.setText("Bloque agregado. La secuencia se lee de arriba hacia abajo.");
    }

    private void quitarBloque() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { mensaje.setText("Selecciona el bloque que quieres quitar."); return; }
        for (int i = fila; i < cantidad - 1; i++) {
            operaciones[i] = operaciones[i + 1]; duraciones[i] = duraciones[i + 1];
        }
        cantidad--;
        operaciones[cantidad] = null; duraciones[cantidad] = 0;
        modelo.fireTableDataChanged();
        actualizarResumen();
        mensaje.setText("Bloque eliminado. El resto conserva su orden.");
    }

    private void actualizarResumen() {
        int cpu = 0, es = 0;
        for (int i = 0; i < cantidad; i++) {
            if ("CPU".equals(operaciones[i])) cpu += duraciones[i]; else es += duraciones[i];
        }
        resumen.setText(cantidad + " bloques  ·  CPU: " + cpu + " ticks  ·  E/S: " + es + " ticks");
    }

    public void generarPCB() {
        String nombre = campoNombre.getText().trim();
        Integer idComputador = getComputadorDestinoId();
        ProcessType tipo = (ProcessType) comboTipoProceso.getSelectedItem();

        try { 
            spinnerPrioridad.commitEdit(); 
            spinnerCiclosOperacion.commitEdit();
            spinnerElementos.commitEdit();
        } catch (Exception ignored) {}
        
        int prioridad = (Integer) spinnerPrioridad.getValue();

        if (nombre.isEmpty()) {
            mensaje.setText("Error: Escribe un nombre para el proceso.");
            return;
        }
        if (idComputador == null) {
            mensaje.setText("Error: Selecciona un computador destino.");
            return;
        }
        if (cantidad == 0) {
            mensaje.setText("Error: Añade al menos un bloque de ejecución.");
            return;
        }
        
        if (tipo == ProcessType.PRODUCER || tipo == ProcessType.CONSUMER) {
            if (comboBufferId.getItemCount() == 0 || comboBufferId.getSelectedItem() == null) {
                mensaje.setText("Error: No puedes crear este proceso. Debes crear un Buffer primero.");
                return;
            }
        }

        int totalTicks = 0;
        for (int i = 0; i < cantidad; i++) {
            totalTicks += duraciones[i];
        }

        int memoriaAsignada = 100;
        int deadline = totalTicks * 2;
        
        SimulationController controller = SimulationController.getInstance();
        ProcessControlBlock nuevoPCB;

        if (tipo == ProcessType.PRODUCER || tipo == ProcessType.CONSUMER) {
            String targetBuffer = (String) comboBufferId.getSelectedItem();
            int ciclosOp = (Integer) spinnerCiclosOperacion.getValue();
            int reqElem = (Integer) spinnerElementos.getValue();
            
            nuevoPCB = controller.createProcess(
                    nombre, idComputador, tipo, totalTicks, memoriaAsignada, 
                    prioridad, deadline, targetBuffer, ciclosOp, reqElem
            );
        } else {
            nuevoPCB = controller.createProcess(
                    nombre, idComputador, tipo, totalTicks, memoriaAsignada, 
                    prioridad, deadline
            );
        }

        if (nuevoPCB != null) {
            mensaje.setText("¡PCB '" + nombre + "' enviado exitosamente al Computador " + idComputador + "!");
        } else {
            mensaje.setText("Error: No se pudo inyectar el proceso en el simulador.");
        }
    }

    private class ModeloBloques extends AbstractTableModel {
        @Override public int getRowCount() { return cantidad; }
        @Override public int getColumnCount() { return 3; }
        @Override public String getColumnName(int columna) {
            return switch (columna) { case 0 -> "ORDEN"; case 1 -> "OPERACIÓN"; default -> "DURACIÓN"; };
        }
        @Override public Object getValueAt(int fila, int columna) {
            return switch (columna) { case 0 -> fila + 1; case 1 -> operaciones[fila]; default -> duraciones[fila] + " ticks"; };
        }
    }
}