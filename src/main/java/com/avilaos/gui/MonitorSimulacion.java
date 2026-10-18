package com.avilaos.gui;

import com.avilaos.hardware.Computer;
import com.avilaos.core.model.ProcessControlBlock;
import com.avilaos.simulation.SimulationController;
import java.awt.*;
import javax.swing.*;
import static com.avilaos.gui.PanelPrincipal.*;

/** Monitor de lectura y controles explícitos del reloj vía SimulationController. */
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
    private final Timer refresco;
    private final JButton iniciar = accion("Iniciar", true);
    private final JButton pausar = accion("Pausar", false);
    private final JButton paso = accion("+1 tick", false);
    private final JButton aplicar = accion("Aplicar", false);
    private final JComboBox<Integer> intervalo = new JComboBox<>(new Integer[]{100, 250, 500, 1000});
    private final JLabel intervaloAplicado = texto("", 12, GRIS, false);

    /** Entradas controlador y navegación. Construye vistas y programa refresco, sin arrancarlo aún. */
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
        // Evita la presentación segmentada de Aqua, que dibuja las pestañas sobre el borde.
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
        add(pestanas);
        // Timer de Swing invoca en EDT: modifica rótulos, no genera ticks del simulador.
        refresco = new Timer(500, e -> { if (isShowing()) actualizarVista(); });
    }

    /** Construye controles; entradas clic/selección, salida panel. Sin ciclos ni cambios de reloj.
     * Cada listener llama una función concreta; elegir intervalo sólo habilita Aplicar. */
    private JPanel controlesReloj() {
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controles.setOpaque(false);
        controles.setAlignmentX(LEFT_ALIGNMENT);
        intervalo.setSelectedItem(controller.getCycleDurationMs());
        intervalo.setToolTipText("Milisegundos por tick; el cambio requiere Aplicar");
        intervalo.setPreferredSize(new Dimension(90, 36));
        iniciar.addActionListener(e -> iniciarReloj());
        pausar.addActionListener(e -> pausarReloj());
        paso.addActionListener(e -> avanzarUnTick());
        aplicar.addActionListener(e -> aplicarIntervalo());
        intervalo.addActionListener(e -> actualizarControlesReloj());
        controles.add(iniciar); controles.add(pausar); controles.add(paso);
        controles.add(texto("Intervalo (ms)", 12, GRIS, false));
        controles.add(intervalo); controles.add(aplicar);
        controles.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        return controles;
    }

    /** Clic → startSimulation. Arranca o reanuda, incluso sin computadores; no reinicia ciclo.
     * Salida void; sin ciclos. Si ya corre, no crea otro hilo. */
    private void iniciarReloj() {
        controller.startSimulation();
        actualizarVista();
    }

    /** Clic → pauseSimulation. Conserva contador; un tick ya iniciado puede completar.
     * Detenido/pausado: no opera. Sin ciclos; refresca controles. */
    private void pausarReloj() {
        if (controller.isRunning() && !controller.isPaused()) controller.pauseSimulation();
        actualizarVista();
    }

    /** Clic → stepSimulation. Sólo detenido/pausado; activo: no modifica nada.
     * Avanza un tick y actualiza vista; sin ciclos propios. */
    private void avanzarUnTick() {
        if (controller.isRunning() && !controller.isPaused()) return;
        controller.stepSimulation();
        actualizarVista();
    }

    /** Entrada: opción del combo. Salida void. Sólo Aplicar cambia intervalo real.
     * Sin selección no opera; no arranca ni reinicia. La espera en curso no se interrumpe. */
    private void aplicarIntervalo() {
        Integer ms = (Integer) intervalo.getSelectedItem();
        if (ms == null) return;
        controller.setCycleDurationMs(ms);
        actualizarControlesReloj();
    }

    /** Lee estado/intervalo; escribe habilitación y rótulos, nunca cambia selección pendiente.
     * Sin ciclos. Activo: deshabilita paso e inicio. Pausado: ofrece Reanudar. */
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
        intervaloAplicado.setText("Aplicado: " + ms + " ms/tick" + (pendiente ? " · Cambio pendiente de aplicar" : ""));
    }

    @Override public void addNotify() { super.addNotify(); refresco.start(); }
    @Override public void removeNotify() { refresco.stop(); super.removeNotify(); }

    /**
     * Entrada: estado actual del controlador; salida: rótulos y pestañas actualizados.
     * Lee reloj fuera del bloqueo del controlador; luego protege la lectura de computadores
     * frente a tick/createComputer sincronizados. No modifica el dominio. Usar en EDT.
     * Ciclos: un recorrido finito de computadores para comparar IDs y otro para pintar datos.
     * Lista vacía muestra navegación a creación. CPU vacía muestra Sin proceso.
     * No convierte errores internos en valores cero ficticios.
     */
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
        }
    }

    /**
     * Entrada: lista del controlador bajo su bloqueo. Reconstruye sólo si cambian IDs.
     * Conserva pestaña por ID si existe; vuelve a Resumen si desaparece.
     * Recorre computadores hasta size y elimina pestañas hasta dejar Resumen.
     * Cada tarjeta conecta su botón con la pestaña de detalle, sin cambiar simulación.
     */
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

    /** Rótulos por computador, reutilizados en cada refresco para conservar la navegación. */
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
            if (detalle) {
                add(Box.createVerticalStrut(20));
                add(texto("Detalle de colas, inspector PCB y cambio de política: pendientes", 12, GRIS, false));
            }
        }

        /** Lee un computador; actualiza rótulos sin mutarlo. Sin ciclos.
         * RAM: muestra usada/total. CPU sin PCB: libre, sin modo activo. */
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
