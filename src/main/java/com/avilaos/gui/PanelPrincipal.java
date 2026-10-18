package com.avilaos.gui;

import java.awt.*;
import com.avilaos.hardware.Computer;
import com.avilaos.simulation.SimulationController;
import com.avilaos.core.model.SchedulingPolicyType;
import javax.swing.table.DefaultTableModel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Vista Swing: conserva formularios; no modifica el kernel ni ejecuta procesos. */
public class PanelPrincipal extends JPanel {
    public enum Destino { MENU, COMPUTADOR, PROCESO, SIMULACION }
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
    private final SimulationController controller = SimulationController.getInstance();
    private final JTextField nombreComputador = new JTextField();
    private final javax.swing.border.Border bordeNombreNormal = nombreComputador.getBorder();
    private final JLabel errorNombre = texto(" ", 12, new Color(166, 44, 38), false);
    private SchedulingPolicyType politicaSeleccionada = SchedulingPolicyType.FCFS;
    private final JLabel resultadoComputador = texto("Todavía no has creado computadores desde esta vista.", 12, GRIS, false);
    // Modelo visual Swing: sólo copia datos para mostrarlos, no almacena el dominio.
    private final DefaultTableModel computadoresVisibles = new DefaultTableModel(
            new String[]{"ID", "Nombre", "RAM", "Política"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };

    /** Entrada ninguna; construye una vez las vistas. Conecta botones → mostrarPanel.
     * El único recorrido crea los cuatro botones y termina al agotar Destino.values(). */
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
        String[] titulos = {"01   Inicio", "02   Computadores", "03   Procesos", "04   Simulación"};
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
        pantallas.add(new EditorProceso(), Destino.PROCESO.name());
        JPanel simulacion = pagina("SIMULACIÓN", "Observa el sistema", "CPU, memoria y colas en un mismo lugar.");
        JPanel espera = tarjeta();
        espera.add(texto("El motor aún no está conectado", 21, TINTA, true));
        espera.add(Box.createVerticalStrut(12));
        espera.add(texto("Esta vista recibirá el estado de tus computadores.", 14, GRIS, false));
        simulacion.add(espera);
        pantallas.add(simulacion, Destino.SIMULACION.name());
        add(pantallas, BorderLayout.CENTER);
        mostrarPanel(Destino.MENU);
    }

    /** Entrada destino válido; salida void. Cambia visibilidad y selección lateral.
     * Conserva formularios. Null lanza excepción antes de mutar. El recorrido de botones
     * termina al visitar los cuatro destinos. Conexión: listeners → CardLayout.show.
     * Usar en EDT. Caso normal: muestra PROCESO; error null: conserva pantalla actual. */
    public void mostrarPanel(Destino destino) {
        if (destino == null) throw new IllegalArgumentException("Destino de navegación requerido");
        distribucion.show(pantallas, destino.name());
        destinoActual = destino;
        for (Destino item : Destino.values()) {
            JButton boton = navegacion[item.ordinal()];
            boton.setBackground(item == destino ? ACENTO : TINTA);
            boton.setForeground(item == destino ? Color.WHITE : new Color(225, 220, 201));
        }
    }

    /** Consulta sin efectos ni ciclos. */
    public Destino getDestinoActual() { return destinoActual; }

    /** Construye inicio con accesos a los formularios; no crea datos de simulación. */
    private JPanel inicio() {
        JPanel panel = pagina("TU ESPACIO DE TRABAJO", "Construye tu simulación", "Configura los computadores y compón los programas que van a ejecutar.");
        JPanel bienvenida = tarjeta();
        bienvenida.add(texto("Un clúster. Muchas posibilidades.", 24, TINTA, true));
        bienvenida.add(Box.createVerticalStrut(12));
        bienvenida.add(texto("Comienza por los recursos; después define el trabajo.", 15, GRIS, false));
        panel.add(bienvenida);
        panel.add(Box.createVerticalStrut(24));
        JPanel opciones = new JPanel(new GridLayout(1, 2, 20, 0));
        opciones.setOpaque(false);
        opciones.setAlignmentX(LEFT_ALIGNMENT);
        opciones.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));
        JPanel equipos = tarjeta();
        equipos.add(texto("01 / COMPUTADORES", 11, ACENTO, true));
        equipos.add(Box.createVerticalStrut(14));
        equipos.add(texto("Define los recursos", 21, TINTA, true));
        equipos.add(Box.createVerticalStrut(8));
        equipos.add(texto("Memoria y política de planificación.", 13, GRIS, false));
        equipos.add(Box.createVerticalStrut(22));
        JButton equipo = accion("Configurar computador  →", true);
        equipo.addActionListener(e -> mostrarPanel(Destino.COMPUTADOR));
        equipos.add(equipo);
        JPanel programas = tarjeta();
        programas.add(texto("02 / PROCESOS", 11, ACENTO, true));
        programas.add(Box.createVerticalStrut(14));
        programas.add(texto("Diseña un programa", 21, TINTA, true));
        programas.add(Box.createVerticalStrut(8));
        programas.add(texto("Agrega bloques de CPU y entrada/salida.", 13, GRIS, false));
        programas.add(Box.createVerticalStrut(22));
        JButton proceso = accion("Crear borrador de proceso  →", true);
        proceso.addActionListener(e -> mostrarPanel(Destino.PROCESO));
        programas.add(proceso);
        opciones.add(equipos); opciones.add(programas);
        panel.add(opciones);
        panel.add(Box.createVerticalStrut(24));
        panel.add(texto("Computadores conectados · El editor de procesos sigue siendo un borrador.", 12, GRIS, false));
        return panel;
    }

    /** Formulario visual RAM/política. Entradas locales; sin reserva de memoria ni creación. */
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
        // Popup de cuatro filas: permite recorrer los siete tamaños con scroll.
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
        tarjeta.add(campos);
        tarjeta.add(Box.createVerticalStrut(22));
        JButton crear = accion("Crear computador", true);
        // ENTRADA: clic → captura del formulario → controlador → lista visible.
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

    /** Entrada ninguna; devuelve cuatro opciones de selección exclusiva, FCFS inicial.
     * ButtonGroup impide seleccionar dos políticas o desmarcar la única seleccionada.
     * Cada evento de selección actualiza sólo el color del botón; no modifica el kernel.
     * El recorrido termina tras crear las cuatro opciones. No admite valores libres.
     */
    private JPanel selectorPolitica() {
        JPanel opciones = new JPanel(new GridLayout(1, 4, 8, 0));
        opciones.setOpaque(false);
        opciones.setPreferredSize(new Dimension(480, 36));
        ButtonGroup grupo = new ButtonGroup();
        for (String nombre : new String[]{"FCFS", "Round Robin", "EDF", "Prioridades"}) {
            JToggleButton boton = new JToggleButton(nombre);
            // Estilo Swing básico para que macOS respete el color de selección.
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

    /**
     * Entrada RAM del selector, nombre y política del formulario; salida void.
     * Valida nombre/RAM antes de delegar. Crea UNA instancia y actualiza filas/mensaje.
     * Sin ciclos. Error de validación: mensaje y ningún computador nuevo.
     * No arranca reloj. Conexión: listener → createComputer → actualizarListaComputadores.
     */
    private void crearComputadorDesdeFormulario(Integer ram) {
        nombreComputador.setBorder(bordeNombreNormal);
        errorNombre.setText(" ");
        if (ram == null || nombreComputador.getText().trim().isEmpty()) {
            resultadoComputador.setText("Escribe un nombre y selecciona la RAM.");
            return;
        }
        Computer creado;
        try {
            creado = controller.createComputer(nombreComputador.getText(), ram, politicaSeleccionada);
        } catch (IllegalArgumentException error) {
            nombreComputador.setBorder(BorderFactory.createLineBorder(new Color(166, 44, 38), 2));
            errorNombre.setText(error.getMessage());
            resultadoComputador.setText("No se creó otro computador.");
            nombreComputador.requestFocusInWindow();
            return;
        }
        actualizarListaComputadores();
        resultadoComputador.setText("Creado: " + creado.getName() + " · ID " + creado.getId());
    }

    /**
     * Entrada: computadores del controlador; salida: filas de la tabla.
     * Reemplaza la copia visual; no modifica computadores ni reloj. Usar en EDT.
     * Recorre índices 0..size-1, termina al alcanzar size. Lista vacía: cero filas.
     * Sin recuperación de errores internos: sólo se usa con el controlador inicializado.
     */
    private void actualizarListaComputadores() {
        computadoresVisibles.setRowCount(0);
        for (int i = 0; i < controller.getComputers().size(); i++) {
            Computer c = controller.getComputers().get(i);
            computadoresVisibles.addRow(new Object[]{c.getId(), c.getName(),
                    c.getRam().getTotalSize(), c.getPolitica()});
        }
    }

    // Ayudas de presentación: reciben texto/componentes y devuelven contenedores nuevos.
    // No tocan dominio, disco ni red; sólo configuran estilo y jerarquía visual.
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
