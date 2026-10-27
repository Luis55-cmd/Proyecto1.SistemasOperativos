package com.avilaos.gui;

import java.awt.*;
import com.avilaos.hardware.Computer;
import com.avilaos.simulation.SimulationController;
import com.avilaos.sync.Buffer;
import com.avilaos.core.model.SchedulingPolicyType;
import javax.swing.table.DefaultTableModel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Vista Swing: conserva formularios; no modifica el kernel ni ejecuta procesos. */
public class PanelPrincipal extends JPanel {
    public enum Destino { MENU, COMPUTADOR, BUFFER, PROCESO, SIMULACION, METRICAS }
    static final Color FONDO = new Color(246, 243, 234);
    static final Color TINTA = new Color(38, 58, 43);
    static final Color GRIS = new Color(119, 105, 85);
    static final Color ACENTO = new Color(82, 112, 64);
    static final Color PAPEL = new Color(255, 253, 247);
    static final Color BORDE = new Color(218, 210, 190);
    private final CardLayout distribucion = new CardLayout();
    private final JPanel pantallas = new JPanel(distribucion);
    private final JButton[] navegacion = new JButton[Destino.values().length];
    private Destino destinoActual;
    private final EditorProceso editorProceso;
    private final MonitorSimulacion monitor;
    private final PanelMetricas metricas;
    private final SimulationController controller = SimulationController.getInstance();
    
    // Controles de Computador
    private final JTextField nombreComputador = new JTextField();
    private final javax.swing.border.Border bordeNombreNormal = nombreComputador.getBorder();
    private final JLabel errorNombre = texto(" ", 12, new Color(166, 44, 38), false);
    private SchedulingPolicyType politicaSeleccionada = SchedulingPolicyType.FCFS;
    private final JSpinner spinnerQuantum = new JSpinner(new SpinnerNumberModel(3, 1, 99, 1));
    private final JLabel resultadoComputador = texto("Todavía no has creado computadores desde esta vista.", 12, GRIS, false);
    private final DefaultTableModel computadoresVisibles = new DefaultTableModel(
            new String[]{"ID", "Nombre", "RAM", "Política"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };

    // Controles de Buffer
    private record OpcionComputador(int id, String nombre) {
        @Override public String toString() { return nombre + " · ID " + id; }
    }
    // NOTA: El campo ID numérico se deshabilita/elimina visualmente porque el Controller lo autogenera.
    private final JComboBox<OpcionComputador> comboComputadorBuffer = new JComboBox<>();
    private final JSpinner spinnerAlmacenamiento = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 1));
    private final JSpinner spinnerTamanioMemoria = new JSpinner(new SpinnerNumberModel(20, 1, 1024, 1));
    private final JLabel resultadoBuffer = texto("Ningún buffer creado en esta sesión.", 12, GRIS, false);
    private final DefaultTableModel buffersVisibles = new DefaultTableModel(
            new String[]{"ID Buffer", "ID Comp", "Capacidad (Items)", "Tamaño en RAM"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };

    public PanelPrincipal() {
        super(new BorderLayout());
        setBackground(FONDO);
        JPanel lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setBackground(TINTA);
        lateral.setBorder(new EmptyBorder(30, 22, 24, 22));
        lateral.setPreferredSize(new Dimension(220, 0));
        LogoAvila marca = new LogoAvila();
        marca.setAlignmentX(LEFT_ALIGNMENT);
        lateral.add(marca);
        lateral.add(Box.createVerticalStrut(28));
        
        String[] titulos = {"01   Inicio", "02   Computadores", "03   Buffers", "04   Procesos", "05   Simulación", "06   Métricas"};
        for (Destino destino : Destino.values()) {
            JButton boton = accion(titulos[destino.ordinal()], false);
            boton.setHorizontalAlignment(SwingConstants.LEFT);
            boton.setMaximumSize(new Dimension(180, 46));
            boton.setAlignmentX(LEFT_ALIGNMENT);
            boton.addActionListener(e -> mostrarPanel(destino));
            navegacion[destino.ordinal()] = boton;
            lateral.add(boton);
            lateral.add(Box.createVerticalStrut(8));
        }
        lateral.add(Box.createVerticalGlue());
        JLabel pie = texto("ENTORNO DE DISEÑO", 10, new Color(192, 181, 148), true);
        pie.setAlignmentX(LEFT_ALIGNMENT);
        lateral.add(pie);
        add(lateral, BorderLayout.WEST);
        pantallas.setBackground(FONDO);
        pantallas.add(inicio(), Destino.MENU.name());
        pantallas.add(computador(), Destino.COMPUTADOR.name());
        
        pantallas.add(buffer(), Destino.BUFFER.name());
        
        editorProceso = new EditorProceso(() -> mostrarPanel(Destino.COMPUTADOR));
        pantallas.add(editorProceso, Destino.PROCESO.name());
        monitor = new MonitorSimulacion(controller, () -> mostrarPanel(Destino.COMPUTADOR));
        pantallas.add(monitor, Destino.SIMULACION.name());
        metricas = new PanelMetricas(controller);
        JPanel vistaMetricas = new JPanel(new BorderLayout(0, 16));
        vistaMetricas.setBackground(FONDO);
        vistaMetricas.setBorder(new EmptyBorder(24, 24, 24, 24));
        vistaMetricas.add(texto("Métricas del sistema", 26, TINTA, true), BorderLayout.NORTH);
        vistaMetricas.add(metricas, BorderLayout.CENTER);
        pantallas.add(vistaMetricas, Destino.METRICAS.name());
        add(pantallas, BorderLayout.CENTER);
        
        // --- CONEXIÓN AL OBSERVER ---
        // Se actualizan las tablas automáticamente cuando el SimulationController cambia
        controller.addGUIListener(() -> {
            synchronized (controller) { metricas.actualizarMetricas(); }
            actualizarListaComputadores();
            actualizarListaBuffers();
            actualizarOpcionesComputadoresBuffer();
        });

        mostrarPanel(Destino.MENU);
    }

    public void mostrarPanel(Destino destino) {
        if (destino == null) throw new IllegalArgumentException("Destino de navegación requerido");
        if (destino == Destino.METRICAS) {
            synchronized (controller) { metricas.actualizarMetricas(); }
        }
        if (destino == Destino.PROCESO) editorProceso.actualizarListas();
        if (destino == Destino.SIMULACION) monitor.actualizarVista();
        
        if (destino == Destino.BUFFER) actualizarOpcionesComputadoresBuffer();
        
        distribucion.show(pantallas, destino.name());
        destinoActual = destino;
        for (Destino item : Destino.values()) {
            JButton boton = navegacion[item.ordinal()];
            boton.setBackground(item == destino ? ACENTO : TINTA);
            boton.setForeground(item == destino ? Color.WHITE : new Color(225, 220, 201));
        }
    }

    public Destino getDestinoActual() { return destinoActual; }

    private JPanel inicio() {
        JPanel panel = pagina("TU ESPACIO DE TRABAJO", "Construye tu simulación", "Configura los computadores y compón los programas que van a ejecutar.");
        JPanel bienvenida = tarjeta();
        bienvenida.add(texto("Un clúster. Muchas posibilidades.", 24, TINTA, true));
        bienvenida.add(Box.createVerticalStrut(12));
        bienvenida.add(texto("Comienza por los recursos; después define el trabajo.", 15, GRIS, false));
        panel.add(bienvenida);
        panel.add(Box.createVerticalStrut(24));
        
        JPanel opciones = new JPanel(new GridLayout(1, 3, 20, 0));
        opciones.setOpaque(false);
        opciones.setAlignmentX(LEFT_ALIGNMENT);
        opciones.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));
        
        JPanel equipos = tarjeta();
        equipos.add(texto("01 / COMPUTADORES", 11, ACENTO, true));
        equipos.add(Box.createVerticalStrut(14));
        equipos.add(texto("Define recursos", 21, TINTA, true));
        equipos.add(Box.createVerticalStrut(8));
        equipos.add(texto("Memoria y política.", 13, GRIS, false));
        equipos.add(Box.createVerticalStrut(22));
        JButton equipo = accion("Configurar  →", true);
        equipo.addActionListener(e -> mostrarPanel(Destino.COMPUTADOR));
        equipos.add(equipo);
        
        JPanel buffers = tarjeta();
        buffers.add(texto("02 / BUFFERS", 11, ACENTO, true));
        buffers.add(Box.createVerticalStrut(14));
        buffers.add(texto("Sincronización", 21, TINTA, true));
        buffers.add(Box.createVerticalStrut(8));
        buffers.add(texto("Espacios de memoria.", 13, GRIS, false));
        buffers.add(Box.createVerticalStrut(22));
        JButton btnBuffer = accion("Crear buffer  →", true);
        btnBuffer.addActionListener(e -> mostrarPanel(Destino.BUFFER));
        buffers.add(btnBuffer);
        
        JPanel programas = tarjeta();
        programas.add(texto("03 / PROCESOS", 11, ACENTO, true));
        programas.add(Box.createVerticalStrut(14));
        programas.add(texto("Diseña programas", 21, TINTA, true));
        programas.add(Box.createVerticalStrut(8));
        programas.add(texto("Bloques de CPU y E/S.", 13, GRIS, false));
        programas.add(Box.createVerticalStrut(22));
        JButton proceso = accion("Borrador  →", true);
        proceso.addActionListener(e -> mostrarPanel(Destino.PROCESO));
        programas.add(proceso);
        
        opciones.add(equipos); opciones.add(buffers); opciones.add(programas);
        panel.add(opciones);
        panel.add(Box.createVerticalStrut(24));
        return panel;
    }

    private JPanel computador() {
        JPanel panel = pagina("COMPUTADORES", "Configura un computador", "Cada computador tendrá su propia CPU, memoria y política.");
        JPanel tarjeta = tarjeta();
        tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));
        tarjeta.add(texto("Recursos del computador", 20, TINTA, true));
        tarjeta.add(Box.createVerticalStrut(16));
        JPanel nombre = campo("Nombre del computador", nombreComputador);
        nombre.setAlignmentX(LEFT_ALIGNMENT);
        nombre.setMaximumSize(new Dimension(400, 62));
        tarjeta.add(nombre);
        errorNombre.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(errorNombre);
        tarjeta.add(Box.createVerticalStrut(24));
        JPanel campos = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        campos.setOpaque(false);
        campos.setAlignmentX(LEFT_ALIGNMENT);
        campos.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        JComboBox<Integer> ram = new JComboBox<>(new Integer[]{32, 64, 128, 256, 512, 1024, 2048});
        ram.setUI(new javax.swing.plaf.basic.BasicComboBoxUI());
        ram.setMaximumRowCount(4);
        ram.setSelectedItem(128);
        JPanel memoria = campo("RAM · unidades", ram);
        memoria.setPreferredSize(new Dimension(150, 62));
        campos.add(memoria);
        campos.add(Box.createHorizontalStrut(24));
        JPanel politica = campo("Política inicial", selectorPolitica());
        politica.setPreferredSize(new Dimension(480, 62));
        campos.add(politica);
        campos.add(Box.createHorizontalStrut(24));
        JPanel panelQuantum = campo("Quantum (RR)", spinnerQuantum);
        panelQuantum.setPreferredSize(new Dimension(110, 62));
        spinnerQuantum.setEnabled(politicaSeleccionada == SchedulingPolicyType.ROUND_ROBIN);
        campos.add(panelQuantum);
        tarjeta.add(campos);
        tarjeta.add(Box.createVerticalStrut(22));
        JButton crear = accion("Crear computador", true);
        crear.addActionListener(e -> crearComputadorDesdeFormulario((Integer) ram.getSelectedItem()));
        tarjeta.add(crear);
        tarjeta.add(Box.createVerticalStrut(10));
        resultadoComputador.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        tarjeta.add(resultadoComputador);
        panel.add(tarjeta);
        panel.add(Box.createVerticalStrut(20));
        JTable tabla = new JTable(computadoresVisibles);
        tabla.setRowHeight(32);
        tabla.setBackground(PAPEL);
        tabla.setForeground(TINTA);
        JScrollPane lista = new JScrollPane(tabla);
        lista.setColumnHeaderView(tabla.getTableHeader());
        lista.setAlignmentX(LEFT_ALIGNMENT);
        lista.setPreferredSize(new Dimension(600, 160));
        panel.add(lista);
        actualizarListaComputadores();
        return panel;
    }

    private JPanel buffer() {
        JPanel panel = pagina("BUFFERS", "Gestión de Buffers", "Asigna memoria en un computador específico para el esquema Productor/Consumidor.");
        JPanel tarjeta = tarjeta();
        tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        tarjeta.add(texto("Propiedades del Buffer", 20, TINTA, true));
        tarjeta.add(Box.createVerticalStrut(16));
        
        JPanel fila1 = new JPanel(new GridLayout(1, 2, 24, 0));
        fila1.setOpaque(false);
        fila1.setAlignmentX(LEFT_ALIGNMENT);
        fila1.setMaximumSize(new Dimension(500, 62));
        // Se autogenera el ID en el controlador, por eso se oculta el ID visual
        fila1.add(campo("Computador Host", comboComputadorBuffer));
        tarjeta.add(fila1);
        tarjeta.add(Box.createVerticalStrut(16));
        
        JPanel fila2 = new JPanel(new GridLayout(1, 2, 24, 0));
        fila2.setOpaque(false);
        fila2.setAlignmentX(LEFT_ALIGNMENT);
        fila2.setMaximumSize(new Dimension(500, 62));
        fila2.add(campo("Capacidad (Elementos)", spinnerAlmacenamiento));
        // El tamaño en memoria podría calcularse, pero lo conservamos si lo quieres enviar
        fila2.add(campo("Tamaño en RAM", spinnerTamanioMemoria));
        tarjeta.add(fila2);
        tarjeta.add(Box.createVerticalStrut(22));
        
        JButton crear = accion("Crear y registrar Buffer", true);
        crear.addActionListener(e -> crearBufferDesdeFormulario());
        tarjeta.add(crear);
        tarjeta.add(Box.createVerticalStrut(10));
        resultadoBuffer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        tarjeta.add(resultadoBuffer);
        
        panel.add(tarjeta);
        panel.add(Box.createVerticalStrut(20));
        
        JTable tabla = new JTable(buffersVisibles);
        tabla.setRowHeight(32);
        tabla.setBackground(PAPEL);
        tabla.setForeground(TINTA);
        JScrollPane lista = new JScrollPane(tabla);
        lista.setColumnHeaderView(tabla.getTableHeader());
        lista.setAlignmentX(LEFT_ALIGNMENT);
        lista.setPreferredSize(new Dimension(600, 160));
        panel.add(lista);
        
        actualizarListaBuffers();
        return panel;
    }

    private void actualizarOpcionesComputadoresBuffer() {
        OpcionComputador seleccion = (OpcionComputador) comboComputadorBuffer.getSelectedItem();
        comboComputadorBuffer.removeAllItems();
        for (int i = 0; i < controller.getComputers().size(); i++) {
            Computer c = controller.getComputers().get(i);
            OpcionComputador op = new OpcionComputador(c.getId(), c.getName());
            comboComputadorBuffer.addItem(op);
            if (seleccion != null && seleccion.id() == c.getId()) {
                comboComputadorBuffer.setSelectedItem(op);
            }
        }
    }

    private void crearBufferDesdeFormulario() {
        try {
            OpcionComputador compSeleccionado = (OpcionComputador) comboComputadorBuffer.getSelectedItem();
            if (compSeleccionado == null) {
                resultadoBuffer.setText("Error: Debes crear y seleccionar un computador primero.");
                return;
            }
            
            spinnerAlmacenamiento.commitEdit();
            spinnerTamanioMemoria.commitEdit();
            
            int capacidad = (Integer) spinnerAlmacenamiento.getValue();
            int memoria = (Integer) spinnerTamanioMemoria.getValue();
            // Inyección real al Controlador usando el host y el almacenamiento deseado.
            // (Tu método createBuffer usa el `storage` para asignar la RAM).
            Buffer nuevoBuffer = controller.createBuffer(compSeleccionado.id(), capacidad, memoria);
            
            if (nuevoBuffer != null) {
                resultadoBuffer.setText("Buffer ID " + nuevoBuffer.getId() + " creado exitosamente en Host " + compSeleccionado.id());
                // No necesitamos añadir a la tabla manualmente, el Observer llamará a actualizarListaBuffers()
            } else {
                resultadoBuffer.setText("Error al crear. ¿Tiene RAM suficiente el Host " + compSeleccionado.id() + "?");
            }
            
        } catch (Exception ex) {
            resultadoBuffer.setText("Error al crear el buffer: " + ex.getMessage());
        }
    }
    
    private void actualizarListaBuffers() {
        buffersVisibles.setRowCount(0);
        for (int i = 0; i < controller.getBuffers().size(); i++) {
            Buffer b = controller.getBuffers().get(i);
            buffersVisibles.addRow(new Object[]{
                b.getId(), 
                b.getIdComp(),       // Computador Host
                b.getStorage(),      // Capacidad de ítems
                b.getSizeInMemory()  // Tamaño en RAM
            });
        }
    }
    
    
    private JPanel selectorPolitica() {
        JPanel opciones = new JPanel(new GridLayout(1, 4, 8, 0));
        opciones.setOpaque(false);
        opciones.setPreferredSize(new Dimension(480, 36));
        ButtonGroup grupo = new ButtonGroup();
        for (String nombre : new String[]{"FCFS", "Round Robin", "EDF", "Prioridades"}) {
            JToggleButton boton = new JToggleButton(nombre);
            boton.setUI(new javax.swing.plaf.basic.BasicToggleButtonUI());
            boton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            boton.setOpaque(true);
            boton.setBackground(PAPEL);
            boton.setForeground(TINTA);
            boton.setBorder(BorderFactory.createLineBorder(BORDE));
            boton.addItemListener(e -> {
                if (boton.isSelected()) {
                    politicaSeleccionada = switch (nombre) {
                        case "Round Robin" -> SchedulingPolicyType.ROUND_ROBIN;
                        case "EDF" -> SchedulingPolicyType.EDF;
                        case "Prioridades" -> SchedulingPolicyType.PRIORITY_PREEMPTIVE;
                        default -> SchedulingPolicyType.FCFS;
                    };
                    spinnerQuantum.setEnabled(politicaSeleccionada == SchedulingPolicyType.ROUND_ROBIN);
                }
                boton.setBackground(boton.isSelected() ? ACENTO : PAPEL);
                boton.setForeground(boton.isSelected() ? Color.WHITE : TINTA);
            });
            grupo.add(boton);
            opciones.add(boton);
            boton.setSelected("FCFS".equals(nombre));
        }
        return opciones;
    }

    private void crearComputadorDesdeFormulario(Integer ram) {
        nombreComputador.setBorder(bordeNombreNormal);
        errorNombre.setText(" ");
        if (ram == null || nombreComputador.getText().trim().isEmpty()) {
            resultadoComputador.setText("Escribe un nombre y selecciona la RAM.");
            return;
        }
        Computer creado;
        try {
            int quantum = (Integer) spinnerQuantum.getValue();
            creado = controller.createComputer(nombreComputador.getText(), ram, politicaSeleccionada, quantum);
        } catch (IllegalArgumentException error) {
            nombreComputador.setBorder(BorderFactory.createLineBorder(new Color(166, 44, 38), 2));
            errorNombre.setText(error.getMessage());
            resultadoComputador.setText("No se creó otro computador.");
            nombreComputador.requestFocusInWindow();
            return;
        }
        resultadoComputador.setText("Creado: " + creado.getName() + " · ID " + creado.getId());
    }

    private void actualizarListaComputadores() {
        computadoresVisibles.setRowCount(0);
        for (int i = 0; i < controller.getComputers().size(); i++) {
            Computer c = controller.getComputers().get(i);
            String polStr = (c.getPolitica() == SchedulingPolicyType.ROUND_ROBIN)
                    ? "Round Robin (Q: " + c.getQuantum() + ")"
                    : c.getPolitica().toString();
            computadoresVisibles.addRow(new Object[]{c.getId(), c.getName(),
                    c.getRam().getTotalSize(), polStr});
        }
    }

    // Funciones de utilidad visual
    static JLabel texto(String valor, int tamano, Color color, boolean negrita) {
        JLabel label = new JLabel(valor);
        label.setFont(new Font(Font.SANS_SERIF, negrita ? Font.BOLD : Font.PLAIN, tamano));
        label.setForeground(color);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }
    static JButton accion(String titulo, boolean primaria) {
        JButton boton = new JButton(titulo);
        boton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        boton.setBackground(primaria ? ACENTO : PAPEL);
        boton.setForeground(primaria ? Color.WHITE : TINTA);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primaria ? ACENTO : BORDE),
                new EmptyBorder(12, 16, 12, 16)));
        boton.setAlignmentX(LEFT_ALIGNMENT);
        return boton;
    }
    static JPanel pagina(String etiqueta, String titulo, String descripcion) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(FONDO);
        panel.setBorder(new EmptyBorder(32, 32, 28, 32));
        panel.add(texto(etiqueta, 11, ACENTO, true));
        panel.add(Box.createVerticalStrut(10));
        panel.add(texto(titulo, 28, TINTA, true));
        panel.add(Box.createVerticalStrut(10));
        panel.add(texto(descripcion, 13, GRIS, false));
        panel.add(Box.createVerticalStrut(28));
        return panel;
    }
    static JPanel tarjeta() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PAPEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE), new EmptyBorder(24, 24, 24, 24)));
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        return panel;
    }
    static JPanel campo(String etiqueta, JComponent control) {
        JPanel panel = new JPanel(new BorderLayout(0, 9));
        panel.setOpaque(false);
        panel.add(texto(etiqueta, 12, GRIS, true), BorderLayout.NORTH);
        control.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        control.setPreferredSize(new Dimension(180, 36));
        panel.add(control, BorderLayout.CENTER);
        return panel;
    }
}