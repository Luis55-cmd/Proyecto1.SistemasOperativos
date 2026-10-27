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
    private final JSpinner spinnerDeadline = new JSpinner(new SpinnerNumberModel(20, 1, 100000, 1));
    
    // Selector de tipo de proceso
    private final JComboBox<ProcessType> comboTipoProceso = new JComboBox<>(ProcessType.values());
    
    // Panel y campos exclusivos para Productor/Consumidor
    private final JPanel panelSync = new JPanel(new GridLayout(1, 3, 24, 0));
    private final JComboBox<String> comboBufferId = new JComboBox<>(); 
    private final JSpinner spinnerCiclosOperacion = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
    private final JSpinner spinnerElementos = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));

    // Panel y campos exclusivos para IO-bound
    private final JPanel panelIO = new JPanel(new GridLayout(1, 2, 24, 0));
    private final JSpinner spinnerIoInterval = new JSpinner(new SpinnerNumberModel(3, 1, 10000, 1));
    private final JSpinner spinnerIoDuration = new JSpinner(new SpinnerNumberModel(2, 1, 10000, 1));
    
    private final JButton botonCrear = accion("Crear y enviar al Kernel", true);
    // ---------------------------------------------

    private record DestinoComputador(int id, String nombre) {
        @Override public String toString() { 
            return id == -1 ? nombre : nombre + " · ID " + id; 
        }
    }
    
    private final JComboBox<String> operacion = new JComboBox<>(new String[]{"CPU", "E/S"});
    private final JSpinner ticks = new JSpinner(new SpinnerNumberModel(5, 1, 10000, 1));
    private final JLabel resumen = texto("0 bloques  ·  CPU: 0 ticks  ·  E/S: 0 ticks  ·  Memoria RAM: 0", 13, TINTA, true);
    private final JLabel mensaje = texto("Agrega el primer bloque para comenzar.", 12, GRIS, false);
    private final ModeloBloques modelo = new ModeloBloques();
    private final JTable tabla = new JTable(modelo);

    public EditorProceso(Runnable irAComputadores) {
        super(new BorderLayout());
        JPanel pagina = pagina("PROCESOS", "Compón tu programa", "Define una secuencia de CPU y E/S, y el computador que la ejecutará.");
        JPanel identidad = tarjeta();
        identidad.setLayout(new BoxLayout(identidad, BoxLayout.Y_AXIS));
        
        // Fila 1: Datos de identificación
        JPanel camposFila1 = new JPanel(new GridLayout(1, 4, 16, 0));
        camposFila1.setOpaque(false);
        camposFila1.add(campo("Nombre del proceso", campoNombre));
        camposFila1.add(campo("Computador destino", destino));
        camposFila1.add(campo("Tipo de Proceso", comboTipoProceso));
        camposFila1.add(campo("Prioridad (1-10)", spinnerPrioridad));
        
        // Fila 2: Límite temporal (Deadline). La memoria RAM se deriva del conjunto de instrucciones (1 inst = 1 RAM)
        JPanel camposFila2 = new JPanel(new GridLayout(1, 4, 16, 0));
        camposFila2.setOpaque(false);
        camposFila2.add(campo("Deadline (ticks)", spinnerDeadline));
        camposFila2.add(new JLabel("")); // Espaciadores para alineación uniforme
        camposFila2.add(new JLabel(""));
        camposFila2.add(new JLabel(""));

        // Fila 3: Datos de Sincronización (Oculto por defecto)
        panelSync.setOpaque(false);
        panelSync.add(campo("ID del Buffer", comboBufferId)); 
        panelSync.add(campo("Ciclos x Operación", spinnerCiclosOperacion));
        panelSync.add(campo("Cant. Elementos", spinnerElementos));
        panelSync.setVisible(false);

        // Fila 4: Parámetros IO-bound (Oculto por defecto)
        panelIO.setOpaque(false);
        panelIO.add(campo("Ciclos CPU antes de IO (ioInterval)", spinnerIoInterval));
        panelIO.add(campo("Duración del bloqueo IO (ioDuration)", spinnerIoDuration));
        panelIO.setVisible(false);
        
        // Lógica para mostrar/ocultar las opciones de Productor/Consumidor y IO-bound
        comboTipoProceso.addActionListener(e -> {
            ProcessType tipo = (ProcessType) comboTipoProceso.getSelectedItem();
            boolean esSync = (tipo == ProcessType.PRODUCER || tipo == ProcessType.CONSUMER);
            boolean esIO = (tipo == ProcessType.IO_BOUND);
            panelSync.setVisible(esSync);
            panelIO.setVisible(esIO);
            identidad.revalidate();
            identidad.repaint();
        });

        identidad.add(camposFila1);
        identidad.add(Box.createVerticalStrut(10));
        identidad.add(camposFila2);
        identidad.add(Box.createVerticalStrut(10));
        identidad.add(panelSync);
        identidad.add(Box.createVerticalStrut(10));
        identidad.add(panelIO);
        
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
        if (ctrl.getComputers().size() > 0) {
            DestinoComputador opcionAuto = new DestinoComputador(-1, "⚡ Automático (Sender-Initiated)");
            destino.addItem(opcionAuto);
            if (compSeleccionado != null && compSeleccionado == -1) {
                destino.setSelectedItem(opcionAuto);
            }
        }
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
        int total = cpu + es;
        resumen.setText(cantidad + " bloques  ·  CPU: " + cpu + " ticks  ·  E/S: " + es + " ticks  ·  Memoria RAM: " + total + " (1 inst = 1 RAM)");
        if (total > 0) {
            spinnerDeadline.setValue(total * 2);
        }
    }

    public void generarPCB() {
        String nombre = campoNombre.getText().trim();
        Integer idComputador = getComputadorDestinoId();
        ProcessType tipo = (ProcessType) comboTipoProceso.getSelectedItem();

        try { 
            spinnerPrioridad.commitEdit(); 
            spinnerDeadline.commitEdit();
            spinnerCiclosOperacion.commitEdit();
            spinnerElementos.commitEdit();
        } catch (Exception ignored) {}
        
        int prioridad = (Integer) spinnerPrioridad.getValue();
        int deadline = (Integer) spinnerDeadline.getValue();

        if (nombre.isEmpty()) {
            mensaje.setText("Error: Escribe un nombre para el proceso.");
            return;
        }
        if (idComputador == null) {
            mensaje.setText("Error: Selecciona un computador destino.");
            return;
        }

        // 1 instrucción == 1 posición RAM (Atributo derivado del conjunto de instrucciones)
        int totalTicks = 0;
        for (int i = 0; i < cantidad; i++) {
            totalTicks += duraciones[i];
        }

        int memoria;
        if (tipo == ProcessType.PRODUCER || tipo == ProcessType.CONSUMER) {
            if (comboBufferId.getItemCount() == 0 || comboBufferId.getSelectedItem() == null) {
                mensaje.setText("Error: No puedes crear este proceso. Debes crear un Buffer primero.");
                return;
            }
            int ciclosOp = (Integer) spinnerCiclosOperacion.getValue();
            int reqElem = (Integer) spinnerElementos.getValue();
            if (ciclosOp <= 0) {
                mensaje.setText("Error: Los ciclos por operación deben ser mayores a 0.");
                return;
            }
            if (reqElem <= 0) {
                mensaje.setText("Error: La cantidad de elementos requeridos debe ser mayor a 0.");
                return;
            }
            // 5 instrucciones de sincronización del SO por cada elemento (wait, wait, op, signal, signal)
            memoria = reqElem * 5;
            if (totalTicks == 0) totalTicks = memoria;
        } else {
            if (cantidad == 0 || totalTicks <= 0) {
                mensaje.setText("Error: Añade al menos un bloque de instrucciones.");
                return;
            }
            memoria = totalTicks;
        }

        SimulationController controller = SimulationController.getInstance();
        Computer compDestino;
        if (idComputador == -1) {
            compDestino = controller.selectSenderInitiatedComputer(memoria);
            if (compDestino == null) {
                mensaje.setText("Error: Ningún computador tiene suficiente RAM total (" + memoria + ") para alojar este proceso.");
                return;
            }
            idComputador = compDestino.getId();
        } else {
            compDestino = controller.getComputerById(idComputador);
            if (compDestino == null) {
                mensaje.setText("Error: El computador destino seleccionado no existe.");
                return;
            }
        }

        int ramTotal = compDestino.getMemoryManager().getTotalMemory();
        if (memoria > ramTotal) {
            mensaje.setText("Error: El programa (" + memoria + " instrucciones/RAM) supera la RAM total (" + ramTotal + ") de " + compDestino.getName() + ".");
            return;
        }

        if (deadline <= 0) {
            mensaje.setText("Error: El deadline debe ser mayor a 0.");
            return;
        }

        ProcessControlBlock nuevoPCB;

        if (tipo == ProcessType.PRODUCER || tipo == ProcessType.CONSUMER) {
            String targetBuffer = (String) comboBufferId.getSelectedItem();
            int ciclosOp = (Integer) spinnerCiclosOperacion.getValue();
            int reqElem = (Integer) spinnerElementos.getValue();
            
            nuevoPCB = controller.createProcess(
                    nombre, idComputador, tipo, totalTicks, memoria, 
                    prioridad, deadline, targetBuffer, ciclosOp, reqElem
            );
        } else if (tipo == ProcessType.IO_BOUND) {
            int ioInterval = (Integer) spinnerIoInterval.getValue();
            int ioDuration = (Integer) spinnerIoDuration.getValue();
            nuevoPCB = controller.createProcess(
                    nombre, idComputador, tipo, totalTicks, memoria,
                    prioridad, deadline, ioInterval, ioDuration
            );
        } else {
            nuevoPCB = controller.createProcess(
                    nombre, idComputador, tipo, totalTicks, memoria, 
                    prioridad, deadline
            );
        }

        if (nuevoPCB != null) {
            String estadoMsg = (nuevoPCB.getState() == ProcessState.LISTO) 
                    ? "admitido a LISTOS" 
                    : "en cola de NUEVOS (esperando memoria)";
            mensaje.setText("¡Proceso '" + nombre + "' (" + estadoMsg + ") en " + compDestino.getName() + "!");
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