package com.avilaos.gui;
import com.avilaos.simulation.SimulationSnapshot;
import com.avilaos.simulation.SimulationSnapshot.Node;
import com.avilaos.concurrency.GuiCommands;
import com.avilaos.concurrency.StateAccess;

import com.avilaos.hardware.Computer;
import com.avilaos.core.model.BlockReason;
import com.avilaos.core.model.ProcessControlBlock;
import com.avilaos.core.model.ProcessState;
import com.avilaos.core.model.ProcessType;
import com.avilaos.core.model.SchedulingPolicyType;
import com.avilaos.core.structures.MyLinkedList;
import com.avilaos.simulation.SimulationController;
import com.avilaos.sync.Buffer;
import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultCaret;
import static com.avilaos.gui.PanelPrincipal.*;

/** Monitor de lectura interactivo. Se actualiza vía Observer desde el SimulationController. */
public final class MonitorSimulacion extends JPanel {
    private final SimulationController controller;
    private SimulationSnapshot view;
    private final JTabbedPane pestanas = new JTabbedPane(JTabbedPane.BOTTOM, JTabbedPane.SCROLL_TAB_LAYOUT);
    private final JPanel tarjetas = new JPanel(new GridLayout(0, 2, 16, 16));
    private final JLabel reloj = texto("Reloj global · ciclo 0", 20, ACENTO, true);
    private final JLabel estado = texto("", 13, GRIS, false);
    private final JPanel vacio = tarjeta();
    private int[] ids = new int[0];
    private VistaComputador[] resumenes = new VistaComputador[0];
    private VistaComputador[] detalles = new VistaComputador[0];
    private PanelMetricas panelMetricas;
    private final JButton iniciar = accion("Iniciar", true);
    private final JButton pausar = accion("Pausar", false);
    private final JButton paso = accion("+1 tick", false);
    private final JButton aplicar = accion("Aplicar", false);
    private final JComboBox<Integer> intervalo = new JComboBox<>(new Integer[]{100, 250, 500, 1000});
    private final JLabel intervaloAplicado = texto("", 12, GRIS, false);
    
    private final DefaultTableModel modeloColas = new DefaultTableModel(new Object[]{"Nuevos", "Listos", "Bloqueados", "Terminados"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tablaColas = new JTable(modeloColas);
    private final JScrollPane scrollColas = new JScrollPane(tablaColas);
    private final JPanel panelDetalleColas = new JPanel(new BorderLayout(0, 8));

    // --- Componentes para visualizar los Buffers Globales ---
    private final DefaultTableModel modeloBuffers = new DefaultTableModel(new Object[]{"Buffer", "Elementos", "Capacidad", "Semáforos (M, L, V)", "Procesos Bloqueados"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tablaBuffers = new JTable(modeloBuffers);
    private final JScrollPane scrollBuffers = new JScrollPane(tablaBuffers);
    private final JPanel panelDetalleBuffers = new JPanel(new BorderLayout(0, 8));

    private final JTextArea areaLog = new JTextArea();
    private final JScrollPane scrollLog = new JScrollPane(areaLog);
    private final JPanel panelLog = new JPanel(new BorderLayout(0, 8));

    public MonitorSimulacion(SimulationController controller, Runnable irAComputadores) {
        super(new BorderLayout(0, 0));
        this.controller = controller;
        this.view=controller.snapshot();
        setBackground(FONDO);
        
        JPanel contenedorPrincipal = new ScrollablePanel(new BorderLayout(0, 20));
        contenedorPrincipal.setBackground(FONDO);
        contenedorPrincipal.setBorder(BorderFactory.createEmptyBorder(28, 28, 24, 28));
        
        JPanel cabecera = new JPanel();
        cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.Y_AXIS));
        cabecera.setOpaque(false);
        cabecera.add(texto("SIMULACIÓN", 11, ACENTO, true));
        cabecera.add(Box.createVerticalStrut(8));
        cabecera.add(texto("Observa tu sistema", 28, TINTA, true));
        cabecera.add(Box.createVerticalStrut(12));
        cabecera.add(crearPanelPersistencia()); // Panel de Guardar/Cargar integrado
        cabecera.add(Box.createVerticalStrut(12));
        cabecera.add(reloj);
        cabecera.add(Box.createVerticalStrut(8));
        cabecera.add(estado);
        cabecera.add(Box.createVerticalStrut(12));
        cabecera.add(controlesReloj());
        cabecera.add(Box.createVerticalStrut(6));
        cabecera.add(intervaloAplicado);
        cabecera.add(Box.createVerticalStrut(16));
        
        tablaColas.setFillsViewportHeight(true);
        tablaColas.getTableHeader().setReorderingAllowed(false);
        tablaColas.setRowHeight(24);
        scrollColas.setPreferredSize(new Dimension(Integer.MAX_VALUE, 120));
        scrollColas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        panelDetalleColas.setOpaque(false);
        panelDetalleColas.setAlignmentX(LEFT_ALIGNMENT);
        JLabel tituloColas = texto("DETALLE DE COLAS DEL KERNEL (Comp. seleccionado)", 12, TINTA, true);
        panelDetalleColas.add(tituloColas, BorderLayout.NORTH);
        panelDetalleColas.add(scrollColas, BorderLayout.CENTER);
        panelDetalleColas.setVisible(false);
        
        // --- Configuración gráfica de la tabla de Buffers ---
        tablaBuffers.setFillsViewportHeight(true);
        tablaBuffers.getTableHeader().setReorderingAllowed(false);
        tablaBuffers.setRowHeight(24);
        scrollBuffers.setPreferredSize(new Dimension(Integer.MAX_VALUE, 120));
        scrollBuffers.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        panelDetalleBuffers.setOpaque(false);
        panelDetalleBuffers.setAlignmentX(LEFT_ALIGNMENT);
        JLabel tituloBuffers = texto("ESTADO DE LOS BUFFERS GLOBALES", 12, TINTA, true);
        panelDetalleBuffers.add(tituloBuffers, BorderLayout.NORTH);
        panelDetalleBuffers.add(scrollBuffers, BorderLayout.CENTER);

        cabecera.add(panelDetalleColas);
        cabecera.add(Box.createVerticalStrut(16));
        cabecera.add(panelDetalleBuffers);
        contenedorPrincipal.add(cabecera, BorderLayout.NORTH);
        
        tarjetas.setOpaque(false);
        vacio.add(texto("Todavía no hay computadores", 20, TINTA, true));
        vacio.add(Box.createVerticalStrut(16));
        JButton crear = accion("Crear un computador →", true);
        crear.addActionListener(e -> irAComputadores.run());
        vacio.add(crear);
        
        JPanel resumen = new JPanel(new BorderLayout(0, 16));
        resumen.setOpaque(false);
        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setOpaque(false);
        contenido.add(tarjetas, BorderLayout.NORTH);
        
        resumen.add(contenido, BorderLayout.CENTER);
        resumen.add(texto("CPU: estado actual · Utilización histórica y métricas: pendientes", 12, GRIS, false), BorderLayout.SOUTH);
        
        pestanas.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override protected void installDefaults() {
                super.installDefaults();
                tabAreaInsets = new Insets(10, 0, 0, 0);
                contentBorderInsets = new Insets(0, 0, 10, 0);
                tabInsets = new Insets(9, 16, 9, 16);
            }
        });
        pestanas.setBackground(PAPEL);
        pestanas.setForeground(TINTA);
        pestanas.addTab("Resumen", resumen);
        
        contenedorPrincipal.add(pestanas, BorderLayout.CENTER);
        pestanas.addChangeListener(e -> actualizarDetalleColasGlobal());
        
        areaLog.setEditable(false);
        areaLog.setBackground(new Color(25, 25, 25));
        areaLog.setForeground(new Color(0, 200, 0));
        areaLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaLog.setMargin(new Insets(5, 5, 5, 5));
        
        DefaultCaret caret = (DefaultCaret) areaLog.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        
        scrollLog.setPreferredSize(new Dimension(Integer.MAX_VALUE, 140));
        
        panelLog.setOpaque(false);
        JLabel tituloLog = texto("LOG DE EVENTOS DEL SISTEMA", 12, TINTA, true);
        panelLog.add(tituloLog, BorderLayout.NORTH);
        panelLog.add(scrollLog, BorderLayout.CENTER);
        
        contenedorPrincipal.add(panelLog, BorderLayout.SOUTH);

        JScrollPane scrollGlobal = new JScrollPane(contenedorPrincipal);
        scrollGlobal.setBorder(null);
        scrollGlobal.getVerticalScrollBar().setUnitIncrement(16);
        scrollGlobal.getViewport().setBackground(FONDO);
        scrollGlobal.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollGlobal, BorderLayout.CENTER);
        
        this.controller.addLogListener(mensaje -> {
            areaLog.append(mensaje + "\n");
        });

        this.controller.addGUIListener(() -> {
            if (isShowing()) actualizarVista();
        });
        this.panelMetricas = new PanelMetricas(controller);
        pestanas.addTab("Métricas", panelMetricas);
    }
    
    private int extraerValorInt(String texto, String clave, int defecto) {
        int idx = texto.indexOf("\"" + clave + "\"");
        if (idx < 0) return defecto;
        int cursor = idx + clave.length() + 2;
        while (cursor < texto.length() && Character.isWhitespace(texto.charAt(cursor))) cursor++;
        if (cursor == texto.length() || texto.charAt(cursor++) != ':') return defecto;
        while (cursor < texto.length() && Character.isWhitespace(texto.charAt(cursor))) cursor++;
        int start = cursor;
        if (cursor < texto.length() && texto.charAt(cursor) == '-') cursor++;
        int digits = cursor;
        while (cursor < texto.length() && texto.charAt(cursor) >= '0' && texto.charAt(cursor) <= '9') cursor++;
        if (cursor == digits) return defecto;
        int end = cursor;
        while (cursor < texto.length() && Character.isWhitespace(texto.charAt(cursor))) cursor++;
        if (cursor < texto.length() && texto.charAt(cursor) != ','
                && texto.charAt(cursor) != '}' && texto.charAt(cursor) != ']') return defecto;
        try {
            return Integer.parseInt(texto.substring(start, end));
        } catch (NumberFormatException ex) {
            return defecto;
        }
    }

private String extraerValorString(String texto, String clave, String defecto) {
    try {
        String patron = "\"" + clave + "\":";
        int idx = texto.indexOf(patron);
        if (idx == -1) return defecto;
        int start = texto.indexOf("\"", idx + patron.length()) + 1;
        int end = texto.indexOf("\"", start);
        return texto.substring(start, end);
    } catch (Exception e) {
        return defecto;
    }
}

    private JPanel crearPanelPersistencia() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(LEFT_ALIGNMENT);

        JButton btnGuardar = accion("Guardar configuración", false);
        JButton btnCargar = accion("Cargar configuración", false);

        btnGuardar.addActionListener(e -> {
    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos JSON (*.json)", "json"));
    
    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        File archivo = chooser.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".json")) {
            archivo = new File(archivo.getAbsolutePath() + ".json");
        }

        File destino = archivo;
        btnGuardar.setEnabled(false);
        GuiCommands.submit(() -> guardarEstado(destino), ok -> { btnGuardar.setEnabled(true); if (!ok) JOptionPane.showMessageDialog(this, "No se pudo guardar la configuración. Consulta el log.", "Error al guardar", JOptionPane.ERROR_MESSAGE); },
                error -> { btnGuardar.setEnabled(true); controller.logEvent(error.getMessage()); });
    }
});
          btnCargar.addActionListener(e -> {
    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos JSON (*.json)", "json"));

    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        File archivo = chooser.getSelectedFile();

        btnCargar.setEnabled(false);
        GuiCommands.submit(() -> cargarEstado(archivo), ok -> {
            btnCargar.setEnabled(true);
            if (!ok) {
                JOptionPane.showMessageDialog(this,
                        "No se pudo cargar el archivo. Consulta el log para conocer el error.",
                        "Error de carga", JOptionPane.ERROR_MESSAGE);
            }
        }, error -> {
            btnCargar.setEnabled(true);
            controller.logEvent(error.getMessage());
            JOptionPane.showMessageDialog(this, "No se pudo cargar el archivo: " + error.getMessage(),
                    "Error de carga", JOptionPane.ERROR_MESSAGE);
        });
    }
});
panel.add(btnGuardar);
        panel.add(btnCargar);
        JButton btnLog = accion("Exportar log", false);
        btnLog.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("avilaos-ciclo-" + controller.getCurrentCycle() + ".log"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File destino = chooser.getSelectedFile();
                String texto = areaLog.getText();
                GuiCommands.submit(() -> {
                    java.nio.file.Files.writeString(destino.toPath(), texto, java.nio.charset.StandardCharsets.UTF_8);
                    return destino.getName();
                }, nombre -> controller.logEvent("Log exportado: " + nombre),
                        error -> controller.logEvent("Error al exportar log: " + error.getMessage()));
            }
        });
        panel.add(btnLog);
        return panel;
    }

    /** Guarda una configuración para comenzar de nuevo, no un checkpoint. */
    boolean guardarEstado(File archivo) {
        try {
            com.google.gson.JsonObject root = new com.google.gson.JsonObject();
            com.google.gson.JsonArray computers = new com.google.gson.JsonArray();
            com.google.gson.JsonArray buffers = new com.google.gson.JsonArray();
            com.google.gson.JsonArray processes = new com.google.gson.JsonArray();
            StateAccess.enter();
            try {
                root.addProperty("currentCycle", 0);
                root.addProperty("cycleDurationMs", controller.getCycleDurationMs());
                root.addProperty("networkLatency", controller.getNetworkLatency());
                MyLinkedList<Computer> cs = controller.getComputers();
                int[] libre = new int[cs.size()];
                for (int i = 0; i < cs.size(); i++) {
                    Computer c = cs.get(i);
                    com.google.gson.JsonObject o = new com.google.gson.JsonObject();
                    o.addProperty("name", c.getName());
                    o.addProperty("ramSize", c.getMemoryManager().getTotalMemory());
                    o.addProperty("policy", c.getPolitica().name());
                    o.addProperty("quantum", c.getQuantum());
                    libre[i] = c.getMemoryManager().getTotalMemory();
                    computers.add(o);
                }
                MyLinkedList<Buffer> bs = controller.getBuffers();
                for (int i = 0; i < bs.size(); i++) {
                    Buffer b = bs.get(i);
                    com.google.gson.JsonObject o = new com.google.gson.JsonObject();
                    o.addProperty("id", b.getId());
                    o.addProperty("hostComputerId", b.getIdComp());
                    o.addProperty("storage", b.getStorage());
                    o.addProperty("sizeInMemory", b.getSizeInMemory());
                    libre[b.getIdComp() - 1] -= b.getStorage();
                    buffers.add(o);
                }
                MyLinkedList<ProcessControlBlock> ps = controller.getAllProcesses();
                for (int i = 0; i < ps.size(); i++) {
                    ProcessControlBlock p = ps.get(i);
                    int memory = p.getRequiredMemory();
                    if (memory <= 0) throw new IllegalStateException("No se conoce la memoria original de " + p.getName());
                    int host = p.getComputerId() - 1;
                    boolean admitted = memory <= libre[host];
                    if (admitted) libre[host] -= memory;
                    com.google.gson.JsonObject o = new com.google.gson.JsonObject();
                    o.addProperty("id", p.getId()); o.addProperty("name", p.getName());
                    o.addProperty("computerId", p.getComputerId()); o.addProperty("type", p.getType().name());
                    o.addProperty("totalInstructions", p.getTotalInstructions());
                    o.addProperty("remainingTime", p.getTotalInstructions());
                    o.addProperty("assignedMemory", memory); o.addProperty("priority", p.getPriority());
                    o.addProperty("deadline", p.getDeadline()); o.addProperty("remainingDeadline", p.getDeadline());
                    o.addProperty("state", admitted ? "LISTO" : "NUEVO"); o.addProperty("blockReason", "NONE");
                    o.addProperty("programCounter", 0); o.addProperty("memoryAddressRegister", 0);
                    o.addProperty("ioInterval", p.getIoInterval()); o.addProperty("ioDuration", p.getIoDuration());
                    o.addProperty("ioCycleCountdown", p.getIoInterval());
                    o.addProperty("targetBufferId", p.getTargetBufferId());
                    o.addProperty("cyclesPerOperation", p.getCyclesPerOperation());
                    o.addProperty("requiredElements", p.getRequiredElements()); o.addProperty("processedElements", 0);
                    processes.add(o);
                }
            } finally { StateAccess.exit(); }
            root.add("computers", computers); root.add("buffers", buffers); root.add("processes", processes);
            String json = new com.google.gson.GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(root);
            ValidarCarga.validar(json);
            java.nio.file.Path destino = archivo.toPath().toAbsolutePath();
            java.nio.file.Path temporal = java.nio.file.Files.createTempFile(destino.getParent(), ".avilaos-", ".tmp");
            try {
                java.nio.file.Files.writeString(temporal, json, java.nio.charset.StandardCharsets.UTF_8);
                try {
                    java.nio.file.Files.move(temporal, destino, java.nio.file.StandardCopyOption.ATOMIC_MOVE, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (java.nio.file.AtomicMoveNotSupportedException ex) {
                    java.nio.file.Files.move(temporal, destino, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            } finally { java.nio.file.Files.deleteIfExists(temporal); }
            controller.logEvent("Configuración guardada exitosamente en JSON: " + archivo.getName());
            return true;
        } catch (Exception ex) {
            controller.logEvent("Error al guardar archivo JSON: " + ex.getMessage());
            return false;
        }
    }

    /** Ruta compartida con Cargar Estado; reemplaza la sesión actual. */
    boolean cargarEstado(File archivo) {
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(archivo))) {
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = reader.readLine()) != null) {
                sb.append(linea.trim());
            }

            String json = sb.toString();

            json = ValidarCarga.validar(json);
            
            StateAccess.enter();
            try {
            // Reiniciar el estado actual del simulador antes de restaurar
            controller.reset();

            // Parámetros globales
            int currentCycle = extraerValorInt(json, "currentCycle", 0);
            int cycleDuration = extraerValorInt(json, "cycleDurationMs", 1000);
            int networkLatency = extraerValorInt(json, "networkLatency", 2);
            
            controller.setCycleDurationMs(cycleDuration);
            controller.setNetworkLatency(networkLatency);
            controller.setCurrentCycle(currentCycle);

            // Reconstrucción de Computadores
            int startComp = json.indexOf("\"computers\": [");
            int endComp = json.indexOf("]", startComp);
            if (startComp != -1 && endComp != -1) {
                String bloqComps = json.substring(startComp, endComp);
                String[] objetos = bloqComps.split("\\}\\s*,\\s*\\{");
                
                for (int i = 0; i < objetos.length; i++) {
                    String obj = objetos[i];
                    if (!obj.contains("\"name\"")) continue;
                    String name = extraerValorString(obj, "name", "Computador " + (i + 1));
                    int ramSize = extraerValorInt(obj, "ramSize", 128);
                    String polStr = extraerValorString(obj, "policy", "FCFS");
                    int quantum = extraerValorInt(obj, "quantum", 2);

                    SchedulingPolicyType pol = SchedulingPolicyType.FCFS;
                    try {
                        pol = SchedulingPolicyType.valueOf(polStr);
                    } catch (Exception ex) { }

                    Computer c = controller.createComputer(name, ramSize, pol, quantum);
                    
                }
            }

            // Reconstrucción de Buffers
            int startBuf = json.indexOf("\"buffers\": [");
            int endBuf = json.indexOf("]", startBuf);
            if (startBuf != -1 && endBuf != -1) {
                String bloqBufs = json.substring(startBuf, endBuf);
                String[] objetos = bloqBufs.split("\\}\\s*,\\s*\\{");
                
                for (int i = 0; i < objetos.length; i++) {
                    String obj = objetos[i];
                    if (!obj.contains("\"hostComputerId\"")) continue;
                    int hostId = extraerValorInt(obj, "hostComputerId", 1);
                    int storage = extraerValorInt(obj, "storage", 10);
                    int sizeInMemory = extraerValorInt(obj, "sizeInMemory", storage);

                    if (controller.getComputerById(hostId) != null) {
                        // Pasar storage y sizeInMemory al controlador
                        controller.createBuffer(hostId, storage,sizeInMemory);
                    }
                }
            }

            // Reconstrucción de Procesos (con todos sus atributos y estado)
            int startProc = json.indexOf("\"processes\": [");
            int endProc = json.indexOf("]", startProc);
            if (startProc != -1 && endProc != -1) {
                String bloqProcs = json.substring(startProc, endProc);
                String[] objetos = bloqProcs.split("\\}\\s*,\\s*\\{");
                int maxId = 0;
                for (int i = 0; i < objetos.length; i++) {
                    String obj = objetos[i];
                    if (obj.trim().isEmpty() || !obj.contains("\"name\"")) continue;

                    int pId = extraerValorInt(obj, "id", i + 1);
                    maxId = Math.max(maxId, pId);
                    String name = extraerValorString(obj, "name", "Proceso-" + pId);
                    int computerId = extraerValorInt(obj, "computerId", 1);
                    String typeStr = extraerValorString(obj, "type", "CPU_BOUND");
                    int totalInstructions = extraerValorInt(obj, "totalInstructions", 10);
                    int remainingTime = extraerValorInt(obj, "remainingTime", totalInstructions);
                    int assignedMemory = extraerValorInt(obj, "assignedMemory", 10);
                    int priority = extraerValorInt(obj, "priority", 1);
                    int deadline = extraerValorInt(obj, "deadline", 100);
                    int remainingDeadline = extraerValorInt(obj, "remainingDeadline", deadline);
                    String stateStr = extraerValorString(obj, "state", "LISTO");
                    String blockReasonStr = extraerValorString(obj, "blockReason", "NONE");
                    int pc = extraerValorInt(obj, "programCounter", 0);
                    int mar = extraerValorInt(obj, "memoryAddressRegister", 0);
                    int ioInterval = extraerValorInt(obj, "ioInterval", 0);
                    int ioDuration = extraerValorInt(obj, "ioDuration", 0);
                    int ioCycleCountdown = extraerValorInt(obj, "ioCycleCountdown", ioInterval);
                    String targetBufId = extraerValorString(obj, "targetBufferId", null);
                    if ("null".equalsIgnoreCase(targetBufId)) targetBufId = null;
                    int cyclesPerOp = extraerValorInt(obj, "cyclesPerOperation", 1);
                    int reqElem = extraerValorInt(obj, "requiredElements", 0);
                    int procElem = extraerValorInt(obj, "processedElements", 0);

                    ProcessType pType = ProcessType.CPU_BOUND;
                    try { pType = ProcessType.valueOf(typeStr); } catch (Exception ex) {}

                    ProcessState pState = ProcessState.LISTO;
                    try { pState = ProcessState.valueOf(stateStr); } catch (Exception ex) {}

                    BlockReason bReason = BlockReason.NONE;
                    try { bReason = BlockReason.valueOf(blockReasonStr); } catch (Exception ex) {}

                    ProcessControlBlock pcb = new ProcessControlBlock(
                        name, computerId, pType, totalInstructions, assignedMemory, priority, deadline
                    );
                    pcb.setId(pId);
                    pcb.setRemainingTime(remainingTime);
                    pcb.setRemainingDeadline(remainingDeadline);
                    pcb.setState(pState);
                    pcb.setBlockReason(bReason);
                    pcb.setProgramCounter(pc);
                    pcb.setMemoryAddressRegister(mar);
                    pcb.setIoInterval(ioInterval);
                    pcb.setIoDuration(ioDuration);
                    pcb.setIoCycleCountdown(ioCycleCountdown);
                    pcb.setTargetBufferId(targetBufId);
                    pcb.setCyclesPerOperation(cyclesPerOp);
                    pcb.setRequiredElements(reqElem);
                    pcb.setProcessedElements(procElem);

                    controller.restoreProcess(pcb);
                }
                if (maxId > 0) {
                    ProcessControlBlock.resetIdGenerator(maxId + 1);
                }
            }

            } finally { StateAccess.exit(); }
            controller.logEvent("Configuración JSON cargada exitosamente desde: " + archivo.getName());
            return true;
        } catch (Exception ex) {
            controller.logEvent("Error al leer el archivo JSON: " + ex.getMessage());
            return false;
        }
    }

    private static class ScrollablePanel extends JPanel implements Scrollable {
        public ScrollablePanel(LayoutManager layout) { super(layout); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) { return Math.max(visibleRect.height / 2, 1); }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    public void agregarLog(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            areaLog.append(mensaje + "\n");
        });
    }

    private JPanel controlesReloj() {
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controles.setOpaque(false);
        controles.setAlignmentX(LEFT_ALIGNMENT);
        intervalo.setSelectedItem(controller.getCycleDurationMs());
        intervalo.setPreferredSize(new Dimension(90, 36));
        iniciar.addActionListener(e -> { GuiRequest.control(controller, controller::startSimulation); });
        pausar.addActionListener(e -> { GuiRequest.control(controller, controller::pauseSimulation); });
        paso.addActionListener(e -> { GuiRequest.control(controller, controller::stepSimulation); });
        aplicar.addActionListener(e -> {
            Integer ms = (Integer) intervalo.getSelectedItem();
            if (ms != null) { GuiRequest.control(controller, () -> controller.setCycleDurationMs(ms)); }
        });
        
        intervalo.addActionListener(e -> actualizarControlesReloj());
        controles.add(iniciar); controles.add(pausar); controles.add(paso);
        controles.add(texto("Intervalo (ms)", 12, GRIS, false));
        controles.add(intervalo); controles.add(aplicar);
        controles.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        return controles;
    }

    private void actualizarControlesReloj() {
        boolean activo = controller.isRunning() && !controller.isPaused();
        iniciar.setText(controller.isRunning() && controller.isPaused() ? "Reanudar" : "Iniciar");
        iniciar.setEnabled(!activo);
        pausar.setEnabled(activo);
        paso.setEnabled(!activo);
        Integer seleccionado = (Integer) intervalo.getSelectedItem();
        int ms = controller.getCycleDurationMs();
        boolean pendiente = seleccionado != null && seleccionado != ms;
        aplicar.setEnabled(pendiente);
        intervaloAplicado.setText("Aplicado: " + ms + " ms/tick" + (pendiente ? " · Cambio pendiente" : ""));
    }

    public void actualizarVista() {
        long previousSession=view.session;
        view=controller.snapshot();
        int ciclo = view.cycle;
        String marcha = !controller.isRunning() ? "Detenida" : controller.isPaused() ? "Pausada" : "En ejecución";
        actualizarControlesReloj();
        {
            var computadores = view.computers;
            int cantidad = computadores.size();
            boolean cambio = cantidad != ids.length || previousSession != view.session;
            if (!cambio) {
                for (int i = 0; i < cantidad; i++) {
                    if (ids[i] != computadores.get(i).getId()) { cambio = true; break; }
                }
            }
            if (cambio || cantidad == 0 && tarjetas.getComponentCount() == 0) reconstruirPestanas();
            
            StringBuilder reporteTick = new StringBuilder("Estado CPUs -> ");

            for (int i = 0; i < cantidad; i++) {
                Node c = computadores.get(i);
                resumenes[i].actualizar(c);
                detalles[i].actualizar(c);
                pestanas.setTitleAt(i + 1, c.getName());

                var pcb = c.getCpu().getPCB();
                // Llama al toString() completo para mostrar todos los registros y tiempos
                String procesoActual = (pcb != null) ? pcb.toString() : "Inactivo";
                
                reporteTick.append(c.getName()).append(": [").append(procesoActual).append("] | ");
            }
            
            reloj.setText("Reloj global · ciclo " + ciclo);
            estado.setText(marcha + "  ·  " + cantidad + " computadores");
            actualizarDetalleColasGlobal();
            actualizarDetalleBuffersGlobal();

            if (controller.isRunning() && !controller.isPaused() && cantidad > 0) {
                agregarLog(reporteTick.toString());
            }
            if (panelMetricas != null) {
                panelMetricas.actualizarMetricas();
            }
        }
    }

    private void actualizarDetalleBuffersGlobal() {
        modeloBuffers.setRowCount(0);
        var buffers = view.buffers;
        for (int i = 0; i < buffers.size(); i++) {
            var b = buffers.get(i);
            String nombre = "Buffer " + b.getId();
            int elementos = b.getDatos().size();
            int capacidad = b.getStorage();
            
            String semaforos = String.format("M:%d, L:%d, V:%d", 
                b.getMutex().availablePermits(), 
                b.getLlenos().availablePermits(), 
                b.getVacios().availablePermits());
                
            String bloqueados = b.getBlockedProcessesAsString();
            
            modeloBuffers.addRow(new Object[]{nombre, elementos, capacidad, semaforos, bloqueados});
        }
    }
    
    private void actualizarDetalleColasGlobal() {
    int seleccion = pestanas.getSelectedIndex();
    var computadores = view.computers;
    int cantidad = computadores.size();
    
    if (cantidad == 0 || pestanas.getSelectedComponent() == panelMetricas) {
        panelDetalleColas.setVisible(false);
        return;
    }
    
    panelDetalleColas.setVisible(true);
    int compIndex = seleccion > 0 ? seleccion - 1 : 0;
    
    if (compIndex < cantidad) {
        Node c = computadores.get(compIndex);
        var kernel = c.getKernel();
        
        String[] nuevos = extraerElementos(kernel.getNewQueue().toString());
        String[] listos = extraerElementos(kernel.getReadyQueue().toString());
        String[] bloqueados = extraerElementos(kernel.getBlockedQueue().toString());
        String[] terminados = extraerElementos(kernel.getEndedQueue().toString());
        
        int maxFilas = Math.max(Math.max(nuevos.length, listos.length), Math.max(bloqueados.length, terminados.length));
        
        modeloColas.setRowCount(0);
        
        // Agrega una fila por cada proceso para que queden verticualizados
        for (int i = 0; i < maxFilas; i++) {
            String n = (i < nuevos.length) ? nuevos[i] : "";
            String l = (i < listos.length) ? listos[i] : "";
            String b = (i < bloqueados.length) ? bloqueados[i] : "";
            String t = (i < terminados.length) ? terminados[i] : "";
            
            modeloColas.addRow(new Object[]{n, l, b, t});
        }
    }
}
    
    private String[] extraerElementos(String textoCola) {
    if (textoCola == null) return new String[0];
    String limpio = textoCola.trim();
    
    // Remueve corchetes si existen al inicio y final
    if (limpio.startsWith("[")) limpio = limpio.substring(1);
    if (limpio.endsWith("]")) limpio = limpio.substring(0, limpio.length() - 1);
    limpio = limpio.trim();
    
    if (limpio.isEmpty()) return new String[0];
    
    // 1. Si contiene el formato detallado completo PCB{...}
    if (limpio.contains("PCB{")) {
        return limpio.split(",\\s*(?=PCB\\{)");
    }
    
    // 2. Si son nombres formateados tipo 'rew(1), rew(2)' o simplemente separados por comas
    return limpio.split(",\\s*");
}

   private void reconstruirPestanas() {
        int seleccion = pestanas.getSelectedIndex();
        boolean viendoMetricas = pestanas.getSelectedComponent() == panelMetricas;
        int seleccionado = seleccion > 0 && seleccion <= ids.length ? ids[seleccion - 1] : -1;
        while (pestanas.getTabCount() > 1) pestanas.removeTabAt(1);
        tarjetas.removeAll();
        
        var computadores = view.computers;
        int cantidad = computadores.size();
        ids = new int[cantidad];
        resumenes = new VistaComputador[cantidad];
        detalles = new VistaComputador[cantidad];
        
        if (cantidad == 0) tarjetas.add(vacio);
        for (int i = 0; i < cantidad; i++) {
            Node c = computadores.get(i);
            ids[i] = c.getId();
            resumenes[i] = new VistaComputador(c, false, view.session);
            detalles[i] = new VistaComputador(c, true, view.session);
            int indice = i + 1;
            JButton abrir = accion("Ver detalle →", false);
            abrir.addActionListener(e -> pestanas.setSelectedIndex(indice));
            resumenes[i].add(Box.createVerticalStrut(14));
            resumenes[i].add(abrir);
            tarjetas.add(resumenes[i]);
            
            // Envolver la vista de detalle directamente en un ScrollPane sin contenedores intermedios que estiren la vista
            JScrollPane scrollDetalle = new JScrollPane(detalles[i]);
            scrollDetalle.setBorder(null);
            scrollDetalle.getVerticalScrollBar().setUnitIncrement(16);
            
            pestanas.addTab(c.getName(), scrollDetalle);
            
            if (ids[i] == seleccionado) pestanas.setSelectedIndex(indice);
        }
        if (panelMetricas != null) {
            pestanas.addTab("Métricas", panelMetricas);
            if (viendoMetricas) pestanas.setSelectedComponent(panelMetricas);
        }
        tarjetas.revalidate(); 
        tarjetas.repaint();
    }

    private static final class VistaComputador extends JPanel {
        private final int computerId;
        private final long session;
        private final JLabel nombre = texto("", 20, TINTA, true);
        private final JComboBox<SchedulingPolicyType> comboPolitica = new JComboBox<>(SchedulingPolicyType.values());
        private final JLabel labelQuantum = texto("Quantum: ", 13, GRIS, false);
        private final JSpinner spinnerQuantum = new JSpinner(new SpinnerNumberModel(3, 1, 99, 1));
        private final JLabel cpu = texto("", 14, TINTA, true);
        private final JLabel proceso = texto("", 13, GRIS, false);
        private final JLabel registros = texto("", 13, GRIS, false);
        private final JLabel colas = texto("", 13, TINTA, false);
        private final JLabel memoria = texto("", 13, GRIS, false);
        private final boolean detalle;
        private boolean actualizandoVista = false;

        VistaComputador(Node c, boolean detalle, long session) {
            this.computerId = c.getId();
            this.session=session;
            this.detalle = detalle;
            
            // Usamos BorderLayout para empaquetar el contenido arriba sin deformaciones
            setLayout(new BorderLayout());
            setBackground(PAPEL);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDE),
                    BorderFactory.createEmptyBorder(22, 22, 22, 22)));

            // Panel interno donde colocamos verticalmente todos los elementos
            JPanel contenido = new JPanel();
            contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
            contenido.setOpaque(false);
            contenido.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel panelPolitica = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            panelPolitica.setOpaque(false);
            panelPolitica.setAlignmentX(Component.LEFT_ALIGNMENT);
            panelPolitica.add(texto("Política: ", 13, GRIS, false));
            
            comboPolitica.setSelectedItem(c.getPolitica());
            comboPolitica.setPreferredSize(new Dimension(160, 26));
            comboPolitica.addActionListener(e -> {
                SchedulingPolicyType seleccion = (SchedulingPolicyType) comboPolitica.getSelectedItem();
                if (!actualizandoVista && seleccion != null) {
                    var ctrl=SimulationController.getInstance();
                    GuiRequest.control(ctrl, () -> { ctrl.requireSession(session); ctrl.setComputerPolicy(computerId,seleccion); });
                    boolean esRR = (seleccion == SchedulingPolicyType.ROUND_ROBIN);
                    labelQuantum.setVisible(esRR);
                    spinnerQuantum.setVisible(esRR);
                    panelPolitica.revalidate();
                    panelPolitica.repaint();
                }
            });
            panelPolitica.add(comboPolitica);

            spinnerQuantum.setValue(c.getQuantum());
            spinnerQuantum.setPreferredSize(new Dimension(55, 26));
            boolean esRRInicial = (c.getPolitica() == SchedulingPolicyType.ROUND_ROBIN);
            labelQuantum.setVisible(esRRInicial);
            spinnerQuantum.setVisible(esRRInicial);

            spinnerQuantum.addChangeListener(e -> {
                if (!actualizandoVista) {
                    int val = (Integer) spinnerQuantum.getValue();
                    var ctrl=SimulationController.getInstance();
                    GuiRequest.control(ctrl, () -> { ctrl.requireSession(session); ctrl.setComputerQuantum(computerId,val); });
                }
            });

            panelPolitica.add(Box.createHorizontalStrut(10));
            panelPolitica.add(labelQuantum);
            panelPolitica.add(spinnerQuantum);

            nombre.setAlignmentX(Component.LEFT_ALIGNMENT);
            cpu.setAlignmentX(Component.LEFT_ALIGNMENT);
            proceso.setAlignmentX(Component.LEFT_ALIGNMENT);
            registros.setAlignmentX(Component.LEFT_ALIGNMENT);
            memoria.setAlignmentX(Component.LEFT_ALIGNMENT);
            colas.setAlignmentX(Component.LEFT_ALIGNMENT);

            contenido.add(nombre); 
            contenido.add(Box.createVerticalStrut(8)); 
            contenido.add(panelPolitica);
            contenido.add(Box.createVerticalStrut(16)); 
            contenido.add(cpu);
            contenido.add(Box.createVerticalStrut(6)); 
            contenido.add(proceso);
            if (detalle) { 
                contenido.add(Box.createVerticalStrut(6)); 
                contenido.add(registros); 
            }
            contenido.add(Box.createVerticalStrut(16));
            contenido.add(memoria);
            contenido.add(Box.createVerticalStrut(16)); 
            contenido.add(colas);

            // Al colocarlo en NORTH, se mantendrá empaquetado arriba sin importar el alto del frame
            add(contenido, BorderLayout.NORTH);
        }
    

        // Obliga al panel a mantener su altura compacta según el contenido y no expandirse en la ventana
        @Override
        public Dimension getMaximumSize() {
            Dimension pref = getPreferredSize();
            return new Dimension(Integer.MAX_VALUE, pref.height);
        }
    

        void actualizar(Node c) {
        actualizandoVista = true;
        try {
            nombre.setText(c.getName() + " · ID " + c.getId());
            
            if (comboPolitica.getSelectedItem() != c.getPolitica()) {
                comboPolitica.setSelectedItem(c.getPolitica());
            }

            boolean esRR = (c.getPolitica() == SchedulingPolicyType.ROUND_ROBIN);
            labelQuantum.setVisible(esRR);
            spinnerQuantum.setVisible(esRR);
            if ((Integer) spinnerQuantum.getValue() != c.getQuantum()) {
                spinnerQuantum.setValue(c.getQuantum());
            }

            var p = c.getCpu().getPCB();
            cpu.setText(p == null ? "CPU libre" : "CPU ocupada · " + c.getCpu().getMode());
            
            if (p == null) {
                proceso.setText("Sin proceso");
            } else {
                String textoFormateado = p.toString()
                        .replace("PCB{", "PCB { ")
                        .replace(", type=", ",<br>type=")
                        .replace(", remDeadline=", ",<br>remDeadline=")
                        .replace("}", " }");
                proceso.setText("<html>" + textoFormateado + "</html>");
            }
            
            if (detalle) registros.setText(p == null 
                    ? "PC: —  ·  MAR: —  ·  Prioridad: —  ·  Deadline: —"
                    : "PC: " + c.getCpu().getPC() 
                    + "  ·  MAR: " + p.getMemoryAddressRegister() 
                    + "  ·  Prioridad: " + p.getPriority()
                    + "  ·  Deadline restante: " + p.getRemainingDeadline());
            
            int total = c.getMemoryManager().getTotalMemory();
            int usada = c.getMemoryManager().getUsedMemory();
            memoria.setText("RAM " + usada + " / " + total);
            
            var kernel = c.getKernel();
            colas.setText("Nuevos: " + kernel.getNewQueue().size() + "  ·  Listos: " + kernel.getReadyQueue().size()
                    + "  ·  Bloqueados: " + kernel.getBlockedQueue().size() + "  ·  Terminados: " + kernel.getEndedQueue().size());
        } finally {
            actualizandoVista = false;
        }
    }}
}
