package com.avilaos.gui;

import com.avilaos.hardware.Computer;
import com.avilaos.core.model.ProcessControlBlock;
import com.avilaos.simulation.SimulationController;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
    
    // TABLA DE COLAS: Las colas son los encabezados
    private final DefaultTableModel modeloColas = new DefaultTableModel(new Object[]{"Nuevos", "Listos", "Bloqueados", "Terminados"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tablaColas = new JTable(modeloColas);
    private final JScrollPane scrollColas = new JScrollPane(tablaColas);
    private final JPanel panelDetalleColas = new JPanel(new BorderLayout(0, 8));

    public MonitorSimulacion(SimulationController controller, Runnable irAComputadores) {
        super(new BorderLayout(0, 20));
        this.controller = controller;
        setBackground(FONDO);
        setBorder(BorderFactory.createEmptyBorder(28, 28, 24, 28));
        
        JPanel cabecera = new JPanel();
        cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.Y_AXIS));
        cabecera.setOpaque(false);
        cabecera.add(texto("SIMULACIÓN", 11, ACENTO, true));
        cabecera.add(Box.createVerticalStrut(8));
        cabecera.add(texto("Observa tu sistema", 28, TINTA, true));
        cabecera.add(Box.createVerticalStrut(12));
        cabecera.add(reloj);
        cabecera.add(Box.createVerticalStrut(8));
        cabecera.add(estado);
        cabecera.add(Box.createVerticalStrut(12));
        cabecera.add(controlesReloj());
        cabecera.add(Box.createVerticalStrut(6));
        cabecera.add(intervaloAplicado);
        cabecera.add(Box.createVerticalStrut(16));
        
        // Configuración de la tabla de colas (columnas divididas equitativamente)
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
        
        cabecera.add(panelDetalleColas);
        
        add(cabecera, BorderLayout.NORTH);
        
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
        JScrollPane scroll = new JScrollPane(contenido);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(FONDO);
        resumen.add(scroll, BorderLayout.CENTER);
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
        
        add(pestanas, BorderLayout.CENTER);
        
        pestanas.addChangeListener(e -> actualizarDetalleColasGlobal());
        
        // --- CONEXIÓN AL CONTROLADOR (OBSERVER) ---
        // Elimina la necesidad del Timer: el controlador avisa cuándo repintar
        this.controller.addGUIListener(() -> {
            if (isShowing()) {
                actualizarVista();
            }
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
            int cantidad = controller.getComputers().size();
            boolean cambio = cantidad != ids.length;
            if (!cambio) {
                for (int i = 0; i < cantidad; i++) {
                    if (ids[i] != controller.getComputers().get(i).getId()) { cambio = true; break; }
                }
            }
            if (cambio || cantidad == 0 && tarjetas.getComponentCount() == 0) reconstruirPestanas();
            
            for (int i = 0; i < cantidad; i++) {
                Computer c = controller.getComputers().get(i);
                resumenes[i].actualizar(c);
                detalles[i].actualizar(c);
                pestanas.setTitleAt(i + 1, c.getName());
            }
            
            reloj.setText("Reloj global · ciclo " + ciclo);
            estado.setText(marcha + "  ·  " + cantidad + " computadores");
            actualizarDetalleColasGlobal();
        }
    }
    
    private void actualizarDetalleColasGlobal() {
        int seleccion = pestanas.getSelectedIndex();
        int cantidad = controller.getComputers().size();
        
        if (cantidad == 0) {
            panelDetalleColas.setVisible(false);
            return;
        }
        
        panelDetalleColas.setVisible(true);
        int compIndex = seleccion > 0 ? seleccion - 1 : 0;
        
        if (compIndex < cantidad) {
            Computer c = controller.getComputers().get(compIndex);
            var kernel = c.getKernel();
            
            // Extraer y limpiar los elementos usando el toString() de MyQueue
            String[] nuevos = extraerElementos(kernel.getNewQueue().toString());
            String[] listos = extraerElementos(kernel.getReadyQueue().toString());
            String[] bloqueados = extraerElementos(kernel.getBlockedQueue().toString());
            String[] terminados = extraerElementos(kernel.getEndedQueue().toString());
            
            // Determinar qué cola tiene más procesos para crear ese número de filas
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
    
    /** Método de ayuda para convertir "[P(1), P(2)]" en un arreglo de Strings limpios */
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
        int cantidad = controller.getComputers().size();
        ids = new int[cantidad];
        resumenes = new VistaComputador[cantidad];
        detalles = new VistaComputador[cantidad];
        if (cantidad == 0) tarjetas.add(vacio);
        for (int i = 0; i < cantidad; i++) {
            Computer c = controller.getComputers().get(i);
            ids[i] = c.getId();
            resumenes[i] = new VistaComputador(false);
            detalles[i] = new VistaComputador(true);
            int indice = i + 1;
            JButton abrir = accion("Ver detalle →", false);
            abrir.addActionListener(e -> pestanas.setSelectedIndex(indice));
            resumenes[i].add(Box.createVerticalStrut(14));
            resumenes[i].add(abrir);
            tarjetas.add(resumenes[i]);
            JPanel contenedor = new JPanel(new BorderLayout());
            contenedor.setBackground(FONDO);
            contenedor.add(detalles[i], BorderLayout.NORTH);
            JScrollPane scroll = new JScrollPane(contenedor);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            pestanas.addTab(c.getName(), scroll);
            if (ids[i] == seleccionado) pestanas.setSelectedIndex(indice);
        }
        tarjetas.revalidate(); tarjetas.repaint();
    }

    private static final class VistaComputador extends JPanel {
        private final JLabel nombre = texto("", 20, TINTA, true);
        private final JLabel politica = texto("", 13, GRIS, false);
        private final JLabel cpu = texto("", 14, TINTA, true);
        private final JLabel proceso = texto("", 13, GRIS, false);
        private final JLabel registros = texto("", 13, GRIS, false);
        private final JLabel colas = texto("", 13, TINTA, false);
        private final JLabel memoria = texto("", 13, GRIS, false);
        private final boolean detalle;

        VistaComputador(boolean detalle) {
            this.detalle = detalle;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(PAPEL);
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE),
                    BorderFactory.createEmptyBorder(22, 22, 22, 22)));
            add(nombre); add(Box.createVerticalStrut(8)); add(politica);
            add(Box.createVerticalStrut(20)); add(cpu);
            add(Box.createVerticalStrut(8)); add(proceso);
            if (detalle) { add(Box.createVerticalStrut(8)); add(registros); }
            add(Box.createVerticalStrut(20));
            add(memoria);
            add(Box.createVerticalStrut(20)); add(colas);
        }

        void actualizar(Computer c) {
            nombre.setText(c.getName() + " · ID " + c.getId());
            politica.setText("Política: " + switch (c.getPolitica()) {
                case FCFS -> "FCFS"; case ROUND_ROBIN -> "Round Robin";
                case EDF -> "EDF"; case PRIORITY_PREEMPTIVE -> "Prioridades";
            });
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