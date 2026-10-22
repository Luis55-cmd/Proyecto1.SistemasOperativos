package com.avilaos.gui;

import com.avilaos.hardware.Computer;
import com.avilaos.core.model.ProcessControlBlock;
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
    private final JTabbedPane pestanas = new JTabbedPane(JTabbedPane.BOTTOM, JTabbedPane.SCROLL_TAB_LAYOUT);
    private final JPanel tarjetas = new JPanel(new GridLayout(0, 2, 16, 16));
    private final JLabel reloj = texto("Reloj global · ciclo 0", 20, ACENTO, true);
    private final JLabel estado = texto("", 13, GRIS, false);
    private final JPanel vacio = tarjeta();
    private int[] ids = new int[0];
    private VistaComputador[] resumenes = new VistaComputador[0];
    private VistaComputador[] detalles = new VistaComputador[0];
    
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
            String timestamp = String.format("[Ciclo %d] ", controller.getCurrentCycle());
            areaLog.append(timestamp + mensaje + "\n");
        });

        this.controller.addGUIListener(() -> {
            if (isShowing()) actualizarVista();
        });
    }
    
    private int extraerValorInt(String texto, String clave, int defecto) {
    try {
        String patron = "\"" + clave + "\":";
        int idx = texto.indexOf(patron);
        if (idx == -1) return defecto;
        int start = idx + patron.length();
        int end = texto.indexOf(",", start);
        if (end == -1) end = texto.indexOf("}", start);
        if (end == -1) end = texto.indexOf("]", start);
        String val = texto.substring(start, end).replaceAll("[^0-9-]", "").trim();
        return Integer.parseInt(val);
    } catch (Exception e) {
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

        JButton btnGuardar = accion("Guardar Estado", false);
        JButton btnCargar = accion("Cargar Estado", false);

        btnGuardar.addActionListener(e -> {
    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos JSON (*.json)", "json"));
    
    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        File archivo = chooser.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".json")) {
            archivo = new File(archivo.getAbsolutePath() + ".json");
        }

        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(archivo))) {
            writer.println("{");
            writer.println("  \"cycleDurationMs\": " + controller.getCycleDurationMs() + ",");
            writer.println("  \"networkLatency\": " + controller.getNetworkLatency() + ",");
            
            // Exportar Computadores
            writer.println("  \"computers\": [");
            MyLinkedList<Computer> comps = controller.getComputers();
            for (int i = 0; i < comps.size(); i++) {
                Computer c = comps.get(i);
                writer.println("    {");
                writer.println("      \"name\": \"" + c.getName() + "\",");
                writer.println("      \"ramSize\": " + c.getMemoryManager().getTotalMemory() + ",");
                writer.println("      \"policy\": \"" + c.getPolitica() + "\",");
                writer.print("    }" + (i < comps.size() - 1 ? "," : ""));
                writer.println();
            }
            writer.println("  ],");

            // Exportar Buffers (distinguiendo storage de sizeInMemory)
            writer.println("  \"buffers\": [");
            MyLinkedList<Buffer> bufs = controller.getBuffers();
            for (int i = 0; i < bufs.size(); i++) {
                Buffer b = bufs.get(i);
                writer.println("    {");
                writer.println("      \"id\": " + b.getId() + ",");
                writer.println("      \"hostComputerId\": " + b.getIdComp() + ",");
                writer.println("      \"storage\": " + b.getStorage() + ",");
                writer.println("      \"sizeInMemory\": " + b.getSizeInMemory());
                writer.print("    }" + (i < bufs.size() - 1 ? "," : ""));
                writer.println();
            }
            writer.println("  ]");
            writer.println("}");
            
            controller.logEvent("Configuración guardada exitosamente en JSON: " + archivo.getName());
        } catch (java.io.IOException ex) {
            controller.logEvent("Error al guardar archivo JSON: " + ex.getMessage());
        }
    }
});
          btnCargar.addActionListener(e -> {
    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos JSON (*.json)", "json"));
    
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        File archivo = chooser.getSelectedFile();
        
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(archivo))) {
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = reader.readLine()) != null) {
                sb.append(linea.trim());
            }

            String json = sb.toString();
            
            // Reiniciar el estado actual del simulador antes de restaurar
            controller.reset();

            // Parámetros globales
            int cycleDuration = extraerValorInt(json, "cycleDurationMs", 1000);
            int networkLatency = extraerValorInt(json, "networkLatency", 2);
            
            controller.setCycleDurationMs(cycleDuration);
            controller.setNetworkLatency(networkLatency);

            // Reconstrucción de Computadores
            int startComp = json.indexOf("\"computers\": [");
            int endComp = json.indexOf("]", startComp);
            if (startComp != -1 && endComp != -1) {
                String bloqComps = json.substring(startComp, endComp);
                String[] objetos = bloqComps.split("\\}\\s*,\\s*\\{");
                
                for (int i = 0; i < objetos.length; i++) {
                    String obj = objetos[i];
                    String name = extraerValorString(obj, "name", "Computador " + (i + 1));
                    int ramSize = extraerValorInt(obj, "ramSize", 128);
                    String polStr = extraerValorString(obj, "policy", "FCFS");
                    int quantum = extraerValorInt(obj, "quantum", 2);

                    SchedulingPolicyType pol = SchedulingPolicyType.FCFS;
                    try {
                        pol = SchedulingPolicyType.valueOf(polStr);
                    } catch (Exception ex) { }

                    Computer c = controller.createComputer(name, ramSize, pol);
                    
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
                    int hostId = extraerValorInt(obj, "hostComputerId", 1);
                    int storage = extraerValorInt(obj, "storage", 10);
                    int sizeInMemory = extraerValorInt(obj, "sizeInMemory", storage);

                    if (controller.getComputerById(hostId) != null) {
                        // Pasar storage y sizeInMemory al controlador
                        controller.createBuffer(hostId, storage,sizeInMemory);
                    }
                }
            }

            controller.logEvent("Configuración JSON cargada exitosamente desde: " + archivo.getName());
        } catch (Exception ex) {
            controller.logEvent("Error al leer el archivo JSON: " + ex.getMessage());
        }
    }
});
panel.add(btnGuardar);
        panel.add(btnCargar);
        return panel;
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
            String timestamp = String.format("[Ciclo %d] ", controller.getCurrentCycle());
            areaLog.append(timestamp + mensaje + "\n");
        });
    }

    private JPanel controlesReloj() {
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controles.setOpaque(false);
        controles.setAlignmentX(LEFT_ALIGNMENT);
        intervalo.setSelectedItem(controller.getCycleDurationMs());
        intervalo.setPreferredSize(new Dimension(90, 36));
        iniciar.addActionListener(e -> controller.startSimulation());
        pausar.addActionListener(e -> { if (controller.isRunning() && !controller.isPaused()) controller.pauseSimulation(); });
        paso.addActionListener(e -> { if (!controller.isRunning() || controller.isPaused()) controller.stepSimulation(); });
        aplicar.addActionListener(e -> {
            Integer ms = (Integer) intervalo.getSelectedItem();
            if (ms != null) { controller.setCycleDurationMs(ms); actualizarControlesReloj(); }
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
        int ciclo = controller.getCurrentCycle();
        String marcha = !controller.isRunning() ? "Detenida" : controller.isPaused() ? "Pausada" : "En ejecución";
        actualizarControlesReloj();
        synchronized (controller) {
            MyLinkedList<Computer> computadores = controller.getComputers();
            int cantidad = computadores.size();
            boolean cambio = cantidad != ids.length;
            if (!cambio) {
                for (int i = 0; i < cantidad; i++) {
                    if (ids[i] != computadores.get(i).getId()) { cambio = true; break; }
                }
            }
            if (cambio || cantidad == 0 && tarjetas.getComponentCount() == 0) reconstruirPestanas();
            
            StringBuilder reporteTick = new StringBuilder("Estado CPUs -> ");

            for (int i = 0; i < cantidad; i++) {
                Computer c = computadores.get(i);
                resumenes[i].actualizar(c);
                detalles[i].actualizar(c);
                pestanas.setTitleAt(i + 1, c.getName());

                ProcessControlBlock pcb = c.getCpu().getPCB();
                String procesoActual = (pcb != null) ? pcb.getName() : "Inactivo";
                reporteTick.append(c.getName()).append(": [").append(procesoActual).append("] | ");
            }
            
            reloj.setText("Reloj global · ciclo " + ciclo);
            estado.setText(marcha + "  ·  " + cantidad + " computadores");
            actualizarDetalleColasGlobal();
            actualizarDetalleBuffersGlobal();

            if (controller.isRunning() && !controller.isPaused() && cantidad > 0) {
                agregarLog(reporteTick.toString());
            }
        }
    }

    private void actualizarDetalleBuffersGlobal() {
        modeloBuffers.setRowCount(0);
        MyLinkedList<Buffer> buffers = controller.getBuffers();
        for (int i = 0; i < buffers.size(); i++) {
            Buffer b = buffers.get(i);
            String nombre = "Buffer " + b.getId();
            int elementos = b.getStorage();
            int capacidad = b.getSizeInMemory();
            
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
        MyLinkedList<Computer> computadores = controller.getComputers();
        int cantidad = computadores.size();
        
        if (cantidad == 0) {
            panelDetalleColas.setVisible(false);
            return;
        }
        
        panelDetalleColas.setVisible(true);
        int compIndex = seleccion > 0 ? seleccion - 1 : 0;
        
        if (compIndex < cantidad) {
            Computer c = computadores.get(compIndex);
            var kernel = c.getKernel();
            
            String[] nuevos = extraerElementos(kernel.getNewQueue().toString());
            String[] listos = extraerElementos(kernel.getReadyQueue().toString());
            String[] bloqueados = extraerElementos(kernel.getBlockedQueue().toString());
            String[] terminados = extraerElementos(kernel.getEndedQueue().toString());
            
            int maxFilas = Math.max(Math.max(nuevos.length, listos.length), Math.max(bloqueados.length, terminados.length));
            
            modeloColas.setRowCount(0);
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
        if (limpio.startsWith("[")) limpio = limpio.substring(1);
        if (limpio.endsWith("]")) limpio = limpio.substring(0, limpio.length() - 1);
        if (limpio.trim().isEmpty()) return new String[0];
        return limpio.split(",\\s*");
    }

    private void reconstruirPestanas() {
        int seleccion = pestanas.getSelectedIndex();
        int seleccionado = seleccion > 0 ? ids[seleccion - 1] : -1;
        while (pestanas.getTabCount() > 1) pestanas.removeTabAt(1);
        tarjetas.removeAll();
        
        MyLinkedList<Computer> computadores = controller.getComputers();
        int cantidad = computadores.size();
        ids = new int[cantidad];
        resumenes = new VistaComputador[cantidad];
        detalles = new VistaComputador[cantidad];
        
        if (cantidad == 0) tarjetas.add(vacio);
        for (int i = 0; i < cantidad; i++) {
            Computer c = computadores.get(i);
            ids[i] = c.getId();
            resumenes[i] = new VistaComputador(c, false);
            detalles[i] = new VistaComputador(c, true);
            int indice = i + 1;
            JButton abrir = accion("Ver detalle →", false);
            abrir.addActionListener(e -> pestanas.setSelectedIndex(indice));
            resumenes[i].add(Box.createVerticalStrut(14));
            resumenes[i].add(abrir);
            tarjetas.add(resumenes[i]);
            
            JPanel contenedor = new JPanel(new BorderLayout());
            contenedor.setBackground(FONDO);
            contenedor.add(detalles[i], BorderLayout.NORTH);
            
            pestanas.addTab(c.getName(), contenedor);
            
            if (ids[i] == seleccionado) pestanas.setSelectedIndex(indice);
        }
        tarjetas.revalidate(); tarjetas.repaint();
    }

    private static final class VistaComputador extends JPanel {
        private final Computer computador;
        private final JLabel nombre = texto("", 20, TINTA, true);
        private final JComboBox<SchedulingPolicyType> comboPolitica = new JComboBox<>(SchedulingPolicyType.values());
        private final JLabel cpu = texto("", 14, TINTA, true);
        private final JLabel proceso = texto("", 13, GRIS, false);
        private final JLabel registros = texto("", 13, GRIS, false);
        private final JLabel colas = texto("", 13, TINTA, false);
        private final JLabel memoria = texto("", 13, GRIS, false);
        private final boolean detalle;

        VistaComputador(Computer c, boolean detalle) {
            this.computador = c;
            this.detalle = detalle;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(PAPEL);
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE),
                    BorderFactory.createEmptyBorder(22, 22, 22, 22)));
            
            JPanel panelPolitica = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            panelPolitica.setOpaque(false);
            panelPolitica.setAlignmentX(Component.LEFT_ALIGNMENT);
            panelPolitica.add(texto("Política: ", 13, GRIS, false));
            
            comboPolitica.setSelectedItem(c.getPolitica());
            comboPolitica.setPreferredSize(new Dimension(160, 26));
            comboPolitica.addActionListener(e -> {
                SchedulingPolicyType seleccion = (SchedulingPolicyType) comboPolitica.getSelectedItem();
                if (seleccion != null && seleccion != computador.getPolitica()) {
                    computador.setPolitica(seleccion);
                }
            });
            panelPolitica.add(comboPolitica);

            nombre.setAlignmentX(Component.LEFT_ALIGNMENT);
            cpu.setAlignmentX(Component.LEFT_ALIGNMENT);
            proceso.setAlignmentX(Component.LEFT_ALIGNMENT);
            registros.setAlignmentX(Component.LEFT_ALIGNMENT);
            memoria.setAlignmentX(Component.LEFT_ALIGNMENT);
            colas.setAlignmentX(Component.LEFT_ALIGNMENT);

            add(nombre); add(Box.createVerticalStrut(8)); add(panelPolitica);
            add(Box.createVerticalStrut(20)); add(cpu);
            add(Box.createVerticalStrut(8)); add(proceso);
            if (detalle) { add(Box.createVerticalStrut(8)); add(registros); }
            add(Box.createVerticalStrut(20));
            add(memoria);
            add(Box.createVerticalStrut(20)); add(colas);
        }

        void actualizar(Computer c) {
            nombre.setText(c.getName() + " · ID " + c.getId());
            
            if (comboPolitica.getSelectedItem() != c.getPolitica()) {
                comboPolitica.setSelectedItem(c.getPolitica());
            }

            ProcessControlBlock p = c.getCpu().getPCB();
            cpu.setText(p == null ? "CPU libre" : "CPU ocupada · " + c.getCpu().getMode());
            proceso.setText(p == null ? "Sin proceso" : "Proceso: " + p.getName() + " · ID " + p.getId());
            
            if (detalle) registros.setText(p == null ? "PC: —  ·  Prioridad: —  ·  Deadline: —"
                    : "PC: " + c.getCpu().getPC() + "  ·  Prioridad: " + p.getPriority()
                    + "  ·  Deadline restante: " + p.getRemainingDeadline());
            
            int total = c.getMemoryManager().getTotalMemory();
            int usada = c.getMemoryManager().getUsedMemory();
            memoria.setText("RAM " + usada + " / " + total);
            
            var kernel = c.getKernel();
            colas.setText("Nuevos: " + kernel.getNewQueue().size() + "  ·  Listos: " + kernel.getReadyQueue().size()
                    + "  ·  Bloqueados: " + kernel.getBlockedQueue().size() + "  ·  Terminados: " + kernel.getEndedQueue().size());
        }
    }
}